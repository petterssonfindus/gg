package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Kurs;
import util.Util;
/**
 * Importiert CSV-Dateien mit Kursdaten 
 * @author oskar
 */
public class ImportCSVsimple {
	private static final Logger log = LogManager.getLogger(ImportCSVsimple.class);

//	static String pfad = "/home/oskar/Documents/finance/DAXkurz1.csv";
//	static String pfad = "/home/oskar/Documents/finance/";

	/**
	 * C:\\Users\\xk02200\\Aktien\\nysekurse\\
	 * @return
	 */
	private static String getPfadCSV() {
    	return Util.getUserProperty("home") + Util.getFileSeparator() + "Aktien" + 
    			Util.getFileSeparator() + "nysekurse" + Util.getFileSeparator();
	}
	
	/**
	 * Steuert das Einlesen aller CSV-Dateien, die sich im o.g. Pfad befinden. 
	 * Erzeugt Tabellen mit dem Dateinamen als Kürzel mit allen enthaltenen Kursen. 
	 * Es dürfen nur csv-Files enthalten sein #TODO die anderen Files ignorieren
	 * Ist die Tabelle vorhanden, geschieht nichts. 
	 */
	public static void readPfadKurseYahooCSV() {
		// holt sich alle Dateien im o.g. Verzeichnis 
		File[] directoryListing = getCSVFilesInPath();
		ImportKursreihe importkursreihe; 
		if (directoryListing != null) {
			// Iteriert über alle enthaltenen Dateien. 
			for (File child : directoryListing) {
				log.info("File: " + child.getName());
				// erzeugt eine ImportKursreihe aus den CSV-Einträgen.
				importkursreihe = readKurseYahooCSV(child);
				if (importkursreihe != null) {
					// erzeugt eine neue Tabelle mit dem Kürzel, falls noch keine vorhanden ist
					if (DBManager.schreibeNeueAktieTabelle(importkursreihe.kuerzel)) {
						// schreibt die Kurse in die neue Tabelle
						DBManager.schreibeKurse(importkursreihe);
					}
				}
				else log.error("ImportKursreihe fehlerhaft: " + importkursreihe.kuerzel);
		    }
		} else {
			log.error("Pfad ist leer " );
		}
	}
	
	/**
	 * Liest eine einzelne Datei aus dem Pfad. 
	 * Erzeugt Tabellen mit dem Dateinamen als Kürzel mit allen enthaltenen Kursen. 
	 * @param name
	 */
	public static File getCSVFile (String name) {
		String pfad = getPfadCSV() + Util.getFileSeparator() + name + ".csv";
		File file = new File(pfad);
		return file; 
	}
	
	protected static File[] getCSVFilesInPath () {
		// holt sich den Pfad
		File dir = new File(getPfadCSV());
		// die Liste aller Files 
		return dir.listFiles();
		
	}
	
	/**
	 * liest eine csv-Datei von Yahoo-Finance ein
	 * Zeilen mit 'null'-Werten werden ignoriert 
	 * Aus jeder Zeile wird ein Tageskurs erzeugt
	 * @return
	 */
    public static ImportKursreihe readKurseYahooCSV(File file) {
        String line = "";
        String cvsSplitBy = ",";
        // aus dem Dateinamen einen Tabellennamen machen 
		String kuerzel = file.getName().replace(".csv", "");
		kuerzel = kuerzel.replace("^", "xxx");  // bei Indizes muss das Sonderzeichen entfernt werden wegen der DB.
		kuerzel = kuerzel.toLowerCase();
		// die Kursreihe mit dem Kürzel erzeugen
		ImportKursreihe importKursreihe = new ImportKursreihe(kuerzel);
		ArrayList<Kurs> kursreihe = importKursreihe.kurse;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        	log.info("CSV-Datei einlesen: " + file.getName());
        	// erste Zeile enthält die Überschriften
        	br.readLine();
        	
            while ((line = br.readLine()) != null) {

                String[] zeile = line.split(cvsSplitBy);
                // wenn der erste Kurs "null" enthält wird die Zeile ignoriert 
                if ( ! zeile[1].contains("null")) {
                	Kurs tageskurs = new Kurs();
                	tageskurs.name = kuerzel;
                	tageskurs.datum = Util.parseDatum(zeile[0]);
                	tageskurs.open = Float.parseFloat(zeile[1]);
                	tageskurs.high = Float.parseFloat(zeile[2]);
                	tageskurs.low = Float.parseFloat(zeile[3]);
                	tageskurs.close = Float.parseFloat(zeile[4]);
                	tageskurs.adjClose = Float.parseFloat(zeile[5]);
                	// Indizes haben als Volume Gleitkommazahlen, deshalb wird gecasted 
                	tageskurs.volume = (int) Float.parseFloat(zeile[6]);
                	kursreihe.add(tageskurs);
                }
            }

        } catch (IOException e) {
        	log.error("Feher beim Einlesen Datei: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        
        return importKursreihe;

    }

}
