package depot;

import java.util.HashMap;

import signal.Signal;
/**
 * Generische Strategie nimmt beliebige Indikatoren entgegen und kombiniert diese mit UND-Verknüpfung
 * Alle Bedingungen müssen vorliegen, damit gekauft wird 
 * @author oskar
 *
 */
public class StrategieKaufKombination extends SignalStrategie {
	/**
	 * Bei der Erzeugung werden die Indikatoren gesetzt, die überwacht werden 
	 */
	public StrategieKaufKombination(HashMap<String, Float> indikator) {

	}
	
	@Override
	public Order entscheideSignal(Signal signal, Depot depot) {
		Order order = null; 
		// TODO Auto-generated method stub
		return order; 
	}

}
