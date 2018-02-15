package depot;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktie;
import kurs.Aktien;
import kurs.Statistik;
import signal.Signalsuche;

public class TestSimuliereDepotstrategie extends TestCase {
	
	private static final Logger log = LogManager.getLogger(DepotTestDAX18J.class);
	
	Aktie aktie; 
	
	private static final GregorianCalendar beginn = new GregorianCalendar(2017,10,2);
	private static final GregorianCalendar ende = new GregorianCalendar(2018,0,2);
	private static Depot depot; 
	
	/** holt eine Kursreihe, 
	 * rechnet Indikatoren und Signale
	 * legt ein Depot an 
	 * Führt eine Simulation durch 
	 */
	@Override
	protected void setUp() throws Exception {
		aktie = Aktien.getInstance().getAktie("appl");
		// berechnet die Indikatoren und Signale 
		Statistik.rechneIndikatoren(aktie);
		Signalsuche.rechneSignale(aktie);
		depot = new Depot("Oskars", 10000f);
	}
	
	public void testSimuliereDepotstrategie() {
		Kaufstrategie kaufstrategie = new StrategieAllesKaufen();
		depot.simuliereDepotstrategie(kaufstrategie, aktie.name, beginn, ende);
		
	}

}
