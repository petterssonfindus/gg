package util;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;

public class UtilTest extends TestCase {
	private static final Logger log = LogManager.getLogger(UtilTest.class);

	public void testFloatString () {
		float test = 17.834f;
		log.info("Utiltest: " + Util.toString(test));
	}
	
	public void testDatumFormat() {
		GregorianCalendar beginn = new GregorianCalendar(2017,11,2);
		assertTrue(Util.formatDate(beginn).equalsIgnoreCase("2017-12-02"));
		GregorianCalendar ende = new GregorianCalendar(2018,0,2);
		assertTrue(Util.formatDate(ende).equalsIgnoreCase("2018-01-02"));
		log.info("Beginn: " + Util.formatDate(beginn) + " Ende: "+ Util.formatDate(ende));
	}
	
	public void testParseDatumJJJJ_MM_TT () {
		String testDatum = "2017-12-02";
		GregorianCalendar datum = Util.parseDatum(testDatum);
		assertNotNull(datum);
	}
	
	public void testParseDatumTT_MM_JJJJ () {
		String testDatum = "04.01.2010";
		GregorianCalendar datum = Util.parseDatum(testDatum);
		assertNotNull(datum);
	}
	
	public void testAnzahlTage () {
		GregorianCalendar beginn = new GregorianCalendar(2017,11,2);
		GregorianCalendar ende = new GregorianCalendar(2018,0,2);
		GregorianCalendar datum1 = new GregorianCalendar(2018,0,1);
		GregorianCalendar datum2 = new GregorianCalendar(2017,11,3);
		GregorianCalendar datum3 = new GregorianCalendar(2017,11,31);
		assertEquals(31, Util.anzahlTage(beginn, ende));
		assertEquals(30, Util.anzahlTage(beginn, datum1));
		assertEquals(1, Util.anzahlTage(beginn, datum2));
		assertEquals(29, Util.anzahlTage(beginn, datum3));
	}
	
	public void testIstInZeitspanne () {
		GregorianCalendar beginn = new GregorianCalendar(2017,11,2);
		GregorianCalendar ende = new GregorianCalendar(2018,0,2);
		GregorianCalendar datum1 = new GregorianCalendar(2018,0,1);
		GregorianCalendar datum2 = new GregorianCalendar(2017,11,3);
		GregorianCalendar datum3 = new GregorianCalendar(2017,11,31);
		GregorianCalendar datum4 = new GregorianCalendar(2016,11,31);
		GregorianCalendar datum5 = new GregorianCalendar(2019,11,31);
		assertTrue(Util.istInZeitspanne(datum1, beginn, ende));
		assertTrue(Util.istInZeitspanne(datum2, beginn, ende));
		assertTrue(Util.istInZeitspanne(datum3, beginn, ende));
		assertFalse(Util.istInZeitspanne(beginn, beginn, ende));
		assertFalse(Util.istInZeitspanne(ende, beginn, ende));
		assertFalse(Util.istInZeitspanne(datum4, beginn, ende));
		assertFalse(Util.istInZeitspanne(datum5, beginn, ende));
	}
	
	public void testUserDirectory () {
		log.info("User-Country: " + Util.getUserProperty("country"));
		log.info("User-Directory: " + Util.getUserProperty("dir"));
		log.info("User-Home: " + Util.getUserProperty("home"));
		log.info("User-Language: " + Util.getUserProperty("language"));
		log.info("User-Name: " + Util.getUserProperty("name"));
	}

}
