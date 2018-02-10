package kurs;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.DBManager;
	/**
	 * Verzeichnis aller Aktien, zu denen Zeitreihen vorhanden sind
	 * Bietet Zugang zu Zeitreihen
	 * Als Singleton verfügbar
	 * @author oskar
	 *
	 */
public class Aktien {
	private static final Logger log = LogManager.getLogger(Aktien.class);

	private static Aktien instance;
	// das Verzeichnis aller Aktien 
	private static HashMap<String, Kursreihe> verzeichnis = new HashMap<String, Kursreihe>();
	
	private Aktien() {}
	
	public static Aktien getInstance() {
		if (instance == null) {
			instance = new Aktien();
			// das Verzeichnis wird versorgt 
			initialisiereVerzeichnis();
		}
		return instance; 
	}
	
	private static void initialisiereVerzeichnis() {
		verzeichnis.put("dax", null);
		verzeichnis.put("appl", null);
	}
	/**
	 * liest und initialisiert eine Kursreihe anhand eines WP-Namens
	 * @param wertpapier
	 * @return
	 */
	public Kursreihe getKursreihe (String wertpapier) {
		if (wertpapier == null) log.error("Inputvariable Wertpapier ist null");
		Kursreihe kursreihe = null; 
		// sucht das Wertpapier im Verzeichnis
		if (verzeichnis.containsKey(wertpapier)) {
			kursreihe = verzeichnis.get(wertpapier);
			// zu Beginn sind alle Kursreihen null 
			if (kursreihe == null) {
				// die Kursreihe wird im Verzeichnis eingefügt 
				// kunftige Aufrufe greifen sofort auf diese Kursreihe zu
				kursreihe = DBManager.getKursreihe(wertpapier);
				verzeichnis.replace(wertpapier, kursreihe);
			}
		}
		else log.error("Kursreihe nicht vorhanden: " + wertpapier);
		return kursreihe; 
	}

}
