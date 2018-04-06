package aktie;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.DBManager;
import util.Util;
import util.Zeitraum;

	/**
	 * Verzeichnis aller Aktien, zu denen Zeitreihen vorhanden sind
	 * Es kann sich auch um einen Index handeln 
	 * Bietet Zugang zu Zeitreihen
	 * Als Singleton verfügbar
	 * Das Verzeichnis wird sofort initialisiert, die Kurse werden erst bei Bedarf den Kursreihen hinzugefügt. 
	 * Derzeit keine DB-Lösung, sondern Programmcode
	 * @author oskar
	 *
	 */
public class Aktien {
	private static final Logger log = LogManager.getLogger(Aktien.class);
	
	public static final byte BOERSEDEPOT = 0;
	public static final byte BOERSEINDEX = 1;
	public static final byte BOERSENYSE = 2;
	public static final byte BOERSENASDAQ = 3;
	public static final byte BOERSEFRANKFURT = 4;
	public static final byte BOERSEXETRA = 5;
	
	public static final String INDEXDAX = "dax";
	public static final String INDEXDOWJONES = "dowjones";
	
	private static Aktien instance;
	// das Verzeichnis aller Aktien 
	private static HashMap<String, Aktie> verzeichnis = new HashMap<String, Aktie>();
	
	private Aktien() {}
	
	public static Aktien getInstance() {
		if (instance == null) {
			instance = new Aktien();
			// das Verzeichnis wird versorgt 
			initialisiereVerzeichnis();
		}
		return instance; 
	}
	/**
	 * liest und initialisiert eine Aktie anhand eines WP-Namens
	 * die Kursreihe ist eventuell noch nicht gefüllt. 
	 * Wird beim Zugriff gefüllt. 
	 * @param wertpapier
	 * @return
	 */
	public Aktie getAktie (String wertpapier) {
		if (wertpapier == null) log.error("Inputvariable Wertpapier ist null");
		if (wertpapier == "") log.error("Inputvariable Wertpapier ist leer");
		wertpapier = wertpapier.toLowerCase();
		Aktie aktie = null; 
		// sucht das Wertpapier im Verzeichnis
		if (verzeichnis.containsKey(wertpapier) ) {
			aktie = verzeichnis.get(wertpapier);
			// zu Beginn sind alle Kursreihen vorhanden, aber ohne Kurse 
			if (aktie == null) log.error("Aktie ist null : " + wertpapier);
		}
		else log.error("Aktie nicht vorhanden: " + wertpapier);
		return aktie; 
	}
	
	/**
	 * eine Liste aller registrierter Aktien incl. Indizes
	 */
	public ArrayList<Aktie> getAllAktien () {
		ArrayList<Aktie> result = new ArrayList<Aktie>();
		for (Aktie aktie : verzeichnis.values()) {
			result.add(aktie);
		}
		return result;
	}
	/**
	 * Alle Aktien und Indizes, bei denen Kurse innerhalb des gewünschten Zeitraums vorhanden sind 
	 * @return Liste von Aktien mit Kursreihen
	 */
	public ArrayList<Aktie> getAktien (Zeitraum zeitraum) {
		ArrayList<Aktie> result = new ArrayList<Aktie>();
		long beginn = zeitraum.beginn.getTimeInMillis();
		long ende = zeitraum.ende.getTimeInMillis();
		for (Aktie aktie : verzeichnis.values()) {
			if (aktie.getZeitraumKurse().beginn.getTimeInMillis() < beginn &&  
					aktie.getZeitraumKurse().ende.getTimeInMillis() > ende) {
				result.add(aktie);
			}
		}
		return result;
		
	}
	/**
	 * Ermittelt für alle Aktien die vorhandenen Kursezeitraum aus den Kursen aus der DB
	 * und hängt den Zeitraum an die Aktie
	 */
	private static void ermittleAktienZeitraeume () {
		for (Aktie aktie : verzeichnis.values()) {
			aktie.setZeitraumKurseAusDB();
		}
	}
	
	/**
	 * das Verzeichnis wird beim Erstellen initialisiert aus der DB 
	 */
	private static void initialisiereVerzeichnis() {
		verzeichnis = DBManager.getVerzeichnis();
	}

}
