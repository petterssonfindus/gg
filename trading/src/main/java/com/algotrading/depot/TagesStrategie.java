package depot;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Util;

/**
 * Eine Strategie, die täglich anhand der situation im Depot entscheidet, ob gehandelt wird
 * @author oskar
 *
 */
public abstract class TagesStrategie {
	private static final Logger log = LogManager.getLogger(Util.class);

	private HashMap<String, Object> parameter = new HashMap<String, Object>();

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
	
	/**
	 * Täglich wird geprüft, ob gehandelt wird. 
	 * @param depot
	 * @return
	 */
	public abstract Order entscheideTaeglich (Depot depot);
	
}
