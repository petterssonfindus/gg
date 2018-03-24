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
	private static final Logger log = LogManager.getLogger(Simulator.class);

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
			ArrayList<Aktie> aktien, 
			GregorianCalendar beginn, 
			GregorianCalendar ende, 
			int dauer, 
			int rhythmus
			) {
		// die Zeitintervalle ermitteln
		
		ArrayList<Zeitraum> zeitraeume = ermittleZeitraum(beginn, ende, dauer, rhythmus);
		Indikator gd10 = new Indikator(Indikatoren.INDIKATOR_GLEITENDER_DURCHSCHNITT);
		gd10.addParameter("dauer", 10f);
		for (Aktie aktie : aktien) {
			// die Indikatoren werden in jede Aktie gespeichert 
			// die Ergebnisse werden für alle Kurse im Kurs gespeichert
			aktie.addIndikator(gd10);
			// für jede Aktie werden die benötigten Indikatoren berechnet 
			aktie.rechneIndikatoren();
			
		}
		// mit jedem Zeitraum eine Simulation durchführen 
		// die Signale werden an der Aktie entfernt 
		// die Indikatoren bleiben erhalten 
		// alle benötigten Indikatoren werden erzeugt
		/*
			Indikator rsi30 = new Indikator(Indikatoren.INDIKATOR_RSI);
			indikatoren.add(rsi30);
			rsi30.addParameter("dauer", 30f);
			Indikator rsi10 = new Indikator(Indikatoren.INDIKATOR_RSI);
			indikatoren.add(rsi10);
			rsi10.addParameter("dauer", 10f);
		 */
		
		for (Zeitraum zeitraum : zeitraeume) {
			// bereite Depot vor
			Depot depot = new Depot("Oskars", 10000f);
			depot.aktien = aktien; 
			// die Signalbeschreibungen werden erzeugt
			SignalBeschreibung sb1 = new SignalBeschreibung(Signal.GDDurchbruch);
			sb1.addParameter("indikator", gd10);
			// wenn Zeitraum gesetzt wird, wird Signalsuche nur hier durchgeführt
			sb1.addParameter("zeitraum", zeitraum);
			
			for (Aktie aktie : aktien) {
				aktie.addSignalBeschreibung(sb1);
			}
		
			// die Strategien werden auf Basis der Signale festgelegt
			SignalStrategie kaufVerkaufStrategie = new StrategieAlleSignale();
			TagesStrategie tagesStrategie = new StopLossStrategieStandard();
			tagesStrategie.addParameter("verlust", 0.01f);
			
			// die Depot-Simulation wird durchgeführt, dabei werden auch Indikatoren und Signale berechnet 
			depot.simuliereDepot(kaufVerkaufStrategie, tagesStrategie, aktien, zeitraum.beginn, zeitraum.ende);
			
			log.info(Util.formatDate(zeitraum.beginn) + 
					Util.separator + Util.formatDate(zeitraum.ende) + 
					depot.strategieBewertung.toString());
		}

	}
	
	/**
	 * Liefert eine Liste von Zeitraumn anhand der Parameter
	 * @param beginn
	 * @param ende
	 * @param dauer wenn Dauer oder Rhythmus 0 ist, dann gibt es nur 1 Zeitraum 
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
		if (dauer == 0 || rhythmus == 0) {
			result.add(new Zeitraum(beginn, ende));
		}
		else {
			do {
				// vom Beginn bis zum Ende liegt die Dauer 
				neuesEnde = Util.addTage(neuerBeginn, dauer); 
				if (neuesEnde.before(ende)) {
					result.add(new Zeitraum(neuerBeginn,neuesEnde));
				}
				neuerBeginn = Util.addTage(neuerBeginn, rhythmus);
			}
			while (neuerBeginn.before(ende));
			
		}
		
		return result; 
	}
}
	
