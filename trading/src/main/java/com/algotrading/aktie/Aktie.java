package aktie;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.DBManager;
import indikator.IndikatorBeschreibung;
import indikator.Indikatoren;
import signal.Signal;
import signal.SignalBeschreibung;
import signal.Signalsuche;
import util.Parameter;
import util.Util;
import util.Zeitraum;

/**
 * Repräsentiert eine Aktie am Aktienmarkt
 * Oder auch einen Index oder ein Depot mit täglichen Depotwerten
 * enthält eine Reihe von Kursen mit aufsteigender Sortierung 
 * Erzeugung und Zugang findet über die Klasse Aktien statt 
 */
public class Aktie extends Parameter {
	private static final Logger log = LogManager.getLogger(Aktie.class);

	public String name; 
	public String firmenname; 
	// kein öffentlicher Zugriff auf kurse, weil Initialisierung über DB erfolgt. 
	private ArrayList<Kurs> kurse; 
	// der Kurs, der zum aktuellen Datum des Depot gehört. NextKurs() sorgt für die Aktualisierung
	private Kurs aktuellerKurs; 
	// der Kurs, der zum Start der Simulation gehört
	private Kurs startKurs; 
	// der Zeitraum in dem Kurse vorhanden sind - stammt aus der DB
	private Zeitraum zeitraumKurse; 
	private ArrayList<Kurs> kurseZeitraum; 
	// ein Cache für die aktuell ermittelte Kursereihe 
	private Zeitraum zeitraum; 
	public String indexname;
	public byte boersenplatz; 
	// die Indikatoren-Beschreibungen, die an der Aktie hängen - Zugriff über Getter  
	ArrayList<IndikatorBeschreibung> indikatorBeschreibungen = new ArrayList<IndikatorBeschreibung>();
	private boolean indikatorenSindBerechnet = false; 
	public ArrayList<SignalBeschreibung> signalbeschreibungen = new ArrayList<SignalBeschreibung>();
	private boolean signaleSindBerechnet = false; 
	
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
	 * Zugriff auf die Indikatoren(Beschreibungen), die für eine Aktie existieren. 
	 * Darüber ist ein Zugriff auf die Eindikatoren-Wert am Kurs möglich. 
	 * @return eine Liste der Indikator-Beschreibungen
	 */
	public ArrayList<IndikatorBeschreibung> getIndikatorBeschreibungen() {
		return indikatorBeschreibungen;
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
	 * ermittelt zu einem gegebenen Kurs den Vortageskurs 
	 * wenn es der erste Kurs ist, dann null 
	 * @param kurs
	 * @return Vortageskurs, oder null 
	 */
	public Kurs getVortageskurs (Kurs kurs) {
		int x = kurse.indexOf(kurs);
		if (x > 0) return kurse.get(x - 1);
		else return null; 
	}
	
	public Kurs getStartKurs () {
		return this.startKurs;
	}
	/**
	 * ermittelt den Kurs zu einem bestimmten Datum
	 * Ist der Kurs nicht vorhanden, dann null. 
	 * @param datum
	 * @return der Kurs dieses Tages, oder null wenn nicht vorhanden 
	 */
	public Kurs getKurs (GregorianCalendar datum) {
		ArrayList<Kurs> kurse = this.getBoersenkurse();
		for (Kurs kurs : kurse) {
			if (Util.istGleicherKalendertag(datum, kurs.datum)) {
				return kurs; 
			}
		}
		return null; 
	}
	
	/**
	 * ermittelt und initialisiert eine Kursreihe innerhalb eines Zeitraums
	 * Ein Cache für einen Zeitraum wird verwendet. 
	 * @param beginn
	 * @param ende
	 * @return
	 */
	public ArrayList<Kurs> getKurse(Zeitraum zeitraum) {
		ArrayList<Kurs> result = null;
		if (zeitraum == null) log.error("Inputvariable Zeispanne ist null");
		// wenn es bereits eine Zeitraum gibt und diese ist identisch mit der angeforderten
		if (this.zeitraum != null && this.zeitraum.equals(zeitraum)) {
			result = this.kurseZeitraum;
		}
		// der Cache muss neu gefüllt werden 
		else {
			result = this.sucheBoersenkurse(zeitraum);
		}
		this.zeitraum = zeitraum; 
		this.kurseZeitraum = result; 
		return result; 
	}
	
	private ArrayList<Kurs> sucheBoersenkurse (Zeitraum Zeitraum) {
		ArrayList<Kurs> kurse = new ArrayList<Kurs>();
		for (Kurs kurs : this.getBoersenkurse()) {
			if (Util.istInZeitraum(kurs.datum, Zeitraum)) {
				kurse.add(kurs);
			}
		}
		return kurse; 
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
	 * der nächste Tageskurs im Ablauf einer Simulation 
	 * darf/muss für jeden Handelstag genau ein Mal aufgerufen werden. 
	 * @param kurs
	 * @return
	 */
	public Kurs nextKurs () {
		int x = this.kurse.indexOf(this.aktuellerKurs);
		if (x > this.kurse.size()-2) {
			log.error("Kursreihe zu Ende " + 
				this.aktuellerKurs.wertpapier + Util.formatDate(this.aktuellerKurs.datum));
			return this.aktuellerKurs;
		}
		Kurs kurs = this.kurse.get(x + 1);
		this.aktuellerKurs = kurs; 
		return kurs; 
	}
	/**
	 * Der Kurs, der innerhalb einer Simulation aktuell ist 
	 * @return
	 */
	public Kurs getAktuellerKurs () {
		return this.aktuellerKurs;
	}
	
	/**
	 * Zum Beginn der Zeitreihe wird der Startkurs auf 100% gesetzt 
	 * @return
	 */
	public float getIndexierterKurs () {
		float aktuellerKurs = this.getAktuellerKurs().getKurs();
		return (100 * aktuellerKurs / this.startKurs.getKurs());
	}
	
	/**
	 * ermittelt und initialisiert eine Kursreihe ab einem bestimmten Datum. 
	 * @param beginn
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
		int anzahl = this.getBoersenkurse().size();
		float[] floatKurse = new float[anzahl];
		for (int i = 0 ; i < kurse.size() ; i++) {
			floatKurse[i] = this.kurse.get(i).getKurs();
		}
		return floatKurse;
	}
	/**
	 * einen neuen Indikator hinzufügen 
	 * @param typ
	 * @return der neue Indikator
	 */
	public void addIndikator (IndikatorBeschreibung indikator) {
		this.indikatorBeschreibungen.add(indikator);
	}
	
	/**
	 * Startet die Berechnung der Indikatoren, die als Indikator-Beschreibungen an der Aktie hängen
	 */
	public void rechneIndikatoren () {
		if (! this.indikatorenSindBerechnet) {
			Indikatoren.rechneIndikatoren(this);
			this.indikatorenSindBerechnet = true;
		}
	}
	/**
	 * Bestehende SignalBeschreibugen werden entfernt. 
	 * Indikatoren bleiben erhalten 
	 * Anschließend muss die SignalBerechnung erneut durchgeführt werden. 
	 */
	public void clearSignale () {
		this.deleteSignale();
		this.signalbeschreibungen = new ArrayList<SignalBeschreibung>();
		this.signaleSindBerechnet = false; 
	}
	/**
	 * Eine neue SignalBeschreibung, die anschließend berechnet wird
	 * Die Berechnung darf noch nicht durchgeführt sein. 
	 * @param typ
	 * @return
	 */
	public void addSignalBeschreibung (SignalBeschreibung signalBeschreibung) {
		if (this.signaleSindBerechnet) log.error("neues Signal, Berechnung bereits durchgeführt");
		this.signalbeschreibungen.add(signalBeschreibung);
	}
	/**
	 * Berechnet alle Signale für alle Kurse anhand der SignalBeschreibungen 
	 */
	public void rechneSignale () {
		if (! this.signaleSindBerechnet && this.signalbeschreibungen.size() > 0) {
			Signalsuche.rechneSignale(this);
			this.signaleSindBerechnet = true; 
		}
	}
	
	public String toSmallString () {
		return this.name;
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
			fileWriter.write(Util.getLineSeparator());
			
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
			fileWriter.write(Util.getLineSeparator());
			
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
	 * ermittelt und setzt den Tageskurs zu einem gegebenen Datum 
	 * oder den ersten darauffolgenden Kurs, kann auch mehrere Tage danach sein. 
	 * Zur einmaligen Initialisierung am Beginn der Simulation
	 * @param datum
	 * @return
	 */
	public Kurs setStartkurs (GregorianCalendar datum) {
		if (datum == null) log.error("Inputvariable datum ist null");
		Kurs kurs; 
		for (int i = 0 ; i < this.kurse.size(); i++) {	// von links nach rechts
			// #TODO der Vergleich müsste mit before() oder after() gelöst werden, nicht mit Milli-Vergleich
			kurs = this.kurse.get(i);
			if (kurs.datum.getTimeInMillis() >= datum.getTimeInMillis()) {
				log.debug("Kurs gefunden: " + kurs);
				this.aktuellerKurs = kurs;
				this.startKurs = kurs; 
				return kurs;
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
		if ( this.kurse == null) log.error("keine Kurse vorhanden in Aktie " + this.name);
		ArrayList<Signal> signale = new ArrayList<Signal>();
		// geht durch alle Kurse und holt die angehängten Signale
		for (Kurs kurs : this.kurse) {
			signale.addAll(kurs.getSignale());
		}
		return signale;
	}
	
	/**
	 * Löscht alle Signale die an Kursen hängen
	 */
	private void deleteSignale () {
		for (Kurs kurs : this.kurse) {
			kurs.clearSignale();
		}
	}
	
	public Zeitraum getZeitraumKurse() {
		return zeitraumKurse;
	}
	
	/**
	 * Setzt den Zeitraum, in dem Kurse vorhanden sind. 
	 * Info stammt aus den Stammdaten in der DB
	 * @param zeitraum
	 */
	public void setZeitraumKurse (Zeitraum zeitraum) {
		this.zeitraumKurse = zeitraum;
	}
	
	/**
	 * Setzt den Zeitraum der vorhandenen Kurse anhand der Kurse in der DB 
	 * Liest dazu alle Kurse aus der DB und ermittelt Anfang und Ende, um die Stammdaten zu aktualisieren. 
	 * Normalerweise werden die Daten aus der DB gelesen. 
	 * @param zeitraumKurse
	 */
	public void setZeitraumKurseAusDB() {
		this.zeitraumKurse = DBManager.getZeitraumVorhandeneKurse(this);
	}

	/**
	 * schreibt alle Kurse, Differenzen und Indikatoren in den Writer
	 * @param writer
	 */
	private void writeIndikatoren (FileWriter writer) {
		try {
			writer.write("datum;close;Talsumme;Bergsumme;LetzterTalKurs;LetzterBergKurs;" + 
					"GD10Tage;GD30Tage;GD100Tage;" +
					"Vola10;Vola30;Vola100;" + 
					"RSI;" ); 
			writer.write(Util.getLineSeparator());
			for (int i = 0 ; i < kurse.size(); i++) {
				writer.write(kurse.get(i).toString());
				writer.write(Util.getLineSeparator());
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
			writer.write(Util.getLineSeparator());
			// mit allen Kursen mit allen Signalen
			ArrayList<Signal> signale = getSignale();
			for (int i = 0 ; i < signale.size() ; i++) {
					writer.write(signale.get(i).toString());
					writer.write(Util.getLineSeparator());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
