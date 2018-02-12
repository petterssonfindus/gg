package util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
	private static final Logger log = LogManager.getLogger(Util.class);

	public static String separator = " ; ";

	/**
	 * macht aus einem GregorianCal-Datum ein String
	 * kann für Text-Ausgabe und SQL-Abfragen genutzt werden
	 * 
	 * @param date das Datum, das umgewandelt werden soll
	 * @return ein String oder der Wert 'NULL'
	 */
	public static String formatDate(GregorianCalendar cal) {
		if (cal == null)
			return "NULL";
		else {
			String dateString;
			// aus dem Caldendar ein Datum erzeugen
			java.sql.Date date = new java.sql.Date (cal.getTimeInMillis());
			// Datumsformat festlegen
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// das Datum in dem Format als String ausgeben
			dateString = formatter.format(date);
			return dateString;
		}
	}
	/**
	 * prüft, ob es sich um den gleichen Kalendertag handelt
	 * @param tag1
	 * @param tag2
	 * @return
	 */
	public static boolean istGleicherKalendertag (GregorianCalendar tag1, GregorianCalendar tag2) {
		boolean result = false;
		// prüft das Jahr
		if (tag1.get(Calendar.YEAR) == tag2.get(Calendar.YEAR)) {
			// prüft den Tag des Jahres (von 1 bis 366) 
			if (tag1.get(Calendar.DAY_OF_YEAR) == tag2.get(Calendar.DAY_OF_YEAR)) {
				result = true; 
			}
		}
		return result; 
	}
	/**
	 * prüft, ob der Stichtag sich innerhalb der Zeitspanne befindet 
	 * @param stichtag der angefragte Stichtag
	 * @param beginn
	 * @param ende
	 * @return true, wenn Stichtag innerhalb der Zeitspanne
	 */
	public static boolean istInZeitspanne (GregorianCalendar stichtag, GregorianCalendar beginn, GregorianCalendar ende) {
		if (stichtag == null) log.error("Inputvariable stichtag ist null");
		if (beginn == null) log.error("Inputvariable beginn ist null");
		if (ende == null) log.error("Inputvariable ende ist null");

		boolean result = false; 
		if (stichtag.before(ende) && stichtag.after(beginn)) {
			result = true; 
		}
		return result; 
	}
	
	/**
	 * Formatiert eine float-Zahl in deutscher Schreibweise mit Komma ohne Punkt. 
	 * @param input
	 * @return
	 */
	public static String toString( float input) {
		DecimalFormat df = (DecimalFormat)DecimalFormat.getInstance(Locale.GERMAN);
		df.applyPattern( "#,###,##0.00" );
		String result = df.format(input);
		return result;
	}
	
	/**
	 * parst einen String im Format jjjj-mm-tt
	 * @param datum
	 * @return
	 */
	public static GregorianCalendar parseDatum (String datum) {
		
        int jahr = Integer.parseInt(datum.substring(0, 4));
        int monat = Integer.parseInt(datum.substring(5, 7));
        int tag = Integer.parseInt(datum.substring(8, 10));
        GregorianCalendar result = new GregorianCalendar(jahr, monat-1, tag);
        return result; 
	}
	
	public static String getLineSeparator () {
		return System.getProperty("line.separator");
	}
	public static String getFileSeparator () {
		return System.getProperty("file.separator");
	}
	
	/**
	 * ermittelt das User-Directory in einem Windows-System 
	 * user.country The ISO code of the operating system's (or local user's) configured country. 
	 * user.dir The local directory from which the Java process has been started, and from which files will be read/written by default unless a path is specified. 
	 * user.home The current user's "home" directory, such as C:\Users\Fred on Windows systems, or /home/fred/ on UNIX-like systems. 
	 * user.language The ISO code of the operating system's (or local user's) configured language, such as "en" for English. 
	 * user.name The local user's system user name. On Windows systems, this is typically close to a "real life" name. On UNIX-like systems, it is common for user names to be all lower case letters. 
	 */
	public static String getUserProperty (String property) {
		return System.getProperty("user." + property);
	}

	
}
