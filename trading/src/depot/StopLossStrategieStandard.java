package depot;

import java.util.GregorianCalendar;
import java.util.HashMap;

import kurs.Aktie;
import kurs.Aktien;

/**
 * Implementiert eine StopLoss-Strategie im Standardfall
 * @author oskar
 *
 */
public class StopLossStrategieStandard implements StopLossStrategie {

	@Override
	public void entscheideStopLoss(Depot depot, GregorianCalendar stichtag) {
		// holt den Aktienbestand
		HashMap<String, Order> bestand = depot.ermittleDepotBestand(stichtag);
		// geht durch alle Wertpapiere durch und prüft die SL-Strategie
		if (bestand != null || bestand.size() > 0) {
			for (Order order : bestand.values()) {
				// holt die Aktie 
				Aktie aktie = Aktien.getInstance().getAktie(order.wertpapier);
				// wenn der aktuelle Kurs unter den Durchschnittskurs sinkt 
				if ((1.01 * aktie.getTageskurs(stichtag).getKurs()) < order.durchschnEinkaufskurs) {
					depot.verkaufeWertpapier(stichtag, aktie.name);
				}
				
			}
		}

	}

}
