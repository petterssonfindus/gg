package util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Util {
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

	
}
