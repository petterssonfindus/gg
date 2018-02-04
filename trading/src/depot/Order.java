package depot;

import java.util.GregorianCalendar;

import kurs.Kursreihe;
import util.Util;
import signal.Signal;
/**
 * repräsentiert einen Wertpapierauftrag mit allen Ausführungsinformationen
 * zusätzlich wird auf Gesamt-Depotbestand aggregiert.  
 * @author oskar
 *
 */
public class Order {
	
	protected Depot depot;			// die Order weiss, zu welchem Depot sie gehört
	protected  String wertpapier; 	// gleiche Bezeichnung wie die Kursreihe
	protected float stueckzahl; 	// Anzahl Stücke 
	protected byte kaufVerkauf; 	// 1 = Kauf, 2 = Verkauf
	protected float kurs; 			// der Kurse, zu dem ausgeführt wurde
	protected float betrag; 		// der Abrechnungsbetrag
	protected GregorianCalendar datum;	// der Zeitpunkt der Ausführung

	protected float depotStueckzahl;// Anzahl dieser Wertpapiere 
	protected float depotGeld; 		// der Geldbestand im Depot
	protected float wertpapierWert;	// der Gesamtwert dieses Wertpapiers 
	
	/**
	 * keine Gelddisposition
	 * schreibt Orderbuch
	 * berechnet den Kurs mit dem Schlusskurs des selben Tages 
	 * aktualisiert Depotbestand
	 * @param datum
	 * @param wertpapier
	 * @param stueckzahl
	 * @return
	 */
	public static Order orderAusfuehren (byte kaufVerkauf, GregorianCalendar datum, String wertpapier, 
			float stueckzahl, Depot depot, Kursreihe kursreihe) {
		// neue Order erzeugen
		Order order = new Order();
		// ermittelt die Kursreihe
		order.depot = depot; 

		//		Kursreihe kursreihe = Kursreihe.getKursreihe("appl");
		order.datum = datum;
		order.kaufVerkauf = kaufVerkauf;
		order.stueckzahl = stueckzahl;
		// ermittelt den Kurs
		order.kurs = kursreihe.getTageskurs(datum).getKurs();
		// ermittelt den Ausführungsbetrag 
		order.betrag = stueckzahl * order.kurs;
		// den Depotbestand in der Order anpassen 
		// bisherige Stückzahl ermitteln 
		float stueckeBisher = depot.getWertpapierStueckzahl(wertpapier);
		
		if (kaufVerkauf == Signal.KAUF) {
			// die Depotbestandsdaten in der Order anpassen 
			order.depotStueckzahl = stueckeBisher + order.stueckzahl;
			// den Geldbestand im Depot anpassen
			order.depotGeld -= order.betrag;
		}
		else {		// ein Verkauf
			// die Depotbestandsdaten in der Order anpassen 
			order.depotStueckzahl = stueckeBisher - order.stueckzahl;
			// den Geldbestand im Depot anpassen
			order.depotGeld += order.betrag;
		}
		// aktuelle Stücke im Depotbestand bewerten 
		order.wertpapierWert = order.depotStueckzahl * order.kurs;
		
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
				Util.toString(this.betrag) + Util.separator + 
				Util.toString(this.depotStueckzahl) + Util.separator + 
				Util.toString(this.wertpapierWert) + Util.separator + 
				Util.toString(this.depotGeld);
	}
	
}
