package kurs;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Beschreibt einen Indikator mit Typ und Wert
 * Der Wert ist meist die Zeitdauer
 * @author oskar
 *
 */
public class Indikator {
	private static final Logger log = LogManager.getLogger(Indikator.class);
	
	short typ; 
	HashMap<String, Float> parameter = new HashMap<String, Float>(); 
	/**
	 * Setzt den Typ. Parameter können mit addParameter hinzugefügt werden. 
	 * @param typ
	 */
	public Indikator (short typ) {
		this.typ = typ; 
	}
	/**
	 * Ein Konstruktor, der den Standard-Parameter "dauer" setzt 
	 * @param typ
	 * @param dauer
	 */
	public Indikator (short typ, int dauer) {
		this.typ = typ; 
		addParameter("dauer", (float) dauer);
	}
	/**
	 * Setzt die Parameter als fertige Liste
	 * Die Liste könnte aus der SignalBeschreibung stammen. 
	 * @param parameter
	 */
	public void addParameter (HashMap<String, Float> parameter) {
		this.parameter = parameter;
	}

	/**
	 * einen Parameter hinzufügen 
	 * @param name dient als Schlüssel 
	 * @param wert der zugehörige Wert
	 */
	public void addParameter (String name, Float wert) {
		this.parameter.put(name, wert);
	}
	/**
	 * Den Wert des Parameters, der zuvor eingestellt werden musste.
	 * Wenn nicht vorhanden, dann 0
	 * @param name
	 * @return
	 */
	public float getParameter (String name) {
		float result = 0;
		if (this.parameter.containsKey(name)) {
			result = Float.valueOf(this.parameter.get(name));
		}
		else {
			log.error("Parameter existiert nicht: " + name + " in Indikator: " + this.typ);
		}
		return result; 
	}
	/**
	 * enthält den Typ und eine Liste der vorhandenen Parameter
	 */
	public String toString() {
		String result = "Indikator: " + this.typ; 
		for (String name : this.parameter.keySet()) {
			result = result + (" - " + name + ": " + this.parameter.get(name));
		}
		return result; 
	}

}
