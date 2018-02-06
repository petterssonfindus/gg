package kurs;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.DBManager;
import junit.framework.TestCase;

public class KursreiheTest extends TestCase {
	private static final Logger log = LogManager.getLogger(KursreiheTest.class);

	public void testGetKurse () {
		
	}
	
	public void testKursreihe() {
		GregorianCalendar cal = new GregorianCalendar();
		Kursreihe kursreihe = DBManager.getKursreihe("appl");
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		log.debug("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		
		Statistik.rechneVola(kursreihe, 10);
		Statistik.rechneVola(kursreihe, 30);
		Statistik.rechneVola(kursreihe, 100);
		kursreihe.writeFileIndikatoren();
//		Statistik.rechneIndikatoren(kursreihe);
//		Signalsuche.rechneSignale(kursreihe);
//		kursreihe.writeIndikatorenSignale();

	}
		
}
