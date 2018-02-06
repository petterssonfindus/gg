package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.TestCase;

public class UtilTest extends TestCase {
	private static final Logger log = LogManager.getLogger(UtilTest.class);

	public void testFloatString () {
		float test = 17.834f;
		log.debug("Utiltest: " + Util.toString(test));
	}

}
