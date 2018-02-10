package depot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktien;
import kurs.Kursreihe;
import kurs.Tageskurs;
import signal.Signal;
import util.Util;

/**
 * Simuliert ein Wertpapierdepot 
 * Verwaltet Wertpapierbestände und die Liste der Orders. 
 * Überwacht die Ausführung von Limitierten Orders. 
 * Errechnet seinen täglichen Depotwert. 
 * @author oskar
 *
 */
public class Depot {
	private static final Logger log = LogManager.getLogger(Depot.class);
	
	String name; 
//	Kursreihe kursreihe;
	float geld;  // Geldbestand 
	ArrayList<Order> orders = new ArrayList<Order>();
	
	private float geldZumStichtag; // ein zum Stichtag ermittelter Geldbestand - Methoden-interne Nutzung 
	
	public Depot (String name, float geld) {
		this.name = name; 
		this.geld = geld;
		
	}
	
	/**
	 * Simulation mit einem Wertpapier
	 * Alle Kaufsignale werden umgesetzt mit der Hälfte des vorhandene Geldes
	 * Wertpapier werden bis zum Stop-Loss gehalten. = 2 % unter Kauf-Kurs 
	 * @param kursreihe
	 */
	public void handleKaufsignaleMitStopLoss (String wertpapier, float SLSchwelle) {
		Kursreihe kursreihe = Aktien.getInstance().getKursreihe(wertpapier);
		ArrayList<Signal> signale = kursreihe.getSignale();
		Tageskurs tageskurs; 
		int signalZaehler = 0; 
		Signal signal = signale.get(signalZaehler);
		// geht durch jeden einzelnen Tag 
		for (int i = 10 ; i < kursreihe.kurse.size(); i++) {
			tageskurs = kursreihe.kurse.get(i);
			// wenn an diesem Tag ein Signal auftritt
			if (Util.istGleicherKalendertag(tageskurs.datum, signal.getTageskurs().datum)) {
				if (signal.getKaufVerkauf() == Order.KAUF) {
					// der Kauf wird ausgelöst
					kaufe(signal.getTageskurs().datum, this.geld/2 , kursreihe);
				}
			}
			// prüfe Stop-Loss
			stopLossKursFaelltUnterEinstand(tageskurs.datum, SLSchwelle);
		}
	}
	/**
	 * 
	 * @param schwelle
	 * @return
	 */
	private void stopLossKursFaelltUnterEinstand(GregorianCalendar datum, float schwelle) {
		HashMap<String, Order> depotBestand = ermittleDepotBestand(datum);
		// geht durch alle Wertpapiere 
		for (Order order : depotBestand.values()) {
			// wenn der Tageskurs unter der Schwelle liegt
			Kursreihe kr = Aktien.getInstance().getKursreihe(order.wertpapier);
			Tageskurs kursAktuell = kr.getTageskurs(datum);
			// aktueller Kurs fällt unter Stop-Loss
			if (kursAktuell.getKurs() < (order.durchschnittskurs - schwelle)) {
				// Gesamtbestand wird verkauft
				verkaufeWertpapier(datum, order.wertpapier);
			}
		}
	}
	
	/**
	 * Simulation mit einem einzigen Wertpapier
	 * mit jedem Kaufsignal wird gekauft - Verkaufssignal wird verkauft. 
	 * Im vergleich zu buy-and-hold 
	 * @param kursreihe
	 */
	public void handleAlleSignale (String wertpapier) {
		if (wertpapier == null || wertpapier == "") log.error("Inputvariable Kursreihe ist null");
		Kursreihe kursreihe = Aktien.getInstance().getKursreihe(wertpapier);
		ArrayList<Signal> signale = kursreihe.getSignale();
		for (int i = 0 ; i < signale.size(); i++) {
			Signal signal = signale.get(i);
			if (signal.getKaufVerkauf() == Order.KAUF) {
				// ist Geld vorhanden
				if (geld > 1000) {
					// KAUFE ein Drittel
					kaufe(signal.getTageskurs().datum , 3000, kursreihe);
				}
			}
			else  // ein Verkauf 
			{
				verkaufe (signal.getTageskurs().datum , 3000, kursreihe);
			}
			
		}
		verkaufe(signale.get(signale.size()-1).getTageskurs().datum,20000, kursreihe);
	}
	/**
	 * kauft mit Disposition 
	 * @param datum
	 * @param betrag
	 */
	private void kaufe (GregorianCalendar datum, float betrag, Kursreihe kursreihe) {
		if (datum == null) log.error("Inputvariable Kursreihe ist null");
		if (kursreihe == null) log.error("Inputvariable Kursreihe ist null");
		// Maximum bestehendes Geld
		if (this.geld < betrag) {	// das Geld reicht nicht aus
			betrag = this.geld;		// das vorhandene Geld wird eingesetzt
		}
		// 
		if (betrag < 100) {			// unter 100 macht es keinen Sinn. 
			return; 
		}	
		float kurs = kursreihe.getTageskurs(datum).getKurs();
		float stueckzahl = betrag / kurs; 
		Order.orderAusfuehren(Order.KAUF, datum, kursreihe.name, stueckzahl, this);
		
	}
	/**
	 * Verkauft Gesamtbestand des vorhandenen Wertpapiers
	 */
	private void verkaufeWertpapier (GregorianCalendar datum, String wertpapier) {
		// ermittelt Bestand des Wertpapiers
		float wertpapierbestand = this.getWertpapierStueckzahl(wertpapier);
		Order.orderAusfuehren(Order.VERKAUF, datum, wertpapier, wertpapierbestand, this);
	}
	/**
	 * Beim Verkauf wird geprüft, ob genügend Wertpapiere vorhanden sind. 
	 * Wenn nicht, wird die Order angepasst auf die vorhandenen Stücke. 
	 * @param datum
	 * @param betrag
	 * @param kursreihe
	 */
	private void verkaufe (GregorianCalendar datum, float betrag, Kursreihe kursreihe) {
		if (datum == null) log.error("Inputvariable beginn ist null");
		if (kursreihe == null) log.error("Inputvariable Kursreihe ist null");
		if (betrag == 0) log.error("Inputvariable betrag ist 0");
		float anzahl = 0;
		float wertpapierbestand = this.getWertpapierStueckzahl(kursreihe.name);
		// wenn etwas vorhanden ist
		if (wertpapierbestand > 0) {
			// wenn der Bestand kleiner ist als der Verkaufswunsch
			if ((wertpapierbestand * kursreihe.getTageskurs(datum).getKurs()) < betrag) {
				// alle vorhandenen Aktien verkaufen
				anzahl = wertpapierbestand;
			}
			else {
				anzahl = betrag / kursreihe.getTageskurs(datum).getKurs();
			}
			Order.orderAusfuehren(Order.VERKAUF, datum, kursreihe.name, anzahl, this);
		}
	}
	/**
	 * ermittelt einen Depotwert an jedem Tag innerhalb eines Zeitraums
	 * @param beginn
	 * @param ende
	 * @return
	 */
	public Kursreihe bewerteDepotTaeglich (GregorianCalendar beginn, GregorianCalendar ende) {
		if (beginn == null) log.error("Inputvariable beginn ist null");
		if (ende == null) log.error("Inputvariable ende ist null");
		// die Datümer stammen aus der DAX-Kursreihe 
		Kursreihe dax = Kursreihe.getKursreihe("dax", beginn);
		Kursreihe depotKursreihe = new Kursreihe();
		depotKursreihe.name = this.name;
		Tageskurs daxkurs; 
		GregorianCalendar datum; 
		// von hinten nach vorne jedes Datum iterieren
		for (int i = dax.kurse.size() - 1 ; i >= 0 ; i--) {
			daxkurs = dax.kurse.get(i);
			// Datum prüfen: DAX-Kurs liegt nach dem gewünschten Beginn und vor dem Ende
			if (daxkurs.datum.after(beginn) && daxkurs.datum.before(ende)) {
				datum = daxkurs.datum;
				float wert = this.bewerteDepot(datum);
				Tageskurs depotTK = new Tageskurs();
				// Datum und Depotwert in den Tageskurs eintragen
				depotTK.close = wert; 
				depotTK.datum = datum; 
				depotKursreihe.addKurs(depotTK);
			}
		}
		return depotKursreihe;
	}
	
	/**
	 * geht durch alle Orders vor diesem Zeitpunkt, ermittelt den Bestand und bewertet mit aktuellen Kursen 
	 * @return Wert des Depot in Euro
	 */
	public float bewerteDepot (GregorianCalendar datum) {
		if (datum == null ) {
			log.error("Inputvariable datum ist null");
		}
		// ermittelt den Depotbestand als Liste von Wertpapier-Beständen
		HashMap<String, Order> depotBestand = ermittleDepotBestand (datum);
		// bewertet den Depotbestand mit zugehörigen Kursen
		float depotwert = bewerteDepotbestand(depotBestand, datum);
		depotwert += geldZumStichtag; // wurde berechnet bei der Ermittlung des DepotBestand
		return depotwert; 
	}
	/**
	 * ermittelt den Depotbestand zu einem bestimmten Zeitpunkt.
	 * Anhand der Order-Historie wird der Depotbestand rekonstruiert. 
	 * Die letzte Order eines Wertpapiers enthält den Depotbestand, 
	 * weil in der Order der aktuelle Depotbestand mitgeführt wird. 
	 * @param datum
	 * @return
	 */
	private HashMap<String, Order> ermittleDepotBestand (GregorianCalendar datum) {
		if (datum == null ) {
			log.error("Inputvariable datum ist null");
		}
		HashMap<String, Order> depotBestand = new HashMap<String, Order>(); 
		Order order; 
		boolean geldbestandErmittelt = false; 
		// geht von den jungen Orders Richtung alte Order. 
		for (int i = this.orders.size() -1 ; i > 0 ; i--) {
			order = this.orders.get(i);
			// prüfe: Order liegt vor Datum 
			if (order.datum.before(datum)) { // die Order hat sich bereits ereignet
				// ermittle Geld: die erste Order, die er findet, enthält den Geldbestand.
				if (! geldbestandErmittelt) {
					geldZumStichtag = order.depotGeld;
					geldbestandErmittelt = true;
				}
				// prüfe: neues Wertpapier - wenn bereits vorhanden, geschieht nichts. 
				if (! depotBestand.containsKey(order.wertpapier)) {  // das Wertpapier ist neu
					// dem Depotbestand ein Wertpapier hinzufügen 
					depotBestand.put(order.wertpapier, order);
				}
			}
		}
		
		return depotBestand;
	}
	/**
	 * Bewertet den Depotbestand mit zugehörigen Kursen. 
	 * @param wertpapierBestand
	 * @return
	 */
	private float bewerteDepotbestand (HashMap<String, Order> depotBestand, GregorianCalendar datum ) {
		if (depotBestand == null) log.error("Inputvariable Depotbestand ist null");
		if (datum == null) log.error("Inputvariable datum ist null");
		// der Depotbestand wird bewertet
		float depotwert = 0;
		float wertpapierwert = 0;
		Tageskurs tageskurs; 
		Kursreihe kursreihe; 
		// geht durch alle Wertpapiere des Depotbestand
		for (Order order : depotBestand.values()) {
			if (order == null) log.error("Ein Wertpapierbestand ist null");
			// ermittle Kurs eines Wertpapier zum Zeitpunkt t
			kursreihe = Aktien.getInstance().getKursreihe(order.wertpapier);
			tageskurs = kursreihe.getTageskurs(datum);
			
			// multipliziere und addiere alle Wertpapiere 
			wertpapierwert = tageskurs.getKurs() * order.depotStueckzahl;
			depotwert += wertpapierwert; 
		}
		return depotwert;
	}
	
	/**
	 * liefert die aktuelle Stückzahl im Depotbestand, oder 0
	 * Anhand der letzten getätigten Order
	 * @param name
	 * @return
	 */
	protected float getWertpapierStueckzahl (String name) {
		
		float result = 0;
		Order letzteOrder = getLetzteOrder(name);
		if (letzteOrder != null) {
			result = letzteOrder.depotStueckzahl;
		}
		return result; 
	}
	/**
	 * ermittelt die letzte Order im Depot, egal welches Wertpapier es war
	 * wird derzeit nicht benötigt, weil der aktuelle Geldbestand im Depot geführt wird. 
	 * @return die letzte Order oder null, wenn es die erste Order ist 
	 */
	protected Order getLetzteOrder () {
		// wenn es keine oder nur eine Order gibt, gibt es keine 'letzte Order' 
		if (this.orders.size() <= 1) return null; 
		// nimm die vorletzte Order in der Liste
		return this.orders.get(this.orders.size() - 2);
	}
	
	/**
	 * ermittelt zum aktuellen Depot-Zustand die letzte Order eines Wertpapiers 
	 * wenn es die 1. Order dieses Wertpapiers ist, dann null. 
	 * @param name
	 * @return die letzte Order, oder null 
	 */
	protected Order getLetzteOrder (String name) {
		if (name == null) log.error("Inputvariable name ist null");
		// geht durch alle Orders rückwärts durch
		Order order = null; 
		for (int i = this.orders.size()-1 ; i >= 0 ; i--) {
			order = this.orders.get(i);
			// prüft, ob das Wertpapier betroffen ist 
			if (order.wertpapier == name) {
				return order; 
			}
		}
		// keine Order gefunden, dann null 
		return null; 
	}
	
	/**
	 * eine ausgeführte Order wird eingetragen. 
	 * wird von der Order selbst vorgenommen
	 * @param order
	 * @return
	 */
	boolean orderEintragen (Order order) {
		if (order == null) log.error("Inputvariable Order ist null");
		this.orders.add(order);
		return true;
	}
	
	public void writeFileDepot () {
		try {
			String dateiname = "depot" + this.name + Long.toString(System.currentTimeMillis());
			File file = new File(dateiname + ".csv");
			boolean createFileResult = file.createNewFile();
			if(!createFileResult) {
				// Die Datei konnte nicht erstellt werden. Evtl. gibt es diese Datei schon?
				log.info("Die Datei konnte nicht erstellt werden!");
			}
			FileWriter fileWriter = new FileWriter(file);
			writeOrders(fileWriter);
			
			// Zeilenumbruch an dem Ende der Datei ausgeben
			fileWriter.write(System.getProperty("line.separator"));
			// Writer schließen
			fileWriter.close();
			log.info("Datei geschrieben: " + file.getAbsolutePath() );
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeOrders (FileWriter writer) {
		try {
			writer.write("Depot ; Wertpapier ; KV ; Datum ; Stücke ; Kurs ; Betrag ; Depotstücke ; WP-Wert ; DepotGeld ; Depotwert");
			writer.write(System.getProperty("line.separator"));
			for (int i = 0 ; i < this.orders.size(); i++) {
				writer.write(orders.get(i).toString());
				writer.write(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
