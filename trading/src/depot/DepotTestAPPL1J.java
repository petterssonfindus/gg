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

	Aktie aktie; 

	private static final GregorianCalendar beginn = new GregorianCalendar(2017,10,2);
	private static final GregorianCalendar ende = new GregorianCalendar(2018,0,2);
	private static Depot depot; 
	
	/** holt eine Kursreihe, 
	 * rechnet Indikatoren und Signale
	 * legt ein Depot 
	 * Führt eine Simulation durch 
	 */
	@Override
	protected void setUp() throws Exception {
		aktie = Aktien.getInstance().getAktie("appl");
		// berechnet die Indikatoren und Signale 
		Statistik.rechneIndikatoren(aktie);
		Signalsuche.rechneSignale(aktie);
		depot = new Depot("Oskars", 10000f);
		depot.handleAlleSignale(aktie.name, beginn, ende);
	}
	/**
	 * erstellt eine Kursreihe
	 * berechnet die Indikatoren und Signale
	 * simuliert einen Handel 
	 */
	/*
	public void testDepotbewertungStichtag() {
		// simuliert den Handel 
		beginn.add(Calendar.MONTH, 1);
		float wert = depot.bewerteDepot(beginn);
		assertTrue(wert > 0);
		log.info("Depotwert: " + wert + " Zeitpunkt: " + Util.formatDate(beginn));
		
	}
*/
	
	public void testTaeglicheDepotbewertung() {
 		depot.writeOrders();
		// tägliche Depot-Bewertung als Kursreihe
		Aktie depotAktie = depot.bewerteDepotTaeglich(beginn, ende);
		assertNotNull(depotAktie);
		assertNotNull(depotAktie.getKurse());
		assertTrue(depotAktie.getKurse().size()>10);
//		Statistik.rechneIndikatoren(depotAktie);
		depotAktie.writeFileIndikatoren();
	}

	
}
