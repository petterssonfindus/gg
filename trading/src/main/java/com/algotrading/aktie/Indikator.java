package aktie;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Parameter;

/**
 * Beschreibt einen Indikator mit Typ und Wert
 * Der Wert ist meist die Zeitdauer
 * @author oskar
 *
 */
public class Indikator extends Parameter {
	private static final Logger log = LogManager.getLogger(Indikator.class);
	
	short typ; 

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
	 * enthält den Typ und eine Liste der vorhandenen Parameter
	 */
	public String toString() {
		String result = "Indi-" + this.typ; 
		for (String name : this.getAllParameter().keySet()) {
			result = result + ("-" + name + ":" + this.getParameter(name));
		}
		return result; 
	}

}
