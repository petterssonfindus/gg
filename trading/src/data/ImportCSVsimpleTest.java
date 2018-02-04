package data;

import junit.framework.TestCase;
import kurs.Kursreihe;

public class ImportCSVsimpleTest extends TestCase {
	
	
	public void testImportCSV() {
		
		Kursreihe kursreihe = ImportCSVsimple.readKurseYahooCSV();
		assertNotNull(kursreihe);
		System.out.println(kursreihe.toString());
		
		DBManager.addKursreihe(kursreihe);
	}

}
