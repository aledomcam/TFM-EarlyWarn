package earlywarn.mh.vnsrs;

import earlywarn.mh.vnsrs.entornos.EntornoVNS;

/**
 * Almacena las estadísticas de una sola iteración
 */
public class EstadísticasIteración {
	public final int numIter;
	public final int numLíneasAbiertas;
	public final EntornoVNS entorno;
	public final double temperatura;
	public final double fitnessActual;
	public final double fitnessMejor;
	public final Double probAceptación;

	/**
	 * Crea una instancia que representa las estadísticas de una sola iteración
	 * @param numIter Número de la iteración que acaba de concluir
	 * @param numLíneasAbiertas Número de líneas que han estado abiertas durante la iteración
	 * @param entorno Entorno en el que se ha ejecutado esta iteración
	 * @param temperatura Valor de temperatura usado durante esta iteración
	 * @param fitnessActual Fitness de la solución considerada en esta iteración
	 * @param fitnessMejor Finess de la mejor solución encontrada hasta el fin de esta iteración
	 * @param probAceptación Probabilidad de aceptar la solución considerada en la iteración actual. Se capa a 1 si
	 *                       es mayor. Null para no inclur este dato en esta iteración.
	 */
	public EstadísticasIteración(int numIter, int numLíneasAbiertas, EntornoVNS entorno, double temperatura,
								 double fitnessActual, double fitnessMejor, Double probAceptación) {
		this.numIter = numIter;
		this.numLíneasAbiertas = numLíneasAbiertas;
		this.entorno = entorno;
		this.temperatura = temperatura;
		this.fitnessActual = fitnessActual;
		this.fitnessMejor = fitnessMejor;
		if (probAceptación != null && probAceptación > 1) {
			probAceptación = 1.0;
		}
		this.probAceptación = probAceptación;
	}

	/**
	 * Devuelve los datos de esta entrada representados como una string. Cada dato se separa por una coma.
	 * La representación de algunos datos puede ser transformada para poder representarlo como un solo valor
	 * textual (por ejemplo, el entorno se convierte a un entero indicando el número de líneas a variar).
	 */
	@Override
	public String toString() {
		return numIter + "," + numLíneasAbiertas + "," + entorno.getNumLíneasConSigno() + "," + temperatura + "," +
			fitnessActual + "," + fitnessMejor + "," + (probAceptación == null ? "" : probAceptación);
	}

	/**
	 * @return String con los nombres de todos los datos cuyos valores se incluyen en la salida
	 * del método {@link #toString()}.
	 */
	public static String cabecera() {
		return "Iteración, Líneas abiertas, Líneas a variar, Temperatura, Fitness actual, Fitness mejor, " +
			"Prob aceptación";
	}
}
