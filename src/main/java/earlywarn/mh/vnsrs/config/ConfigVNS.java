package earlywarn.mh.vnsrs.config;

import earlywarn.main.Utils;

/**
 * Clase que almacena los valores necesarios para configurar VNS
 */
public class ConfigVNS {
	/*
	 * La memoria en la dimensión de cambio de entorno Y será <este valor> veces el valor de umbral de iteraciones
	 * para la comprobación más grande
	 */
	private static final float MULT_TAMAÑO_MEMORIA_Y = 3;

	// --- Valores leídos del fichero de configuración ---

	// Cada cuántas iteraciones se debería re-evaluar si es necesario un cambio de entorno
	public int itCambioEntorno;

	// Si usar o no los cambios de entorno complejos
	public boolean cambioEntornoXComplejo;
	public boolean cambioEntornoYComplejo;

	/*
	 * Factor de tamaño de la memoria usada para el cambio de entorno horizontal (abrir o cerrar líneas). El valor
	 * final se obtiene multiplicando por el número de líneas.
	 */
	public float tamañoMemoriaX;
	/*
	 * Al seleccionar entradas de la memoria de casos X, se elegirán los que tengan un número de líneas abiertas
	 * del +-<este parámetro>% con respecto a las que haya abiertas actualmente.
	 */
	public float distanciaMemoriaX;

	/*
	 * Hasta qué porcentaje de líneas totales se van a realizar comprobaciones para asegurar la diversidad
	 * de las soluciones
	 */
	public float maxPorcentLíneas;
	/*
	 * Número de comprobaciones totales a realizar (cada una con un porcentaje diferente, desde el máximo indicado
	 * hasta 0 (no inclusive))
	 */
	public int numComprobaciones;
	/*
	 * Dos valores usados para marcar el ritmo de diversificación. Especifican cada cuántas iteraciones se debería
	 * producir una variación en el número de líneas abiertas de al menos el porcentaje indicado.
	 */
	public float porcentLíneas;
	public int iteraciones;
	/*
	 * Determina la velocidad de operación del algoritmo al abrir y cerrar líneas cuando justo se logra una
	 * diversificación igual a la especificada con los dos parámetros anteriores.
	 */
	public float líneasPorIt;
	/*
	 * Porcentaje máximo de líneas que se pueden variar de una sola vez. El valor
	 * final se obtiene multiplicando por el número de líneas.
	 */
	public float variaciónMax;


	// --- Valores derivados de los leídos del XML ---
	/*
	 * Cada vez que tardemas más de <umbralIt> iteraciones en variar al menos <getDistComprobacionesY()> líneas,
	 * el valor de estancamiento de la primera de las comprobaciones aumentará en 1. El esto de comprobaciones usan
	 * n * <umbralIt> como umbral, siendo n el número de comprobación (desde n = 1 para la primera hasta
	 * n = <numComprobaciones> para la última)
	 * Vale -1 si aún no se ha inicializado.
	 */
	private double umbralIt = -1;

	/*
	 * Número de entorno vertical máximo permitido. Se calcula a partir del número máximo de líneas a variar.
	 * Vale -1 si aún no se ha inicializado.
	 */
	private int maxEntornoY = -1;

	/**
	 * @return Distancia a la que se encuentra cada una de las comprobaciones de estancamiento usadas en el cambio
	 * de entorno vertical. La distancia se mide en porcentaje de las líneas totales (0-1).
	 */
	public float getDistComprobacionesY() {
		return maxPorcentLíneas / numComprobaciones;
	}

	public double getUmbralIt() {
		if (umbralIt < 0) {
			umbralIt = iteraciones * getDistComprobacionesY() / porcentLíneas;
		}
		return umbralIt;
	}

	/**
	 * @return Número de entradas que deberían almacenarse en la memoria de cambio de entorno vertical
	 */
	public int getTamañoMemoriaY() {
		return ((Long) Math.round(getUmbralIt() * numComprobaciones * MULT_TAMAÑO_MEMORIA_Y)).intValue();
	}

	/**
	 * Obtiene el valor máximo permitido para los cambios de entorno verticales
	 * @param numLíneas Número total de líneas
	 * @return Máximo valor permitido para los cambios de entorno verticales
	 */
	public int getMaxEntornoY(int numLíneas) {
		if (maxEntornoY == -1) {
			float maxLíneasAVariar = variaciónMax * numLíneas;
			maxEntornoY = Utils.redondearAPotenciaDeDosExponente(maxLíneasAVariar);
		}
		return maxEntornoY;
	}
}
