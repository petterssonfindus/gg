package signal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import depot.Order;
import kurs.Kurs;
import util.Util;

/**
 * repräsentiert ein Kauf/Verkaufsignal 
 * Es können mehrere Signale vom gleichen Typ hintereinander auftreten. 
 * Ein Signal gehört zu einem Tageskurs.
 * Ein Signal hat keinen Parameter. 
 * Liefert häufig einen Wert über die Stärke
 * @author oskar
 *
 */
public class Signal {
	private static final Logger log = LogManager.getLogger(Signal.class);

	private Kurs tageskurs; 

	private byte kaufVerkauf;
	// die Liste aller Signale
	public static final short SteigenderBerg = 1;
	public static final short FallenderBerg = 2;
	public static final short SteigendesTal= 3;
	public static final short FallendesTal= 4;

	public static final short GDDurchbruch = 5;
	
	public static final short RSI = 10; 
	
	private int typ;
	
	// optional - eine Zahl von 0 - 100 über die Stärke
	public float staerke; 
	/**
	 * private Konstruktor kann nur über die Methode erzeugen genutzt werden. 
	 * Dadurch kann beim Erzeugen die Referenz auf den Tageskurs eingetragen werden. 
	 * @param tageskurs der Kurs, an dem das Signal hängt. 
	 * @param kaufVerkauf
	 * @param typ
	 * @param staerke
	 */
	private Signal (Kurs tageskurs, byte kaufVerkauf, int typ, float staerke){
		this.tageskurs = tageskurs; 
		this.kaufVerkauf = kaufVerkauf;
		this.typ = typ;
		this.staerke = staerke;
	}
	
	/**
	 * Die Signalsuche hat ein Signal identifiziert und hängt es in den Kurs ein
	 * @param tageskurs
	 * @param kaufVerkauf
	 * @param typ
	 * @param staerke
	 * @return
	 */
	public static Signal create (Kurs tageskurs, byte kaufVerkauf, int typ, float staerke) {
		Signal signal = new Signal(tageskurs, kaufVerkauf, typ, staerke);
		tageskurs.addSignal(signal);
		log.debug("neues Signal: " + signal.toString());
		return signal;
	}

	public void setTyp (byte typ) {
		this.typ = typ;
	}
	
	public int getTyp () {
		return this.typ;
	}
	
	public Kurs getTageskurs () {
		return this.tageskurs;
	}

	public byte getKaufVerkauf() {
		return this.kaufVerkauf;
	}
	
	public String kaufVerkaufToString() {
		if (this.kaufVerkauf == Order.KAUF) return "Kauf";
		else return "Verkauf";
	}

	public void setKaufVerkauf(byte kaufVerkauf) {
		this.kaufVerkauf = kaufVerkauf;
	}
	public String toString () {
		String result; 
		result = this.tageskurs.wertpapier + Util.separator +
			Util.formatDate(this.tageskurs.datum) + Util.separator + 
			this.kaufVerkaufToString() + Util.separator + 
			this.typ + Util.separator + 
			Util.toString(this.staerke);
		return result;
	}

}
