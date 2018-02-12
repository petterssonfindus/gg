package data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;

public class ImportCSVsimpleTest extends TestCase {
	private static final Logger log = LogManager.getLogger(ImportCSVsimple.class);
	private static String name = "BAC";
	
/*
	public void testImportCSV() {
		ImportKursreihe kursreihe = ImportCSVsimple.readKurseYahooCSV(name);
		assertNotNull(kursreihe);
		log.info("Kursreihe wurde eingelesen: " + kursreihe.kuerzel);
		DBManager.schreibeNeueAktieTabelle(name);
		DBManager.schreibeKurse(kursreihe);
	}
*/
	
	public void testImportPath() {
		ImportCSVsimple.readPfadKurseYahooCSV();
		
	}
	
	
}
