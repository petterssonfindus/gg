package kurs;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.DBManager;
	/**
	 * Verzeichnis aller Aktien, zu denen Zeitreihen vorhanden sind
	 * Es kann sich auch um einen Index handeln 
	 * Bietet Zugang zu Zeitreihen
	 * Als Singleton verfügbar
	 * Das Verzeichnis wird sofort initialisiert, die Kurse werden erst bei Bedarf den Kursreihen hinzugefügt. 
	 * Derzeit keine DB-Lösung, sondern Programmcode
	 * @author oskar
	 *
	 */
public class Aktien {
	private static final Logger log = LogManager.getLogger(Aktien.class);
	
	public static final byte BOERSEDEPOT = 0;
	public static final byte BOERSEINDEX = 1;
	public static final byte BOERSENYSE = 2;
	public static final byte BOERSENASDAQ = 3;
	public static final byte BOERSEFRANKFURT = 4;
	public static final byte BOERSEXETRA = 5;
	
	public static final String INDEXDAX = "dax";
	public static final String INDEXDOWJONES = "dowjones";
	
	private static Aktien instance;
	// das Verzeichnis aller Aktien 
	private static HashMap<String, Aktie> verzeichnis = new HashMap<String, Aktie>();
	
	private Aktien() {}
	
	public static Aktien getInstance() {
		if (instance == null) {
			instance = new Aktien();
			// das Verzeichnis wird versorgt 
			initialisiereVerzeichnis();
		}
		return instance; 
	}
	/**
	 * liest und initialisiert eine Aktie anhand eines WP-Namens
	 * die Kursreihe ist eventuell noch nicht gefüllt. 
	 * Wird beim Zugriff gefüllt. 
	 * @param wertpapier
	 * @return
	 */
	public Aktie getAktie (String wertpapier) {
		if (wertpapier == null) log.error("Inputvariable Wertpapier ist null");
		if (wertpapier == "") log.error("Inputvariable Wertpapier ist leer");
		Aktie aktie = null; 
		// sucht das Wertpapier im Verzeichnis
		if (verzeichnis.containsKey(wertpapier)) {
			aktie = verzeichnis.get(wertpapier);
			// zu Beginn sind alle Kursreihen vorhanden, aber ohne Kurse 
			if (aktie == null) log.error("Aktie ist null : " + wertpapier);
		}
		else log.error("Aktie nicht vorhanden: " + wertpapier);
		return aktie; 
	}

	/**
	 * das Verzeichnis wird beim Erstellen initialisiert
	 * Dies könnte auch über DB erfolgen. 
	 */
	private static void initialisiereVerzeichnis() {
		verzeichnis.put("dax", new Aktie ("dax", "DAX", Aktien.INDEXDAX, Aktien.BOERSEINDEX));
		verzeichnis.put("appl", new Aktie ("appl", "Apple", Aktien.INDEXDOWJONES, Aktien.BOERSENASDAQ) );
		verzeichnis.put("AA", new Aktie ("AA", "AA", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("ABT", new Aktie ("ABT", "ABT", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("AXP", new Aktie ("AXP", "American Express", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("BA", new Aktie ("BA", "Boeing", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("BAC", new Aktie ("BAC", "BAC", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("C", new Aktie ("C", "C", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("CAT", new Aktie ("CAT", "Caterpillar", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("CL", new Aktie ("CL", "CL", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("DIS", new Aktie ("DIS", "Walt Disney", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("DVN", new Aktie ("DVN", "DVN", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("EMR", new Aktie ("EMR", "EMR", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("FDX", new Aktie ("FDX", "FDX", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("GE", new Aktie ("GE", "General Electric", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("GSBD", new Aktie ("GSBD", "GSBD", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("HP", new Aktie ("HP", "HP", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("IBM", new Aktie ("IBM", "IBM", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("JNJ", new Aktie ("JNJ", "Johnson&Johnson", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("JPM", new Aktie ("JPM", "JPMorgan Chase", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("KO", new Aktie ("KO", "Coca-Cola", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("LMT", new Aktie ("LMT", "LMT", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("MCD", new Aktie ("MCD", "McDonald's", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("MMM", new Aktie ("MMM", "3M", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("MON", new Aktie ("MON", "MON", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("MRK", new Aktie ("MRK", "Merck", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("MRO", new Aktie ("MRO", "MRO", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("MS", new Aktie ("MS", "Microsoft", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("PEP", new Aktie ("PEP", "PEP", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("PFE", new Aktie ("PFE", "Pfizer", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("PG", new Aktie ("PG", "Procter&Gamble", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("T", new Aktie ("T", "T", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("TWX", new Aktie ("TWX", "TWX", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("TXN", new Aktie ("TXN", "TXN", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("UNH", new Aktie ("UNH", "UNH", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("UNP", new Aktie ("UNP", "UNP", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("UPS", new Aktie ("UPS", "UPS", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("WFC", new Aktie ("WFC", "WFC", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("WMT", new Aktie ("WMT", "WMT", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("XOM", new Aktie ("XOM", "ExxonMobil", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("^DJA", new Aktie ("^DJA", "^DJA", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));
		verzeichnis.put("^GOX", new Aktie ("^GOX", "^GOX", Aktien.INDEXDOWJONES, Aktien.BOERSENYSE));

	}

}
