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
	private static final Logger log = LogManager.getLogger(DepotTest.class);

	Kursreihe kursreihe; 
	private GregorianCalendar datum1;
	private GregorianCalendar datum2;
	
	@Override
	protected void setUp() throws Exception {
		datum1 = new GregorianCalendar(2017,11,1);
		datum2 = new GregorianCalendar(2017,12,1);
		kursreihe = Aktien.getInstance().getKursreihe("dax");
		// berechnet die Indikatoren und Signale 
		Statistik.rechneIndikatoren(kursreihe);
		Signalsuche.rechneSignale(kursreihe);
		
	}

	/**
	 * erstellt eine Kursreihe
	 * berechnet die Indikatoren und Signale
	 * simuliert einen Handel 
	 */

	public void testKursreiheOhneCSV() {
		
		GregorianCalendar beginn = new GregorianCalendar(2000,0,2);
		GregorianCalendar ende = new GregorianCalendar(2018,0,2);
		
		// simuliert den Handel 
		Depot depot = new Depot("Oskars", 100000f);
		depot.handleAlleSignale("dax");
		float wert = depot.bewerteDepot(beginn);
		assertTrue(wert > 0);
		log.info("Depotwert: " + wert + " Zeitpunkt: " + Util.formatDate(beginn));
		
		// tägliche Depot-Bewertung als Kursreihe
		Kursreihe depotKR = depot.bewerteDepotTaeglich(beginn, ende);
		assertNotNull(depotKR);
		assertTrue(depotKR.kurse.size()>10);
		Statistik.rechneIndikatoren(depotKR);
		depotKR.writeFileIndikatoren();
		
	}

/*
	public void testKursreiheMitCSV() {
		// Kursreihe erzeugen 

		kursreihe = Aktien.getInstance().getKursreihe("appl");
		assertNotNull(kursreihe);
		assertTrue(kursreihe.kurse.size() > 1);
		log.info("Kursreihe hat Kurse: " + kursreihe.kurse.size());
		
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
