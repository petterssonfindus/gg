package aktie;

import java.util.ArrayList;

import aktie.Aktie;
import aktie.Aktien;
import aktie.Kurs;
import indikator.Indikatoren;
import junit.framework.TestCase;

public class TestRSI extends TestCase {
	
	Aktie RSIAktie; 
	
	public void setUp() {
		RSIAktie = Aktien.getInstance().getAktie("sardata5");
		
	}
	
	public void testRSI() {
		Indikatoren.rechneRSI(RSIAktie, 10);
		
		ArrayList<Kurs> kurse = RSIAktie.getBoersenkurse();
		Kurs testKurs;
		testKurs = kurse.get(3);
		assertEquals(46.55f,testKurs.rsi);

	}

}
