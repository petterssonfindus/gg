package signal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aktie.Aktie;
import aktie.Indikator;
import aktie.Kurs;
import depot.Order;
import util.Util;
import util.Zeitraum;

public class GDDurchbruch implements SignalAlgorithmus {
	static final Logger log = LogManager.getLogger(GDDurchbruch.class);

	// wie weit muss der Tageskurs den Gleitenden Durchschnitt durchbrechen
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
		Indikator indikator = (Indikator) signalbeschreibung.getParameter("indikator");
		if (indikator == null) log.error("Signal enthaelt keinen Indikator");
		Zeitraum zeitraum = (Zeitraum) signalbeschreibung.getParameter("zeitraum");
		
		for (Kurs kurs : aktie.getBoersenkurse(zeitraum)) {
			Kurs vortageskurs = aktie.getVortageskurs(kurs);
			if (vortageskurs != null) {
				// bisher darunter, jetzt darüber
				// dabei werden die Signale erstellt und mit dem Tageskurs verbunden 
				if (GDDurchbruch.pruefeGleitenderDurchschnittSteigung(kurs, vortageskurs, indikator)) anzahl++;
				
				// bisher darüber, jetzt darunter
				if (GDDurchbruch.pruefeGleitenderDurchschnittSinkflug(kurs, vortageskurs, indikator)) anzahl++;
			}
		}
		return anzahl; 
	}

	/**
	 * bisher darunter, jetzt darüber
	 * erzeugt Signale und hängt sie an den Kurs an
	 */
	private static boolean pruefeGleitenderDurchschnittSteigung (Kurs tageskurs, Kurs vortageskurs, Indikator indikator ) {
		if (tageskurs == null || vortageskurs == null || indikator == null) log.error("Inputvariable ist null"); 
		boolean result = false; 
		Float gd = tageskurs.getIndikatorWert(indikator);
		Float gdvt = vortageskurs.getIndikatorWert(indikator);
		log.trace("GD-Signal Steigung: " + Util.formatDate(tageskurs.datum) + " - " + vortageskurs.getKurs() + " GDVt: " + gdvt + 
				" Kurs: " + tageskurs.getKurs() + " GD: " + gd);
		Signal signal = null; 
		// wenn am Vortag der Kurs unter dem GD war, und heute der Kurs über dem GD ist 
		if ((vortageskurs.getKurs() < gdvt + GDDurchbruch.SCHWELLEGDDURCHBRUCH) && 
				tageskurs.getKurs() > (gd + GDDurchbruch.SCHWELLEGDDURCHBRUCH)) {
			signal = Signal.create(tageskurs, Order.KAUF, Signal.GDDurchbruch, 0);
			result = true; 
			signal.staerke = (tageskurs.getKurs() - gd) / gd;
			log.debug("GD-Steigung erkannt: " + Util.formatDate(tageskurs.datum) + " VTKurs " + vortageskurs.getKurs() + " GDVt: " + gdvt + 
					" Kurs: " + tageskurs.getKurs() + " GD: " + gd);
		} 
		return result; 
	}

	/**
	 * bisher darüber, jetzt darunter
	 */
	private static boolean pruefeGleitenderDurchschnittSinkflug (Kurs tageskurs, Kurs vortageskurs, Indikator indikator ) {
		if (tageskurs == null || vortageskurs == null || indikator == null) log.error("Inputvariable ist null"); 
		boolean result = false; 
		Float gd = tageskurs.getIndikatorWert(indikator);
		Float gdvt = vortageskurs.getIndikatorWert(indikator);
		Signal signal = null; 
		
		if ((vortageskurs.getKurs() > gdvt - GDDurchbruch.SCHWELLEGDDURCHBRUCH) && 
				tageskurs.getKurs() < (gd - GDDurchbruch.SCHWELLEGDDURCHBRUCH)) {
			signal = Signal.create(tageskurs, Order.VERKAUF, Signal.GDDurchbruch, 0);
			result = true;
			signal.staerke = (gd - tageskurs.getKurs()) / gd;
			log.debug("GD-Sinkflug: " + Util.formatDate(tageskurs.datum) + " VTKurs " + vortageskurs.getKurs() + " GDVt: " + gdvt + 
					" Kurs: " + tageskurs.getKurs() + " GD: " + gd);
		} 
		return result; 
	}

}
