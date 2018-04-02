package kurs;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aktie.Aktie;
import aktie.Aktien;
import aktie.Kurs;
import junit.framework.TestCase;

public class AktieTest extends TestCase {
	private static final Logger log = LogManager.getLogger(AktieTest.class);

	public void setUp() {
		
	}
	
	public void testGetKurse () {
		Aktie aktie = Aktien.getInstance().getAktie("AA");
		ArrayList<Kurs> kursreihe = aktie.getBoersenkurse(); 
		assertNotNull(kursreihe);
		assertTrue(kursreihe.size() > 1);
		log.info("Kursreihe hat Kurse: " + kursreihe.size());
		Kurs kurs = kursreihe.get(100);
		assertEquals(5.41476f, kurs.open);
		assertEquals(5.30662f, kurs.close);
		assertEquals(5.30662f, kurs.low);
		assertEquals(5.4468f, kurs.high);
		assertEquals(45900, kurs.volume);
	}
	
/*
	public void testKursreihe() {
		log.info("Start AktieTest");
		Aktie aktie = Aktien.getInstance().getAktie("AA");
		ArrayList<Kurs> kursreihe = aktie.getBoersenkurse(); 
		
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
 */
		
}
