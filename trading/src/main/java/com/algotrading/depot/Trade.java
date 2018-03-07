package depot;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Util;

	/**
	 * ein Trade besteht aus einem oder mehreren Käufen und Verkäufen 
	 * hat einen Beginn und ein Ende
	 * am Ende ist nichts mehr übrig. 
	 * @author oskar
	 *
	 */
public class Trade {
	private static final Logger log = LogManager.getLogger(Trade.class);
	static final byte STATUS_EROEFFNET = 1; 
	static final byte STATUS_LAEUFT = 2; 
	static final byte STATUS_GESCHLOSSEN = 3; 
	String wertpapier; 
	ArrayList<Order> orders = new ArrayList<Order>();
	GregorianCalendar beginn;
	GregorianCalendar ende;
	int dauer = 0;
	// der jeweilige Bestand an Wertpapieren - am Anfang und Ende = 0
	float bestand = 0; 
	float investiertesKapital = 0;
	// Gewinn/Verlust in Euro 
	float erfolg = 0;
	boolean erfolgreich = false; 
	byte status = Trade.STATUS_EROEFFNET; 

	/**
	 * der Trade darf nicht geschlossen sein
	 * der Bestand darf nicht negativ werden 
	 * Status wird angepasst 
	 * @param order
	 */
	byte addOrder (Order order) {
		if (order == null) log.error("Inputvariable Order ist null");
		if (this.status == Trade.STATUS_LAEUFT && order.wertpapier != this.wertpapier) log.error("Inputvariable Order abweichendes Wertpapier: " + order.wertpapier);
		if (this.status == Trade.STATUS_GESCHLOSSEN) log.error("Trade ist geschlossen");
		// die erste Order legt das Wertpapier fest
		if (this.status == Trade.STATUS_EROEFFNET) {
			this.wertpapier = order.wertpapier;
		}
		// Bestand wird angepasst
		if (order.kaufVerkauf == Order.KAUF) {
			this.bestand += order.stueckzahl;
			this.investiertesKapital += order.abrechnungsbetrag;
		}			
		else {
			bestand -= order.stueckzahl;
		}
		this.orders.add(order);
		// den Erfolg fortschreiben - Am Ende ist es der Gesamterfolg
		if (order.kaufVerkauf == Order.KAUF) this.erfolg -= order.abrechnungsbetrag;
		else this.erfolg += order.abrechnungsbetrag;
		// Status wird angepasst
		this.status = getStatus();
		// die letzte Order hat den Trade geschlossen 
		if (this.status == Trade.STATUS_GESCHLOSSEN) {
			// der Trade beginnt am Datum der 1. Order
			this.beginn = this.orders.get(0).datum;
			// der Trade endet am Datum der letzten Order
			this.ende = this.orders.get(this.orders.size() - 1).datum;
			// die Dauer in Tagen 
			this.dauer = Util.anzahlTage(beginn, ende);
			this.erfolgreich = this.erfolg > 0;
			log.info("Trade abgeschlossen mit: " + this.erfolg);
		}
		return this.status;
	}
	/**
	 * prüft, ob der Trade im aktuellen Zustand offen oder geschlossen ist
	 * @return eröfnet, läuft, geschlossen
	 */
	public byte getStatus() {
		byte result; 
		if (orders.size() == 0) result = Trade.STATUS_EROEFFNET;
		else {	// mehrere Orders sind vorhanden 
			if (this.bestand < -0.001) log.error("Trade ist negativ Wertpapier :" + this.wertpapier);
			if (this.bestand > 0.001) {
				result = Trade.STATUS_LAEUFT;
			}
			else {
				result = Trade.STATUS_GESCHLOSSEN;  // Bestand ist 0
			}
		}
		return result; 
	}
	
	public String toString() {
		String result = this.wertpapier + Util.separator + 
			Util.formatDate(beginn) + Util.separator + 
			Util.formatDate(ende) + Util.separator + 
			Integer.toString(dauer) + Util.separator + 
			Util.toString(investiertesKapital) + Util.separator +
			Util.toString(erfolg);
		return result; 
	}
}
