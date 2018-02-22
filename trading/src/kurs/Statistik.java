package kurs;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * berechnet alle statistischen Indikatoren auf Basis einer Kursreihe
 * und ergänzt die Kursreihe mit den Daten. 
 * @author oskar
 *
 */
public class Statistik {
	private static final Logger log = LogManager.getLogger(Statistik.class);

	private static final float BERG = 0.01f;
	private static final float TAL = -0.01f;
	
	/**
	 * steuert die Berechnung aller Indikatoren 
	 * Nutzung erfolgt über Aktie
	 * @param aktie
	 */
	protected static void rechneIndikatoren(Aktie aktie) {
		if (aktie == null) log.error("Inputvariable aktie ist null");
		rechneMinusDifferenzen(aktie);
		rechnePlusDifferenzen(aktie);
		rechneBergTal(aktie);
		rechneKursLetztesExtrem(aktie);
		rechneGleitenderDurchschnitt(aktie, 10);
		rechneGleitenderDurchschnitt(aktie, 30);
		rechneGleitenderDurchschnitt(aktie, 100);
		rechneVola(aktie, 10);
		rechneVola(aktie, 30);
		rechneVola(aktie, 100);
		StatisticSAR.rechneSAR(aktie, 0.02f, 0.02f, 0.2f);
	}
	
	/**
	 * berechnet Differenzen zu vergangenen Tagen im Verhältnis zum Tageskurs
	 */
	private static void rechneMinusDifferenzen(Aktie aktie) {
		ArrayList<Kurs> kurse = aktie.getBoersenkurse();
		if (kurse == null) log.error("Aktie hat keine Kurse: " + aktie.name);
		Kurs aktuellerTageskurs; 
		float kurs = 0;
		
		for (int i = 0; i < kurse.size() ; i++) {
			aktuellerTageskurs = kurse.get(i);
			float aktuellerKurs = aktuellerTageskurs.getKurs();

			try {
				kurs = kurse.get(i - 1).getKurs();
				aktuellerTageskurs.diffminus[0] = rechneDifferenzInProzent(aktuellerKurs, kurs);
				kurs = kurse.get(i - 2).getKurs();
				aktuellerTageskurs.diffminus[1] = rechneDifferenzInProzent(aktuellerKurs, kurs);
				kurs = kurse.get(i - 3).getKurs();
				aktuellerTageskurs.diffminus[2] = rechneDifferenzInProzent(aktuellerKurs, kurs);
				kurs = kurse.get(i - 4).getKurs();
				aktuellerTageskurs.diffminus[3] = rechneDifferenzInProzent(aktuellerKurs, kurs);
			} catch (Exception e) {
				// kein Vergleichskurs vorhanden
			}
		}
	}
	/**
	 * Rechenoperation für die Differenzberechnung
	 * @param tageskurs
	 * @param vergleichskurs
	 * @return
	 */
	private static Float rechneDifferenzInProzent (float tageskurs, float vergleichskurs) {
		float ergebnis = (tageskurs - vergleichskurs) / tageskurs;
		Float result = new Float(ergebnis);
		return result; 
	}
	/**
	 * berechnet Differenzen zu künftigen Tagen 
	 */
	private static void rechnePlusDifferenzen (Aktie aktie) {
		ArrayList<Kurs> kurse = aktie.getBoersenkurse();

		Kurs aktuellerTageskurs; 
		float kurs = 0;
		// iteriere alle Kurse über Zählvariable
		for (int i = 0; i < kurse.size() ; i++) {
			aktuellerTageskurs = kurse.get(i);
			float aktuellerKurs = aktuellerTageskurs.getKurs();

			try {
				kurs = kurse.get(i + 1).getKurs();
				aktuellerTageskurs.diffplus[0] = rechneDifferenzInProzent(aktuellerKurs, kurs);
				kurs = kurse.get(i + 2).getKurs();
				aktuellerTageskurs.diffplus[1] = rechneDifferenzInProzent(aktuellerKurs, kurs);
				kurs = kurse.get(i + 3).getKurs();
				aktuellerTageskurs.diffplus[2] = rechneDifferenzInProzent(aktuellerKurs, kurs);
				kurs = kurse.get(i + 4).getKurs();
				aktuellerTageskurs.diffplus[3] = rechneDifferenzInProzent(aktuellerKurs, kurs);
			} catch (Exception e) {
				// kein Vergleichskurs vorhanden
			}
		}

	}
	
	/**
	 * wenn Differenz nach vorne und hinten positiv oder negativ
	 */
	public static void rechneBergTal (Aktie kursreihe) {
		if (kursreihe == null) log.error("Inputvariable kursreihe ist null");
		ArrayList<Kurs> kurse = kursreihe.getBoersenkurse();
		for (int i = 0; i < kurse.size() ; i++) {
			
			Kurs tk = kurse.get(i);
			rechneTal (tk);
			rechneBerg(tk);
		}
		
	}
	/**
	 * die Täler werden unsortiert in das Tal-Array gestellt 
	 * hohe negative Kursdifferenzen nach vorne und hinten
	 * Kursdifferenzen werden positiv addiert 
	 * @param tk
	 */
	private static void rechneTal(Kurs tk) {
		for (int i = 0 ; i < 4 ; i++) {
			if ((tk.diffminus[i] < Statistik.TAL) && (tk.diffplus[i] < Statistik.TAL)) {
				// die HÃ¶he des Tals wird berechnet 
				tk.tal[i] = -(tk.diffminus[i] + tk.diffplus[i]);
				// die Summe einer Spanne wird addiert im 'Tal'
				tk.talSumme += tk.tal[i];
			}
		}
	}
	
	/**
	 * die Berge werden unsortiert in das Berg-Array gestellt 
	 * Ein Berg hat hohe positive Kursdifferenzen nach vorne und hinten
	 * @param tk
	 */
	private static void rechneBerg(Kurs tk) {
		for (int i = 0 ; i < 4 ; i++) {
			if ((tk.diffminus[i] > Statistik.BERG) && (tk.diffplus[i] > Statistik.BERG)) {
				tk.berg[i] = (tk.diffminus[i] + tk.diffplus[i]);
				tk.bergSumme += tk.berg[i];
			}
		}
	}

	/**
	 * berechnet den letzten Extremkurs und schreibt ihn in den Tageskurs
	 * wenn der aktuelle Kurs kein Extrem ist, dann nehme den Kurs des Vortages 'vom letzten Extrem'. 
	 * (Wenn der aktuelle Kurs ein neues Extrem ist, dann nimm den aktuellen Kurs.)
	 * Wenn der nächste Kurs kein Extrem ist, dann ist das Extrem vorüber und er nimt den nächsten Kurs 
	 * Wenn der aktuelle Kurs ein bestehendes Extrem ist, dann nimm den HÃ¶chstkurs von heute oder gestern.
	 * @return
	 */
	private static void rechneKursLetztesExtrem (Aktie aktie) {

		for (Kurs tk : aktie.getBoersenkurse()) {
			Kurs tkm1 = aktie.ermittleTageskursVortag(tk);
			if (tkm1 != null) {
				
				// normalerweise wird der letzte Bergkurs übernommen
				tk.letzterBergkurs = tkm1.letzterBergkurs;
				// prüfe, ob aktueller Tageskurs kein Berg ist
				if (tk.bergSumme == 0 ) {	// kein Berg
					if (tkm1.bergSumme > 0) {  // Tag davor war ein Berg
						// passe den Kurs an. 
						tk.letzterBergkurs = Math.max(tk.getKurs(), tkm1.letzterBergkurs);
					}
				}
				
				// normalerweise wird der letzte Talkurs übernommen
				tk.letzterTalkurs = tkm1.letzterTalkurs;
				// prüfe, ob aktueller Tageskurs kein Tal ist
				if (tk.talSumme == 0 ) {	// kein Berg
					if (tkm1.talSumme > 0) {  // Tag davor war ein Berg
						// passe den Kurs an. 
						tk.letzterTalkurs = Math.max(tk.getKurs(), tkm1.letzterTalkurs);
					}
				}
			}
		}
	}
	/**
	 * Summe aller Tageskurse der letzten x Tage / Anzahl 
	 * incluse aktueller Tageskurs 
	 * @param aktie
	 */
	private static void rechneGleitenderDurchschnitt (Aktie aktie, int x) {
		// holt die Kursreihe 
		float[] kurse = aktie.getKursArray();
		float summe = 0;
		// addiert die Kurse der vergangenen x Tage. 
		// dabei wird nicht geschrieben, da die Berechnung noch unvollständig ist. 
		if (kurse.length <= x) return; // wenn weniger Kurse vorhanden sind
		// addiert die ersten x Kurse. 
		for (int i = 0 ; i < x ; i++) {
			summe += kurse[i];
		}
		// ein neuer Kurs kommt hinzu, ein alter Kurs fällt weg 
		for (int i = x ; i < kurse.length; i++) {
			float kursneu = kurse[i];
			float kursalt = kurse[i - x];
			summe += kursneu;
			summe -= kursalt; 
			// das Ergebnis in den Kurs eintragen
			aktie.getBoersenkurse().get(i).setGleitenderDurchschnitt(summe / x, x); 
		}
	}
	/**
	 * Volatilität mit Hilfe der apache.math.statistic-Komponente
	 * @param aktie
	 * @param x - die gewünscht Zeitspanne
	 */
	public static void rechneVola (Aktie aktie, int x) {
		// wenn weniger Kurse vorhanden sind, als die Zeitspanne 
		if (aktie.getBoersenkurse().size() <= x) return;
		
		Kurs tageskurs; 
		DescriptiveStatistics stats = new DescriptiveStatistics();
		// beim Einfügen weiterer Werte fliegt automatisch der erst raus
		stats.setWindowSize(x);
		// die Werte auffüllen ohne Berechnung
		for (int i = 0 ; i < x ; i++) {
			stats.addValue(aktie.getBoersenkurse().get(i).getKurs());
		}
		for (int i = x ; i < aktie.getBoersenkurse().size() ; i++) {
			tageskurs = aktie.getBoersenkurse().get(i);
			stats.addValue(tageskurs.getKurs());
			double vola = stats.getStandardDeviation();
			tageskurs.setVola((float) vola, x); 
		}
	}

}
