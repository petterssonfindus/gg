package signal;

import depot.Order;
import kurs.Aktie;
import kurs.Kurs;

public class SteigendeBergeFallendeTaeler implements SignalAlgorythmus {

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
	public void ermittleSignal(Kurs tageskurs, Aktie aktie) {

		float staerke; 
		// prüfe, ob Berg vorhanden
		if (istBerg(tageskurs)) {
			// prüfe, ob Kurs ansteigt - Delta ist positiv
			float kursdelta = (tageskurs.getKurs() - tageskurs.letzterBergkurs)/tageskurs.getKurs();
			Signalsuche.log.info("Berg: Kursdelta: " + kursdelta + " " + tageskurs.getKurs() + " " + tageskurs.letzterBergkurs);
			if (kursdelta > SteigendeBergeFallendeTaeler.SCHWELLEBERGSTEIGT) {
				staerke = (kursdelta / SteigendeBergeFallendeTaeler.FAKTORSTAERKEBERGTAL);
				Signal.create(tageskurs, Order.KAUF, Signal.SteigenderBerg, staerke);
			}
			else if (kursdelta < SteigendeBergeFallendeTaeler.SCHWELLEBERGFAELLT) {
				staerke = (kursdelta / SteigendeBergeFallendeTaeler.FAKTORSTAERKEBERGTAL);
				Signal.create(tageskurs, Order.VERKAUF, Signal.FallenderBerg, staerke);
			}
		}
		// prüfe, ob Tal vorhanden
		if (istTal(tageskurs)) {
			// prüfe, ob Kurs ansteigt
			float kursdelta = (tageskurs.getKurs() - tageskurs.letzterTalkurs)/tageskurs.getKurs();
			Signalsuche.log.info("Tal: Kursdelta: " + kursdelta + " " + tageskurs.getKurs() + " " + tageskurs.letzterTalkurs);
			if (kursdelta < SteigendeBergeFallendeTaeler.SCHWELLETALFAELLT) {
				staerke = (kursdelta / SteigendeBergeFallendeTaeler.FAKTORSTAERKEBERGTAL);
				Signal.create(tageskurs, Order.VERKAUF, Signal.FallendesTal, staerke);
			}
			else if (kursdelta > SteigendeBergeFallendeTaeler.SCHWELLETALSTEIGT) {
				staerke = (kursdelta / SteigendeBergeFallendeTaeler.FAKTORSTAERKEBERGTAL);
				Signal.create(tageskurs, Order.KAUF, Signal.SteigendesTal, staerke);
			}
		}
	}
	
	/**
	 * prüft, ob der Tageskurs ein Berg ist 
	 * @param tageskurs
	 * @return
	 */
	static boolean istBerg (Kurs tageskurs) {
		if (tageskurs.bergSumme > SteigendeBergeFallendeTaeler.SCHWELLEBERGSUMME) {
			return true;
		}
		else return false; 
	}

	static boolean istTal (Kurs tageskurs) {
		if (tageskurs.talSumme > SteigendeBergeFallendeTaeler.SCHWELLETALSUMME) {
			return true;
		}
		else return false; 
	}
}
