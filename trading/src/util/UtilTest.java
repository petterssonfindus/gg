package util;

import junit.framework.TestCase;

public class UtilTest extends TestCase {
	
	public void testFloatString () {
		float test = 17.834f;
		System.out.println("Utiltest: " + Util.toString(test));
	}

}
