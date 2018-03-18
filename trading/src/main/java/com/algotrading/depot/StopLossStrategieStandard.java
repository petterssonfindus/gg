package depot;

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
	public void entscheideStopLoss(Depot depot) {
		// holt den aktuellen Wertpapierbestand 
		HashMap<String, Wertpapierbestand> bestand = depot.wertpapierbestand;
		// geht durch alle Wertpapiere durch und prüft die SL-Strategie
		if (bestand != null && bestand.keySet() != null && bestand.keySet().size() > 0) {
			for (Wertpapierbestand wertpapierbestand : bestand.values()) {
				// holt die Aktie 
				Aktie aktie = Aktien.getInstance().getAktie(wertpapierbestand.wertpapier);
				// wenn der aktuelle Kurs unter den Durchschnittskurs sinkt 
				if ((1.01 * aktie.getTageskurs(depot.beginn).getKurs()) < wertpapierbestand.durchschnittskurs) {
					depot.verkaufeWertpapier(aktie.name);
				}
				
			}
		}

	}

}
