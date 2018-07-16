package signal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aktie.Aktie;
import aktie.Kurs;
import depot.Order;
import indikator.Indikator;
import util.Util;
import util.Zeitraum;

public class GDSchnitt implements SignalAlgorithmus {
	static final Logger log = LogManager.getLogger(GDSchnitt.class);

	// wie weit muss ein GD den anderen durchbrechen
	private static final float SCHWELLEGDDURCHBRUCH = 0.00f;

	@Override
	/**
	 * erzeugt ein Signal, wenn der Tageskurs den GD schneidet 
	 * Stärke ist maximal, wenn alle 3 GDs über/unter dem Tageskurs sind 
	 * @param kursreihe
	 */
	public int ermittleSignal(Aktie aktie, SignalBeschreibung signalbeschreibung) {
		if (aktie == null) log.error("Inputparameter Aktie ist null");
		if (signalbeschreibung == null) log.error("Inputparameter Signalbeschreibung ist null");
		int anzahl = 0;
		Indikator gd1 = (Indikator) signalbeschreibung.getParameter("gd1");
		Indikator gd2 = (Indikator) signalbeschreibung.getParameter("gd2");
		float schwelledurchbruch = (float) signalbeschreibung.getParameter("schwelledurchbruch");
		if (gd1 == null) log.error("Signal enthaelt keinen Indikator1");
		if (gd2 == null) log.error("Signal enthaelt keinen Indikator2");
		Zeitraum zeitraum = (Zeitraum) signalbeschreibung.getParameter("zeitraum");
		
		for (Kurs kurs : aktie.getKurse(zeitraum)) {
			Kurs vortageskurs = aktie.getVortageskurs(kurs);
			if (vortageskurs != null) {
				// bisher darunter, jetzt darüber
				// dabei werden die Signale erstellt und mit dem Tageskurs verbunden 
				if (GDSchnitt.pruefeGDSchnittSteigung(kurs, vortageskurs, gd1, gd2)) anzahl++;
			}
		}
		return anzahl; 
	}

	/**
	 * bisher darunter, jetzt darüber
	 * erzeugt Signale und hängt sie an den Kurs an
	 */
	private static boolean pruefeGDSchnittSteigung (Kurs tageskurs, Kurs vortageskurs, Indikator gd1, Indikator gd2) {
		if (tageskurs == null || vortageskurs == null || gd1 == null || gd2 == null) log.error("Inputvariable ist null"); 
		boolean result = false; 
		float kursAktuell = tageskurs.getKurs();
		float gd1Wert = (float) tageskurs.getIndikatorWert(gd1);
		float gd2Wert = (float) tageskurs.getIndikatorWert(gd2);
		float gd1WertVT = (float) vortageskurs.getIndikatorWert(gd1);
		float gd2WertVT = (float) vortageskurs.getIndikatorWert(gd2);
		log.trace("GD-Schnitt Steigung" + Util.separator + Util.formatDate(tageskurs.datum) + Util.separator + 
				"Kurs" + Util.separator + kursAktuell + Util.separator + 
				"GD1VT" + Util.separator + gd1WertVT +  Util.separator + 
				"GD2VT" + Util.separator + gd2WertVT + Util.separator + 
				"GD1" + Util.separator + gd1Wert + Util.separator + 
				"GD2" + Util.separator + gd2Wert );
		Signal signal = null; 
		// wenn am Vortag der Kurs GD1 unter GD2 war, und heute GD1 über GD2 ist 
		if ((gd1WertVT < gd2WertVT ) && (gd1Wert > gd2Wert)) {
			signal = Signal.create(tageskurs, Order.KAUF, Signal.GDSchnitt, 0);
			result = true; 
			// die Stärke bestimmt sich nach dem aktuellen Kurs 
			signal.staerke = (gd2Wert - gd1Wert) / gd2Wert;
			log.debug("GD-Schnitt erkannt: " + Util.formatDate(tageskurs.datum) + 
							" Kurs: " + kursAktuell + " - " + 
							" GD1VT: " + gd1WertVT + " GD2VT: " + gd2WertVT + 
							" GD1: " + gd1Wert + " GD2: " + gd2Wert + " Staerke: " + signal.staerke);
		} 
		return result; 
	}
}
