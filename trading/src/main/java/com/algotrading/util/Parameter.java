package util;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parameter {
	static final Logger log = LogManager.getLogger(Parameter.class);

	private HashMap<String, Object> parameter = new HashMap<String, Object>();

	public Object getParameter (String name) {
		Object result; 
		result = this.parameter.get(name);
		if (result == null) log.error("Parameter " + name + " ist nicht vorhanden");
		return result; 
	}
	
	public HashMap<String, Object> getAllParameter () {
		return this.parameter;
	}
	/**
	 * wenn es den Parameter bereits gibt, wird der Wert überschrieben 
	 * @param name Parametername 
	 * @param wert Zahlenwert
	 */
	public void addParameter (String name, float wert) {
		this.parameter.put(name, wert);
	}
	
	/**
	 * wenn es den Parameter bereits gibt, wird der Wert überschrieben 
	 * @param name Parametername 
	 * @param wert Integer-Zahlenwert
	 */
	public void addParameter (String name, int wert) {
		this.parameter.put(name, wert);
	}

	public void addParameter (String name, Object object) {
		this.parameter.put(name, object);
	}
	
}
