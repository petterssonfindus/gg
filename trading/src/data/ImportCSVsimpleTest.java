package data;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktie;
import kurs.Aktien;
import util.Util;

public class ImportCSVsimpleTest extends TestCase {
	private static final Logger log = LogManager.getLogger(ImportCSVsimple.class);
	private static String name = "BAC";

	
	public void testAllesEinlesen() {
		ImportCSVsimple.readPfadKurseYahooCSV();
	}
	
/*
	public void testImportCSV() {
		ImportKursreihe kursreihe = ImportCSVsimple.readKurseYahooCSV(name);
		assertNotNull(kursreihe);
		log.info("Kursreihe wurde eingelesen: " + kursreihe.kuerzel);
		DBManager.schreibeNeueAktieTabelle(name);
		DBManager.schreibeKurse(kursreihe);
	}
*/

	/*
	public void testImportPath() {
		String test; 
		File[] files = ImportCSVsimple.getCSVFilesInPath();
		for (File file : files) {
			String name = Util.addAnfZeichen(file.getName().replace(".csv", ""));
			test = "		verzeichnis.put(" + name + ", new Aktie (" + name + ", " + name + ", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));";
			System.out.println(test);
		}
	}
 */
	
	
}
