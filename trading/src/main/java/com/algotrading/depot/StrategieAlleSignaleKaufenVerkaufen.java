package depot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Kurs;
import signal.Signal;
import signal.Signalsuche;

public class StrategieAlleSignaleKaufenVerkaufen implements KaufVerkaufStrategie {
	static final Logger log = LogManager.getLogger(Signalsuche.class);

	/**
	 * Nutzt jedes Kaufsignal zum Kauf und Verkaufsignal zum Verkauf
	 * Aber am gleichen Tag wird nicht gekauft und verkauft 
	 */
	@Override
	public void entscheideSignal(Signal signal, Depot depot) {
		Kurs kurs = signal.getTageskurs();
		String wertpapier = kurs.wertpapier;
		
		if (signal.getKaufVerkauf() == Order.KAUF) {
			depot.kaufe(depot.anfangsbestand/3, wertpapier);
		}
		if (signal.getKaufVerkauf() == Order.VERKAUF) {
			// Ein Verkauf erfolgt nur, wenn ein Bestand dieses Wertpapiers vorhanden ist 
			if (depot.getWertpapierStueckzahl(wertpapier) > 0) {
				depot.verkaufe(depot.anfangsbestand/3, wertpapier);
			}
		}
		
		
	}

}
