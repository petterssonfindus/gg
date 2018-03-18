package depot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktien;
import kurs.Aktie;
import kurs.Kurs;
import signal.Signal;
import util.Util;

/**
 * Simuliert ein Wertpapierdepot in einem Zeitraum 
 * Verwaltet Wertpapierbest�nde und die Liste der Orders und Trades. 
 * �berwacht die Ausf�hrung von limitierten Orders. 
 * Errechnet seinen t�glichen Depotwert. 
 * @author oskar
 *
 */
public class Depot {
	private static final Logger log = LogManager.getLogger(Depot.class);
	
	String name; 
	// der aktuelle Tag der Simulation - manche Methoden beziehen sich auf aktuelle Kurse 
	GregorianCalendar heute; 	// ist gleichzeit das Ende der Simulation nach Beendigung
	GregorianCalendar beginn;	// Beginn der Simulation
	float geld;  // Geldbestand 
	float anfangsbestand; // 
	ArrayList<Order> orders = new ArrayList<Order>();
	ArrayList<Trade> trades = new ArrayList<Trade>();
	// eine Zeitreihe mit Tagesend-Bewertungen 
	Aktie depotwert; 
	// Liste aller Trades incl Historie mit verschiedenen Zust�nden
	HashMap<String, Trade> aktuelleTrades = new HashMap<String, Trade>();
	// aktuelle Liste aller vorhandenen Wertpapiere
	HashMap<String, Wertpapierbestand> wertpapierbestand = new HashMap<String, Wertpapierbestand>();
	// das Depot kennt seine Strategien
	KaufVerkaufStrategie kaufVerkaufStrategie; 
	StopLossStrategie slStrategie; 
	// Bewertung aller Trades des Depot �ber eine Laufzeit
	Strategiebewertung strategieBewertung; 
	// verbleibt bim Verkauf ein Restbestand, wir dieser mit verkauft
	public static float SCHWELLE_VERKAUF_RESTBESTAND = 100; 
	private static int signalzaehler = 0;
	
	public Depot (String name, float geld) {
		this.name = name; 
		this.geld = geld;
		this.anfangsbestand = geld; 
		log.debug("neues Depot angelegt: " + name + " - " + geld);
	}

	/**
	 * Durchl�uft alle Tage, holt sich alle Signale und handelt
	 * Bewertet die Strategie am Ende 
	 * @param aktie vorbereitet incl. Indikatoren 
	 */
	public void simuliereDepot (KaufVerkaufStrategie kaufVerkaufStrategie, StopLossStrategie slStrategie,
			Aktie aktie, GregorianCalendar beginn, GregorianCalendar ende) {
		if (kaufVerkaufStrategie == null) log.error("Inputparameter Kaufstrategie = null");
		if (slStrategie == null) log.error("Inputparameter SLStrategie = null");
		if (beginn == null) log.error("Inputparameter Beginn = null");
		if (ende == null) log.error("Inputparameter Ende = null");
		if (aktie == null) log.error("Inputparameter Aktie = null");
		if ( ! ende.after(beginn)) log.error("Inputparameter Ende liegt vor Beginn");
		this.beginn = beginn;
		this.slStrategie = slStrategie;
		this.kaufVerkaufStrategie = kaufVerkaufStrategie; 
		// eine Aktie mit Zeitreihe wird angelegt, um die Depotwerte zu speichern 
		this.depotwert = new Aktie(this.name, "Depot " + this.name, Aktien.INDEXDAX,Aktien.BOERSEDEPOT);
		
		int tagZaehler = 0;

		// geht durch jeden einzelnen Tag der Input-Aktie
		for (Kurs kurs : aktie.getBoersenkurse()) {
			tagZaehler ++;
			if (tagZaehler > 10) {  // die ersten Tage werden ignoriert
				// der aktuelle Tag der Simulation 
				this.heute = kurs.datum;
				// pr�ft die Zeitraum 
				if (Util.istInZeitraum(this.heute, beginn, ende)) {
					// die Simulation beginnt und wird als Depot-Kurs eingetragen 
					this.depotwert.addKurs(simuliereHandelstag(kaufVerkaufStrategie, slStrategie, kurs));
				}
				// am letzten Tag wird alles verkauft und damit der letzte Trade geschlossen 
				else if (this.heute.after(ende) && this.wertpapierbestand.keySet().size() > 0) {
					verkaufeGesamtbestand();
				}
				
			}
		}
		log.debug("In Depotsimulation wurden Signale erkannt: " + signalzaehler);
		log.debug("In Depotsimulation wurden Orders erzeugt: " + orders.size());
		log.debug("In Depotsimulation wurden Trades erzeugt: " + trades.size());
		this.bewerteStrategie();
	}
	
	/**
	 * Simuliert einen Handelstag mit Kauf / Verkaufsentscheidungen und einer Depotbewertung 
	 * @param kaufstrategie
	 * @param slStrategie
	 * @param kurs
	 * @return der Tagesendwert des Depot 
	 */
	private Kurs simuliereHandelstag(KaufVerkaufStrategie kaufstrategie, StopLossStrategie slStrategie, Kurs kurs) {
		Kurs kursDepot = new Kurs();
		kursDepot.datum = this.heute;
		// Signale werden genutzt
		if (kurs.getSignale() != null && kurs.getSignale().size() > 0) {
			// jedes Signal wird weiter geleitet
			for (Signal signal : kurs.getSignale()) {
				// die Kaufstrategie bekommt das Signal 
				signalzaehler ++; 
				kaufstrategie.entscheideSignal(signal, this);
			}
		}
		// Stop-Loss wird �berwacht 
		slStrategie.entscheideStopLoss(this);
		
		kursDepot.close = this.bewerteDepotAktuell();
		return kursDepot; 
	}
	/**
	 * kauft mit Disposition 
	 * @param aktie
	 * @param betrag
	 */
	protected void kaufe (float betrag, Aktie aktie) {
		if (aktie == null) log.error("Inputvariable Aktie ist null");
		// Maximum bestehendes Geld
		if (this.geld < betrag) {	// das Geld reicht nicht aus
			betrag = this.geld;		// das vorhandene Geld wird eingesetzt
		}
		// 
		if (betrag < 100) {			// unter 100 macht es keinen Sinn. 
			return; 
		}	
		float kurs = aktie.getTageskurs(this.heute).getKurs();
		float stueckzahl = betrag / kurs; 
		Order.orderAusfuehren(Order.KAUF, aktie.name, stueckzahl, this);
		
	}
	/**
	 * kauft mit Disposition 
	 * @param betrag
	 * @param wertpapier
	 */
	protected void kaufe (float betrag, String wertpapier) {
		Aktie aktie = Aktien.getInstance().getAktie(wertpapier);
		this.kaufe(betrag, aktie);
	}

	/**
	 * am Ende einer Simulation wird der Gesamtbestand verkauft, damit alle Trades geschlossen werden. 
	 */
	protected void verkaufeGesamtbestand () {
		for (Wertpapierbestand wertpapier : this.wertpapierbestand.values()) {
			this.verkaufeWertpapier(wertpapier.wertpapier);
		}
	}
	
	/**
	 * Verkauft Gesamtbestand des vorhandenen Wertpapiers
	 */
	protected void verkaufeWertpapier (String wertpapier) {
		// ermittelt Bestand des Wertpapiers
		float wertpapierbestand = this.getWertpapierStueckzahl(wertpapier);
		Order.orderAusfuehren(Order.VERKAUF, wertpapier, wertpapierbestand, this);
	}
	/**
	 * Beim Verkauf wird gepr�ft, ob gen�gend Wertpapiere vorhanden sind. 
	 * Wenn nicht, wird die Order angepasst auf die vorhandenen St�cke. 
	 * Wenn ein kleiner Restbestand verbleibt, wird der Betrag entsprechend erh�ht
	 * @param datum
	 * @param betrag
	 * @param aktie
	 */
	protected void verkaufe (float betrag, Aktie aktie) {
		if (aktie == null) log.error("Inputvariable Kursreihe ist null");
		if (betrag == 0) log.error("Inputvariable betrag ist 0");
		
		float stueckzahl = 0;
		// Ausf�hrungskurs festlegen
		float kurs = aktie.getTageskurs(this.heute).close;
		// aktuellen Bestand ermitteln 
		float wertpapierbestand = this.getWertpapierStueckzahl(aktie.name);
		if (wertpapierbestand <= 0) log.error("Verkauf ohne Bestand");
		else {	// es ist etwas vorhanden
			// wenn der Bestand kleiner ist als der Verkaufswunsch oder geringer Restbestand 
			if ((wertpapierbestand * kurs - SCHWELLE_VERKAUF_RESTBESTAND) < betrag) {
				// alle vorhandenen Aktien verkaufen
				stueckzahl = wertpapierbestand;
			}
			else {
				stueckzahl = betrag / kurs;
			}
			Order.orderAusfuehren(Order.VERKAUF, aktie.name, stueckzahl, this);
		}
	}
	
	protected void verkaufe (float betrag, String wertpapier) {
		Aktie aktie = Aktien.getInstance().getAktie(wertpapier);
		this.verkaufe(betrag, aktie);
	}
	
	/**
	 * geht durch den aktuellen Wertpapierbestand und bewertet mit aktuellen Kursen
	 * @return
	 */
	public float bewerteDepotAktuell () {
		float result = 0;
		if (this.wertpapierbestand.keySet().size() > 0) {
			for (Wertpapierbestand wertpapierbestand : this.wertpapierbestand.values()) {
				float kurs = Aktien.getInstance().getAktie(wertpapierbestand.wertpapier).getTageskurs(this.heute).getKurs();
				result += kurs * wertpapierbestand.bestand;
			}
		}
		result += this.geld;
		return result; 
	}
	
	/**
	 * F�hrt eine Strategiebewertung durch
	 * Ergebnis ist eine Instanz der Strategiebewertung 
	 */
	public void bewerteStrategie() {
		this.strategieBewertung = Strategiebewertung.bewerteStrategie(this);
	}
	
	/**
	 * liefert die aktuelle St�ckzahl im Depotbestand, oder 0
	 * @param name
	 * @return Anzahl Wertpapiere
	 */
	protected float getWertpapierStueckzahl (String name) {
		float result = 0;
		if (this.wertpapierbestand.containsKey(name)) {
			result = this.wertpapierbestand.get(name).bestand;
		}
		return result; 
	}
	
	/**
	 * eine ausgef�hrte Order wird im Orderbuch des Depot eingetragen. 
	 * wird von der Order selbst vorgenommen
	 * Dabei werden die Trades aktualisiert
	 * @param order
	 * @return
	 */
	boolean orderEintragen (Order order) {
		if (order == null) log.error("Inputvariable Order ist null");
		// die Order in die Order-Liste aufnehmen
		this.orders.add(order);
		// das Wertpapier in den Bestand einliefern oder ausliefern 
		this.wertpapiereEinAusliefern(order);
		// die Order einem Trade zuordnen
		addOrderToTrade (order);
		
		return true;
	}
	
	/**
	 * k�mmert sich um die Ein- und Auslierung der Wertpapier im aktuellen Wertpapierbestand
	 * Falls alle Wertpapiere ausgeliefert wurden, wird der Bestand gel�scht 
	 * @param order
	 */
	private void wertpapiereEinAusliefern (Order order) {
		if (order == null) log.error("Inputvariable Order ist null");
		float result = 0;
		if (! this.wertpapierbestand.containsKey(order.wertpapier)) {	// wenn es das Wertpapier noch nicht gibt
			// neues Wertpapier im Bestand anlegen 
			this.wertpapierbestand.put(order.wertpapier, new Wertpapierbestand(order.wertpapier));
		}
		if (order.kaufVerkauf == Order.KAUF) {
		// Wertpapier dem Bestand hinzuf�gen 
			result = this.wertpapierbestand.get(order.wertpapier).liefereWertpapierEin(order.stueckzahl, order.kurs);
		}
		else {
			result = this.wertpapierbestand.get(order.wertpapier).EntnehmeWertpapier(order.stueckzahl, order.kurs);
		}
		// wenn die St�ckzahl 0 ist, wird der Bestand gel�scht 
		if (result > -0.01 && result < 0.01 ) {
			this.wertpapierbestand.remove(order.wertpapier);
		}
	}
	
	
	/**
	 * eine neue Order wird den Trades hinzugef�gt. 
	 * Jede Order geh�rt zu einem Trade. 
	 * @param order
	 */
	private void addOrderToTrade (Order order) {
		if (order == null) log.error("Order ist null"); 
		byte status; 
		// pr�ft, ob es einen aktuellen Trade mit diesem Wertpapier gibt
		if (this.aktuelleTrades.containsKey(order.wertpapier)) {
			// f�gt die Order dem laufenden Trade hinzu, egal ob Kauf oder Verkauf
			status = this.aktuelleTrades.get(order.wertpapier).addOrder(order);
			if (status == Trade.STATUS_GESCHLOSSEN) {  // der Trade ist geschlossen worden 
				// der Trade wird aus der Liste entfernt. 
				this.aktuelleTrades.remove(order.wertpapier);
			}
		}
		else {	// es gibt keinen aktuellen Trade f�r diese Order
			if (order.kaufVerkauf == Order.VERKAUF) log.error("Verkauf in's Leere");
			// einen neuen Trade anlegen 
			Trade trade = new Trade (); 
			// die Order hinzuf�gen
			status = trade.addOrder(order);
			// den neuen Trade einf�gen 
			this.aktuelleTrades.put(order.wertpapier, trade);
			this.trades.add(trade);
		}
		
	}
	
	/**
	 * schreibt alle Order des Depot als CSV 
	 */
	public void writeOrders () {
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
			// Writer schlie�en
			fileWriter.close();
			log.info("Datei geschrieben: " + file.getAbsolutePath() );
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeOrders (FileWriter writer) {
		try {
			writer.write("Depot ; Wertpapier ; KV ; Datum ; St�cke ; Kurs ; Abrechnungsbetrag ; Depotst�cke ; Investiert ;  Durchschnittskurs ; Marktwert ; G/V ; Geldbestand");
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
