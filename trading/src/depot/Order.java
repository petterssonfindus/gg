package depot;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktien;
import kurs.Aktie;
import util.Util;
/**
 * repräsentiert einen Wertpapierauftrag mit allen Ausführungsinformationen
 * zusätzlich wird auf Gesamt-Depotbestand aggregiert zum aktuellen Zeitpunkt 
 * @author oskar
 *
 */
public class Order {
	private static final Logger log = LogManager.getLogger(Order.class);

	protected Depot depot;			// die Order weiss, zu welchem Depot sie gehÃ¶rt
	protected String wertpapier; 	// gleiche Bezeichnung wie die Kursreihe
	protected float stueckzahl; 	// Anzahl Stücke 
	protected byte kaufVerkauf; 	// 1 = Kauf, 2 = Verkauf
	protected float kurs; 			// der Kurse, zu dem ausgeführt wurde
	protected float abrechnungsbetrag; 	// der Abrechnungsbetrag
	protected GregorianCalendar datum;	// der Zeitpunkt der Ausführung

	protected float depotStueckzahl;// Anzahl dieser Wertpapiere 
	protected float depotGeld; 		// der Geldbestand im Depot zum Zeitpunkt der Order
	protected float durchschnittskurs; // gewichteter Mittelkurs aller Order dieses Wertpapiers 
	protected float investiertesKapital; // Saldo Käufe - Verkäufe in diesem Wertpapier 

	public static final byte KAUF = 1;
	public static final byte VERKAUF = 2;
	
	/**
	 * keine Gelddisposition
	 * schreibt Orderbuch
	 * berechnet den Kurs mit dem Schlusskurs des selben Tages 
	 * aktualisiert Depotbestand
	 * @return die fertige Order
	 */
	public static Order orderAusfuehren (byte kaufVerkauf, GregorianCalendar datum, String wertpapier, 
			float stueckzahl, Depot depot) {
		if (datum == null) log.error("Inputvariable Datum ist null");
		if (wertpapier == null) log.error("Inputvariable Wertpapier ist null");
		if (depot == null) log.error("Inputvariable Depot ist null");
		if (stueckzahl == 0) log.error("Inputvariable Stückzahl ist 0");
		// neue Order erzeugen
		Order order = new Order();
		// Referenz auf das zugehörige Depot setzen
		order.depot = depot; 
		// zugehörige Kursreihe ermitteln 
		Aktie kursreihe = Aktien.getInstance().getAktie(wertpapier);

		order.datum = datum;
		order.kaufVerkauf = kaufVerkauf;
		order.stueckzahl = stueckzahl;
		order.wertpapier = wertpapier;
		// den Ausführungskurs ermitteln
		order.kurs = kursreihe.getTageskurs(datum).getKurs();
		// den Abrechnungsbetrag ermitteln
		order.abrechnungsbetrag = stueckzahl * order.kurs;
		// *** alle Infos über den Depotbestand in der Order anpassen ***
		// bisherige Stückzahl ermitteln, wenn es die erste Order ist, dann kommt null  
		Order letzteOrder = depot.getLetzteOrder(wertpapier);
		float depotStueckzahl = 0; 
		float investiertesKapital = 0;
		// wenn es eine vorherige Order gibt, lese die Daten aus 
		if (letzteOrder != null) {
			depotStueckzahl = letzteOrder.depotStueckzahl;
			investiertesKapital = letzteOrder.investiertesKapital;
		}
		// Geldbetrag in die Order schreiben 
		order.depotGeld = depot.geld;
		// Depot-Daten in die Order schreiben 
		if (kaufVerkauf == Order.KAUF) {
			// die Depotbestandsdaten in der Order anpassen 
			order.depotStueckzahl = depotStueckzahl + order.stueckzahl;
			// der Geldbestand im Depot reduziert sich 
			depot.geld -= order.abrechnungsbetrag;
			// investiertes Kapital in diesem Wertpapier erhöht sich  
			order.investiertesKapital = investiertesKapital + order.abrechnungsbetrag;
		}
		else {		// ein Verkauf
			// die Depotbestandsdaten in der Order anpassen 
			order.depotStueckzahl = depotStueckzahl - order.stueckzahl;
			// der Geldbestand im Depot erhöht sich
			depot.geld += order.abrechnungsbetrag;
			// investiertes Kapital in diesem Wertpapier reduziert sich 
			order.investiertesKapital = investiertesKapital - order.abrechnungsbetrag;
		}
		// Durchschnittskurs berechnen: investiertes Kapital / Stückzahl im Depot
		order.durchschnittskurs = order.investiertesKapital / order.depotStueckzahl;
		
		depot.orderEintragen(order);
		return order;
	}
	
	public String toString () {
		String datum = Util.formatDate(this.datum);
		return this.depot.name + Util.separator + 
				this.wertpapier + Util.separator + 
				this.kaufVerkauf + Util.separator + 
				datum + Util.separator + 
				Util.toString(this.stueckzahl) + Util.separator + 
				Util.toString(this.kurs) + Util.separator + 
				Util.toString(this.abrechnungsbetrag) + Util.separator + 
				Util.toString(this.depotStueckzahl) + Util.separator + 
				Util.toString(this.abrechnungsbetrag) + Util.separator + 
				Util.toString(this.depotGeld);
	}
	
}
