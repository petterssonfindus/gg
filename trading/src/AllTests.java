
import data.DBManagerTest;
import depot.DepotTestDAX18J;
import junit.framework.Test;
import junit.framework.TestSuite;
import kurs.AktienTest;
import kurs.KursreiheTest;
import signal.SignalsucheTest;
import util.UtilTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(UtilTest.class);
		suite.addTestSuite(DBManagerTest.class);
		suite.addTestSuite(AktienTest.class);
		suite.addTestSuite(KursreiheTest.class);
		suite.addTestSuite(SignalsucheTest.class);
		suite.addTestSuite(DepotTestDAX18J.class);
		//$JUnit-END$
		return suite;
	}

}
