package depot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Kurs;
import signal.Signal;

public class StrategieJahrAlleSignale implements SignalStrategie {
	static final Logger log = LogManager.getLogger(StrategieJahrAlleSignale.class);

	/**
	 * Nutzt Jahrestag um aktive und passive Handelsphase zu steuern 
	 * Wenn Jahrestag-Verkauf, dann kein Handel 
	 * An der Aktie wird ein Parameter "phase" genutzt, um den Zustand abzubilden. 
	 */
	@Override
	public Order entscheideSignal(Signal signal, Depot depot) {
		Kurs kurs = signal.getTageskurs();
		String wertpapier = kurs.wertpapier;
		Order order = null; 
		
		if (signal.getTyp() == Signal.Jahrestag) {
			
			if (signal.getKaufVerkauf() == Order.KAUF) {
				signal.getTageskurs().getAktie().addParameter("phase", 1);
				log.debug("JahrestagSignal Kauf: " + signal.toString() );
			}
			// beim Verkauf wird alles verkauft 
			// und nicht mehr gehandelt, bis ein Kauf-Signal auftritt 
			if (signal.getKaufVerkauf() == Order.VERKAUF) {
				order = depot.verkaufeGesamtbestand();
				signal.getTageskurs().getAktie().addParameter("phase", 0);
				log.debug("JahrestagSignal Verkauf: " + signal.toString() );
			}
		}
		
		if (signal.getKaufVerkauf() == Order.KAUF) {
			log.debug("Signal->Kauf: " + signal.toString() );
			order = depot.kaufe(depot.anfangsbestand/3, wertpapier);
		}
		if (signal.getKaufVerkauf() == Order.VERKAUF) {
			// Ein Verkauf erfolgt nur, wenn ein Bestand dieses Wertpapiers vorhanden ist 
			if (depot.getWertpapierBestand(wertpapier) != null) {
				log.debug("Signal->Verkauf: " + signal.toString() );
				order = depot.verkaufe(wertpapier);
			}
		}
		return order; 
	}

}
