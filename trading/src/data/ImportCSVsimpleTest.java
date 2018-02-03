package data;

import algo.Kursreihe;
import junit.framework.TestCase;

public class ImportCSVsimpleTest extends TestCase {
	
	
	public void testImportCSV() {
		
		Kursreihe kursreihe = ImportCSVsimple.readKurseYahooCSV();
		assertNotNull(kursreihe);
		System.out.println(kursreihe.toString());
		
		DBManager.addKursreihe(kursreihe);
	}

}
