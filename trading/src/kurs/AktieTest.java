package kurs;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;

public class AktieTest extends TestCase {
	private static final Logger log = LogManager.getLogger(AktieTest.class);

	public void testGetKurse () {
		
	}
	
	public void testKursreihe() {
		log.info("Start AktieTest");
		Aktie aktie = Aktien.getInstance().getAktie("AA");
		ArrayList<Kurs> kursreihe = aktie.getBoersenkurse(); 
		assertNotNull(kursreihe);
		assertTrue(kursreihe.size() > 1);
		log.info("Kursreihe hat Kurse: " + kursreihe.size());
		
		Statistik.rechneVola(aktie, 10);
		Statistik.rechneVola(aktie, 30);
		Statistik.rechneVola(aktie, 100);
		log.info("Kursreihe hat Kurse: " + kursreihe.size());
		Statistik.rechneIndikatoren(aktie);
		log.info("Schreibe File " );
		
		aktie.writeFileIndikatoren();
//		Statistik.rechneIndikatoren(kursreihe);
//		Signalsuche.rechneSignale(kursreihe);
//		kursreihe.writeIndikatorenSignale();

	}
		
}
