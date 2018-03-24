package kurs;

import java.sql.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

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
	public String wertpapier; 
	/**
	 * Der Indikator ist eine Referenz auf die Parameter des Indikators. 
	 * Der Float ist der Wert für diesen Kurs 
	 */
	public HashMap<Indikator, Float> indikatoren = new HashMap<Indikator, Float>();

	public float sar; 
	public float rsi; 
	// die Höhe des Berges - Summe der Kursdifferenzen vor und zurück
	public float berg;
	// die Tiefe des Tales 
	public float tal;
	
	// Liste aller Signale - Öffentlicher Zugriff nur über add() und get()
	private ArrayList<Signal> signale; 

	public Kurs() {
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
	
	public void clearSignale () {
		this.signale = new ArrayList<Signal>();
	}
	
	public String getClose () {
		return Float.toString(close);
	}
	/**
	 * ein Indikator wurde berechnet und wird dem Kurs hinzugefügt
	 * @param indikator
	 * @param wert
	 */
	public void addIndikator (Indikator indikator, float wert) {
		this.indikatoren.put(indikator, wert);
	}
	
	public float getIndikatorWert (Indikator indikator) {
		if (this.indikatoren.containsKey(indikator)) {
			return this.indikatoren.get(indikator);
		}
		else return 0;
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
				indikatorenToString();
	}
	
	private String indikatorenToString() {
		String result = ""; 
		for (Float wert : this.indikatoren.values()) {
			result.concat(Util.toString(wert) + Util.separator);
		}
		return result; 
	}
	
}
