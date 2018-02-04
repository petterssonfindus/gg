package signal;

import java.util.GregorianCalendar;

import junit.framework.TestCase;
import kurs.Aktien;
import kurs.Kursreihe;
import kurs.Statistik;

public class SignalsucheTest extends TestCase {
	public void testSignalsuche () {
		
	// Kursreihe erzeugen appl, dax
	GregorianCalendar cal = new GregorianCalendar();
	Kursreihe kursreihe = Aktien.getInstance().getKursreihe("dax");
	assertNotNull(kursreihe);
	assertTrue(kursreihe.kurse.size() > 1);
	System.out.println("Kursreihe hat Kurse: " + kursreihe.kurse.size());
	
	// Indikatoren berechnen
	Statistik.rechneIndikatoren(kursreihe);
//	kursreihe.writeFileIndikatoren();
	
	// Signale berechnen 
	Signalsuche.rechneSignale(kursreihe);
	kursreihe.writeFileSignale();

	}

}
