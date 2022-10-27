package earlywarn.definiciones;

/**
 * Representa un algoritmo de recocido simulado capaz de calcular el porcentaje de soluciones que acepta dada una cierta
 * temperatura inicial
 */
public interface IRecocidoSimulado {
	/**
	 * El algoritmo calcula el porcentaje de soluciones que acepta con la temperatura inicial indicada
	 * @param tInicial Temperatura inicial a usar
	 * @param numIteraciones Número de iteraciones a realizar antes de calcular qué porcentaje de las mismas
	 *                       han sido aceptadas
	 * @return Porcentaje de soluciones peores aceptadas al ejecutar el recocido simulado durante el número
	 * de iteraciones indicado con la temperatura inicial indicada.
	 */
	float calcularPorcentajeAceptadas(float tInicial, int numIteraciones);
}
