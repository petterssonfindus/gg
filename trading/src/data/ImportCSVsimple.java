package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import util.Util;

import algo.Kursreihe;
import algo.Tageskurs;

public class ImportCSVsimple {

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
        	// erste Zeile enthält die Überschriften
        	br.readLine();
        	File file = new File (pfad);
        	String name = file.getName();
        	System.out.println("Dateiname: " + name);
        	name = "dax";
        	
            while ((line = br.readLine()) != null) {

                String[] zeile = line.split(cvsSplitBy);
                // wenn der erste Kurs "null" enthält wird die Zeile ignoriert 
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
