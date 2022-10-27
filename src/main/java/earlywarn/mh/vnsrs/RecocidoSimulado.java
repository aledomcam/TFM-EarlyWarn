package earlywarn.mh.vnsrs;

import earlywarn.mh.vnsrs.config.ConfigRS;

import java.util.Random;

/**
 * Clase que implementa los método necesarios para ejecutar la metaheurística del recocido simulado
 */
public class RecocidoSimulado {
	/*
	 * Determina la penalización en la probabilidad de aceptación de las soluciones infactibles.
	 */
	private static final float PENALIZACIÓN_PROBABILIDAD_ACEPTACIÓN = 0.5f;

	// Iteración actual
	public int iteración;
	// Valor cacheado de la temperatura actual. Solo se recalcula cuando se cambia de fase.
	public double temperatura;
	// Valor cacheado del número de fase. Usado para saber cuándo hemos cambiado de fase.
	public int fase;

	private final ConfigRS config;
	private final Random rand;

	public RecocidoSimulado(ConfigRS config) {
		this.config = config;
		rand = new Random();

		iteración = 0;
		temperatura = config.tInicial;
		fase = 0;
	}

	/**
	 * Avanza a la siguiente iteración, actualizando la temperatura si es necesario.
	 */
	public void sigIter() {
		iteración++;
		int faseActual = iteración / config.itReducciónT;
		if (faseActual != fase) {
			fase = faseActual;
			temperatura = config.tInicial * Math.pow(config.alfa, fase);
		}
	}

	/**
	 * Dado el fitness de dos soluciones, determina si se debe aceptar la nueva solución o si se debe mantener
	 * la actual.
	 * @param fitnessActual Fitness de la solución con la que se trabaja actualmente
	 * @param fitnessNueva Fitness de la nueva solución a considerar
	 * @return True si debería aceptarse la nueva solución, false si debería mantenerse la anterior
	 */
	public boolean considerarSolución(double fitnessActual, double fitnessNueva) {
		if (fitnessNueva > fitnessActual) {
			return true;
		} else {
			return rand.nextDouble() < probabilidadAceptación(fitnessActual, fitnessNueva);
		}
	}

	/**
	 * Obtiene una versión penalizada del fitness de una nueva solución infactible. Su fitness recibirá una
	 * reducción de forma que la probabilidad de aceptación de la nueva solución será
	 * {@link #PENALIZACIÓN_PROBABILIDAD_ACEPTACIÓN} veces la original.
	 * @param fitnessActual Fitness de la solución actual
	 * @param fitnessNueva Fitness de la nueva solución infactible
	 * @return Versión reducida de {@code fitnessNueva}, de forma que la probabilidad de aceptación de la solución
	 * infactible será {@link #PENALIZACIÓN_PROBABILIDAD_ACEPTACIÓN} veces la original.
	 */
	public double penalizarFitness(double fitnessActual, double fitnessNueva) {
		double probabilidadObjetivo =
			probabilidadAceptación(fitnessActual, fitnessNueva) * PENALIZACIÓN_PROBABILIDAD_ACEPTACIÓN;
		return Math.log(probabilidadObjetivo) * temperatura + fitnessActual;
	}

	/**
	 * Devuelve la probabilidad de aceptación de una nueva solución dado su fitness y el de la solución actual
	 * @param fitnessActual Fitness de la solución actual
	 * @param fitnessNueva Fitness de la nueva solución que se está considerando
	 * @return Probabilidad de acpetación de la nueva solución
	 */
	public double probabilidadAceptación(double fitnessActual, double fitnessNueva) {
		return Math.exp((fitnessNueva - fitnessActual) / temperatura);
	}
}
