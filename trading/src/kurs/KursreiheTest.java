package kurs;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;

public class KursreiheTest extends TestCase {
	private static final Logger log = LogManager.getLogger(KursreiheTest.class);

	public void testGetKurse () {
		
	}
	
	public void testKursreihe() {
		GregorianCalendar cal = new GregorianCalendar();
		Aktie aktie = Aktien.getInstance().getAktie("appl");
		ArrayList<Kurs> kursreihe = aktie.getKursreihe(); 
		assertNotNull(kursreihe);
		assertTrue(kursreihe.size() > 1);
		log.info("Kursreihe hat Kurse: " + kursreihe.size());
		
		Statistik.rechneVola(aktie, 10);
		Statistik.rechneVola(aktie, 30);
		Statistik.rechneVola(aktie, 100);
		aktie.writeFileIndikatoren();
//		Statistik.rechneIndikatoren(kursreihe);
//		Signalsuche.rechneSignale(kursreihe);
//		kursreihe.writeIndikatorenSignale();

	}
		
}
