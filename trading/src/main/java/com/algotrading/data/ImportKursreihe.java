package data;

import java.util.ArrayList;

import aktie.Kurs;

/**
 * Dient als Zwischenspeicher f�r eingelesene Kursdaten, die anschlie�end in die DB geschrieben werden. 
 * @author Oskar
 */
public class ImportKursreihe {
	
	protected String kuerzel; // wird aus dem Dateinamen erzeugt
	protected ArrayList<Kurs> kurse = new ArrayList<Kurs>();
	
	ImportKursreihe (String kuerzel) {
		this.kuerzel = kuerzel; 
	}
	
}
