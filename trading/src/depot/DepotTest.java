package depot;

import signal.Signalsuche;
import util.Util;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktien;
import kurs.Kursreihe;
import kurs.Statistik;

public class DepotTest extends TestCase {

	Kursreihe kursreihe; 
	GregorianCalendar datum1 = new GregorianCalendar(2017,11,1);
	private static final Logger log = LogManager.getLogger(DepotTest.class);

	/**
	 * erstellt eine Kursreihe
	 * berechnet die Indikatoren und Signale
	 * simuliert einen Handel 
	 */

	public void testKursreiheOhneCSV() {
		
		// Kursreihe erzeugen 

		kursreihe = Aktien.getInstance().getKursreihe("appl");
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		log.debug("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		
		// berechnet die Indikatoren und Signale 
		Statistik.rechneIndikatoren(kursreihe);
		Signalsuche.rechneSignale(kursreihe);
		
		// simuliert den Handel 
		Depot depot = new Depot("Oskars", 10000f);
		depot.simuliereHandel(kursreihe);
		float wert = depot.bewerteDepot(datum1);
		assertTrue(wert > 0);
		log.debug("Depotwert: " + wert + " Zeitpunkt: " + Util.formatDate(datum1));
		
		// tägliche Depot-Bewertung als Kursreihe
		Kursreihe depotKR = depot.bewerteDepotTaeglich(datum1);
		assertNotNull(depotKR);
		assertTrue(depotKR.kurse.size()>50);
		
	}

/*
	public void testKursreiheMitCSV() {
		// Kursreihe erzeugen 

		kursreihe = Aktien.getInstance().getKursreihe("appl");
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		log.debug("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		
		// berechnet die Indikatoren und Signale 
		Statistik.rechneIndikatoren(kursreihe);
		Signalsuche.rechneSignale(kursreihe);
		kursreihe.writeIndikatorenSignale();
		
		// simuliert den Handel 
		Depot depot = new Depot("Oskars", 10000f);
		depot.simuliereHandel(kursreihe);
 		depot.writeFileDepot();

	}
*/
	
	
}
