package depot;

import java.util.HashMap;

import signal.Signal;
/**
 * Strategie nimmt beliebige Indikatoren entgegen und kombiniert diese mit UND-Verkn�pfung
 * Alle Bedingungen m�ssen vorliegen, damit gekauft wird 
 * @author oskar
 *
 */
public class StrategieKaufKombination implements KaufVerkaufStrategie {
	/**
	 * Bei der Erzeugung werden die Indikatoren gesetzt, die �berwacht werden 
	 */
	public StrategieKaufKombination(HashMap<String, Float> indikator) {

	}
	
	@Override
	public void entscheideSignal(Signal signal, Depot depot) {
		// TODO Auto-generated method stub

	}

}
