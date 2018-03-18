package depot;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Aktie;
import kurs.Aktien;
import kurs.Indikator;
import kurs.Indikatoren;
import signal.Signal;
import signal.SignalBeschreibung;
import signal.Signalsuche;
import util.Util;
import util.Zeitraum;

/**
 * Führt Depot-Simulationen durch
 * Speichert die Ergebnisse als .csv
 * @author oskar
 *
 */
public class Simulator {
	private static final Logger log = LogManager.getLogger(Util.class);

	ArrayList<Depot> depots = new ArrayList<Depot>();
	/**
	 * Führt eine Reihe von Simulationen durch
	 * @param wertpapier
	 * @param beginn
	 * @param ende
	 * @param dauer
	 * @param rhythmus
	 */
	public static void simuliereDepots (
			String wertpapier, 
			GregorianCalendar beginn, 
			GregorianCalendar ende, 
			int dauer, 
			int rhythmus
			) {
		// die Aktie vorbereiten - wird für alle Simulationen verwendet
		Aktie aktie = Aktien.getInstance().getAktie(wertpapier);
		// die Zeitintervalle ermitteln
		ArrayList<Zeitraum> zeitraeume = ermittleZeitraum(beginn, ende, dauer, rhythmus);
		// mit jeder Zeitraum eine Simulation durchführen 
		for (Zeitraum zeitraum : zeitraeume) {
			// bereite Depot vor
			Depot depot = new Depot("Oskars", 10000f);
			
			// alle benötigten Indikatoren werden erzeugt
			ArrayList<Indikator> indikatoren = new ArrayList<Indikator>();
			Indikator gd10 = new Indikator(Indikatoren.INDIKATOR_GLEITENDER_DURCHSCHNITT);
			indikatoren.add(gd10);
			gd10.addParameter("dauer", 10f);

	/*
			Indikator rsi30 = new Indikator(Indikatoren.INDIKATOR_RSI);
			indikatoren.add(rsi30);
			rsi30.addParameter("dauer", 30f);
			Indikator rsi10 = new Indikator(Indikatoren.INDIKATOR_RSI);
			indikatoren.add(rsi10);
			rsi10.addParameter("dauer", 10f);
	*/
			// die Indikatoren werden ermittelt in der gesamten Zeitreihe - die Ergebnisse werden im Kurs gespeichert
			Indikatoren.rechneIndikatoren(aktie, indikatoren);
			
			// die Signalbeschreibungen werden erzeugt
			ArrayList<SignalBeschreibung> signalbeschreibungen = new ArrayList<SignalBeschreibung>();
			SignalBeschreibung sb1 = new SignalBeschreibung(Signal.GDDurchbruch);
			signalbeschreibungen.add(sb1);
			sb1.addParameter("indikator", gd10);
			// wenn Beginn und Ende gesetzt wird, wird Signalsuche nur hier durchgeführt
			sb1.addParameter("zeitraum", zeitraum);
			
			// die Signale werden erzeugt - die Ergebnisse werden im Kurs gespeichert
			Signalsuche.rechneSignale(aktie, signalbeschreibungen);
			
			// die Strategien werden auf Basis der Signale festgelegt
			KaufVerkaufStrategie kaufVerkaufStrategie = new StrategieAlleSignaleKaufenVerkaufen();
			StopLossStrategie slStrategie = new StopLossStrategieStandard();
			
			// die Depot-Simulation wird durchgeführt
			depot.simuliereDepot(kaufVerkaufStrategie, slStrategie, aktie, zeitraum.beginn, zeitraum.ende);
			log.info(Util.formatDate(zeitraum.beginn) + 
					" - " + Util.formatDate(zeitraum.ende) + 
					depot.strategieBewertung.toString());
		}

	}
	
	/**
	 * Liefert eine Liste von Zeitraumn anhand der Parameter
	 * @param beginn
	 * @param ende
	 * @param dauer
	 * @param rhythmus
	 * @return
	 */
	static ArrayList<Zeitraum> ermittleZeitraum (
			GregorianCalendar beginn, 
			GregorianCalendar ende, 
			int dauer, 
			int rhythmus
		)
	{
		ArrayList<Zeitraum> result = new ArrayList<Zeitraum>();
		GregorianCalendar neuerBeginn; 
		GregorianCalendar neuesEnde; 
		neuerBeginn = beginn;
		
		do {
			// vom Beginn bis zum Ende liegt die Dauer 
			neuesEnde = Util.addTage(neuerBeginn, dauer); 
			if (neuesEnde.before(ende)) {
				result.add(new Zeitraum(neuerBeginn,neuesEnde));
			}
			neuerBeginn = Util.addTage(neuerBeginn, rhythmus);
		}
		while (neuerBeginn.before(ende));
		
		return result; 
	}
}
	
