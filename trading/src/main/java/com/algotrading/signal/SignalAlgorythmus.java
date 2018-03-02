package signal;

import kurs.Aktie;
import kurs.Kurs;

/**
 * Ein Signal-Algorythmus muss diese Schnittstelle implementieren 
 * @author Oskar 
 *
 */
public interface SignalAlgorythmus {
	/**
	 * ermittelt Signale anhand eines Kurses 
	 * @param tageskurs
	 * @param aktie
	 */
	public void ermittleSignal(Kurs tageskurs, Aktie aktie);
	

}
