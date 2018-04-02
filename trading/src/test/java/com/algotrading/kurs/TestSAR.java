package kurs;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import aktie.Aktie;
import aktie.Aktien;
import aktie.Kurs;
import junit.framework.TestCase;

public class TestSAR extends TestCase {
	
	private static Aktie SARAktie; 
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		
		SARAktie = Aktien.getInstance().getAktie("sardata5");

	}
	
	public void testSAR () {
		SARAktie.rechneIndikatoren();
		ArrayList<Kurs> kurse = SARAktie.getBoersenkurse();
		Kurs testKurs;
		testKurs = kurse.get(3);
		assertEquals(46.55f,testKurs.sar);
		assertEquals(46.27f,testKurs.high);
		assertEquals(45.92f,testKurs.low);
		testKurs = kurse.get(30);
		assertEquals(42.350117f,testKurs.sar);
		assertEquals(44.57f,testKurs.high);
		assertEquals(44.26f,testKurs.low);
		
	}

}
