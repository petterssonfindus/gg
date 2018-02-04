package data;

import java.util.Calendar;
import java.util.GregorianCalendar;

import signal.Signal;
import junit.framework.TestCase;
import kurs.Kursreihe;
import kurs.Statistik;
import kurs.Tageskurs;

public class DBManagerTest extends TestCase {
	
/*	
	public void testAddTageskurs() {
		GregorianCalendar datum1 = new GregorianCalendar(2017,1,1);
		Tageskurs kurs = new Tageskurs();
		kurs.close = 111.22f;
		kurs.name = "appl";
		kurs.datum = datum1;
		DBManager.addTageskurs(kurs);
		System.out.println("AddTageskurs erfolgreich getestet");
	}
*/	

/*	
	public void testGetTageskurs() {
		GregorianCalendar cal = new GregorianCalendar(2018,01,01);
		Tageskurs kurs = DBManager.getTageskurs("appl", cal);
		assertNotNull(kurs);
		System.out.println("Tageskurs: " + kurs.toString());
		
	}
*/
/*
	public void testRechneTageskurs() {
		GregorianCalendar cal = new GregorianCalendar();
		Kursreihe kursreihe = DBManager.getKursreihe("appl", cal);
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		System.out.println("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		Statistik.rechneIndikatoren(kursreihe);
		Signal.rechneSignale(kursreihe);
		
		Tageskurs tageskurs = kursreihe.kurse.get(1);
		assertNotNull(tageskurs);
		kursreihe.writeFile();
//		DBManager.schreibeTageskurse(kursreihe);
		
	}
*/	

/*	
	public void testGetKursreihe() {
		GregorianCalendar cal = new GregorianCalendar();
		Kursreihe kursreihe = DBManager.getKursreihe("appl", cal);
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		System.out.println("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		
		kursreihe.rechneIndikatoren();
	}
*/
}
