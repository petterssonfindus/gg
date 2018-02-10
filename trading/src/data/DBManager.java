/*
 * Created on 09.10.2006
 */
package data;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Kursreihe;
import kurs.Tageskurs;
import util.Util;

/**
 * @author oskar <br>
 *         über diese Klasse läuft sämtlich Kommunikation mit der Datenbank.<br>
 */

public class DBManager {
	private static final Logger log = LogManager.getLogger(DBManager.class);

	private static final String DBName = "kurse";

	/**
	 * fügt eine neue Wertpapier-Kursreihe hinzu
	 * wenn das Wertpapier noch nicht vorhanden ist, wird es angelegt
	 * wenn bereits Kurs vorhanden sind, werden Daten ergänzt
	 * @param kursreihe
	 * @return
	 */
	public static boolean addKursreihe (Kursreihe kursreihe) {
		// TODO ist die Kursreihe bereits vorhanden? 
		String name = kursreihe.name;
		
		Connection connection = ConnectionFactory.getConnection();

		for (int i = 0 ; i < kursreihe.kurse.size() ; i++) {
			DBManager.addKurs(kursreihe.kurse.get(i), connection);
		}
		return true; 
	}

	/**
	 * fügt einen neuen Kurs in eine bestehende Tabelle
	 * Im Tageskurs sind nur Datum und die Kurse relevant 
	 * Sortierung spielt keine Rolle.
	 * # TODO Fehlerbehandlung, wenn Kurs bereits vorhanden. 
	 */
	public static boolean addKurs(Tageskurs kurs, Connection connection) {
		String name = kurs.name;
		String datum = addApostroph(formatSQLDate(kurs.datum), false);
		String close = addApostroph(kurs.getClose(),true);
		String open = addApostroph(Float.toString(kurs.open), true);
		String high = addApostroph(Float.toString(kurs.high), true);
		String low = addApostroph(Float.toString(kurs.low), true);
		String volume = addApostroph(Integer.toString(kurs.volume), true);

		String insert = "INSERT INTO " + name + " (`datum`, `open`, `high`, `low`, `close`, `volume`) " + 
			"VALUES ("+ datum + open + high + low + close + volume + ")";

		log.info("InsertStatement: " + insert);
		if (connection == null) {
			connection = ConnectionFactory.getConnection();
		}
		Statement anweisung = null;

		try {
			anweisung = (Statement) connection.createStatement();
			anweisung.execute(insert);
		} catch (SQLException e) {
			log.error("Fehler beim Schreiben von Tageskurs "
					+ kurs.toString() + e.toString());
			return false;
		}
		log.info("Kurs " + kurs + " in DB geschrieben ");
		return true;
	}
	
	/**
	 * schreibt alle errechneten Werte dieser Kursreihe in die DB
	 * @param kursreihe
	 */
	public static void schreibeTageskurse (Kursreihe kursreihe) {
		
		Tageskurs tageskurs;
		Connection verbindung = ConnectionFactory.getConnection();
		
		for (int i = 0 ; i < kursreihe.kurse.size(); i++) {
			tageskurs = kursreihe.kurse.get(i);
			DBManager.schreibeTageskurs(tageskurs, kursreihe.name, verbindung);
		}
	}
	
	/**
	 * schreibt oder überschreibt alle Werte des Tageskurses in die DB
	 * Voraussetzung ist vorhandene Tabelle und vorhandener Kurs
	 */
	public static boolean schreibeTageskurs(Tageskurs kurs, String name, Connection verbindung ) {
		
		DBManager.schreibeBergTalLE(kurs,name, verbindung);
		return true;
	}
	/**
	 * schreibt Berg, Tal, letztes Extrem
	 * @param kurs
	 * @param name
	 * @param verbindung
	 * @return
	 */
	private static boolean schreibeBergTalLE(Tageskurs kurs, String name, Connection verbindung) {
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
	public static Tageskurs getTageskurs (String name, GregorianCalendar cal) {
		// SELECT * FROM `appl` WHERE `datum` = '2018-01-02' 
		String select = "SELECT * FROM `appl` WHERE `datum` = '2018-01-02'";
		
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
        Tageskurs kurs = createTageskursAusDBSelect(response);
        kurs.name = name; 
    	return kurs; 
		
	}
	
	/**
	 * Liest alle vorhandenen Kursinformationen zu einem Wertpapier
	 * @param cal
	 * @return
	 */
	public static Kursreihe getKursreihe (String name) {
		String select = "SELECT * FROM `" + name ;
		Kursreihe kursreihe = getKursreiheSELECT(select);
		kursreihe.name = name;
		return kursreihe; 
	}
	/**
	 * Liest alle vorhandenen Kursinformationen zu einem Wertpapier
	 * ab einem Beginn-Datum 
	 * @return
	 */
	public static Kursreihe getKursreihe (String name, GregorianCalendar beginn) {
		if (beginn == null) {
			return null;  // #TODO Exception werfen 
		}
		String select = "SELECT * FROM " + name + " WHERE `datum` >= '" + Util.formatDate(beginn) + "'";
		Kursreihe kursreihe = getKursreiheSELECT(select);
		kursreihe.name = name;
		return kursreihe; 
	}
	/**
	 * erzeugt eine Kursreihe aus einem vorbereiteten SELECT-Statement
	 * @param select
	 * @return
	 */
	private static Kursreihe getKursreiheSELECT (String select) {
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
        Kursreihe kursreihe = createKursreiheAusDBSelect(response);
    	return kursreihe; 
	}
	
	/**
	 * der Kursreihe werden die einzelnen Kurse hinzugefügt
	 * @param response
	 * @return
	 */
	private static Kursreihe createKursreiheAusDBSelect (ResultSet response)
    {
    	Kursreihe kursreihe = new Kursreihe();
    	
    	try {
	        while (response.next())
	        {
	        	Tageskurs tageskurs = new Tageskurs();
	        	tageskurs.setKurs(response.getFloat("close"));
	        	tageskurs.setDatum( response.getDate("datum"));
	        	kursreihe.addKurs(tageskurs);
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
    private static Tageskurs createTageskursAusDBSelect (ResultSet response)
    {
    	
    	Tageskurs kurs = new Tageskurs ();

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