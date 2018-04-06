package depot;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aktie.Aktie;
import aktie.Indikator;
import signal.SignalBeschreibung;
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
			int rhythmus, 
			ArrayList<Indikator> indikatoren, 
			ArrayList<SignalBeschreibung> signalBeschreibungen, 
			SignalStrategie signalStrategie, 
			TagesStrategie tagesStrategie, 
			boolean writeOrders,
			boolean writeHandelstag
			) {
		
		// die Zeitintervalle ermitteln
		ArrayList<Zeitraum> zeitraeume = ermittleZeitraum(beginn, ende, dauer, rhythmus);
		for (Aktie aktie : aktien) {
			for (Indikator indikator : indikatoren){
				// die Indikator-Konfigurationen werden in jeder Aktie gespeichert
				aktie.addIndikator(indikator);
			}
			// für jede Aktie werden die benötigten Indikatoren berechnet 
			aktie.rechneIndikatoren();
		}
		// für jeden Zeitraum wird eine Simulation durchgeführt 
		for (Zeitraum zeitraum : zeitraeume) {
			// bereite Depot vor
			Depot depot = new Depot("Oskars", 10000f);
			depot.aktien = aktien; 
			// jede Signalbeschreibung wird in jeder Aktie gesetzt 
			for (SignalBeschreibung signalBeschreibung : signalBeschreibungen) {
				// wenn Zeitraum gesetzt wird, wird Signalsuche nur hier durchgeführt
				signalBeschreibung.addParameter("zeitraum", zeitraum);
				// Signalbeschreibung wird in jeder Aktie gespeichert
				for (Aktie aktie : aktien) {
					aktie.addSignalBeschreibung(signalBeschreibung);
				}
			}
	
			// die Depot-Simulation wird durchgeführt, dabei werden auch Signale berechnet 
			depot.simuliereDepot(signalStrategie, tagesStrategie, aktien, zeitraum.beginn, zeitraum.ende, writeHandelstag);
			
			// auf Wunsch wird pro Simulation csv-Listen erstellt
			if (writeOrders) {
				depot.writeOrders();
			}
			
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
	
