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
	
	public void testZeitraum () {
		GregorianCalendar beginn = new GregorianCalendar(2017,11,2);
		GregorianCalendar ende = new GregorianCalendar(2018,0,2);
		GregorianCalendar test1 = new GregorianCalendar(2017,11,3);
		GregorianCalendar test2 = new GregorianCalendar(2018,11,2);
		Zeitraum zs1 = new Zeitraum(beginn, ende);
		Zeitraum zs2 = new Zeitraum(beginn, ende);
		Zeitraum zs3 = new Zeitraum(beginn, test1);
		Zeitraum zs4 = new Zeitraum(beginn, test2);
		assertTrue(zs1.equals(zs2));
		assertFalse(zs1.equals(zs3));
		assertFalse(zs1.equals(zs4));
		assertFalse(zs2.equals(zs3));
		assertFalse(zs1 == zs2);
		
	}
	
	public void testRundeBetrag () {
		assertEquals(0.11f, Util.rundeBetrag(0.1111f));
		assertEquals(6.67f, Util.rundeBetrag(6.6666f));
		assertEquals(0.01f, Util.rundeBetrag(0.006f));
		assertEquals(0.01f, Util.rundeBetrag(0.005f));
		assertEquals(0f, Util.rundeBetrag(0.004f));
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
	
	public void testAddTage() {
		GregorianCalendar testDatum;
		GregorianCalendar beginn = new GregorianCalendar(2017,11,2);
		testDatum = Util.addTage(beginn, 1);
		assertEquals(Util.formatDate(testDatum), "2017-12-03");
		testDatum = Util.addTage(beginn, 10);
		assertEquals(Util.formatDate(testDatum), "2017-12-12");
		testDatum = Util.addTage(beginn, 30);
		assertEquals(Util.formatDate(testDatum), "2018-01-01");
		testDatum = Util.addTage(beginn, 60);
		assertEquals(Util.formatDate(testDatum), "2018-01-31");
		testDatum = Util.addTage(beginn, 365);
		assertEquals(Util.formatDate(testDatum), "2018-12-02");
		
	}
	
	public void testIstInZeitraum () {
		GregorianCalendar beginn = new GregorianCalendar(2017,11,2);
		GregorianCalendar ende = new GregorianCalendar(2018,0,2);
		GregorianCalendar datum1 = new GregorianCalendar(2018,0,1);
		GregorianCalendar datum2 = new GregorianCalendar(2017,11,3);
		GregorianCalendar datum3 = new GregorianCalendar(2017,11,31);
		GregorianCalendar datum4 = new GregorianCalendar(2016,11,31);
		GregorianCalendar datum5 = new GregorianCalendar(2019,11,31);
		assertTrue(Util.istInZeitraum(datum1, beginn, ende));
		assertTrue(Util.istInZeitraum(datum2, beginn, ende));
		assertTrue(Util.istInZeitraum(datum3, beginn, ende));
		assertTrue(Util.istInZeitraum(beginn, beginn, ende));
		assertTrue(Util.istInZeitraum(ende, beginn, ende));
		assertFalse(Util.istInZeitraum(datum4, beginn, ende));
		assertFalse(Util.istInZeitraum(datum5, beginn, ende));
	}
	
	public void testUserDirectory () {
		log.info("User-Country: " + Util.getUserProperty("country"));
		log.info("User-Directory: " + Util.getUserProperty("dir"));
		log.info("User-Home: " + Util.getUserProperty("home"));
		log.info("User-Language: " + Util.getUserProperty("language"));
		log.info("User-Name: " + Util.getUserProperty("name"));
	}

}
