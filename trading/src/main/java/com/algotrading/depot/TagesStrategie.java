package depot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Parameter;
import util.Util;

/**
 * Eine Strategie, die t�glich anhand der situation im Depot entscheidet, ob gehandelt wird
 * @author oskar
 *
 */
public abstract class TagesStrategie extends Parameter {
	private static final Logger log = LogManager.getLogger(Util.class);

	
	/**
	 * T�glich wird gepr�ft, ob gehandelt wird. 
	 * @param depot
	 * @return
	 */
	public abstract Order entscheideTaeglich (Depot depot);
	
}
