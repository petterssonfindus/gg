package depot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import signal.Signal;
import signal.Signalsuche;

public class StrategieAllesKaufen implements KaufVerkaufStrategie {
	static final Logger log = LogManager.getLogger(Signalsuche.class);
	
	/**
	 * Nutzt jedes Kaufsignal zum Kauf
	 */
	@Override
	public void entscheideSignal(Signal signal, Depot depot) {
		
		if (signal.getKaufVerkauf() == Order.KAUF) {
			depot.kaufe(depot.anfangsbestand/3, signal.getTageskurs().wertpapier);
		}
		
	}

}
