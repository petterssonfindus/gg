package depot;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aktie.Aktie;
import aktie.Aktien;
import aktie.Indikator;
import aktie.Indikatoren;
import junit.framework.TestCase;
import signal.Signal;
import signal.SignalBeschreibung;
import util.Util;

public class SimulatorTest extends TestCase {
	private static final Logger log = LogManager.getLogger(Util.class);

	protected void setUp() throws Exception {

	}
	
	public void testSimuliereDepots () {
		// Zeitspannen bestimmen
		GregorianCalendar beginn = new GregorianCalendar(2010,0,1);
		GregorianCalendar ende = new GregorianCalendar(2017,11,1);
		int dauer = 0;
		int rhythmus = 0;
		// Aktienliste bestimmen
		ArrayList<Aktie> aktien = new ArrayList<Aktie>();
		aktien.add(Aktien.getInstance().getAktie("xxxgdaxi"));
		aktien.add(Aktien.getInstance().getAktie("aa"));
		// Indikatoren konfigurieren 
		ArrayList<Indikator> indikatoren = new ArrayList<Indikator>();
		Indikator gd38 = new Indikator(Indikatoren.INDIKATOR_GLEITENDER_DURCHSCHNITT);
		indikatoren.add(gd38);
		gd38.addParameter("dauer", 38f);
		Indikator gd200 = new Indikator(Indikatoren.INDIKATOR_GLEITENDER_DURCHSCHNITT);
		indikatoren.add(gd200);
		gd200.addParameter("dauer", 200f);
		
		// Signalbeschreibungen bestimmen
		ArrayList<SignalBeschreibung> signalBeschreibungen = new ArrayList<SignalBeschreibung>();
		SignalBeschreibung sb1 = new SignalBeschreibung(Signal.GDSchnitt);
		signalBeschreibungen.add(sb1);
		sb1.addParameter("gd1", gd38);
		sb1.addParameter("gd2", gd200);
		sb1.addParameter("schwelledurchbruch", 0.01f);
		SignalBeschreibung sb2 = new SignalBeschreibung(Signal.Jahrestag);
		signalBeschreibungen.add(sb2);
		sb2.addParameter("tage", 120);
		sb2.addParameter("kaufverkauf", Order.VERKAUF);
		SignalBeschreibung sb3 = new SignalBeschreibung(Signal.Jahrestag);
		signalBeschreibungen.add(sb3);
		sb3.addParameter("tage", 240);
		sb3.addParameter("kaufverkauf", Order.KAUF);

		// die Strategien werden festgelegt
		SignalStrategie signalStrategie = new StrategieJahrAlleSignale();
		TagesStrategie tagesStrategie = new StopLossStrategieStandard();
		tagesStrategie.addParameter("verlust", 0.01f);
		// die Dokumentation festlegen 
		boolean writeOrders = false; 
		
		// Simulation ausführen
		Simulator.simuliereDepots(
				aktien, 
				beginn, 
				ende, 
				dauer, 
				rhythmus, 
				indikatoren, 
				signalBeschreibungen, 
				signalStrategie, 
				tagesStrategie,
				writeOrders);
		
	}
	

}
