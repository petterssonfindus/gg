package signal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Parameter;

/**
 * Beschreibt die Eigenschaften, die ein Signal erfüllen muss
 * Dient als Berechnungs-Vorschrift 
 * @author oskar
 *
 */
public class SignalBeschreibung extends Parameter {
	static final Logger log = LogManager.getLogger(SignalBeschreibung.class);

	short signalTyp; 

	public SignalBeschreibung(short signalTyp) {
		this.signalTyp = signalTyp; 
		log.debug("neue SignalBeschreibung Typ: " + signalTyp);
	}
	
}
