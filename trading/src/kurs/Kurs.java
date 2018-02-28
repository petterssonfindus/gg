package kurs;

import java.sql.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import signal.Signal;
import util.Util;

import data.DBManager;
/**
 * ein Tageskurs enthält Kursdaten, Indikatoren, und Signale
 * Dummer Datenbehälter
 * Kursdaten stammen aus der Datenbank, Indikatoren und Signale werden berechnet. 
 * @author oskar
 *
 */
public class Kurs {
	private static final Logger log = LogManager.getLogger(Kurs.class);
	
	public GregorianCalendar datum; 
	public float close; 
	public float open;
	public float high;
	public float low;
	public float adjClose;
	public int volume; 
	public String name; 
	public float[] diffminus;
	public float[] diffplus;
	public float gleitenderDurchschnitt10; 
	public float gleitenderDurchschnitt30; 
	public float gleitenderDurchschnitt100; 
	public float vola10;
	public float vola30;
	public float vola100;
	public float sar; 
	public float rsi; 
	// die Höhe des Berges im Umkreis von x Tagen 
	public float[] berg;
	// die Tiefe des Tales im Umkreis von x Tagen 
	public float[] tal;
	// die Summe der Tiefen - sagt aus, ob es ein Tal ist 
	public float talSumme; 
	// die Summe der Höhen - sagt aus, ob es ein Berg ist
	public float bergSumme; 
	// letzter Kurs eines Berges #TODO müsste der höchste Kurs sein 
	public float letzterBergkurs;
	// letzter Kurs eines Tales #TODO müsste der tiefste Kurs sein 
	public float letzterTalkurs;
	
	// Liste aller Signale - Öffentlicher Zugriff nur über add() und get()
	protected ArrayList<Signal> signale; 

	public Kurs() {
		this.diffminus = new float[4];
		this.diffplus = new float[4];
		this.berg = new float[4];
		this.tal = new float[4];
		this.signale = new ArrayList<Signal>();
	}
	/**
	 * hängt an einen Kurs ein Signal an
	 * @param signal
	 */
	public void addSignal (Signal signal) {
		if (signal == null) log.error("Inputvariable signal ist null");
		if (signal == null) {
			log.info("leeres Signal bei Tageskurs: " + this.toString());
		}
		else this.signale.add(signal);
	}
	/**
	 * Zugriff auf die Signale eines Kurses 
	 * @return eine Liste mit Signalen, oder null
	 */
	public ArrayList<Signal> getSignale () {
		return this.signale; 
	}
	
	public String getClose () {
		return Float.toString(close);
	}
	
	/**
	 * schreibt das Ergebnis der Gleitenden Durchschnitts mit Angabe der Laufzeit
	 * @param ergebnis
	 * @param x
	 */
	public void setGleitenderDurchschnitt (float ergebnis, int x) {
		if (x == 30) this.gleitenderDurchschnitt30 = ergebnis; 
		else if (x == 10) this.gleitenderDurchschnitt10 = ergebnis; 
		else if (x == 100) this.gleitenderDurchschnitt100 = ergebnis; 
	}
	/**
	 * Zugriff auf die GleitendenDuchschnittswerte abhängig von der Laufzeit
	 * @param x
	 * @return
	 */
	public float getGleitenderDurchschnitt (int x) {
		if (x == 10) return this.gleitenderDurchschnitt10;
		else if (x == 30) return this.gleitenderDurchschnitt30;
		else if (x == 100) return this.gleitenderDurchschnitt100;
		return 0;
	}
	/**
	 * schreibt das Ergebnis der Gleitenden Durchschnitts mit Angabe der Laufzeit
	 * @param ergebnis
	 * @param x
	 */
	public void setVola (float ergebnis, int x) {
		if (x == 30) this.vola30 = ergebnis; 
		else if (x == 10) this.vola10 = ergebnis; 
		else if (x == 100) this.vola100 = ergebnis; 
	}
	/**
	 * Zugriff auf die GleitendenDuchschnittswerte abhängig von der Laufzeit
	 * @param x
	 * @return
	 */
	public float getVola (int x) {
		if (x == 10) return this.vola10;
		else if (x == 30) return this.vola30;
		else if (x == 100) return this.vola100;
		return 0;
	}

	/**
	 * gibt den Kurs eines Tages zurück - i.d.R. der Close-Kurs
	 * @return
	 */
	public float getKurs () {
		return close; 
	}
	
	public void setKurs (float kurs) {
		this.close = kurs;
	}
	
	/**
	 * das Datum mit einem GregCalendar setzen
	 * @param datum
	 */
	public void setDatum (GregorianCalendar datum) {
		this.datum = datum; 
	}
	
	/**
	 * das Datum mit einem Date setzen
	 * @param datum
	 */
	public void setDatum (Date datum) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(datum);
		this.datum = cal;
	}
	
	public String toString() {
		return DBManager.formatSQLDate(datum) + Util.separator + 
				Util.toString(close) + Util.separator + 
				Util.toString(talSumme) + Util.separator + 
				Util.toString(bergSumme) + Util.separator + 
				Util.toString(letzterTalkurs) + Util.separator + 
				Util.toString(letzterBergkurs) + Util.separator + 
				Util.toString(gleitenderDurchschnitt10) + Util.separator + 
				Util.toString(gleitenderDurchschnitt30) + Util.separator + 
				Util.toString(gleitenderDurchschnitt100) + Util.separator + 
				Util.toString(vola10) + Util.separator + 
				Util.toString(vola30) + Util.separator + 
				Util.toString(vola100) + Util.separator + 
				Util.toString(rsi) + Util.separator
				;
	}
	
}
