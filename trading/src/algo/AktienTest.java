package algo;

import junit.framework.TestCase;

public class AktienTest extends TestCase {
	
	public void testGetKursreihe() {
		Kursreihe kursreihe = Aktien.getInstance().getKursreihe("dax");
		assertNotNull(kursreihe);
		assertEquals("dax", kursreihe.name);
		assertTrue(kursreihe.kurse.size() > 200);
		// der 2. Aufruf bekommt die selbe Kursreihe 
		Kursreihe kursreihe2 = Aktien.getInstance().getKursreihe("dax");
		assertTrue(kursreihe == kursreihe2);
		
		
	}

}
