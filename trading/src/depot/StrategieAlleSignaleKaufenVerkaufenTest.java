package depot;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktie;
import kurs.Aktien;

public class StrategieAlleSignaleKaufenVerkaufenTest extends TestCase {
	
	private static final Logger log = LogManager.getLogger(StrategieAlleSignaleKaufenVerkaufenTest.class);
	
	Aktie aktie; 
	
	private static final GregorianCalendar beginn = new GregorianCalendar(2017,10,2);
	private static final GregorianCalendar ende = new GregorianCalendar(2018,0,2);
	
	/** 
	 * Vorbereitung: Rechnet Indikatoren und Signale
	 */
	@Override
	protected void setUp() throws Exception {
		aktie = Aktien.getInstance().getAktie("appl");
		// berechnet die Indikatoren und Signale 
		aktie.rechneIndikatoren();
		aktie.rechneSignale();
	}
	
	public void testSimuliereDepotstrategie() {
		Depot depot = new Depot("Oskars", 10000f);
		DepotStrategie kaufVerkaufStrategie = new StrategieAlleSignaleKaufenVerkaufen();
		depot.simuliereDepotstrategie(kaufVerkaufStrategie, aktie.name, beginn, ende);
 		depot.writeOrders();
		Aktie depotAktie = depot.bewerteDepotTaeglich(beginn, ende);
		depotAktie.writeFileIndikatoren();
	}


}
