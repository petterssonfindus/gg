package depot;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bewertet die Qualität einer Strategie
 * @author oskar
 *
 */
public class Strategiebewertung {
	private static final Logger log = LogManager.getLogger(Strategiebewertung.class);

	// die Quote positiv verlaufener Trades im Verhältnis zu negative verlaufener
	private float trefferquote;  
	// wieviel Performance (Erfolg pro Trade) erzielen die positiven Trades
	private float performancePositiv;
	private float performanceNegativ;
	// wie lange wird ein positiver Trend ausgenutzt
	private int trenddauerpositiv;
	// wie schnell wird aus einem negativen Trend ausgestiegen
	private int trenddauernegativ; 
	// wie lange laufen positive Trends im Verhältnis zu negativen 
	private float quotedauer;
	private int anzahlPositiv = 0;
	private int anzahlNegativ = 0;
	private float erfolgPositiv = 0;
	private float erfolgNegativ = 0;
	
	/**
	 * Instantiierung nur über statische Methode "bewerteStrategie"
	 */
	private Strategiebewertung () {
		
	}
	
	/**
	 * führt die Bewertung durch und gibt das Ergebnis als Instanz zurück 
	 * @param depot
	 * @return
	 */
	static Strategiebewertung bewerteStrategie(Depot depot, GregorianCalendar datum) {
		Strategiebewertung result = new Strategiebewertung(); 

		for (Trade trade : depot.trades) {
			System.out.println("Trade: " + trade.toString());
			// Anzahl positiv / negativ ermitteln
			if (trade.erfolgreich) {
				result.anzahlPositiv++; 
				result.erfolgPositiv += trade.erfolg;
				result.trenddauerpositiv += trade.dauer;
			}
			else {
				result.anzahlNegativ++;
				result.erfolgNegativ += trade.erfolg;
				result.trenddauernegativ += trade.dauer;
			}

		}
		result.performancePositiv = result.erfolgPositiv / result.anzahlPositiv;
		result.performanceNegativ = result.erfolgNegativ / result.anzahlNegativ;
		// Trefferquote
		result.trefferquote = result.anzahlPositiv / result.anzahlNegativ;
		// durchschnittliche Haltedauer ermitteln
		result.trenddauerpositiv = (int) result.trenddauerpositiv / result.anzahlPositiv;
		result.trenddauernegativ = (int) result.trenddauernegativ / result.anzahlNegativ;
		// Quotient aus positiver/negativer Haltedauer ermitteln 
		result.quotedauer = result.trenddauerpositiv / result.trenddauernegativ;
		
		return result; 
	}

}
