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
	 * Ruft f�r jeden Kurs die vorhandene Signal-Suche
	 * 
	 * #TODO das k�nnte auch f�r einen vorgegebenen Zeitabschnitt erfolgen 
	 * Die Signalsuche k�nnte auch separat/einzeln beauftragt werden. 
	 * @param aktie
	 */
	public static void rechneSignale (Aktie aktie) {
		
		// die Implementierungen der Signal-Algorithmen einh�ngen 
		signalAlgorithmen.add(new GDDurchbruch());
		signalAlgorithmen.add(new SteigendeBergeFallendeTaeler());
				
		for (SignalAlgorythmus algo : signalAlgorithmen) {
			
			for (Kurs tageskurs : aktie.getBoersenkurse()) {
				
				algo.ermittleSignal(tageskurs, aktie);
	
			}
		}
	}

}
