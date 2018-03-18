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
public class Indikatoren {
	private static final Logger log = LogManager.getLogger(Indikatoren.class);

	private static final float BERG = 0.01f;
	private static final float TAL = -0.01f;
	
	public static final short INDIKATOR_GLEITENDER_DURCHSCHNITT = 1; 
	public static final short INDIKATOR_MINUS_DIFFERENZ = 2; 
	public static final short INDIKATOR_PLUS_DIFFERENZ = 3; 
	public static final short INDIKATOR_VOLATILITAET = 4; 
	public static final short INDIKATOR_BERG = 5; 
	public static final short INDIKATOR_TAL= 6; 
	public static final short INDIKATOR_SAR= 7; 
	public static final short INDIKATOR_RSI= 8; 
	
	/**
	 * steuert die Berechnung der gewünschten Indikatoren
	 * @param aktie
	 */
	public static void rechneIndikatoren(Aktie aktie, ArrayList<Indikator> indikatoren) {
		if (aktie == null) log.error("Inputvariable aktie ist null");
		if (indikatoren == null) log.error("Inputvariable Indikatoren ist null");
		
		for (Indikator indikator : indikatoren) {
			switch (indikator.typ) {
				case 1: {
					int anzahl = rechneGleitenderDurchschnitt(aktie, indikator);
					log.debug("GleitenderD  berechnet Aktie: " + aktie.toSmallString() + " " + 
							anzahl + " Berechnungen " + 
							"Indikator: " + indikator.toString());
					break;
				}
				case 4: {
					rechneVola(aktie, indikator);
					
					break;
				}
				case 5: {
					Indikatoren.rechneBerg(aktie, (int) indikator.getParameter("dauer"));
					break;
				}
				case 6: {
//					rechneTal(aktie);
					break;
				}
				case 7: {
					StatisticSAR.rechneSAR(aktie, 0.02f, 0.02f, 0.2f);
					
					break;
				}
				case 8: {
					rechneRSI(aktie, 10);
					break;
				}
			}
		}
	}
	
	/**
	 * rechnet Berg, wenn nach vorne und nach hinten die Kurse fallen 
	 * geht beliebig weit nach vorne und hinten, solange die Kurs kontinuierlich sinken.
	 * Differenz zu jedem Tag wird addiert
	 * @param aktie
	 * @return
	 */
	private static void rechneBerg (Aktie aktie, int dauer) {
		
		ArrayList<Kurs> kurse = aktie.getBoersenkurse();
		float[] kursArray = aktie.getKursArray();
		Kurs kurs; 
		float kursdiffVorAlt = 0;
		float kursdiffZurueckAlt = 0;
		float kursdiffVor = 0;
		float kursdiffZurueck = 0;
		float summe = 0;
		int position; 
		boolean istBerg = false; 
		
		for (int i = dauer; i < kurse.size() ; i++) {
			kurs = kurse.get(i);
			position = 1;
			float ausgangskurs = kursArray[i];
			do {
				summe += kursdiffVor + kursdiffZurueck; // Kursdifferenzen werden zur Summe addiert 
				kursdiffVorAlt = kursdiffVor; // alte Kursdifferenz merken
				kursdiffZurueckAlt = kursdiffZurueck; // alte Kursdifferenz merken
				// neue Kursdifferenzen berechnen 
				kursdiffVor = ausgangskurs - kursArray[i + position]; // Kursdifferenz berechnen 
				kursdiffZurueck = ausgangskurs - kursArray[i - position]; // Kursdifferenz berechnen 
				position ++ ;  // ein Tag weiter

				// wenn die Kursdifferenz nach vorne und hinten von Tag zu Tag steigt 
				// UND die Position größer bleibt als die erlaubte Dauer 
				istBerg = kursdiffVor > kursdiffVorAlt && kursdiffZurueck > kursdiffZurueckAlt && position > i - dauer;
				if (istBerg) {
					// wenn ein Berg identifiziert wurde, wird die Summe eingetragen 
					// mit jedem erfolgreichen Durchlauf wird die Summe größer
					kurs.berg = summe; 
				}
			}
			while (istBerg);
		}
	}
	
	/**
	 * Summe aller Tageskurse der letzten x Tage / Anzahl 
	 * incluse aktueller Tageskurs 
	 * @param aktie
	 * @return Anzahl Tage, die erfolgreich berechnet wurden
	 */
	private static int rechneGleitenderDurchschnitt (Aktie aktie, Indikator indikator) {
		// holt die Kursreihe 
		float[] kurse = aktie.getKursArray();
		// holt den Parameter
		int x = (int) indikator.getParameter("dauer");
		float summe = 0;
		int berechnet = 0;
		
		// addiert die Kurse der vergangenen x Tage. 
		// dabei wird nicht geschrieben, da die Berechnung noch unvollständig ist. 
		if (kurse.length <= x) log.error("zu wenig Kurse: " + kurse.length + " vorhanden: " + x + " benötigt."); // wenn weniger Kurse vorhanden sind
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
			aktie.getBoersenkurse().get(i).addIndikator(indikator, summe / x); 
			berechnet ++;
		}
		return berechnet; 
	}
	/**
	 * Volatilität mit Hilfe der apache.math.statistic-Komponente
	 * @param aktie
	 * @param x - die gewünscht Zeitraum
	 */
	public static void rechneVola (Aktie aktie, Indikator indikator) {
		int x = (int) indikator.getParameter("dauer");
		// wenn weniger Kurse vorhanden sind, als die Zeitraum 
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
			tageskurs.addIndikator(indikator, (float) vola); 
		}
	}
	/**
	 * Relative-Stärke-Index
	 * Das Verhältnis der durschnittlich positiven Tage zu den durchschnittlich negativen Tagen 
	 * Wie stark sind die guten Tage im Vergleich zu den schlechten Tagen. 
	 * Die Implementierung ist effizient. 
	 * An jedem Tag kommt eine neue Differnz hinzu, eine alte fällt weg. 
	 * @param aktie
	 * @param tage
	 */
	public static void rechneRSI (Aktie aktie, int tage) {
		ArrayList<Kurs> kurse = aktie.getBoersenkurse();
		float sumUp = 0;
		float sumDown = 0;
		float sumUpA = 0; // Summe Up Average
		float sumDownA = 0; // Summe Down Average
		float rsi; 
		float kurs = 0; 
		float kursVortag = kurse.get(0).getKurs(); 
		float differenz = 0;
		float differenzAlt = 0;
		float[] differenzen = new float[kurse.size()];
		
		// Vorbereitung der Kursdaten 
		for (int i = 0 ; i < tage ; i++) {
			Kurs kursO = kurse.get(i);
			kurs = kursO.getKurs();
			differenz = kurs - kursVortag;
			differenzen[i] = differenz; 
			
			// Summen addieren
			if (differenz > 0) sumUp += differenz;
			else sumDown += differenz;

			kursVortag = kurs; 
		}
		
		for (int i = tage ; i < kurse.size() ; i++) {
			Kurs kursO = kurse.get(i);
			kursVortag = kurs; 
			kurs = kursO.getKurs();
			differenz = kurs - kursVortag;
			differenzen[i] = differenz; 
			differenzAlt = differenzen[i - tage];
			
			// Summen anpassen: neue Differenz hinzuzählen
			if (differenz > 0) sumUp += differenz;
			else sumDown += differenz;
			// Alte Differenz abziehen
			if (differenzAlt > 0) sumUp -= differenzAlt;
			else sumDown -= differenzAlt; 
			
			// Durchschnitte berechnen
			sumUpA = sumUp / tage;
			sumDownA = Math.abs(sumDown / tage);
			// rsi berechnen
			rsi = sumUpA / (sumUpA + sumDownA);
			kursO.rsi = rsi; 
		
		}
	}
}
