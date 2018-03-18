package depot;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;
import util.Util;
import util.Zeitraum;

public class TestErmittleZeitraum extends TestCase {
	private static final Logger log = LogManager.getLogger(Util.class);

	public void testErmittleZeitraumn() {
		GregorianCalendar beginn = new GregorianCalendar(2017,0,1);
		GregorianCalendar ende = new GregorianCalendar(2018,0,1);

		ArrayList<Zeitraum> zeitraeume = Simulator.ermittleZeitraum(beginn, ende, 180, 30);
		assertEquals(zeitraeume.size(), 7);
		assertEquals(Util.formatDate(zeitraeume.get(0).ende), "2017-06-30");
		
		for (Zeitraum zeitraum : zeitraeume) {
			log.info(zeitraum.toString());
		}
		
	}

}
