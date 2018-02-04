package kurs;

import java.util.HashMap;

import data.DBManager;
	/**
	 * Verzeichnis aller Aktien, zu denen Zeitreihen vorhanden sind
	 * Bietet Zugang zu Zeitreihen
	 * Als Singleton verfügbar
	 * @author oskar
	 *
	 */
public class Aktien {

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
	
	public Kursreihe getKursreihe (String wertpapier) {
		Kursreihe kursreihe = null; 
		// sucht das Wertpapier im Verzeichnis
		if (verzeichnis.containsKey(wertpapier)) {
			kursreihe = verzeichnis.get(wertpapier);
			// zu Beginn sind alle Kursreihen null 
			if (kursreihe == null) {
				// die Kursreihe wird im Verzeichnis eingefügt 
				// kunftige Aufrufe greifen sofort auf diese Kursreihe zu
				kursreihe = DBManager.getKursreihe(wertpapier, null);
				verzeichnis.replace(wertpapier, kursreihe);
			}
		}
		return kursreihe; 
		
	}

}
