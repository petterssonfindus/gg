package depot;

import signal.Signalsuche;
import util.Util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktien;
import kurs.Aktie;
import kurs.Statistik;

public class DepotTestAPPL1J extends TestCase {
	private static final Logger log = LogManager.getLogger(DepotTestDAX18J.class);

	Aktie kursreihe; 

	private static GregorianCalendar beginn = new GregorianCalendar(2017,6,2);
	private static GregorianCalendar ende = new GregorianCalendar(2018,0,2);
	private static Depot depot; 
	
	/** holt eine Kursreihe, 
	 * rechnet Indikatoren und Signale
	 * legt ein Depot 
	 * Führt eine Simulation durch 
	 */
	@Override
	protected void setUp() throws Exception {
		kursreihe = Aktien.getInstance().getAktie("appl");
		// berechnet die Indikatoren und Signale 
		Statistik.rechneIndikatoren(kursreihe);
		Signalsuche.rechneSignale(kursreihe);
		depot = new Depot("Oskars", 10000f);
		depot.handleAlleSignale("dax", beginn, ende);
	}

	/**
	 * erstellt eine Kursreihe
	 * berechnet die Indikatoren und Signale
	 * simuliert einen Handel 
	 */

	public void testDepotbewertungStichtag() {
		// simuliert den Handel 
		beginn.add(Calendar.MONTH, 1);
		float wert = depot.bewerteDepot(beginn);
		assertTrue(wert > 0);
		log.info("Depotwert: " + wert + " Zeitpunkt: " + Util.formatDate(beginn));
		
	}
	
	public void testTaeglicheDepotbewertung() {
		// tägliche Depot-Bewertung als Kursreihe
		Aktie depotAktie = depot.bewerteDepotTaeglich(beginn, ende);
		assertNotNull(depotAktie);
		assertTrue(depotAktie.getKursreihe().size()>10);
		Statistik.rechneIndikatoren(depotAktie);
		depotAktie.writeFileIndikatoren();
		
	}

	
}
