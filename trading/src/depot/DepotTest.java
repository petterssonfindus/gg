package depot;

import java.util.ArrayList;

import signal.Signalsuche;
import junit.framework.TestCase;
import kurs.Aktien;
import kurs.Kursreihe;
import kurs.Statistik;

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
