package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import depot.DepotTest;
import kurs.Kursreihe;
import kurs.Tageskurs;
import util.Util;

public class ImportCSVsimple {
	private static final Logger log = LogManager.getLogger(ImportCSVsimple.class);

//	static String pfad = "/home/oskar/Documents/finance/DAXkurz1.csv";
	static String pfad = "/home/oskar/Documents/finance/^GDAXI-1.csv";


	/**
	 * liest eine csv-Datei von Yahoo-Finance ein
	 * Zeilen mit 'null'-Werten werden ignoriert 
	 * @return
	 */
    public static Kursreihe readKurseYahooCSV() {

        String line = "";
        String cvsSplitBy = ",";
        Kursreihe kursreihe = new Kursreihe();

        try (BufferedReader br = new BufferedReader(new FileReader(pfad))) {
        	// erste Zeile enth�lt die �berschriften
        	br.readLine();
        	File file = new File (pfad);
        	String name = file.getName();
        	log.debug("Dateiname: " + name);
        	name = "dax";
        	
            while ((line = br.readLine()) != null) {

                String[] zeile = line.split(cvsSplitBy);
                // wenn der erste Kurs "null" enth�lt wird die Zeile ignoriert 
                if ( ! zeile[1].contains("null")) {
                	Tageskurs tageskurs = new Tageskurs();
                	tageskurs.name = name;
                	tageskurs.datum = Util.parseDatum(zeile[0]);
                	tageskurs.open = Float.parseFloat(zeile[1]);
                	tageskurs.high = Float.parseFloat(zeile[2]);
                	tageskurs.low = Float.parseFloat(zeile[3]);
                	tageskurs.close = Float.parseFloat(zeile[4]);
                	tageskurs.adjClose = Float.parseFloat(zeile[5]);
                	tageskurs.volume = Integer.parseInt(zeile[6]);
                	kursreihe.addKurs(tageskurs);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return kursreihe;

    }

}
