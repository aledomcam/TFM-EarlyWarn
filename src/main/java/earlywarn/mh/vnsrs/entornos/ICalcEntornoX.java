package earlywarn.mh.vnsrs.entornos;

import earlywarn.definiciones.OperaciónLínea;

/**
 * Interfaz implementada por las clases que permiten calcular el siguiente entorno horizontal al que cambiar
 */
public interface ICalcEntornoX {
	/**
	 * Obtiene cuál será la opreración de variación de líneas (apertura o cierre) que se ejecutará en el siguiente
	 * entorno.
	 * @param numLíneasAbiertas Número de líneas actualmente abiertas
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 * @return Operación horizontal (cierre o apertura de líneas) que se debería realizar en el próximo entorno
	 */
	OperaciónLínea entornoX(int numLíneasAbiertas, double temperaturaActual);

	/**
	 * Debe ser llamado tras considerar una nueva solución
	 * @param numLíneasAbiertas Número de líneas abiertas en la solución actual
	 * @param operaciónRealizada Operación realizada para llegar a la nueva solución
	 * @param nuevoFitness Fitness de la nueva solución considerada
	 * @param fitnessActual Fitness de la solución actual
	 */
	void registrarNuevaSolución(int numLíneasAbiertas, OperaciónLínea operaciónRealizada, double nuevoFitness,
								double fitnessActual);
}
