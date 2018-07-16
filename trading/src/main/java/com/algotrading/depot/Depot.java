package depot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aktie.Aktie;
import aktie.Aktien;
import aktie.Kurs;
import indikator.IndikatorBeschreibung;
import signal.Signal;
import util.Util;

/**
 * Simuliert ein Wertpapierdepot in einem Zeitraum 
 * Verwaltet Wertpapierbestände und die Liste der Orders und Trades. 
 * Überwacht die Ausführung von limitierten Orders. 
 * Errechnet seinen täglichen Depotwert. 
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
	// Liste aller Trades, die im Depot erzeugt wurden 
	ArrayList<Trade> trades = new ArrayList<Trade>();
	// Liste der Trades im Zustand "läuft" 
	ConcurrentHashMap<String, Trade> aktuelleTrades = new ConcurrentHashMap<String, Trade>();
	// eine Zeitreihe mit Tagesend-Bewertungen 
	Aktie depotwert; 
	// aktuelle Liste aller vorhandenen Wertpapiere
	ConcurrentHashMap<String, Wertpapierbestand> wertpapierbestand = new ConcurrentHashMap<String, Wertpapierbestand>();
	// das Anlage-Universum. Nehmen teil an der Simulation 
	ArrayList<Aktie> aktien; 
	// das Depot kennt seine Strategien
	SignalStrategie signalStrategie; 
	TagesStrategie tagesStrategie; 
	// Bewertung aller Trades des Depot über eine Laufzeit
	Strategiebewertung strategieBewertung; 
	// verbleibt bim Verkauf ein Restbestand, wir dieser mit verkauft
	public static float SCHWELLE_VERKAUF_RESTBESTAND = 100; 
	private static int signalzaehler = 0;
	private static int stoplossZaehler = 0; 
	private static FileWriter fileWriterHandelstag; 
	
	public Depot (String name, float geld) {
		this.name = name; 
		this.geld = geld;
		this.anfangsbestand = geld; 
		log.debug("neues Depot angelegt: " + name + " - " + geld);
	}

	/**
	 * Durchläuft alle Tage, holt sich alle Signale und handelt
	 * Bewertet die Strategie am Ende 
	 * @param aktie vorbereitet incl. Indikatoren 
	 */
	public void simuliereDepot (SignalStrategie signalStratgie, TagesStrategie tagesStrategie,
			ArrayList<Aktie> aktien, GregorianCalendar beginn, GregorianCalendar ende, boolean writeHandelstag) {
		if (signalStratgie == null) log.error("Inputparameter Kaufstrategie = null");
		if (beginn == null) log.error("Inputparameter Beginn = null");
		if (ende == null) log.error("Inputparameter Ende = null");
		if (aktien == null || aktien.size() == 0) log.error("Inputparameter Aktien = null");
		if ( ! ende.after(beginn)) log.error("Inputparameter Ende liegt vor Beginn");
		this.beginn = beginn;
		this.tagesStrategie = tagesStrategie;
		this.signalStrategie = signalStratgie; 
		// eine Aktie mit Zeitreihe wird angelegt, um die Depotwerte zu speichern 
		this.depotwert = new Aktie(this.name, "Depot " + this.name, Aktien.INDEXDAX,Aktien.BOERSEDEPOT);
		// die Berechnungen an den Aktien werden durchgeführt
		for (Aktie aktie : aktien ) {
			aktie.rechneSignale();
		}
		log.info("starte Simulation von bis: " + Util.formatDate(beginn) + " - " + Util.formatDate(ende));
		this.heute = beginn; 
		int tagZaehler = 0;
		// stellt für alle Aktien den Kurs zum Beginn ein 
		for (Aktie aktie : aktien) {
			aktie.setStartkurs(heute);
		}
		while (Util.istInZeitraum(this.heute, beginn, ende)) {
			tagZaehler ++;
			if (tagZaehler > 10) {  // die ersten Tage werden ignoriert
				Kurs kurs = simuliereHandelstag(signalStratgie, tagesStrategie);
				this.depotwert.addKurs(kurs);
				if (writeHandelstag) writeHandelstag(kurs);
			}
			this.nextDay();	// dabei wird this.heute weiter gestellt und die Aktienkurse weiter geschaltet
		}
		// am letzten Tag wird alles verkauft und damit der letzte Trade geschlossen 
		if (this.heute.after(ende) && this.wertpapierbestand.keySet().size() > 0) {
			verkaufeGesamtbestand();
		}
		log.debug("In Depotsimulation wurden Orders aus Signalen erzeugt: " + signalzaehler);
		log.debug("In Depotsimulation wurden Orders aus SL erzeugt: " + stoplossZaehler);
		log.debug("In Depotsimulation wurden Orders erzeugt: " + orders.size());
		log.debug("In Depotsimulation wurden Trades erzeugt: " + trades.size());
		this.bewerteStrategie();
		// Aufräumarbeiten: Signale werden gelöscht 
		if (writeHandelstag) this.closeHandelstagFile();
		for (Aktie aktie : aktien ) {
			aktie.clearSignale();
		}
	}
	
	/**
	 * Simuliert einen Handelstag mit Kauf / Verkaufsentscheidungen für alle Aktien 
	 * und einer Depotbewertung 
	 * @param kaufstrategie
	 * @param slStrategie
	 * @param kurs
	 * @return der Tagesendwert des Depot 
	 */
	private Kurs simuliereHandelstag(SignalStrategie kaufstrategie, TagesStrategie tagesStrategie) {
		Order order; 
		// der Kurs für den Depotwert
		Kurs kursDepot = new Kurs();
		kursDepot.datum = this.heute;
		int anzahlOrder = 0;
		
		// Signale von allen Aktien heute werden eingesammelt
		ArrayList<Signal> signale = this.getSignale();
		if (signale != null && signale.size() > 0) {
			// jedes Signal wird weiter geleitet
			for (Signal signal : signale) {
				// die Kaufstrategie bekommt das Signal 
				order = kaufstrategie.entscheideSignal(signal, this);
				if (order != null) {
					signalzaehler ++; 
					anzahlOrder ++;
				}
			}
		}
		// Stop-Loss wird überwacht, falls eines vorhanden ist
		if (tagesStrategie != null) {
			order = tagesStrategie.entscheideTaeglich(this);
			// wenn eine Order entstanden ist 
			if (order != null) {
				stoplossZaehler ++;
				anzahlOrder ++; 
			}
		}
		kursDepot.close = this.bewerteDepotAktuell();
		log.debug("Handelstag: " + Util.formatDate(this.heute) + " Signale: " + signale.size() + " Order: " + anzahlOrder + 
				" Wert: " + kursDepot.close);
		return kursDepot; 
	}
	/**
	 * Holt an einem neuen Handelstag die Kurse der Aktien 
	 * Setzt das aktuelle Datum 
	 * #TODO Prüfen, ob das Datum übereinstimmt, Fehlerbehandlung
	 * @return
	 */
	private ArrayList<Kurs> nextDay () {
		ArrayList<Kurs> kurse = new ArrayList<Kurs>();
		for (Aktie aktie : this.aktien) {
			kurse.add(aktie.nextKurs());
		}
		this.heute = kurse.get(0).datum;
		log.debug("nextDay: " + Util.formatDate(this.heute));
		return kurse; 
	}
	
	private ArrayList<Signal> getSignale () {
		ArrayList<Signal> signale = new ArrayList<Signal>();
		
		for (Aktie aktie : this.aktien) {
			// holt sich den aktuellen Kurs der Aktie
			Kurs kurs = aktie.getAktuellerKurs();
			signale.addAll(kurs.getSignale());
		}
		return signale; 
	}
	
	/**
	 * kauft mit Disposition 
	 * @param aktie
	 * @param betrag
	 */
	protected Order kaufe (float betrag, Aktie aktie) {
		if (aktie == null) log.error("Inputvariable Aktie ist null");
		Order result = null; 
		// Maximum bestehendes Geld
		if (this.geld < betrag) {	// das Geld reicht nicht aus
			betrag = this.geld;		// das vorhandene Geld wird eingesetzt
		}
		// 
		if (betrag > 100) {			// unter 100 macht es keinen Sinn. 
			float kurs = aktie.getAktuellerKurs().getKurs();
			float stueckzahl = betrag / kurs; 
			result = Order.orderAusfuehren(Order.KAUF, aktie.name, stueckzahl, this);
		}
		return result; 
	}
	/**
	 * kauft mit Disposition 
	 * Wenn kein Geld vorhanden, wird nichts gekauft
	 * @param betrag
	 * @param wertpapier
	 * @return die Kauf-Order falls gekauft wurde - ansonsten null 
	 */
	protected Order kaufe (float betrag, String wertpapier) {
		Aktie aktie = Aktien.getInstance().getAktie(wertpapier);
		return this.kaufe(betrag, aktie);
	}

	/**
	 * am Ende einer Simulation wird der Gesamtbestand verkauft, damit alle Trades geschlossen werden. 
	 */
	protected Order verkaufeGesamtbestand () {
		Order order = null; 
		for (Wertpapierbestand wertpapier : this.wertpapierbestand.values()) {
			order = this.verkaufe(wertpapier.getAktie());
		}
		return order;
	}
	
	/**
	 * Verkauft Gesamtbestand des vorhandenen Wertpapiers
	 * Wenn kein Bestand vorhanden, dann null
	 * @return Die ausgeführte Verkaufs-Order, oder null, wenn kein Bestand vorhanden
	 */
	protected Order verkaufe (Aktie aktie) {
		Order order = null; 
		// ermittelt Bestand des Wertpapiers
		Wertpapierbestand wertpapierbestand = this.getWertpapierBestand(aktie.name);
		if (wertpapierbestand != null) {
			float bestand = wertpapierbestand.bestand;
			order = Order.orderAusfuehren(Order.VERKAUF, aktie.name, bestand, this);
		}
		return order; 
	}

	protected Order verkaufe (String wertpapier) {
		Aktie aktie = Aktien.getInstance().getAktie(wertpapier);
		return verkaufe (aktie);
	}
	/**
	 * Beim Verkauf wird geprüft, ob genügend Wertpapiere vorhanden sind. 
	 * Wenn nicht, wird die Order angepasst auf die vorhandenen Stücke. 
	 * Wenn ein kleiner Restbestand verbleibt, wird der Betrag entsprechend erhöht
	 * @param datum
	 * @param betrag
	 * @param aktie
	 */
	protected Order verkaufe (float betrag, Aktie aktie) {
		if (aktie == null) log.error("Inputvariable Kursreihe ist null");
		if (betrag == 0) log.error("Inputvariable betrag ist 0");
		Order order = null; 
		
		float stueckzahl = 0;
		// Ausführungskurs festlegen
		float kurs = aktie.getAktuellerKurs().close;
		// aktuellen Bestand ermitteln 
		float wertpapierbestand = this.getWertpapierBestand(aktie.name).bestand;
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
			order = Order.orderAusfuehren(Order.VERKAUF, aktie.name, stueckzahl, this);
		}
		return order; 
	}
	
	/**
	 * geht durch den aktuellen Wertpapierbestand und bewertet mit aktuellen Kursen
	 * @return
	 */
	public float bewerteDepotAktuell () {
		float result = 0;
		if (this.wertpapierbestand.keySet().size() > 0) {
			for (Wertpapierbestand wertpapierbestand : this.wertpapierbestand.values()) {
				float kurs = Aktien.getInstance().getAktie(wertpapierbestand.wertpapier).getAktuellerKurs().getKurs();
				result += kurs * wertpapierbestand.bestand;
			}
		}
		result += this.geld;
		return result; 
	}
	
	/**
	 * Führt eine Strategiebewertung durch
	 * Ergebnis ist eine Instanz der Strategiebewertung 
	 */
	public void bewerteStrategie() {
		this.strategieBewertung = Strategiebewertung.bewerteStrategie(this);
	}
	
	/**
	 * liefert die aktuelle Stückzahl im Depotbestand, oder 0
	 * @param name
	 * @return Anzahl Wertpapiere, oder null wenn nicht vorhanden
	 */
	protected Wertpapierbestand getWertpapierBestand (String name) {
		Wertpapierbestand result = null;
		if (this.wertpapierbestand.containsKey(name)) {
			result = this.wertpapierbestand.get(name);
		}
		return result; 
	}
	
	/**
	 * eine ausgeführte Order wird im Orderbuch des Depot eingetragen. 
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
		log.debug("neue Order eintragen: " + order.toString());
		
		return true;
	}
	
	/**
	 * kümmert sich um die Ein- und Auslierung der Wertpapier im aktuellen Wertpapierbestand
	 * Falls alle Wertpapiere ausgeliefert wurden, wird der Bestand gelöscht 
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
		// Wertpapier dem Bestand hinzufügen 
			result = this.wertpapierbestand.get(order.wertpapier).liefereWertpapierEin(order.stueckzahl, order.kurs);
		}
		else {
			result = this.wertpapierbestand.get(order.wertpapier).EntnehmeWertpapier(order.stueckzahl, order.kurs);
		}
		// wenn die Stückzahl 0 ist, wird der Bestand gelöscht 
		if (result > -0.01 && result < 0.01 ) {
			this.wertpapierbestand.remove(order.wertpapier);
		}
	}
	
	
	/**
	 * eine neue Order wird den Trades hinzugefügt. 
	 * Entweder an einen bestehenden Trade angehängt, oder ein neuer Trade eröffnet 
	 * Jede Order gehört zu einem Trade. 
	 * @param order
	 */
	private void addOrderToTrade (Order order) {
		if (order == null) log.error("Order ist null"); 
		byte status; 
		// wenn es einen aktuellen Trade mit diesem Wertpapier gibt
		if (this.aktuelleTrades.containsKey(order.wertpapier)) {
			// holt sich den Trade 
			Trade trade = this.aktuelleTrades.get(order.wertpapier);
			// fügt die Order dem laufenden Trade hinzu, egal ob Kauf oder Verkauf
			status = trade.addOrder(order);
			log.debug("neue Order an bestehenden Trade: " + this.aktuelleTrades.size() + " - " + trade.toString());
			if (status == Trade.STATUS_GESCHLOSSEN) {  // der Trade ist geschlossen worden 
				// der Trade wird aus der Liste entfernt. 
				this.aktuelleTrades.remove(order.wertpapier);
			}
		}
		else {	// es gibt keinen aktuellen Trade für diese Order
			if (order.kaufVerkauf == Order.VERKAUF) log.error("Verkauf in's Leere");
			// einen neuen Trade anlegen 
			Trade trade = new Trade (); 
			// die Order hinzufügen
			status = trade.addOrder(order);
			// den neuen Trade einfügen 
			this.aktuelleTrades.put(order.wertpapier, trade);
			this.trades.add(trade);
			log.debug("neue Order an neuen Trade: " + trade.toString());
		}
		
	}
	/**
	 * Schreibt an einem einzigen Tag die relevanten Informationen in die Handels-Tag-CSV
	 * Das File wird zu Beginn erzeugt bleibt während der Simutation geöffnet
	 * und wird am Ende der Simuation von der Simulation geschlossen. 
	 * @param depotKurs
	 */
	private void writeHandelstag(Kurs depotKurs) {
		
		if (fileWriterHandelstag == null) {
			fileWriterHandelstag = getHandelstagFile();
			try {
				fileWriterHandelstag.write(toStringHandelstagHeader());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fileWriterHandelstag.write(toStringHandelstag(depotKurs));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * die Header-Zeile mit den Spaltenüberschriften 
	 * @return
	 */
	private String toStringHandelstagHeader () {
		String result = "Datum" + Util.separator + 
				"Depotwert"  + Util.separator;
		for (Aktie aktie : this.aktien) {
			result = result.concat(aktie.name + Util.separator);
			for (IndikatorBeschreibung indikator : aktie.getIndikatorBeschreibungen()) {
				result = result.concat(indikator.toString() + Util.separator);
			}
		}
		result = result.concat("Signal1" + Util.separator + "Signal2" + Util.separator + "Signal3" + 
				Util.getLineSeparator());
		return result; 
	}
	
	/**
	 * am Abend eines Handesltages wird der Ablauf protokolliert 
	 * Datum - Depotwert - Aktienkurs / n*Indikator - 3*Signal
	 * @param depotKurs
	 * @return
	 */
	private String toStringHandelstag(Kurs depotKurs) {
		// zu Beginn das Datum 
		String result = Util.formatDate(this.heute) + Util.separator;
		
//		result = result.concat(this.depotwert.getIndexierterKurs() + Util.separator);
		result = result.concat(depotKurs.getKurs() + Util.separator);
		// der Kurs jeder Aktie
		for (Aktie aktie : this.aktien) {
			Kurs kurs = aktie.getAktuellerKurs();
			// der indexierte Kurs
//			result = result.concat(aktie.getIndexierterKurs() + Util.separator);
			result = result.concat(aktie.getAktuellerKurs().getKurs() + Util.separator);
			// für jede Aktie die Indikatoren
			for (IndikatorBeschreibung indikator : aktie.getIndikatorBeschreibungen()) {
				// die Indikatoren-Wert am Kurs auslesen
				float wert = kurs.getIndikatorWert(indikator);
				result = result + (wert + Util.separator);
			}
		}
		// für jede Aktie die Signale 
		for (Aktie aktie : this.aktien) {
			Kurs kurs = aktie.getAktuellerKurs();
			for (Signal signal : kurs.getSignale()) {
				result = result + (kurs.wertpapier + " _ " + signal.getKaufVerkauf() + " _ " + signal.getTyp() + Util.separator);
			}
		}
		result = result.concat(Util.getLineSeparator());

		return result; 
		
	}
	
	private FileWriter getHandelstagFile () {
		FileWriter fileWriter = null;
		String dateiname = null;
		try {
			dateiname = "Depothandel" + this.name + Long.toString(System.currentTimeMillis());
			File file = new File(dateiname + ".csv");
			boolean createFileResult = file.createNewFile();
			if(!createFileResult) {
				// Die Datei konnte nicht erstellt werden. Evtl. gibt es diese Datei schon?
				log.error("Datei konnte nicht erstellt werden:" + dateiname);
			}
			fileWriter = new FileWriter(file);
		} catch (Exception e) {log.error("File konnte nicht eröffnet werden: " + dateiname); }
		log.info("File erstellt: " + dateiname);
		return fileWriter; 
	}
	
	private void closeHandelstagFile () {
		try {
			fileWriterHandelstag.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			
			// Zeilenumbruch am Ende der Datei ausgeben
			fileWriter.write(Util.getLineSeparator());
			// Writer schließen
			fileWriter.close();
			log.info("Datei geschrieben: " + file.getAbsolutePath() );
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeOrders (FileWriter writer) {
		try {
			writer.write("Depot;Wertpapier;KV;Datum;Stücke;Kurs;Abrechnungsbetrag;Geld;Tradedauer;TradeErfolg");
			writer.write(Util.getLineSeparator());
			for (int i = 0 ; i < this.orders.size(); i++) {
				writer.write(orders.get(i).toString());
				writer.write(Util.getLineSeparator());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
