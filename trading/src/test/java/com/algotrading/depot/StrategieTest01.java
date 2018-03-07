package depot;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktie;
import kurs.Aktien;

public class StrategieTest01 extends TestCase {
	
	private static final Logger log = LogManager.getLogger(StrategieTest01.class);
	
	Aktie aktie; 
	
	private static final GregorianCalendar beginn = new GregorianCalendar(2017,10,2);
	private static final GregorianCalendar ende = new GregorianCalendar(2018,0,2);
	
	/** 
	 * Vorbereitung: Rechnet Indikatoren und Signale
	 */
	@Override
	protected void setUp() throws Exception {
		aktie = Aktien.getInstance().getAktie("xxxgdaxi");
		// berechnet die Indikatoren und Signale 
		aktie.rechneIndikatoren();
		aktie.writeFileIndikatoren();
		aktie.rechneSignale();
		aktie.writeFileSignale();
	}
	
	public void testSimuliereDepotstrategie() {
		Depot depot = new Depot("Oskars", 10000f);
		DepotStrategie kaufVerkaufStrategie = new StrategieAlleSignaleKaufenVerkaufen();
		StopLossStrategie slStrategie = new StopLossStrategieStandard();
		
		depot.simuliereDepot(kaufVerkaufStrategie, slStrategie, aktie.name, beginn, ende);
 		depot.writeOrders();
		Aktie depotAktie = depot.bewerteDepotNachtraeglich(beginn, ende);
		depotAktie.writeFileIndikatoren();
		
	}


}
