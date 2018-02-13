/*
 * Created on 09.10.2006
 */
package data;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktie;
import kurs.Kurs;
import util.Util;

/**
 * @author oskar <br>
 *         über diese Klasse läuft sämtlich Kommunikation mit der Datenbank.<br>
 */

public class DBManager {
	private static final Logger log = LogManager.getLogger(DBManager.class);

	private static final String DBName = "kurse";

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
		String name = kurs.name;
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
		
		for (Kurs tageskurs : aktie.getKursreihe()) {
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
		String berg = addApostroph(Float.toString(kurs.bergSumme), false);
		String tal = addApostroph(Float.toString(kurs.talSumme), false);
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
        kurs.name = name; 
    	return kurs; 
		
	}
	
	/**
	 * Liest alle vorhandenen Kursinformationen zu einem Wertpapier
	 * @param cal
	 * @return
	 */
	public static ArrayList<Kurs> getKursreihe (String name) {
		String select = "SELECT * FROM `" + name ;
		ArrayList<Kurs> kursreihe = getKursreiheSELECT(select);
		return kursreihe; 
	}
	/**
	 * Liest alle vorhandenen Kursinformationen zu einem Wertpapier
	 * ab einem Beginn-Datum 
	 * @return
	 */
	public static ArrayList<Kurs> getKursreihe (String name, GregorianCalendar beginn) {
		if (beginn == null) {
			return null;  // #TODO Exception werfen 
		}
		String select = "SELECT * FROM " + name + " WHERE `datum` >= '" + Util.formatDate(beginn) + "'";
		ArrayList<Kurs> kursreihe = getKursreiheSELECT(select);
		return kursreihe; 
	}
	/**
	 * erzeugt eine Liste von Tageskursreihen aus einem vorbereiteten SELECT-Statement
	 * @param select
	 * @return
	 */
	private static ArrayList<Kurs> getKursreiheSELECT (String select) {
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
	        	tageskurs.setDatum( response.getDate("datum"));
	        	kursreihe.add(tageskurs);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return kursreihe; 
    }
	/**
	 * ein einzelner Kurs wird erzeugt aus einem B-SELECT
	 * @param response
	 * @return
	 */
    private static Kurs createTageskursAusDBSelect (ResultSet response)
    {
    	
    	Kurs kurs = new Kurs ();

		try {
			response.next();
            kurs.close = response.getFloat("close");
            kurs.setDatum(response.getDate("datum"));
            

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