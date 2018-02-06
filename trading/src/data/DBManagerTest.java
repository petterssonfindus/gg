package data;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import signal.Signal;
import util.Util;
import junit.framework.TestCase;
import kurs.Kursreihe;
import kurs.Statistik;
import kurs.Tageskurs;

public class DBManagerTest extends TestCase {
	private static final Logger log = LogManager.getLogger(DBManagerTest.class);

	GregorianCalendar cal = new GregorianCalendar(2018,01,01);

/*	
	public void testAddTageskurs() {
		GregorianCalendar datum1 = new GregorianCalendar(2017,1,1);
		Tageskurs kurs = new Tageskurs();
		kurs.close = 111.22f;
		kurs.name = "appl";
		kurs.datum = datum1;
		DBManager.addTageskurs(kurs);
		log.debug("AddTageskurs erfolgreich getestet");
	}
*/	

/*	
	public void testGetTageskurs() {
		GregorianCalendar cal = new GregorianCalendar(2018,01,01);
		Tageskurs kurs = DBManager.getTageskurs("appl", cal);
		assertNotNull(kurs);
		log.debug("Tageskurs: " + kurs.toString());
		
	}
*/
/*
	public void testRechneTageskurs() {
		GregorianCalendar cal = new GregorianCalendar();
		Kursreihe kursreihe = DBManager.getKursreihe("appl", cal);
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		log.debug("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		Statistik.rechneIndikatoren(kursreihe);
		Signal.rechneSignale(kursreihe);
		
		Tageskurs tageskurs = kursreihe.kurse.get(1);
		assertNotNull(tageskurs);
		kursreihe.writeFile();
//		DBManager.schreibeTageskurse(kursreihe);
		
	}
*/	

	public void testGetKursreihe() {
		Kursreihe kursreihe = DBManager.getKursreihe("appl");
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		log.debug("Kursreihe appl hat Kurse: " + kursreihe.kurse.size());
		
	}
	public void testGetKursreiheBeginn() {
		GregorianCalendar cal = new GregorianCalendar(2017,06,01);
		Kursreihe kursreihe = DBManager.getKursreihe("dax", cal);
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 50);
		log.debug("DAX ab " + Util.formatDate(cal) + " hat Kurse: " + kursreihe.kurse.size());
		
	}

}
