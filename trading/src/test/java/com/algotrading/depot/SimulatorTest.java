package depot;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import kurs.Aktie;
import kurs.Aktien;
import util.Util;

public class SimulatorTest extends TestCase {
	private static final Logger log = LogManager.getLogger(Util.class);

	protected void setUp() throws Exception {

	}
	
	public void testSimuliereDepots () {
		GregorianCalendar beginn = new GregorianCalendar(2000,0,1);
		GregorianCalendar ende = new GregorianCalendar(2013,0,1);
		ArrayList<Aktie> aktien = new ArrayList<Aktie>();
		aktien.add(Aktien.getInstance().getAktie("xxxgdaxi"));
		aktien.add(Aktien.getInstance().getAktie("aa"));
		Simulator.simuliereDepots(aktien, beginn, ende, 2000, 360);
		
	}
	

}
