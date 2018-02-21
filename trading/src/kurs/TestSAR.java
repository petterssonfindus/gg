package kurs;

import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class TestSAR extends TestCase {
	
	private static Aktie aktie; 
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		
		aktie = new Aktie("testSAR", "testSAR", Aktien.INDEXDAX, Aktien.BOERSEINDEX);
		Kurs kurs = new Kurs(); 
		kurs.high = 46.59f;
		kurs.low = 45.9f;
		kurs.datum = new GregorianCalendar(2010, 0, 19); 
		aktie.addKurs(kurs);
		
		kurs = new Kurs(); 
		kurs.high = 46.55f;
		kurs.low = 45.38f;
		kurs.datum = new GregorianCalendar(2010, 0, 20); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 46.30f;
		kurs.low = 45.25f;
		kurs.datum = new GregorianCalendar(2010, 0, 21); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 45.43f;
		kurs.low = 43.99f;
		kurs.datum = new GregorianCalendar(2010, 0, 22); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 44.55f;
		kurs.low = 44.07f;
		kurs.datum = new GregorianCalendar(2010, 0, 25); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 44.84f;
		kurs.low = 44.00f;
		kurs.datum = new GregorianCalendar(2010, 0, 26); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 44.80f;
		kurs.low = 43.96f;
		kurs.datum = new GregorianCalendar(2010, 0, 27); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 44.38f;
		kurs.low = 43.27f;
		kurs.datum = new GregorianCalendar(2010, 0, 28); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.97f;
		kurs.low = 42.58f;
		kurs.datum = new GregorianCalendar(2010, 0, 29); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.23f;
		kurs.low = 42.83f;
		kurs.datum = new GregorianCalendar(2010, 1, 1); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.73f;
		kurs.low = 42.98f;
		kurs.datum = new GregorianCalendar(2010, 1, 2); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.92f;
		kurs.low = 43.37f;
		kurs.datum = new GregorianCalendar(2010, 1, 3); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.61f;
		kurs.low = 42.57f;
		kurs.datum = new GregorianCalendar(2010, 1, 4); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 42.97f;
		kurs.low = 42.07f;
		kurs.datum = new GregorianCalendar(2010, 1, 5); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.13f;
		kurs.low = 42.59f;
		kurs.datum = new GregorianCalendar(2010, 1, 8); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.46f;
		kurs.low = 42.71f;
		kurs.datum = new GregorianCalendar(2010, 1, 9); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.26f;
		kurs.low = 42.70f;
		kurs.datum = new GregorianCalendar(2010, 1, 10); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.74f;
		kurs.low = 42.71f;
		kurs.datum = new GregorianCalendar(2010, 1, 11); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 43.83f;
		kurs.low = 43.11f;
		kurs.datum = new GregorianCalendar(2010, 1, 12); 
		aktie.addKurs(kurs);

		kurs = new Kurs(); 
		kurs.high = 44.30f;
		kurs.low = 43.80f;
		kurs.datum = new GregorianCalendar(2010, 1, 16); 
		aktie.addKurs(kurs);

	}
	
	public void testSAR () {
		aktie.rechneIndikatoren();
	}

}
