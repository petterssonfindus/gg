package signal;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Legt die Parameter fest, die ein Signal erfüllen muss
 * @author oskar
 *
 */
public class SignalBeschreibung {
	static final Logger log = LogManager.getLogger(SignalBeschreibung.class);

	short signalTyp; 
	private HashMap<String, Object> parameter = new HashMap<String, Object>();

	public SignalBeschreibung(short signalTyp) {
		this.signalTyp = signalTyp; 
		log.debug("neue SignalBeschreibung Typ: " + signalTyp);
	}
	
	public Object getParameter (String name) {
		Object result; 
		result = this.parameter.get(name);
		if (result == null) log.error("Parameter " + name + " ist nicht vorhanden");
		return result; 
	}
	
	public void addParameter (String name, float wert) {
		this.parameter.put(name, wert);
	}
	
	public void addParameter (String name, int wert) {
		this.parameter.put(name, wert);
	}

	public void addParameter (String name, Object object) {
		this.parameter.put(name, object);
	}
}
