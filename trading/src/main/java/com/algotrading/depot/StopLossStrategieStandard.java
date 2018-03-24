package depot;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktie;
import kurs.Aktien;
import util.Util;

/**
 * Implementiert eine StopLoss-Strategie im Standardfall
 * @author oskar
 *
 */
public class StopLossStrategieStandard extends TagesStrategie {
	private static final Logger log = LogManager.getLogger(StopLossStrategieStandard.class);

	@Override
	public Order entscheideTaeglich (Depot depot) {
		Order order = null; 
		float verlust = (float) this.getParameter("verlust");
		// holt die aktuell laufenden Trades
		ConcurrentHashMap<String, Trade> trades = depot.aktuelleTrades;
		// gibt es aktuell laufende Trades ?
		if (trades != null && trades.keySet() != null && trades.keySet().size() > 0) {
			synchronized (trades) {
				for (String wertpapier : trades.keySet()) {
					Trade trade = trades.get(wertpapier);
	//			for (Trade trade : trades.values()) {
					Aktie aktie = Aktien.getInstance().getAktie(trade.wertpapier);
					float aktuellerKurs = aktie.getTageskurs(depot.heute).getKurs();
					float einstandsKurs = trade.investiertesKapital / trade.bestand;
					if (einstandsKurs == 0) {
						log.trace("Einstandskurs = 0");
					}
					// wenn der aktuelle Kurs unter den durschnittl. Einstandskurs fällt, wird verkauft 
					float grenze = einstandsKurs * (1 - verlust);
					log.trace(Util.formatDate(depot.heute) + "StopLoss Grenze: " + grenze + " aktKurs: " + aktuellerKurs);
					if (aktuellerKurs < grenze) {
						log.debug("StopLoss verkauft: " + verlust + "% " + grenze + " -aktuell: " + einstandsKurs);
						order = depot.verkaufe(aktie);
					}
				}
			}
		}
		return order; 
	}

}
