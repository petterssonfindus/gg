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
import util.Zeitraum;

public class SimulatorTest extends TestCase {
	private static final Logger log = LogManager.getLogger(Util.class);

	protected void setUp() throws Exception {

	}
	
	public void testSimuliereDepots () {
		// Zeitspannen bestimmen
		GregorianCalendar beginn = new GregorianCalendar(1995,1,1);
		GregorianCalendar ende = new GregorianCalendar(2017,6,1);
		Zeitraum zeitraum = new Zeitraum(beginn, ende);
		int dauer = 0;
		int rhythmus = 0;
		// Aktienliste bestimmen

		ArrayList<Aktie> aktien = new ArrayList<Aktie>();
		aktien.add(Aktien.getInstance().getAktie("xxxdja"));
//		aktien.add(Aktien.getInstance().getAktie("aa"));

//		ArrayList<Aktie> aktien = Aktien.getInstance().getAktien(zeitraum, false);
		// Indikatoren konfigurieren 
		ArrayList<Indikator> indikatoren = new ArrayList<Indikator>();
/*		
		Indikator adl = new Indikator(Indikatoren.INDIKATOR_MFM);
		indikatoren.add(adl);
		adl.addParameter("dauer", 10f);
		adl.addParameter("durchschnitt", 2f);
		// MInimim der Dauer ist 1 Tag. 
*/	
		// Signalbeschreibungen bestimmen
		// anhand der Signalbeschreibungen werden dann die Signale ermittelt
		// die erforderlichen Indikatoren müssen vorhanden sein. 
		ArrayList<SignalBeschreibung> signalBeschreibungen = new ArrayList<SignalBeschreibung>();
		
/*		SignalBeschreibung sb1 = new SignalBeschreibung(Signal.ADL);
		signalBeschreibungen.add(sb1);
		sb1.addParameter("indikator", adl);
		sb1.addParameter("schwelle", 0f);
		sb1.addParameter("gd2", obv10);
		sb1.addParameter("schwelledurchbruch", 0.01f);
 */
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
		signalStrategie.addParameter("kaufbetrag", 0.2f);

		TagesStrategie tagesStrategie = null;
/*		
		TagesStrategie tagesStrategie = new StopLossStrategieStandard();
		tagesStrategie.addParameter("verlust", 0.05f);
*/
		// die Dokumentation festlegen 
		boolean writeOrders = true;
		boolean writeHandelstag = true; 
		
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
				writeOrders,
				writeHandelstag);
		
	}
	

}
