package signal;

import kurs.Kursreihe;
import kurs.Tageskurs;

/**
 * identifiziert Kauf/Verkaufssignale in einer Kursreihe
 * und schreibt sie in die Kursreihe
 * @author oskar
 *
 */
public class Signalsuche {

	// Schwelle, ab der Berge und Täler berücksichtigt werden. 
	private static final float SCHWELLETALSUMME = 0.05f;
	private static final float SCHWELLEBERGSUMME = 0.05f;
	private static final float SCHWELLEBERGSTEIGT = 0.01f;
	private static final float SCHWELLETALFAELLT = -0.01f;
	private static final float SCHWELLEBERGFAELLT = -0.01f;
	private static final float SCHWELLETALSTEIGT = 0.01f;
	// wie weit muss der Tageskurs den Gleitenden Durchschnitt durchbrechen
	private static final float SCHWELLEGDDURCHBRUCH = 0.01f;
	// Kursdifferenz in Prozentpunkte
	private static final float FAKTORSTAERKEBERGTAL = 0.01f;
	
	/**
	 * steuert die Berechnung aller Signale auf Basis einer Zeitreihe
	 * Die Signaltypen können auch separat beauftragt werden. 
	 * @param kursreihe
	 */
	public static void rechneSignale (Kursreihe kursreihe) {
		Signalsuche.steigendeBergeFallendeTaeler(kursreihe);
		Signalsuche.gleitenderDurchschnittDurchbruch(kursreihe);
	}
	
	/**
	 * erzeugt Kaufsignal, wenn Berge ansteigen
	 * Verkaufsignal, wenn Täler fallen
	 * @param kursreihe
	 */
	public static void steigendeBergeFallendeTaeler (Kursreihe kursreihe) {
		Tageskurs tageskurs;
		float staerke; 
		for (int i = 0 ; i < kursreihe.kurse.size(); i++) {
			tageskurs = kursreihe.kurse.get(i);
			// prüfe, ob Berg vorhanden
			if (istBerg(tageskurs)) {
				// prüfe, ob Kurs ansteigt - Delta ist positiv
				float kursdelta = (tageskurs.getKurs() - tageskurs.letzterBergkurs)/tageskurs.getKurs();
				System.out.println("Berg: Kursdelta: " + kursdelta + " " + tageskurs.getKurs() + " " + tageskurs.letzterBergkurs);
				if (kursdelta > SCHWELLEBERGSTEIGT) {
					staerke = (kursdelta / FAKTORSTAERKEBERGTAL);
					Signal.create(tageskurs, Signal.KAUF, Signal.SteigenderBerg, staerke);
				}
				else if (kursdelta < SCHWELLEBERGFAELLT) {
					staerke = (kursdelta / FAKTORSTAERKEBERGTAL);
					Signal.create(tageskurs, Signal.VERKAUF, Signal.FallenderBerg, staerke);
				}
			}
			// prüfe, ob Tal vorhanden
			if (istTal(tageskurs)) {
				// prüfe, ob Kurs ansteigt
				float kursdelta = (tageskurs.getKurs() - tageskurs.letzterTalkurs)/tageskurs.getKurs();
				System.out.println("Tal: Kursdelta: " + kursdelta + " " + tageskurs.getKurs() + " " + tageskurs.letzterTalkurs);
				if (kursdelta < SCHWELLETALFAELLT) {
					staerke = (kursdelta / FAKTORSTAERKEBERGTAL);
					Signal.create(tageskurs, Signal.VERKAUF, Signal.FallendesTal, staerke);
				}
				else if (kursdelta > SCHWELLETALSTEIGT) {
					staerke = (kursdelta / FAKTORSTAERKEBERGTAL);
					Signal.create(tageskurs, Signal.KAUF, Signal.SteigendesTal, staerke);
				}
			}
			
		}
		
	}

	/**
	 * prüft, ob der Tageskurs ein Berg ist 
	 * @param tageskurs
	 * @return
	 */
	private static boolean istBerg (Tageskurs tageskurs) {
		if (tageskurs.bergSumme > SCHWELLEBERGSUMME) {
			return true;
		}
		else return false; 
	}
	
	private static boolean istTal (Tageskurs tageskurs) {
		if (tageskurs.talSumme > SCHWELLETALSUMME) {
			return true;
		}
		else return false; 
	}

	/**
	 * erzeugt ein Signal, wenn der Tageskurs den GD schneidet
	 * Stärke ist maximal, wenn alle 3 GDs über/unter dem Tageskurs sind 
	 * @param kursreihe
	 */
	public static void gleitenderDurchschnittDurchbruch (Kursreihe kursreihe) {
		Signal signal; 
		float staerke;
		for (int i = 1 ; i < kursreihe.kurse.size() ; i++) {
			Tageskurs tageskurs = kursreihe.kurse.get(i);
			Tageskurs vortageskurs = kursreihe.kurse.get(i-1);
			float kurs = kursreihe.kurse.get(i).getKurs();
			float kursm1 = kursreihe.kurse.get(i-1).getKurs();
			// bisher darunter, jetzt darüber
			signal = pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 10);
			signal = pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 30);
			signal = pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 100);
			// bisher darüber, jetzt darunter
			signal = pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 10);
			signal = pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 30);
			signal = pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 100);
		}
	}
	/**
	 * bisher darunter, jetzt darüber
	 */
	private static Signal pruefeGleitenderDurchschnittSteigung (Tageskurs tageskurs, Tageskurs vortageskurs, int x ) {
		Float gd = tageskurs.getGleitenderDurchschnitt(x);
		Float gdvt = vortageskurs.getGleitenderDurchschnitt(x);
		Signal signal = null; 
		
		if ((vortageskurs.getKurs() < gdvt + SCHWELLEGDDURCHBRUCH) && 
				tageskurs.getKurs() > (gd + SCHWELLEGDDURCHBRUCH)) {
			signal = Signal.create(tageskurs, Signal.KAUF, Signal.GD10Durchbruch, 0);
			signal.staerke = berechneGDSignalStaerke(tageskurs, Signal.KAUF);
		} 
		return signal; 
	}
	/**
	 * bisher darüber, jetzt darunter
	 */
	private static Signal pruefeGleitenderDurchschnittSinkflug (Tageskurs tageskurs, Tageskurs vortageskurs, int x ) {
		Float gd = tageskurs.getGleitenderDurchschnitt(x);
		Float gdvt = vortageskurs.getGleitenderDurchschnitt(x);
		Signal signal = null; 
		
		if ((vortageskurs.getKurs() > gdvt - SCHWELLEGDDURCHBRUCH) && 
				tageskurs.getKurs() < (gd - SCHWELLEGDDURCHBRUCH)) {
			signal = Signal.create(tageskurs, Signal.VERKAUF, Signal.GD10Durchbruch, 0);
			signal.staerke = berechneGDSignalStaerke(tageskurs, Signal.VERKAUF);
		} 
		return signal; 
	}
	/**
	 * ermittelt, wie viele GDs über oder unter dem Tageskurs liegen. 
	 * Beim Kauf sind die darunterliegenden GDs relevant, beim Verkauf anders rum. 
	 * Jeder GD wird mit 0,33 gewichtet. Alle 3 ergibt 1,0
	 * @param signal wird ergänzt um die Stärke 
	 * @return
	 */
	private static float berechneGDSignalStaerke (Tageskurs tageskurs, byte kaufVerkauf) {
		float result = 0;
		float kurs = tageskurs.getKurs();
		if (kaufVerkauf == Signal.KAUF) {
			if (kurs > tageskurs.gleitenderDurchschnitt10 + SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs > tageskurs.gleitenderDurchschnitt30 + SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs > tageskurs.gleitenderDurchschnitt100 + SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
		}
		else {
			if (kurs < tageskurs.gleitenderDurchschnitt10 - SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs < tageskurs.gleitenderDurchschnitt30 - SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs < tageskurs.gleitenderDurchschnitt100 - SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
		}
		return result / 3; 
	}

}
