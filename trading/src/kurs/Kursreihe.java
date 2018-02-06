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
import util.Util;

/**
 * eine Reihe von Kursen mit aufsteigender Sortierung 
 * die zeitlichen Abstände sind beliebig, es können Tages oder Intradaykurse sein. 
 * Die Erzeugung findet über die Klasse Aktien statt 
 */
public class Kursreihe {
	private static final Logger log = LogManager.getLogger(Kursreihe.class);

	public String name; 
	public ArrayList<Tageskurs> kurse = new ArrayList<Tageskurs>(); 
	
	/**
	 * Ermittelt und initialisiert eine Kursreihe mit allen vorhandenen Kursen
	 * Es wird gecacht. 
	 * Mit Hilfe der Klasse Aktien 
	 * @param name
	 * @return
	 */
	public static Kursreihe getKursreihe (String name) {
		return Aktien.getInstance().getKursreihe(name);
	}
	/**
	 * ermittelt und initialisiert eine Kursreihe ab einem bestimmten Datum. 
	 * Die Kursreihe wird jedes Mal neu erzeugt. 
	 * @param beginn
	 * @param ende
	 * @return
	 */
	public static Kursreihe getKursreihe (String name, GregorianCalendar beginn) {
		if (beginn == null) log.error("Inputvariable Beginn ist null");
		return DBManager.getKursreihe(name, beginn);
	}
	
	/**
	 * hängt einen Kurs an das Ende der bestehenden Kette an
	 * @param ein beliebiger Kurs 
	 */
	public void addKurs(Tageskurs kurs) {
		if (kurs == null) log.error("Inputvariable Kurs ist null");
		kurse.add(kurs);
	}
	
	/**
	 * ein Array mit allen vorhandenen Kursen 
	 * wird für Rechen-Operationen genutzt, um schnell auf Kurse zugreifen zu können. 
	 * @return
	 */
	public float[] getKurse () {
		int anzahl = kurse.size();
		float[] floatKurse = new float[anzahl];
		for (int i = 0 ; i < kurse.size() ; i++) {
			floatKurse[i] = this.kurse.get(i).getKurs();
		}
		return floatKurse;
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
				log.debug("Die Datei konnte nicht erstellt werden!");
			}
			
			FileWriter fileWriter = new FileWriter(file);
			writeIndikatoren(fileWriter);
			
			// Zeilenumbruch an dem Ende der Datei ausgeben
			fileWriter.write(System.getProperty("line.separator"));
			
			// Writer schlieÃŸen
			fileWriter.close();
			log.debug("Datei geschrieben: " + file.getAbsolutePath() );
			
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
				log.debug("Die Datei konnte nicht erstellt werden!");
			}
			
			FileWriter fileWriter = new FileWriter(file);
			writeSignale(fileWriter);
			
			// Zeilenumbruch an dem Ende der Datei ausgeben
			fileWriter.write(System.getProperty("line.separator"));
			
			// Writer schlieÃŸen
			fileWriter.close();
			log.debug("Datei geschrieben: " + file.getAbsolutePath() );
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
	public Tageskurs ermittleTageskursVortag (Tageskurs tk) {
		if (tk == null) log.error("Inputvariable Tageskurs ist null");
		int stelle = this.kurse.indexOf(tk);
		if (stelle > 0) {
			return this.kurse.get(stelle - 1);
		}
		else return null;
	}
	/**
	 * ermittelt den Tageskurs an einem gegebenen Datum 
	 * @param datum
	 * @return
	 */
	public Tageskurs getTageskurs (GregorianCalendar datum) {
		if (datum == null) log.error("Inputvariable datum ist null");
		for (int i = 0 ; i < this.kurse.size(); i++) {
//			if (this.kurse.get(i).datum.equals(datum)) {
			// #TODO der Vergleich müsste mit before() oder after() gelÃ¶st werden, nicht mit Milli-Vergleich
			if (this.kurse.get(i).datum.getTimeInMillis() == datum.getTimeInMillis()) {
				return this.kurse.get(i);
			}
		}
		log.debug("Tageskurs nicht gefunden: " + Util.formatDate(datum));
		return null;
	}
	/**
	 * alle Signale von allen Tageskursen nach Datum aufsteigend
	 * @return
	 */
	public ArrayList<Signal> getSignale() {
		ArrayList<Signal> signale = new ArrayList<Signal>();
		for (int i = 0 ; i < kurse.size(); i++) {
			signale.addAll(kurse.get(i).signale);
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
