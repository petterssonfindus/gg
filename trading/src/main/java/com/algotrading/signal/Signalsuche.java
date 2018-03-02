package signal;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktie;
import kurs.Kurs;

/**
 * identifiziert Kauf/Verkaufssignale in einer Kursreihe
 * und schreibt sie in die Kursreihe
 * @author oskar
 *
 */
public class Signalsuche {

	static final Logger log = LogManager.getLogger(Signalsuche.class);
	
	private static ArrayList<SignalAlgorythmus> signalAlgorithmen = new ArrayList<SignalAlgorythmus>();

	/**
	 * steuert die Berechnung aller Signale auf Basis einer Zeitreihe
	 * Ruft für jeden Kurs die vorhandene Signal-Suche
	 * 
	 * #TODO das könnte auch für einen vorgegebenen Zeitabschnitt erfolgen 
	 * Die Signalsuche könnte auch separat/einzeln beauftragt werden. 
	 * @param aktie
	 */
	public static void rechneSignale (Aktie aktie) {
		
		// die Implementierungen der Signal-Algorithmen einhängen 
		signalAlgorithmen.add(new GDDurchbruch());
		signalAlgorithmen.add(new SteigendeBergeFallendeTaeler());
				
		for (SignalAlgorythmus algo : signalAlgorithmen) {
			
			for (Kurs tageskurs : aktie.getBoersenkurse()) {
				
				algo.ermittleSignal(tageskurs, aktie);
	
			}
		}
	}

}
