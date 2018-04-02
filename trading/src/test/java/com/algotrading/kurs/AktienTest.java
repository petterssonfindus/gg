package kurs;

import java.util.ArrayList;

import aktie.Aktie;
import aktie.Aktien;
import aktie.Kurs;
import junit.framework.TestCase;

public class AktienTest extends TestCase {
	
	public void testGetKursreihe() {
		Aktie aktie = Aktien.getInstance().getAktie("dax");
		assertNotNull(aktie);
		assertEquals("dax", aktie.name);
		ArrayList<Kurs> kursreihe = aktie.getBoersenkurse();
		assertTrue(kursreihe.size() > 200);
		// der 2. Aufruf bekommt die selbe Kursre ihe 
		Aktie aktie2 = Aktien.getInstance().getAktie("dax");
		assertTrue(aktie == aktie2);
		assertTrue(aktie.getBoersenkurse() == aktie2.getBoersenkurse());
		
	}
	

}
