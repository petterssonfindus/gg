package algo;

import java.util.GregorianCalendar;

import signal.Signalsuche;

import data.DBManager;
import junit.framework.TestCase;

public class KursreiheTest extends TestCase {
	
	public void testGetKurse () {
		
	}
	
	public void testKursreihe() {
		GregorianCalendar cal = new GregorianCalendar();
		Kursreihe kursreihe = DBManager.getKursreihe("appl", cal);
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		System.out.println("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		
		Statistik.rechneVola(kursreihe, 10);
		Statistik.rechneVola(kursreihe, 30);
		Statistik.rechneVola(kursreihe, 100);
		kursreihe.writeFileIndikatoren();
//		Statistik.rechneIndikatoren(kursreihe);
//		Signalsuche.rechneSignale(kursreihe);
//		kursreihe.writeIndikatorenSignale();

	}
		
}
