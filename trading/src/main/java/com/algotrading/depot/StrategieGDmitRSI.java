package depot;

import signal.Signal;

/**
 * Kauft, wenn GD und RSI positiv sind 
 * @author oskar
 *
 */
public class StrategieGDmitRSI implements SignalStrategie {

	private boolean GDDurchbruch = false; 
	private boolean RSIKauf = false; 
	
	@Override
	public Order entscheideSignal(Signal signal, Depot depot) {
		Order order = null; 
		// filtere die Signale
		// reagiert auf GD-Durchbrüche ( Kauf oder Verkauf) 
		if (signal.getTyp() == Signal.GDDurchbruch) {
			// GD -Kauf-Signal 
			if (signal.getKaufVerkauf() == Order.KAUF) {
				this.GDDurchbruch = true;
			}
			// GD - Verkauf-Signal 
			// die Kauf-Zone wird damit verlassen 
			else {
				this.GDDurchbruch = false;
			}
		}
		// reagiert auf RSI - Durchbrüche  
		if (signal.getTyp() == Signal.RSI) {
			// Eintritt in die Kaufzone
			if (signal.getKaufVerkauf() == Order.KAUF) {
				this.RSIKauf = true;
			}
			// Austritt aus der Kauf-Zone
			else {
				this.RSIKauf = false;
			}
		}
		// wenn sich beide Indikatoren in der Kaufzone befinden, wird gekauft 
		if (this.GDDurchbruch && this.RSIKauf) {
			order = depot.kaufe(depot.anfangsbestand / 3, signal.getTageskurs().wertpapier);
			// Abwarten, bis zum nächsten Doppelsignal
			this.GDDurchbruch = false; 
			this.RSIKauf = false; 
		}
		return order; 
	}

}
