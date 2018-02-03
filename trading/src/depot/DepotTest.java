package depot;

import java.util.ArrayList;

import algo.Aktien;
import algo.Kursreihe;
import signal.Signalsuche;
import algo.Statistik;
import junit.framework.TestCase;

public class DepotTest extends TestCase {

	/**
	 * erstellt eine Kursreihe
	 * berechnet die Indikatoren und Signale
	 * simuliert einen Handel 
	 */

	public void testKursreihe() {
		// Kursreihe erzeugen 

		Kursreihe kursreihe = Aktien.getInstance().getKursreihe("appl");
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		System.out.println("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		
		// berechnet die Indikatoren und Signale 
		Statistik.rechneIndikatoren(kursreihe);
		Signalsuche.rechneSignale(kursreihe);
		kursreihe.writeIndikatorenSignale();
		
		// simuliert den Handel 
		Depot depot = new Depot();
		depot.simuliereHandel(kursreihe);
		depot.writeFileDepot();

	}


}
