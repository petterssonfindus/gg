package depot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Repräsentiert den aktuellen Wertpapierbestand zu einem Zeitpunkt. 
 * @author oskar
 *
 */
public class Wertpapierbestand {
	private static final Logger log = LogManager.getLogger(Wertpapierbestand.class);

	String wertpapier; 
	float bestand;
	float durchschnittskurs; 
	
	Wertpapierbestand (String wertpapier) {
		this.wertpapier = wertpapier; 
	}
	/**
	 * Wertpapiere wurden gekauft und werden jetzt eingeliefert
	 * @param stueckzahl
	 * @param kurs
	 * @return der Bestand an Wertpapieren 
	 */
	float liefereWertpapierEin (float stueckzahl, float kurs) {
		if (stueckzahl == 0) log.error("Inputvariable stueckzahl = 0");
		if (kurs == 0) log.error("Inputvariable Kurs = 0");
		this.durchschnittskurs = ((this.bestand * this.durchschnittskurs) + (stueckzahl * kurs)) / (this.bestand + stueckzahl);
		this.bestand += stueckzahl;
		return this.bestand;
	}
	
	/**
	 * Wertpapiere wurden verkauft und werden jetzt ausgeliefert
	 * @param stueckzahl
	 * @param kurs
	 * @return der Restsbestand an Wertpapieren 
	 */
	float EntnehmeWertpapier (float stueckzahl, float kurs) {
		if (stueckzahl == 0) log.error("Inputvariable stueckzahl = 0");
		if (kurs == 0) log.error("Inputvariable Kurs = 0");
		if (stueckzahl > (this.bestand + 0.01)) log.info("Achtung: Leerverkauf");
		if ((this.bestand - stueckzahl) < 0.01) {	// es ist nichts mehr übrig 
			this.bestand = 0;
			this.durchschnittskurs = 0;
		}
		else {
			this.durchschnittskurs = ((this.bestand * this.durchschnittskurs) + (stueckzahl * kurs)) / (this.bestand + stueckzahl);
			this.bestand -= stueckzahl; 
		}
		return this.bestand;
	}

}
