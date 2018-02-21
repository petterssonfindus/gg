/**
 * 
 */
package depot;

import java.util.GregorianCalendar;

/**
 * Bei einer Depot-Simulation wird eine StopLossstrategie eingesetzt um das Risiko zu begrenzen. 
 * Wird täglich aufgerufen um das Risiko zu bewerten und zu verkaufen. 
 * Hat Zugriff auf den Aktienbestand des Depots. 
 * @author oskar
 */
public interface StopLossStrategie {
	
	public void entscheideStopLoss (Depot depot, GregorianCalendar stichtag);
	
}
