package depot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktien;
import kurs.Kursreihe;
import kurs.Tageskurs;
import signal.Signal;
import util.Util;
/**
 * Simuliert ein Wertpapierdepot 
 * Verwaltet Wertpapierbest�nde und die Liste der Orders. 
 * �berwacht die Ausf�hrung von Limitierten Orders. 
 * Errechnet seinen t�glichen Depotwert. 
 * @author oskar
 *
 */
public class Depot {
	private static final Logger log = LogManager.getLogger(Depot.class);
	
	String name; 
//	Kursreihe kursreihe;
	float geld;  // Startkapital
	ArrayList<Order> orders = new ArrayList<Order>();
	private float geldZumStichtag; // ein zum Stichtag ermittelter Geldbestand 
	
	public Depot (String name, float geld) {
		this.name = name; 
		this.geld = geld;
		
	}
	
	/**
	 * Simulation mit einem einzigen Wertpapier
	 * mit jedem Kaufsignal wird gekauft - Verkaufssignal wird verkauft. 
	 * Im vergleich zu buy-and-hold 
	 * @param kursreihe
	 */
	public void simuliereHandel (Kursreihe kursreihe) {
		if (kursreihe == null) log.error("Inputvariable Kursreihe ist null");

		ArrayList<Signal> signale = kursreihe.getSignale();
		for (int i = 0 ; i < signale.size(); i++) {
			Signal signal = signale.get(i);
			if (signal.getKaufVerkauf() == Signal.KAUF) {
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
		Order.orderAusfuehren(Signal.KAUF, datum, kursreihe.name, stueckzahl, this, kursreihe);
		
	}
	/**
	 * Beim Verkauf wird gepr�ft, ob gen�gend Wertpapiere vorhanden sind. 
	 * Wenn nicht, wird die Order angepasst auf die vorhandenen St�cke. 
	 * @param datum
	 * @param betrag
	 * @param kursreihe
	 */
	private void verkaufe (GregorianCalendar datum, float betrag, Kursreihe kursreihe) {
		if (datum == null) log.error("Inputvariable beginn ist null");
		if (kursreihe == null) log.error("Inputvariable Kursreihe ist null");
		float anzahl = 0;
		float depotstuecke = 0;
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
			Order.orderAusfuehren(Signal.VERKAUF, datum, kursreihe.name, anzahl, this, kursreihe);
		}
	}
	/**
	 * ermittelt einen Depotwert an jedem Tag ab einem gew�nschten Datum 
	 * @param beginn
	 * @param ende
	 * @return
	 */
	public Kursreihe bewerteDepotTaeglich (GregorianCalendar beginn) {
		if (beginn == null) log.error("Inputvariable beginn ist null");
		// die Dat�mer stammen aus der DAX-Kursreihe 
		Kursreihe dax = Kursreihe.getKursreihe("dax", beginn);
		Kursreihe depotKursreihe = new Kursreihe();
		depotKursreihe.name = this.name;
		Tageskurs daxkurs; 
		GregorianCalendar datum; 
		// von hinten nach vorne jedes Datum iterieren
		for (int i = dax.kurse.size() - 1 ; i >= 0 ; i--) {
			daxkurs = dax.kurse.get(i);
			// Datum pr�fen: DAX-Kurs liegt nach dem gew�nschten Beginn
			if (daxkurs.datum.after(beginn)) {
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
		// ermittelt den Depotbestand als Liste von Wertpapier-Best�nden
		ArrayList<WertpapierBestand> depotBestand = ermittleDepotBestand (datum);
		// bewertet den Depotbestand mit zugeh�rigen Kursen
		float depotwert = bewerteDepotbestand(depotBestand, datum);
		depotwert += geldZumStichtag; // wurde berechnet bei der Ermittlung des DepotBestand
		return depotwert; 
	}
	/**
	 * ermittelt den Depotbestand zu einem bestimmten Zeitpunkt.
	 * Anhand der Order-Historie wird der Depotbestand rekonstruiert. 
	 * @param datum
	 * @return
	 */
	private ArrayList<WertpapierBestand> ermittleDepotBestand (GregorianCalendar datum) {
		if (datum == null ) {
			log.error("Inputvariable datum ist null");
		}
		ArrayList<WertpapierBestand> depotBestand = new ArrayList<WertpapierBestand>(); 
		Order order; 
		boolean geldbestandErmittelt = false; 
		ArrayList<String> wertpapiere = new ArrayList<String>(); // die bisher gefundenen Wertpapiere
		// geht von den jungen Orders Richtung alte Order. 
		for (int i = this.orders.size() -1 ; i > 0 ; i--) {
			order = this.orders.get(i);
			// pr�fe: Order liegt vor Datum 
			if (order.datum.before(datum)) { // die Order hat sich bereits ereignet
				// ermittle Geld: die erste Order, die er findet, enth�lt den Geldbestand.
				if (! geldbestandErmittelt) {
					geldZumStichtag = order.depotGeld;
					geldbestandErmittelt = true;
				}
				// pr�fe: neues Wertpapier
				if (! wertpapiere.contains(order.wertpapier)) {  // das Wertpapier ist neu
					// einen neuen Wertpapierbestand anlegen
					depotBestand.add(new WertpapierBestand(order.wertpapier, order.depotStueckzahl));
					wertpapiere.add(order.wertpapier);
				}
			}
		}
		
		return depotBestand;
	}
	/**
	 * Bewertet den Depotbestand mit zugeh�rigen Kursen. 
	 * @param wertpapierBestand
	 * @return
	 */
	private float bewerteDepotbestand (ArrayList<WertpapierBestand> wertpapierBestand, GregorianCalendar datum ) {
		if (wertpapierBestand == null || datum == null) {
			log.error("Inputvariable ist null: wertpapierbestand oder " + Util.formatDate(datum));
		}
		// der Depotbestand wird bewertet
		float depotwert = 0;
		float wertpapierwert = 0;
		Tageskurs tageskurs; 
		Kursreihe kursreihe; 
		// geht durch alle Wertpapiere des Depotbestand
		for (int i = 0 ; i < wertpapierBestand.size() ; i++) {
			WertpapierBestand wertpapierbestand = wertpapierBestand.get(i);
			// ermittle Kurs eines Wertpapier zum Zeitpunkt t
			kursreihe = Aktien.getInstance().getKursreihe(wertpapierbestand.wertpapier);
			tageskurs = kursreihe.getTageskurs(datum);
			// multipliziere und addiere
			wertpapierwert = tageskurs.getKurs() * wertpapierbestand.stueckzahl;
			depotwert += wertpapierwert; 
		}
		return depotwert;
	}
	
	/**
	 * liefert die aktuelle St�ckzahl im Depotbestand, oder 0
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
	 * ermittelt zum aktuellen Depot-Zustand die letzte Order eines Wertpapiers 
	 * wenn es die 1. Order dieses Wertpapiers ist, dann null. 
	 * @param name
	 * @return die letzte Order, oder null 
	 */
	protected Order getLetzteOrder (String name) {
		if (name == null) log.error("Inputvariable name ist null");
		// geht durch alle Orders r�ckw�rts durch
		Order order = null; 
		for (int i = this.orders.size()-1 ; i >= 0 ; i--) {
			order = this.orders.get(i);
			// pr�ft, ob das Wertpapier betroffen ist 
			if (order.wertpapier == name) {
				return order; 
			}
		}
		// keine Order gefunden, dann null 
		return null; 
	}
	
	/**
	 * eine ausgef�hrte Order wird eingetragen. 
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
				log.debug("Die Datei konnte nicht erstellt werden!");
			}
			FileWriter fileWriter = new FileWriter(file);
			writeOrders(fileWriter);
			
			// Zeilenumbruch an dem Ende der Datei ausgeben
			fileWriter.write(System.getProperty("line.separator"));
			// Writer schließen
			fileWriter.close();
			log.debug("Datei geschrieben: " + file.getAbsolutePath() );
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeOrders (FileWriter writer) {
		try {
			writer.write("Depot ; Wertpapier ; KV ; Datum ; St�cke ; Kurs ; Betrag ; Depotst�cke ; WP-Wert ; DepotGeld ; Depotwert");
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

	class WertpapierBestand {
		String  wertpapier;
		float stueckzahl;
		
		WertpapierBestand (String wertpapier, float stueckzahl) {
			this.wertpapier = wertpapier; 
			this.stueckzahl = stueckzahl;
		}
	}
}
