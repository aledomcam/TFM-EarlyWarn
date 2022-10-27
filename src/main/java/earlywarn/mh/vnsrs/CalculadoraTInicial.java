package earlywarn.mh.vnsrs;

import earlywarn.definiciones.IRecocidoSimulado;
import org.neo4j.logging.Log;

/**
 * Clase usada para determinar la mejor temperatura inicial para un cierto algoritmo de recocido simulado
 */
public class CalculadoraTInicial {
	private final Log log;
	private final IRecocidoSimulado algoritmo;

	public CalculadoraTInicial(IRecocidoSimulado algoritmo, Log log) {
		this.log = log;
		this.algoritmo = algoritmo;
	}

	/**
	 * Determina qué parámetro de temperatura inicial debería usar el algoritmo en base al porcentaje de
	 * soluciones aceptadas inicial establecido.
	 * @param porcentajeAceptación Porcentaje de aceptación deseado. Debe estar entre 0 y 1.
	 * @param tolerancia Diferencia máxima permitida entre el porcentaje de aceptación obtenido y el indicado
	 *                   en el parámetro anterior. Debe ser > 0.
	 * @param iteraciones Número de iteraciones a realizar cada vez que se quiera determinar el porcentaje de
	 *                    aceptación asociado a la temperatura actual
	 * @return Valor de temperatura inicial que debería establecerse para lograr que se acepte un
	 * (porcentajeAceptación)+-(tolerancia) % de las soluciones exploradas.
	 */
	public float determinarTInicial(float porcentajeAceptación, float tolerancia, int iteraciones) {
		float ret;

		if (porcentajeAceptación < 0 || porcentajeAceptación > 1) {
			throw new IllegalArgumentException("El porcentaje de aceptación inicial deseado debe estar entre 0 y 1");
		} else if (tolerancia <= 0) {
			throw new IllegalArgumentException("La tolerancia debe ser mayor que 0");
		}

		float tMax = 1;
		float tMin = 0;

		log.info("Inicio cálculo de temperatura inicial");

		// Fase 1: Duplicamos tMax hasta que logremos un porcentaje de aceptación mayor al buscado
		float porcentajeActual;
		do {
			porcentajeActual = algoritmo.calcularPorcentajeAceptadas(tMax, iteraciones);
			log.info("tMax = " + tMax + ", % aceptadas = " + porcentajeActual);
			if (porcentajeActual < porcentajeAceptación) {
				tMax *= 2;
			}
		} while (porcentajeActual < porcentajeAceptación);

		// Fase 2: Hacemos una búsqueda binaria entre tMax y tMin hasta encontrar un valor de temperatura adecuado
		ret = tMax;
		while (Math.abs(porcentajeActual - porcentajeAceptación) > tolerancia) {
			float tMedio = (tMax + tMin) / 2;
			porcentajeActual = algoritmo.calcularPorcentajeAceptadas(tMedio, iteraciones);
			log.info("tActual = " + tMedio + ", % aceptadas = " + porcentajeActual);
			if (porcentajeActual < porcentajeAceptación) {
				tMin = tMedio;
			} else {
				tMax = tMedio;
			}
			ret = tMedio;
		}

		return ret;
	}
}
