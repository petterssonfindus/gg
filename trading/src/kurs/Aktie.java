package kurs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.DBManager;
import signal.Signal;
import signal.Signalsuche;
import util.Util;

/**
 * Repräsentiert eine Aktie am Aktienmarkt
 * Oder auch einen Index oder ein Depot mit täglichen Depotwerten
 * enthält eine Reihe von Kursen mit aufsteigender Sortierung 
 * Erzeugung und Zugang findet über die Klasse Aktien statt 
 */
public class Aktie {
	private static final Logger log = LogManager.getLogger(Aktie.class);

	public String name; 
	public String firmenname; 
	// kein öffentlicher Zugriff auf kurse, weil Initialisierung über DB erfolgt. 
	private ArrayList<Kurs> kurse; 
	public String indexname;
	public byte boersenplatz; 
	
	/**
	 * ein Konstruktor mit beschränktem Zugriff für die Klasse Aktien 
	 * enthält alles, außer den Kursen
	 * @param name Kurzname, Kürzel - intern wird immer mit LowerCase gearbeitet
	 * @param firmenname offizieller Firmenname, zur Beschriftung verwendet 
	 * @param indexname zugehöriger Index zu Vergleichszwecken 
	 * @param boersenplatz 
	 */
	public Aktie (String name, String firmenname, String indexname, byte boersenplatz) {
		this.name = name.toLowerCase();
		this.firmenname = firmenname;
		this.indexname = indexname; 
		this.boersenplatz = boersenplatz;
	}
	
	/**
	 * Gibt den Inhalt der Kurse, ohne diese zu initialisieren 
	 * @return
	 */
	public ArrayList<Kurs> getKurse () {
		if (this.kurse == null) log.error("Kurse sind null");
		return this.kurse;
	}
	
	/**
	 * ermittelt und initialisiert eine Kursreihe mit allen vorhandenen Kursen
	 * ungeeignet für Depot-Kursreihen 
	 * @param beginn
	 * @param ende
	 * @return
	 */
	public ArrayList<Kurs> getBoersenkurse () {
		if (this.kurse == null) {
			this.kurse = DBManager.getKursreihe(name);
		}
		return kurse;
	}
	/**
	 * ermittelt und initialisiert eine Kursreihe ab einem bestimmten Datum. 
	 * @param beginn
	 * @param ende
	 * @return
	 */
	public ArrayList<Kurs> getKursreihe (GregorianCalendar beginn) {
		if (beginn == null) log.error("Inputvariable Beginn ist null");
		if (this.kurse == null) {
			this.kurse = DBManager.getKursreihe(name, beginn);
		}
		return kurse;
	}
	
	/**
	 * hängt einen Kurs an das Ende der bestehenden Kette an
	 * @param ein beliebiger Kurs 
	 */
	public void addKurs(Kurs kurs) {
		if (kurs == null) log.error("Inputvariable Kurs ist null");
		if (kurse == null) {  // Aktie aus Depobewertung, die noch keine Kursliste besitzt
			this.kurse = new ArrayList<Kurs>();
		}
		kurse.add(kurs);
	}
	
	/**
	 * ein Array mit allen vorhandenen Kursen 
	 * wird für Rechen-Operationen genutzt, um schnell auf Kurse zugreifen zu können. 
	 * @return
	 */
	public float[] getKursArray () {
		int anzahl = kurse.size();
		float[] floatKurse = new float[anzahl];
		for (int i = 0 ; i < kurse.size() ; i++) {
			floatKurse[i] = this.kurse.get(i).getKurs();
		}
		return floatKurse;
	}
	/**
	 * Rechnet auf Basis vorhandener Kurse alle Indikatoren 
	 */
	public void rechneIndikatoren() {
		if (this.getKurse() == null || this.getKurse().size() == 0) log.error("Keine Kurse vorhanden");
		Statistik.rechneIndikatoren(this);
	}
	
	/**
	 * Rechnet auf Basis vorhandener Kurse alle Signale 
	 */
	public void rechneSignale() {
		if (this.getKurse() == null || this.getKurse().size() == 0) log.error("Keine Kurse vorhanden");
		Signalsuche.rechneSignale(this);
	}

	public String toString () {
		String result;
		result = name + " " + kurse.size() + " Kurse";
		for (int i = 0 ; i < kurse.size(); i++) {
			result = result + (kurse.get(i).toString());
		}
		return result; 
	}

	/**
	 * veranlasst das Schreiben on 2 Dateien und Kursen und Signalen
	 */
	public void writeIndikatorenSignale () {
		writeFileIndikatoren();
		writeFileSignale();
	}
	/**
	 * schreibt eine neue Datei mit allen Kursen, Indikatoren
	 */
	public void writeFileIndikatoren () {
		try {
			String dateiname = "kurse" + this.name + Long.toString(System.currentTimeMillis());
			File file = new File(dateiname + ".csv");
 
			boolean createFileResult = file.createNewFile();
			
			if(!createFileResult) {
				// Die Datei konnte nicht erstellt werden. Evtl. gibt es diese Datei schon?
				log.info("Die Datei konnte nicht erstellt werden!");
			}
			
			FileWriter fileWriter = new FileWriter(file);
			writeIndikatoren(fileWriter);
			
			// Zeilenumbruch an dem Ende der Datei ausgeben
			fileWriter.write(System.getProperty("line.separator"));
			
			// Writer schlieÃŸen
			fileWriter.close();
			log.info("Datei geschrieben: " + file.getAbsolutePath() );
			
		} catch(Exception e) {
			
			// Ausgabe des genauen Fehler Stapels
			e.printStackTrace();
			
		}
	}
	/**
	 * schreibt eine neue Datei mit allen Signalen dieser Kursreihe
	 */
	public void writeFileSignale () {
		try {
			String dateiname = "signale" + this.name + Long.toString(System.currentTimeMillis());
			File file = new File(dateiname + ".csv");
 
			boolean createFileResult = file.createNewFile();
			
			if(!createFileResult) {
				// Die Datei konnte nicht erstellt werden. Evtl. gibt es diese Datei schon?
				log.info("Die Datei konnte nicht erstellt werden!");
			}
			
			FileWriter fileWriter = new FileWriter(file);
			writeSignale(fileWriter);
			
			// Zeilenumbruch an dem Ende der Datei ausgeben
			fileWriter.write(System.getProperty("line.separator"));
			
			// Writer schlieÃŸen
			fileWriter.close();
			log.info("Datei geschrieben: " + file.getAbsolutePath() );
		} catch(Exception e) {
			// Ausgabe des genauen Fehler Stapels
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param tk
	 * @return
	 */
	public Kurs ermittleTageskursVortag (Kurs tk) {
		if (tk == null) log.error("Inputvariable Tageskurs ist null");
		int stelle = this.kurse.indexOf(tk);
		if (stelle > 0) {
			return this.kurse.get(stelle - 1);
		}
		else return null;
	}
	/**
	 * ermittelt den Tageskurs an einem gegebenen Datum 
	 * oder den ersten darauffolgenden Kurs, kann auch mehrere Tage danach sein. 
	 * @param datum
	 * @return
	 */
	public Kurs getTageskurs (GregorianCalendar datum) {
		if (datum == null) log.error("Inputvariable datum ist null");
		for (int i = 0 ; i < this.kurse.size(); i++) {	// von links nach rechts
			// #TODO der Vergleich müsste mit before() oder after() gelöst werden, nicht mit Milli-Vergleich
			if (this.kurse.get(i).datum.getTimeInMillis() >= datum.getTimeInMillis()) {
				return this.kurse.get(i);
			}
		}
		log.info("Tageskurs nicht gefunden: " + Util.formatDate(datum));
		return null;
	}
	/**
	 * alle Signale von allen Tageskursen nach Datum aufsteigend
	 * @return
	 */
	public ArrayList<Signal> getSignale() {
		if ( kurse == null) log.error("keine Kurse vorhanden in Aktie " + this.name);
		ArrayList<Signal> signale = new ArrayList<Signal>();
		// geht durch alle Kurse und holt die angehängten Signale
		for (Kurs kurs : kurse) {
			signale.addAll(kurs.signale);
		}
		return signale;
	}
	/**
	 * schreibt alle Kurse, Differenzen und Indikatoren in den Writer
	 * @param writer
	 */
	private void writeIndikatoren (FileWriter writer) {
		try {
			writer.write("datum ; close ; Talsumme ; Bergsumme ; LetzterTalKurs ; LetzterBergKurs ; GD10Tage ; GD30Tage ; GD100Tage ; " +
					"Vola10;Vola30;Vola100" + 
					"KursDiff-1 ; KursDiff+1; KursDiff-2 ; KursDiff+2; KursDiff-3; KursDiff+3; KursDiff-4; KursDiff+4");
			writer.write(System.getProperty("line.separator"));
			for (int i = 0 ; i < kurse.size(); i++) {
				writer.write(kurse.get(i).toString());
				writer.write(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * schreibt ein Signal in den Writer
	 * @param writer
	 */
	private void writeSignale (FileWriter writer) {
		try {
			writer.write("Name ; Datum ; KaufVerkauf; Typ; Stärke");
			writer.write(System.getProperty("line.separator"));
			// mit allen Kursen mit allen Signalen
			ArrayList<Signal> signale = getSignale();
			for (int i = 0 ; i < signale.size() ; i++) {
					writer.write(signale.get(i).toString());
					writer.write(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
