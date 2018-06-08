/*
 * Created on 09.10.2006
 */
package data;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.fabric.HashShardMapping;

import aktie.Aktie;
import aktie.Aktien;
import aktie.Kurs;
import util.Util;
import util.Zeitraum;

/**
 * @author oskar <br>
 *         über diese Klasse läuft sämtlich Kommunikation mit der Datenbank.<br>
 */

public class DBManager {
	private static final Logger log = LogManager.getLogger(DBManager.class);

	private static final String DBName = "kurse";
	private static final String StammdatenTabelle = "stammdaten";

	/**
	 * schreibt in eine bestehende Aktie neue Kurse in die DB
	 * TODO wenn bereits Kurs vorhanden sind, werden Daten ergänzt
	 * @param kursreihe
	 * @return
	 */
	public static boolean schreibeKurse (ImportKursreihe kursreihe) {
		String name = kursreihe.kuerzel;
		int zaehler = 0;
		
		Connection connection = ConnectionFactory.getConnection();
		// iteriert über alle vorhandenen Kurse 
		for (Kurs kurs : kursreihe.kurse) {
			// schreibt den Kurs in die Tabelle 
			// wenn ein Fehler entsteht z.B. duplicate Entry, wird gezählt. 
			if (! DBManager.addKurs(kurs, connection)) zaehler ++;
		}
		log.info("Anzahl " + kursreihe.kurse.size() + " Kurse für " + kursreihe.kuerzel + " Fehler: " + zaehler);
		return true; 
	}
	
	/*
	 * Schreibt die Stammdaten einer neuen Aktie in die Stammdaten-Tabelle
	 * Wenn ein Kurs-Zeitraum vorhanden ist, wird dieser mit eingetragen 
	 */
	static boolean trageNeueAktieInStammdatenEin (Aktie aktie) {

		String name = aktie.name;
		String firmenname = aktie.firmenname;
		String indexname = aktie.indexname; 
		Zeitraum zeitraum = aktie.getZeitraumKurse();
		String beginn; 
		String ende; 
		String insert; 
		if (zeitraum != null) {
			beginn = formatSQLDate(aktie.getZeitraumKurse().beginn);
			ende = formatSQLDate(aktie.getZeitraumKurse().ende);
			insert = "INSERT INTO `" + DBName + "`.`" + StammdatenTabelle + 
					"` (`name`, `firmenname`, `indexname`, `beginn`, `ende`) VALUES ('" + 
					name + "', '" + firmenname + "', '" + indexname + "', '" + beginn + "', '" + ende + "');";
		}
		else {
			insert = "INSERT INTO `" + DBName + "`.`" + StammdatenTabelle + 
					"` (`name`, `firmenname`, `indexname`) VALUES ('" + 
					name + "', '" + firmenname + "', '" + indexname + "');";
		}

		Connection connection = ConnectionFactory.getConnection();
		Statement anweisung = null;
		try {
			anweisung = (Statement) connection.createStatement();
			anweisung.execute(insert);
		} catch (SQLException e) {
			log.error("Fehler beim INSERT Stammdaten von Aktie: " + name);
			return false;
		}
		log.info("Aktie " + name + " in Stammdaten eingetragen");
		return true;

	}
	
	public static boolean schreibeNeueAktieTabelle (String name) {
		
//		String create = "CREATE TABLE IF NOT EXISTS `" + name + "` (" + 
		String create = "CREATE TABLE `" + name + "` (" + 
				  "`datum` date NOT NULL," + 
				  "`open` float DEFAULT NULL," + 
				  "`high` float DEFAULT NULL," + 
				  "`low` float DEFAULT NULL," + 
				  "`close` float NOT NULL," + 
				  "`volume` int(11) DEFAULT NULL," + 
				  "`berg` float DEFAULT NULL," + 
				  "`tal` float DEFAULT NULL," + 
				  "`kurslt` float DEFAULT NULL," + 
				  "`kurslb` float DEFAULT NULL," + 
				  "PRIMARY KEY (`datum`)," + 
				  "UNIQUE KEY `datum` (`datum`)" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		log.info("CreateTable-Statement: " + create);
		Statement anweisung = null;

		Connection connection = ConnectionFactory.getConnection();
		try {
			anweisung = (Statement) connection.createStatement();
			anweisung.execute(create);
		} catch (SQLException e) {
			log.error("Fehler beim CREATE TABLE von Aktie: " + name);
			return false;
		}
		log.info("Tabelle " + name + " in DB angelegt ");
		return true;

	}

	/**
	 * fügt einen neuen Kurs in eine bestehende Tabelle
	 * Im Tageskurs sind nur Datum und die Kursreihe relevant 
	 * Sortierung spielt keine Rolle.
	 * # TODO Fehlerbehandlung, wenn Kurs bereits vorhanden. 
	 */
	public static boolean addKurs(Kurs kurs, Connection connection) {
		String name = kurs.wertpapier;
		String datum = addApostroph(formatSQLDate(kurs.datum), false);
		String close = addApostroph(kurs.getClose(),true);
		String open = addApostroph(Float.toString(kurs.open), true);
		String high = addApostroph(Float.toString(kurs.high), true);
		String low = addApostroph(Float.toString(kurs.low), true);
		String volume = addApostroph(Integer.toString(kurs.volume), true);

		String insert = "INSERT INTO " + name + " (`datum`, `open`, `high`, `low`, `close`, `volume`) " + 
			"VALUES ("+ datum + open + high + low + close + volume + ")";

//		log.info("InsertStatement: " + insert);
		if (connection == null) {
			connection = ConnectionFactory.getConnection();
		}
		Statement anweisung = null;

		try {
			anweisung = (Statement) connection.createStatement();
			anweisung.execute(insert);
		} catch (SQLException e) {
//			log.error("Fehler beim Schreiben von Tageskurs " + kurs.name + kurs.toString() + e.toString());
			return false;
		}
//		log.info("Kurs " + kurs + " in DB geschrieben ");
		return true;
	}
	
	/**
	 * schreibt alle in einer Kursreihe vorhandenen statistischen Werte, Berg/Tal in die DB
	 * Voraussetzung ist eine vorhandene Kursreihe mit Kursen 
	 * @param aktie mit Kursreihe
	 */
	public static boolean schreibeIndikatoren(Aktie aktie) {
		
		Connection verbindung = ConnectionFactory.getConnection();
		
		for (Kurs tageskurs : aktie.getBoersenkurse()) {
			DBManager.schreibeIndikatoren(tageskurs, aktie.name, verbindung);
		}
		return true; 
	}
	
	/**
	 * schreibt Berg, Tal, letztes Extrem, Vola ... 
	 * @param kurs 
	 * @param name Name der Aktie
	 * @param verbindung
	 * @return
	 */
	public static boolean schreibeIndikatoren (Kurs kurs, String name, Connection verbindung) {
		String datum = addApostroph(formatSQLDate(kurs.datum), false);
		String berg = addApostroph(Float.toString(kurs.berg), false);
		String tal = addApostroph(Float.toString(kurs.tal), false);
		String kurslE = "";
// 		String kurslE = addApostroph(Float.toString(kurs.letzterExtremkurs), false);

		String update = "UPDATE " + name + " SET `berg` = " + berg + 
				", `tal` = " + tal + 
				", `kurslE` = " + kurslE + 
				" WHERE `datum` = " + datum ;
		log.info("UpdateStatement: " + update);
		Statement anweisung = null;
		
		try {
			anweisung = (Statement) verbindung.createStatement();
			anweisung.execute(update);
		} catch (SQLException e) {
			log.info("Fehler beim Schreiben von Tageskurs minus"
					+ kurs.toString() + e.toString());
			return false;
		}
		log.info("Tageskurs minus geschrieben" + kurs + " in DB geschrieben ");
		
		return true; 
	}
	
	/**
	 * 
	 * @param cal
	 * @return
	 */
	public static Kurs getTageskurs (String name, GregorianCalendar cal) {
		// SELECT * FROM `appl` WHERE `datum` = '2018-01-02' 
		String select = "SELECT * FROM `" + name + "` WHERE `datum` = '2018-01-02'";
		
    	Connection verbindung = ConnectionFactory.getConnection();
        Statement anweisung = null;
        ResultSet response = null;
        try
		{
			anweisung = (Statement) verbindung.createStatement();
            response = (ResultSet) anweisung.executeQuery(select);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
        Kurs kurs = createTageskursAusDBSelect(response);
        kurs.wertpapier = name; 
    	return kurs; 
		
	}
	/**
	 * Prüft eine Kursreihe nach Inkonsistenzen - fehlerhafte Kurse
	 * Bei Fehler werden logs geschrieben
	 * @param name
	 * @param schwelle der Prozentwert der erlaubten Abweichung z.B. 0.1 
	 */
	protected static void checkKursreihe (String name, float schwelle) {
		ArrayList<Kurs> kurse = getKursreihe(name);
		int zaehler = 0;
		Kurs vortageskurs = null; 
		for (Kurs kurs : kurse) {
			if (zaehler > 2) {
				if ((kurs.getKurs() * (1+schwelle)) < vortageskurs.getKurs() || (kurs.getKurs() * (1-schwelle)) > vortageskurs.getKurs()) {
					log.error("Kurs " + name + " " + Util.formatDate(kurs.datum) + " - " + kurs.getKurs() + " - " + vortageskurs.getKurs());
				}
			}
			vortageskurs = kurs; 
			zaehler++;
		}
	}
	/**
	 * Prüft, ob alle Kurse vorhanden sind 
	 * Referenz-Kursreihe ist xxxdja
	 * @param name
	 */
	protected static void checkKursreiheTage (String name) {
		Aktie aktie = Aktien.getInstance().getAktie(name);
		Aktie dow = Aktien.getInstance().getAktie("xxxdja");
		ArrayList<Kurs> aktieKurse = aktie.getBoersenkurse();
		ArrayList<Kurs> dowKurse = dow.getBoersenkurse();
		// Kurs zum Beginn der Zeitreihe
		Kurs kurs1 = aktieKurse.get(0);
		Kurs dow1 = dowKurse.get(0);
		Kurs kurs2 = null; 
		Kurs dow2; 
		int abstand = 0;
		// AktienKurs ist jünger 
		if (kurs1.datum.getTimeInMillis() > dow1.datum.getTimeInMillis()) {
			// sucht den Dow-Kurs zum 1. Aktienkurs
			dow2 = dow.getKurs(kurs1.datum);
			// Abstand ist die Anzahl Kurse, die der DOW älter ist 
			abstand = dowKurse.indexOf(dow2);
			int i = 1; // i iteriert über die Kurse der Aktie
			do {
				if (i >= aktieKurse.size()) {
					System.out.println("Kurs fehlt: " + name + " " + i + " last: " + Util.formatDate(kurs2.datum));
				}
				kurs2 = aktieKurse.get(i);
				dow2 = dowKurse.get(i + abstand);
				
				if ( ! Util.istGleicherKalendertag(kurs2.datum, dow2.datum)) {
					System.out.println("fehlender Kurs " + aktie.name + " " + 
							Util.formatDate(dow2.datum) + " " + Util.formatDate(kurs2.datum));
					return; 
				}
				i ++;
			}
			while (i < aktieKurse.size() - 10) ;
			System.out.println("Aktienkurse geprüft: " + name + 
					" von " + Util.formatDate(kurs1.datum) + " bis " + Util.formatDate(kurs2.datum));
		}
		else {	// Aktienkurs ist älter als 1. DowJones-Kurs
			// sucht den Aktien-Kurs zum 1. Dowkurs
			kurs2 = aktie.getKurs(dow1.datum);
			// Abstand ist die Anzahl Kurse, die der DOW älter ist 
			abstand = aktieKurse.indexOf(kurs2);
			int i = 1; // i iteriert über die Kurse der Aktie
			do {
				if (i >= dowKurse.size()) {
					System.out.println("Kurs fehlt: " + name + " " + i + " last: " + Util.formatDate(kurs2.datum));
				}
				kurs2 = aktieKurse.get(i + abstand);
				dow2 = dowKurse.get(i);
				
				if ( ! Util.istGleicherKalendertag(kurs2.datum, dow2.datum)) {
					System.out.println("fehlender Kurs " + aktie.name + " " + 
							Util.formatDate(dow2.datum) + " " + Util.formatDate(kurs2.datum));
					return; 
				}
				i ++;
			}
			while (i < dowKurse.size() - 10) ;
			System.out.println("Aktienkurse geprüft: " + name + 
					" von " + Util.formatDate(kurs1.datum) + " bis " + Util.formatDate(kurs2.datum));

		}
		// 
		
		if (dow1 == null) {	// Kurs im Dow-Jones-Kursreihe nicht gefunden. 
			log.error("Referenzkurs im xxxdja nicht gefunden: " + Util.formatDate(kurs1.datum));
		}
		else {
			// geht durch alle Kurse der zu prüfenden Aktie 
			for (Kurs kurs : aktie.getKurse()) {
				// in beiden Kursreihen wird ein Tag weiter gespult und der Tag wird verglichen
				
				
			}
		}
		
	}
	
	/**
	 * Ermittelt den Zeitraum, in dem Kurse einer Aktie vorhanden sind 
	 * @return
	 */
	public static Zeitraum getZeitraumVorhandeneKurse (Aktie aktie) {
		ArrayList<Kurs> kurse = DBManager.getKursreihe(aktie.name);
		GregorianCalendar beginn = kurse.get(0).datum;
		GregorianCalendar ende = kurse.get(kurse.size() - 1).datum;
		return new Zeitraum(beginn, ende);
	}
	
	/**
	 * Befüllt ein Verzeichnis mit allen Aktien-Stammdaten 
	 * @return
	 */
	public static HashMap<String, Aktie> getVerzeichnis () {
		HashMap<String, Aktie> result; 
		String select = "SELECT * FROM `" + StammdatenTabelle ;
	
    	Connection verbindung = ConnectionFactory.getConnection();
        Statement anweisung = null;
        ResultSet response = null;
        try
		{
			anweisung = (Statement) verbindung.createStatement();
            response = (ResultSet) anweisung.executeQuery(select);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
        // die nackte Liste ohne Key
        ArrayList<Aktie> aktien = createVerzeichnisAusDBSelect(response);
        // das Verzeichnis mit Key
        result = new HashMap<String, Aktie>();
		// den Wertpapier-Namen als Key setzen 
		for (Aktie aktie : aktien) {
			result.put(aktie.name, aktie);
		}
    	return result; 
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 */
	private static ArrayList<Aktie> createVerzeichnisAusDBSelect (ResultSet response) {
		ArrayList<Aktie> aktien = new ArrayList<Aktie>();
    	
    	try {
	        while (response.next())
	        {
	        	String name = (response.getString("name"));
	        	String firmenname = (response.getString("firmenname"));
	        	String indexname = (response.getString("indexname"));
	        	Aktie aktie = new Aktie(name, firmenname, indexname, (byte) 0); 
	        	GregorianCalendar beginn = Util.toGregorianCalendar(response.getDate("beginn"));
	        	GregorianCalendar ende = Util.toGregorianCalendar(response.getDate("ende"));
	        	Zeitraum zeitraum = new Zeitraum(beginn, ende);
	        	aktie.setZeitraumKurse(zeitraum);
	        	aktien.add(aktie);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return aktien; 
	}
	
	/**
	 * Liest alle vorhandenen Kursinformationen zu einem Wertpapier
	 * @param cal
	 * @return
	 */
	public static ArrayList<Kurs> getKursreihe (String name) {
		String select = "SELECT * FROM `" + name ;
		ArrayList<Kurs> kursreihe = getKursreiheSELECT(select, name);
		return kursreihe; 
	}
	/**
	 * Liest alle vorhandenen Kursinformationen zu einem Wertpapier
	 * ab einem Beginn-Datum 
	 * @return
	 */
	public static ArrayList<Kurs> getKursreihe (String name, GregorianCalendar beginn) {
		if (beginn == null) return null;  // #TODO Exception werfen 

		String select = "SELECT * FROM " + name + " WHERE `datum` >= '" + Util.formatDate(beginn) + "'";
		// den DB-SELECT Ausführen und eine Kursreihe erzeugen mit enthaltenen Wertpapier-Namen
		ArrayList<Kurs> kursreihe = getKursreiheSELECT(select, name);
		return kursreihe; 
	}
	
	/**
	 * erzeugt eine Liste von Tageskursreihen aus einem vorbereiteten SELECT-Statement
	 * @param select
	 * @return
	 */
	private static ArrayList<Kurs> getKursreiheSELECT (String select, String name) {
    	Connection verbindung = ConnectionFactory.getConnection();
        Statement anweisung = null;
        ResultSet response = null;
        try
		{
			anweisung = (Statement) verbindung.createStatement();
            response = (ResultSet) anweisung.executeQuery(select);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
        ArrayList<Kurs> kursreihe = createKursreiheAusDBSelect(response);
		// den Wertpapier-Namen in allen Kursen setzen
		for (Kurs kurs : kursreihe) {
			kurs.wertpapier = name; 
		}
    	return kursreihe; 
	}
	
	/**
	 * die Kursreihe werden mit Tageskursreihen befüllt 
	 * @param response
	 * @return
	 */
	private static ArrayList<Kurs> createKursreiheAusDBSelect (ResultSet response)
    {
		ArrayList<Kurs> kursreihe = new ArrayList<Kurs>();
    	
    	try {
	        while (response.next())
	        {
	        	Kurs tageskurs = new Kurs();
	        	tageskurs.setKurs(response.getFloat("close"));
	        	tageskurs.close = response.getFloat("close");
	        	tageskurs.high = response.getFloat("high");
	        	tageskurs.low = response.getFloat("low");
	        	tageskurs.open = response.getFloat("open");
	        	tageskurs.volume = response.getInt("volume");
	        	tageskurs.setDatum( Util.toGregorianCalendar(response.getDate("datum")));
	        	kursreihe.add(tageskurs);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return kursreihe; 
    }
	/**
	 * ein einzelner Kurs wird erzeugt aus einem DB-SELECT
	 * @param response
	 * @return
	 */
    private static Kurs createTageskursAusDBSelect (ResultSet response)
    {
    	Kurs kurs = new Kurs ();
		try {
			response.next();
            kurs.close = response.getFloat("close");
            kurs.setDatum(Util.toGregorianCalendar(response.getDate("datum")));

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return kurs; 
    }


	/**
	 * ergänzt einen Text um ein führendes Komma und einen SQL-Apostroph vorne
	 * und hinten aus Mueller wird , 'Mueller'
	 * 
	 * @param text
	 * @return
	 */
	private static String addApostroph(String text, boolean mitKomma) {
		String result;
		if (text != null) {
			result = " '" + text + "' ";
		} else {
			result = " NULL ";
		}
		if (mitKomma) {
			result = " , " + result;
		}
		return result;
	}

	/**
	 * macht aus einem GregorianCal-Datum ein String, der für SQL-Abfragen genutzt wird
	 * 
	 * @param date das Datum, das umgewandelt werden soll
	 * @return ein String oder der Wert 'NULL'
	 */
	public static String formatSQLDate(GregorianCalendar cal) {
		if (cal == null)
			return "NULL";
		else {
			String dateString;
			// aus dem Caldendar ein Datum erzeugen
			java.sql.Date date = new java.sql.Date (cal.getTimeInMillis());
			// Datumsformat festlegen
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// das Datum in dem Format als String ausgeben
			dateString = formatter.format(date);
			return dateString;
		}
	}

}