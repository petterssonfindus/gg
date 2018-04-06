package signal;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aktie.Aktie;

/**
 * identifiziert Kauf/Verkaufssignale in einer Kursreihe
 * und schreibt sie in die Kursreihe
 * Ist Bestandteil einer Aktie 
 * @author oskar
 *
 */
public class Signalsuche {

	static final Logger log = LogManager.getLogger(Signalsuche.class);
	//Zuordnung zwischen Signaltyp und Berechnung-Algorythmus
	private static HashMap<Short, SignalAlgorithmus> signalAlgorithmen = initialisiereAlgorythmen();
	
	private static HashMap<Short, SignalAlgorithmus> initialisiereAlgorythmen () {
		HashMap<Short, SignalAlgorithmus> result = new HashMap<Short, SignalAlgorithmus>();
		// die Implementierungen der Signal-Algorithmen einhängen 
		result.put(Signal.GDDurchbruch, new GDDurchbruch());
		result.put(Signal.GDSchnitt, new GDSchnitt());
		result.put(Signal.Jahrestag, new Jahrestag());
		result.put(Signal.FallenderBerg, new SteigendeBergeFallendeTaeler());
		result.put(Signal.SteigenderBerg, new SteigendeBergeFallendeTaeler());
		result.put(Signal.FallendesTal, new SteigendeBergeFallendeTaeler());
		result.put(Signal.SteigendesTal, new SteigendeBergeFallendeTaeler());
		return result; 
	}
	
	/**
	 * steuert die Berechnung von Signalen für eine Aktie
	 * Die Signalsuche könnte auch separat/einzeln beauftragt werden. 
	 * Die Indikatoren sind bereits berechnet und hängen am Kurs. 
	 * @param aktie
	 */
	public static void rechneSignale (Aktie aktie) {
		ArrayList<SignalBeschreibung> signalbeschreibungen = aktie.signalbeschreibungen;
		SignalAlgorithmus algo; 
		for (SignalBeschreibung signalbeschreibung : signalbeschreibungen) {
			// holt sich den zugehörigen Algoithmus
			algo = signalAlgorithmen.get(signalbeschreibung.signalTyp);
			// startet die Berechnung 
			int anzahl = algo.ermittleSignal(aktie, signalbeschreibung);
			log.debug("Signale berechnet: " + signalbeschreibung.signalTyp + " Aktie: " + aktie.name + " Anzahl: " + anzahl);
		}
		
	}

}
