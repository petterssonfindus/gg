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
    			Util.getFileSeparator() + "testkurse" + Util.getFileSeparator();
	}
	
	/**
	 * liest alle CSV-Dateien ein, die sich im o.g. Pfad befinden. 
	 * Erzeugt Tabellen mit dem Dateinamen als Kürzel mit allen enthaltenen Kursen. 
	 * @param pfad
	 */
	public static void readPfadKurseYahooCSV() {
		File[] directoryListing = getCSVFilesInPath();
		ImportKursreihe importkursreihe; 
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String kuerzel = child.getName().replace(".csv", "");
				log.info("File: " + kuerzel);
				importkursreihe = readKurseYahooCSV(child);
				DBManager.schreibeNeueAktieTabelle(kuerzel);
				DBManager.schreibeKurse(importkursreihe);
		    }
		} else {
			log.error("Pfad ist leer " );
		}
	}
	
	private static File[] getCSVFilesInPath () {
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
        ImportKursreihe importKursreihe = new ImportKursreihe(file.getName());
        ArrayList<Kurs> kursreihe = importKursreihe.kurse;
		String kuerzel = file.getName().replace(".csv", "");

//        try (BufferedReader br = new BufferedReader(new FileReader(getPfadCSV() + kuerzel + ".csv"))) {
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
                	tageskurs.volume = Integer.parseInt(zeile[6]);
                	kursreihe.add(tageskurs);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return importKursreihe;

    }

}
