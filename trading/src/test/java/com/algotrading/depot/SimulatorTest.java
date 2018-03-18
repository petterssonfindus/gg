package depot;

import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import util.Util;

public class SimulatorTest extends TestCase {
	private static final Logger log = LogManager.getLogger(Util.class);

	protected void setUp() throws Exception {

	}
	
	public void testSimuliereDepots () {
		GregorianCalendar beginn = new GregorianCalendar(2010,0,1);
		GregorianCalendar ende = new GregorianCalendar(2013,0,1);
		Simulator.simuliereDepots("xxxgdaxi", beginn, ende, 360, 360);
		
	}
	

}
