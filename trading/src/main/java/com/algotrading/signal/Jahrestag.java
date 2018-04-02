package signal;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktie;
import kurs.Kurs;
import util.Util;
import util.Zeitraum;

public class Jahrestag implements SignalAlgorithmus {
	static final Logger log = LogManager.getLogger(Jahrestag.class);
	// merkt sich das Jahr der letzten Signalerzeugung
	private static int jahreszahl = 0;
	
	@Override
	/**
	 * erzeugt ein Signal, wenn der Jahrestag eintritt 
	 * Genau ein Signal pro Jahr
	 * @param kursreihe
	 */
	public int ermittleSignal(Aktie aktie, SignalBeschreibung signalbeschreibung) {
		if (aktie == null) log.error("Inputparameter Aktie ist null");
		if (signalbeschreibung == null) log.error("Inputparameter Signalbeschreibung ist null");
		int anzahl = 0;
		jahreszahl = 0;
		int tage = (int) signalbeschreibung.getParameter("tage");
		int kaufverkauf = (int) signalbeschreibung.getParameter("kaufverkauf");
		Zeitraum zeitraum = (Zeitraum) signalbeschreibung.getParameter("zeitraum");
		
		for (Kurs kurs : aktie.getBoersenkurse(zeitraum)) {
			if (Jahrestag.pruefeJahrestag(kurs, tage, kaufverkauf)) anzahl++;
		}
		return anzahl; 
	}

	/**
	 * erzeugt Jahrestag-Signal und hängt es an den Kurs an
	 */
	private static boolean pruefeJahrestag (Kurs tageskurs, int jahrestag, int kaufverkauf) {
		if (tageskurs == null ) log.error("Inputvariable ist null"); 
		boolean result = false; 
		GregorianCalendar datum = tageskurs.datum;
		int dayofyear = datum.get(Calendar.DAY_OF_YEAR);
		int year = datum.get(Calendar.YEAR);
		if (dayofyear > jahrestag && year > jahreszahl) {
			log.debug("Jahrestag eingetreten: " + tageskurs.wertpapier + " " + 
					jahrestag + " " + kaufverkauf + " " + Util.formatDate(datum));
			Signal.create(tageskurs, (byte) kaufverkauf, Signal.Jahrestag, 0);
			jahreszahl = datum.get(Calendar.YEAR);
			result = true; 
		}

		return result; 
	}
}
