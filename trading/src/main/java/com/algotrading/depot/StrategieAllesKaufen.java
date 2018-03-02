package depot;

import java.util.GregorianCalendar;

import kurs.Aktien;
import kurs.Kurs;
import signal.Signal;

public class StrategieAllesKaufen implements DepotStrategie {
	
	/**
	 * Nutzt jedes Kaufsignal zum Kauf
	 */
	@Override
	public void entscheideSignal(Signal signal, Depot depot) {
		Kurs kurs = signal.getTageskurs();
		GregorianCalendar datum = kurs.datum;
		
		if (signal.getKaufVerkauf() == Order.KAUF) {
			depot.kaufe(datum, depot.anfangsbestand/3, Aktien.getInstance().getAktie(kurs.name));
		}
		
		
	}

}
