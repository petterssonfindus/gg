package signal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktien;
import kurs.Kursreihe;
import kurs.Statistik;

public class SignalsucheTest extends TestCase {
	
	private static final Logger log = LogManager.getLogger(SignalsucheTest.class);

	public void testSignalsuche () {
		
	// Kursreihe erzeugen appl, dax
	Kursreihe kursreihe = Aktien.getInstance().getKursreihe("dax");
	assertNotNull(kursreihe);
	assertTrue(kursreihe.kurse.size() > 1);
	log.debug("Kursreihe hat Kurse: " + kursreihe.kurse.size());
	
	// Indikatoren berechnen
	Statistik.rechneIndikatoren(kursreihe);
//	kursreihe.writeFileIndikatoren();
	
	// Signale berechnen 
	Signalsuche.rechneSignale(kursreihe);
	kursreihe.writeFileSignale();

	}

}
