package earlywarn.definiciones;

/**
 * Lista todos los criterios que se están usando para puntuar posibles soluciones al problema del cierre de líneas
 * aéreas. Cada criterio tiene asignado un ID de este enum y cada ID solo puede estar asignado a un crierio.
 */
public enum IDCriterio {
	// Riesgo importado total
	RIESGO_IMPORTADO,
	// Número de pasajeros. Usado para aproximar el impacto económico derivado de la pérdida de pasajeros.
	NÚMERO_PASAJEROS,
	// Ingresos turísticos derivados de los viajeros de entrada
	INGRESOS_TURÍSTICOS,
	/*
	 * Homogeneidad sobre aerolíneas al cancelar vuelos. Intenta que el porcentaje de vuelos que pierde cada aerolínea
	 * sea lo más homogéneo posible.
	 */
	HOMOGENEIDAD_AEROLÍNEAS,
	/*
	 * Versión lineal de HOMOGENEIDAD_AEROLÍNEAS que usa la diferencia media entre el porcentaje de vuelos restantes
	 * de cada aerolínea para calcular el valor
	 */
	HOMOGENEIDAD_AEROLÍNEAS_LINEAL,
	/*
	 * Homogeneidad sobre aeropuertos al cancelar vuelos. Intenta que el porcentaje de vuelos que pierde cada aeropuerto
	 * sea lo más homogéneo posible.
	 */
	HOMOGENEIDAD_AEROPUERTOS,
	/*
	 * Versión lineal de HOMOGENEIDAD_AEROPUERTOS que usa la diferencia media entre el porcentaje de vuelos restantes
	 * de cada aeropuerto para calcular el valor
	 */
	HOMOGENEIDAD_AEROPUERTOS_LINEAL,
	/*
	 * Conectividad de la red de tráfico aéreo. Solo tiene en cuenta la parte de esta conectividad que se puede perder
	 * al cerrar vuelos que entran al país actual.
	 */
	CONECTIVIDAD
}
