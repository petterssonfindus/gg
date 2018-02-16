package signal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kurs.Kurs;
import util.Util;

/**
 * repräsentiert ein Kauf/Verkaufsignal 
 * Es können mehrere Signale vom gleichen Typ hintereinander auftreten. 
 * Ein Signal gehört zu einem Tageskurs.
 * @author oskar
 *
 */
public class Signal {
	private static final Logger log = LogManager.getLogger(Signal.class);

	private Kurs tageskurs; 

	private byte kaufVerkauf;
	
	public static final byte SteigenderBerg = 1;
	public static final byte FallenderBerg = 2;
	public static final byte SteigendesTal= 3;
	public static final byte FallendesTal= 4;

	public static final byte GD10Durchbruch = 5;
	public static final byte GD30Durchbruch = 6;
	public static final byte GD100Durchbruch = 7;

	private byte typ;
	
	// optional - eine Zahl von 0 - 100 über die Stärke
	public float staerke; 
	/**
	 * private Konstruktor kann nur über die Methode erzeugen genutzt werden. 
	 * Dadurch kann beim Erzeugen die Referenz auf den Tageskurs eingetragen werden. 
	 * @param tageskurs
	 * @param kaufVerkauf
	 * @param typ
	 * @param staerke
	 */
	private Signal (Kurs tageskurs, byte kaufVerkauf, byte typ, float staerke){
		this.tageskurs = tageskurs; 
		this.kaufVerkauf = kaufVerkauf;
		this.typ = typ;
		this.staerke = staerke;
	}
	
	public static Signal create (Kurs tageskurs, byte kaufVerkauf, byte typ, float staerke) {
		Signal signal = new Signal(tageskurs, kaufVerkauf, typ, staerke);
		tageskurs.addSignal(signal);
		return signal;
	}
	
	public void setTyp (byte typ) {
		this.typ = typ;
	}
	
	public byte getTyp () {
		return this.typ;
	}
	
	public Kurs getTageskurs () {
		return this.tageskurs;
	}

	public byte getKaufVerkauf() {
		return kaufVerkauf;
	}

	public void setKaufVerkauf(byte kaufVerkauf) {
		this.kaufVerkauf = kaufVerkauf;
	}
	public String toString () {
		String result; 
		result = this.tageskurs.name + Util.separator +
			Util.formatDate(this.tageskurs.datum) + Util.separator + 
			this.kaufVerkauf + Util.separator + 
			this.typ + Util.separator + 
			Util.toString(this.staerke);
		return result;
	}

}
