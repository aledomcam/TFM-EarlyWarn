package earlywarn.mh.vnsrs.entornos;

import earlywarn.definiciones.OperaciónLínea;

import java.util.Random;

/**
 * Determina el entorno horizontal (la operación a realizar) de forma aleatoria
 */
public class CalcEntornoXSimple implements ICalcEntornoX {
	private final Random random;

	public CalcEntornoXSimple() {
		random = new Random();
	}

	/**
	 * Devuelve el entorno horizontal, eligiéndolo de forma aleatoria
	 * @param numLíneasAbiertas Número de líneas actualmente abiertas
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 * @return Entorno horizontal aleatorio
	 */
	@Override
	public OperaciónLínea entornoX(int numLíneasAbiertas, double temperaturaActual) {
		return random.nextDouble() < 0.5 ? OperaciónLínea.ABRIR : OperaciónLínea.CERRAR;
	}

	@Override
	public void registrarNuevaSolución(int numLíneasAbiertas, OperaciónLínea operaciónRealizada,
									   double nuevoFitness, double fitnessActual) {
		// El cálculo aleatorio no necesita esta información para nada
	}
}
