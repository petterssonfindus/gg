
import data.DBManagerTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import kurs.AktienTest;
import kurs.AktieTest;
import signal.SignalsucheTest;
import util.UtilTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(UtilTest.class);
		suite.addTestSuite(DBManagerTest.class);
		suite.addTestSuite(AktienTest.class);
		suite.addTestSuite(AktieTest.class);
		suite.addTestSuite(SignalsucheTest.class);
		//$JUnit-END$
		return suite;
	}

}
