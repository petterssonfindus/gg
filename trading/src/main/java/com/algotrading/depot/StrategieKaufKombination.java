package depot;

import java.util.HashMap;

import signal.Signal;
/**
 * Strategie nimmt beliebige Indikatoren entgegen und kombiniert diese mit UND-Verknüpfung
 * Alle Bedingungen müssen vorliegen, damit gekauft wird 
 * @author oskar
 *
 */
public class StrategieKaufKombination implements KaufVerkaufStrategie {
	/**
	 * Bei der Erzeugung werden die Indikatoren gesetzt, die überwacht werden 
	 */
	public StrategieKaufKombination(HashMap<String, Float> indikator) {

	}
	
	@Override
	public void entscheideSignal(Signal signal, Depot depot) {
		// TODO Auto-generated method stub

	}

}
