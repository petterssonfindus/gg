package data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Kursreihe;

public class ImportCSVsimpleTest extends TestCase {
	private static final Logger log = LogManager.getLogger(ImportCSVsimple.class);

	
	public void testImportCSV() {
		
		Kursreihe kursreihe = ImportCSVsimple.readKurseYahooCSV();
		assertNotNull(kursreihe);
		log.info(kursreihe.toString());
		
		DBManager.addKursreihe(kursreihe);
	}

}
