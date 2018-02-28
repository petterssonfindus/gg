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
	 * Stärke ist maximal, wenn alle 3 GDs über/unter dem Tageskurs sind 
	 * @param kursreihe
	 */
	public void ermittleSignal(Kurs tageskurs, Aktie aktie) {
		Kurs vortageskurs = aktie.getVortageskurs(tageskurs);
		// bisher darunter, jetzt darüber
		// dabei werden die Signale erstellt und mit dem Tageskurs verbunden 
		GDDurchbruch.pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 10);
		GDDurchbruch.pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 30);
		GDDurchbruch.pruefeGleitenderDurchschnittSteigung(tageskurs, vortageskurs, 100);
		// bisher darüber, jetzt darunter
		GDDurchbruch.pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 10);
		GDDurchbruch.pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 30);
		GDDurchbruch.pruefeGleitenderDurchschnittSinkflug(tageskurs, vortageskurs, 100);
	}

	/**
	 * bisher darunter, jetzt darüber
	 * erzeugt Signale und hängt sie an den Kurs an
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
	 * bisher darüber, jetzt darunter
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
	 * ermittelt, wie viele GDs über oder unter dem Tageskurs liegen. 
	 * Beim Kauf sind die darunterliegenden GDs relevant, beim Verkauf anders rum. 
	 * Jeder GD wird mit 0,33 gewichtet. Alle 3 ergibt 1,0
	 * @param signal wird ergänzt um die Stärke 
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
