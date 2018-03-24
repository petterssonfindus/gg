package depot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Util;

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
	// wie lange wird ein Trend ausgenutzt
	private int trenddauerpositiv;
	private int trenddauernegativ; 
	// wie lange laufen positive Trends im Verhältnis zu negativen 
	private float quotedauer;
	// wieviele Trades wurden positiv / negativ beendet
	private int anzahlPositiv = 0;
	private int anzahlNegativ = 0;
	// wieviel Gewinn/Verlust machen die Positiven / negativen Trades 
	private float gewinnPositiv = 0;
	private float gewinnNegativ = 0;
	private float gewinnSaldo = 0;
	// Gewinn/Verlust im Durchschnitt
	private float gewinnPositivD = 0;
	private float gewinnNegativD = 0;
	
	/**
	 * Instantiierung nur über statische Methode "bewerteStrategie"
	 */
	private Strategiebewertung () {
		
	}
	
	/**
	 * führt die Bewertung durch anhand der Trades und gibt das Ergebnis als Instanz zurück 
	 * @param depot
	 * @return
	 */
	static Strategiebewertung bewerteStrategie(Depot depot) {
		Strategiebewertung sb = new Strategiebewertung(); 

		for (Trade trade : depot.trades) {
			// Aggregate summieren
			if (trade.erfolgreich) { 
				// die Anzahl 
				sb.anzahlPositiv++; 
				// der Erfolg
				sb.gewinnPositiv += trade.erfolg;
				// die Dauer
				sb.trenddauerpositiv += trade.dauer;
			}
			else {
				sb.anzahlNegativ++;
				sb.gewinnNegativ += trade.erfolg;
				sb.trenddauernegativ += trade.dauer;
			}
		}
		sb.gewinnSaldo = Util.rundeBetrag(sb.gewinnPositiv + sb.gewinnNegativ);
		// wenn es positive Trades gegeben hat, werden Durchschnitswerte berechnet
		if (sb.anzahlPositiv > 0) {
			sb.gewinnPositivD = Util.rundeBetrag((float) sb.gewinnPositiv / sb.anzahlPositiv);
			sb.trenddauerpositiv = (int) sb.trenddauerpositiv / sb.anzahlPositiv;
			sb.performancePositiv = Util.rundeBetrag((float) sb.gewinnPositivD / sb.trenddauerpositiv);
		}
		// wenn es negative Trades gegeben hat, werden Durchschnitswerte berechnet
		if (sb.anzahlNegativ > 0) {
			sb.trefferquote = Util.rundeBetrag((float) sb.anzahlPositiv / sb.anzahlNegativ);
			sb.gewinnNegativD = Util.rundeBetrag((float) sb.gewinnNegativ / sb.anzahlNegativ);
			sb.trenddauernegativ = (int) sb.trenddauernegativ / sb.anzahlNegativ;
			// durchschnittliche Haltedauer ermitteln
			if (sb.trenddauernegativ > 0) {
				sb.quotedauer = Util.rundeBetrag((float)sb.trenddauerpositiv / sb.trenddauernegativ);
				sb.performanceNegativ = Util.rundeBetrag((float) sb.gewinnNegativD / sb.trenddauernegativ);
			}
		}
		
		return sb; 
	}
	
	public String toString () {
		return "Anzahl: " + this.anzahlPositiv + Util.separator + 
				this.anzahlNegativ + Util.separator + 
				this.trefferquote + Util.separator + 
				this.trenddauerpositiv + Util.separator + 
				this.trenddauernegativ + Util.separator + 
				this.quotedauer + Util.separator + 
				this.gewinnPositivD + Util.separator + 
				this.gewinnNegativD + Util.separator + 
				this.gewinnSaldo;
	}

}
