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
	 * @param kursreihe
	 */
	public static void rechneIndikatoren(Kursreihe kursreihe) {
		if (kursreihe == null) log.error("Inputvariable kursreihe ist null");
		rechneMinusDifferenzen(kursreihe);
		rechnePlusDifferenzen(kursreihe);
		rechneBergTal(kursreihe);
		rechneKursLetztesExtrem(kursreihe);
		rechneGleitenderDurchschnitt(kursreihe, 10);
		rechneGleitenderDurchschnitt(kursreihe, 30);
		rechneGleitenderDurchschnitt(kursreihe, 100);
		rechneVola(kursreihe, 10);
		rechneVola(kursreihe, 30);
		rechneVola(kursreihe, 100);
	}
	
	/**
	 * berechnet Differenzen zu vergangenen Tagen im Verhältnis zum Tageskurs
	 */
	private static void rechneMinusDifferenzen(Kursreihe kursreihe) {
		ArrayList<Tageskurs> kurse = kursreihe.kurse;
		Tageskurs aktuellerTageskurs; 
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
	private static void rechnePlusDifferenzen (Kursreihe kursreihe) {
		ArrayList<Tageskurs> kurse = kursreihe.kurse;
		Tageskurs aktuellerTageskurs; 
		float kurs = 0;
		
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
	public static void rechneBergTal (Kursreihe kursreihe) {
		if (kursreihe == null) log.error("Inputvariable kursreihe ist null");
		ArrayList<Tageskurs> kurse = kursreihe.kurse;
		for (int i = 0; i < kurse.size() ; i++) {
			
			Tageskurs tk = kurse.get(i);
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
	private static void rechneTal(Tageskurs tk) {
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
	private static void rechneBerg(Tageskurs tk) {
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
	private static void rechneKursLetztesExtrem (Kursreihe kursreihe) {
		ArrayList<Tageskurs> kurse = kursreihe.kurse;
		Tageskurs tk;
		for (int i = 0 ; i < kurse.size(); i++) {
			tk = kurse.get(i);
			Tageskurs tkm1 = kursreihe.ermittleTageskursVortag(tk);
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
	 * @param kursreihe
	 */
	private static void rechneGleitenderDurchschnitt (Kursreihe kursreihe, int x) {
		// holt die Kursreihe 
		float[] kurse = kursreihe.getKurse();
		float summe = 0;
		// addiert die Kurse der vergangenen x Tage. 
		// dabei wird nicht geschrieben, da die Berechnung noch unvollständig ist. 
		if (kursreihe.kurse.size() <= x) return; // wenn weniger Kurse vorhanden sind
		// addiert die ersten x Kurse. 
		for (int i = 0 ; i < x ; i++) {
			summe += kurse[i];
		}
		// ein neuer Kurs kommt hinzu, ein alter Kurs fällt weg 
		for (int i = x ; i < kursreihe.kurse.size(); i++) {
			float kursneu = kursreihe.kurse.get(i).getKurs();
			float kursalt = kursreihe.kurse.get(i - x).getKurs();
			summe += kursneu;
			summe -= kursalt; 
			// das Ergebnis in den Kurs eintragen
			kursreihe.kurse.get(i).setGleitenderDurchschnitt(summe / x, x); 
		}
	}
	/**
	 * Volatilität mit Hilfe der apache.math.statistic-Komponente
	 * @param kursreihe
	 * @param x - die gewünscht Zeitspanne
	 */
	public static void rechneVola (Kursreihe kursreihe, int x) {
		// wenn weniger Kurse vorhanden sind, als die Zeitspanne 
		if (kursreihe.kurse.size() <= x) return;
		
		Tageskurs tageskurs; 
		DescriptiveStatistics stats = new DescriptiveStatistics();
		// beim Einfügen weiterer Werte fliegt automatisch der erst raus
		stats.setWindowSize(x);
		// die Werte auffüllen ohne Berechnung
		for (int i = 0 ; i < x ; i++) {
			stats.addValue(kursreihe.kurse.get(i).getKurs());
		}
		for (int i = x ; i < kursreihe.kurse.size() ; i++) {
			tageskurs = kursreihe.kurse.get(i);
			stats.addValue(tageskurs.getKurs());
			double vola = stats.getStandardDeviation();
			tageskurs.setVola((float) vola, x); 
		}
		
	}

}
