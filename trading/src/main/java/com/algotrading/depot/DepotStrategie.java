package depot;

import signal.Signal;
/**
 * Bei einer Depot-Simulation wird eine Depotstrategie eingesetzt
 * Setzt Signale in Orders um.
 * Hat Zugriff auf die Aktie über das Signal und den Zustand des Depots.  
 * @author oskar
 */
public interface DepotStrategie {
	/**
	 * anhand eines Signals wird entschieden, ob es in eine Order umgesetzt wird.
	 * @param signal
	 * @param depot
	 */
	public void entscheideSignal (Signal signal, Depot depot);
	
}
