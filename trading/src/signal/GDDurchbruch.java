package signal;

import depot.Order;
import kurs.Aktie;
import kurs.Kurs;

public class GDDurchbruch implements SignalAlgorythmus {
	
	// wie weit muss der Tageskurs den Gleitenden Durchschnitt durchbrechen
	private static final float SCHWELLEGDDURCHBRUCH = 0.01f;

	@Override
	/**
	 * erzeugt ein Signal, wenn der Tageskurs den GD schneidet
	 * St�rke ist maximal, wenn alle 3 GDs �ber/unter dem Tageskurs sind 
	 * @param kursreihe
	 */
	public void ermittleSignal(Kurs tageskurs, Aktie aktie) {
		Kurs vortageskurs = aktie.getVortageskurs(tageskurs);
		// bisher darunter, jetzt dar�ber
		// dabei werden die Signale erstellt und mit dem Tageskurs verbunden 
		GDDurchbruch.pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 10);
		GDDurchbruch.pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 30);
		GDDurchbruch.pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 100);
		// bisher dar�ber, jetzt darunter
		GDDurchbruch.pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 10);
		GDDurchbruch.pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 30);
		GDDurchbruch.pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 100);
	}

	/**
	 * bisher darunter, jetzt dar�ber
	 * erzeugt Signale und h�ngt sie an den Kurs an
	 */
	private static void pruefeGleitenderDurchschnittSteigung (Kurs tageskurs, Kurs vortageskurs, int x ) {
		Float gd = tageskurs.getGleitenderDurchschnitt(x);
		Float gdvt = vortageskurs.getGleitenderDurchschnitt(x);
		Signal signal = null; 
		
		if ((vortageskurs.getKurs() < gdvt + GDDurchbruch.SCHWELLEGDDURCHBRUCH) && 
				tageskurs.getKurs() > (gd + GDDurchbruch.SCHWELLEGDDURCHBRUCH)) {
			signal = Signal.create(tageskurs, Order.KAUF, Signal.GD10Durchbruch, 0);
			signal.staerke = GDDurchbruch.berechneGDSignalStaerke(tageskurs, Order.KAUF);
		} 
	}

	/**
	 * bisher dar�ber, jetzt darunter
	 */
	private static void pruefeGleitenderDurchschnittSinkflug (Kurs tageskurs, Kurs vortageskurs, int x ) {
		Float gd = tageskurs.getGleitenderDurchschnitt(x);
		Float gdvt = vortageskurs.getGleitenderDurchschnitt(x);
		Signal signal = null; 
		
		if ((vortageskurs.getKurs() > gdvt - GDDurchbruch.SCHWELLEGDDURCHBRUCH) && 
				tageskurs.getKurs() < (gd - GDDurchbruch.SCHWELLEGDDURCHBRUCH)) {
			signal = Signal.create(tageskurs, Order.VERKAUF, Signal.GD10Durchbruch, 0);
			signal.staerke = GDDurchbruch.berechneGDSignalStaerke(tageskurs, Order.VERKAUF);
		} 
	}

	/**
	 * ermittelt, wie viele GDs �ber oder unter dem Tageskurs liegen. 
	 * Beim Kauf sind die darunterliegenden GDs relevant, beim Verkauf anders rum. 
	 * Jeder GD wird mit 0,33 gewichtet. Alle 3 ergibt 1,0
	 * @param signal wird erg�nzt um die St�rke 
	 * @return
	 */
	private static float berechneGDSignalStaerke (Kurs tageskurs, byte kaufVerkauf) {
		float result = 0;
		float kurs = tageskurs.getKurs();
		if (kaufVerkauf == Order.KAUF) {
			if (kurs > tageskurs.gleitenderDurchschnitt10 + GDDurchbruch.SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs > tageskurs.gleitenderDurchschnitt30 + GDDurchbruch.SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs > tageskurs.gleitenderDurchschnitt100 + GDDurchbruch.SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
		}
		else {
			if (kurs < tageskurs.gleitenderDurchschnitt10 - GDDurchbruch.SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs < tageskurs.gleitenderDurchschnitt30 - GDDurchbruch.SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
			if (kurs < tageskurs.gleitenderDurchschnitt100 - GDDurchbruch.SCHWELLEGDDURCHBRUCH) {
				result += 1;
			}
		}
		return result / 3; 
	}


}
