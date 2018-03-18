package signal;

import java.util.ArrayList;

import depot.Order;
import kurs.Aktie;
import kurs.Kurs;

public class SteigendeBergeFallendeTaeler implements SignalAlgorithmus {

	private static final float SCHWELLEBERGSUMME = 0.05f;
	private static final float SCHWELLEBERGSTEIGT = 0.01f;
	private static final float SCHWELLETALFAELLT = -0.01f;
	private static final float SCHWELLEBERGFAELLT = -0.01f;
	private static final float SCHWELLETALSTEIGT = 0.01f;
	// Schwelle, ab der Berge und Täler berücksichtigt werden. 
	private static final float SCHWELLETALSUMME = 0.05f;
	// Kursdifferenz in Prozentpunkte
	private static final float FAKTORSTAERKEBERGTAL = 0.01f;

	/**
	 * erzeugt Kaufsignal, wenn Berge ansteigen
	 * Verkaufsignal, wenn Täler fallen
	 * @param kursreihe
	 */
	@Override
	public int ermittleSignal(Aktie aktie, SignalBeschreibung signalbeschreibung) {
		int anzahl = 0;
		ArrayList<Kurs> alleBerge = new ArrayList<Kurs>();
		for (Kurs kurs : aktie.getBoersenkurse()) {
			float staerke; 
			// prüfe, ob Berg vorhanden
			if (istBerg(kurs)) {
				alleBerge.add(kurs);
				// prüfe, ob Kurs zum letzten Berg ansteigt - Delta ist positiv 
				if (alleBerge.size() > 1) {
					float kursdelta = (kurs.getKurs() - alleBerge.get(alleBerge.size() - 2).getKurs()) / kurs.getKurs();
					if (kursdelta > SteigendeBergeFallendeTaeler.SCHWELLEBERGSTEIGT) {
						staerke = (kursdelta / SteigendeBergeFallendeTaeler.FAKTORSTAERKEBERGTAL);
						Signal.create(kurs, Order.KAUF, Signal.SteigenderBerg, staerke);
						anzahl++;
					}
					else if (kursdelta < SteigendeBergeFallendeTaeler.SCHWELLEBERGFAELLT) {
						staerke = (kursdelta / SteigendeBergeFallendeTaeler.FAKTORSTAERKEBERGTAL);
						Signal.create(kurs, Order.VERKAUF, Signal.FallenderBerg, staerke);
						anzahl++;
					}
				}
			}
		}
		return anzahl; 
	}
	
	/**
	 * prüft, ob der Tageskurs ein Berg ist 
	 * @param tageskurs
	 * @return
	 */
	static boolean istBerg (Kurs tageskurs) {
		if (tageskurs.berg > SteigendeBergeFallendeTaeler.SCHWELLEBERGSUMME) {
			return true;
		}
		else return false; 
	}

	static boolean istTal (Kurs tageskurs) {
		if (tageskurs.tal > SteigendeBergeFallendeTaeler.SCHWELLETALSUMME) {
			return true;
		}
		else return false; 
	}
}
