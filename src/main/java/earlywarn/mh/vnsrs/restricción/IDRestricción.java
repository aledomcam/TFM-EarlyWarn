package earlywarn.mh.vnsrs.restricción;

/**
 * Identificadores de las posibles restricciones que pueden imponerse a las soluciones del algoritmo VNS-RS.
 * Cada restricción tiene una serie de parámetros que deben especificarse en el fichero de configuración cuando
 * la restricción se fija.
 */
public enum IDRestricción {
	/*
	 * El valor porcentual del riesgo importado debe ser menor o igual que umbral indicado
	 * Parámetros:
	 * 	max (float): Valor porcentual máximo admitido (0-1)
	 */
	PORCENT_RIESGO,
	/*
	 * El porcentaje de pasajeros totales perdidos debe ser menor o igual que umbral indicado
	 * Parámetros:
	 * 	max (float): Valor porcentual máximo admitido (0-1)
	 */
	PORCENT_PASAJEROS_PERDIDOS,
	/*
	 * El porcentaje de ingresos por turismo debe ser mayor o igual que umbral indicado
	 * Parámetros:
	 * 	min (float): Valor porcentual mínimo admitido (0-1)
	 */
	PORCENT_INGRESOS,
	/*
	 * El valor total de ingresos por turismo debe ser mayor o igual que umbral indicado
	 * Parámetros:
	 * 	min (float): Valor mínimo admitido (>= 0)
	 */
	INGRESOS,
	/*
	 * El grado de homogeneidad porcentual entre las aerolíneas debe ser mayor o igual que el umbral indicado
	 * Parámetros:
	 * 	min (float): Valor porcentual mínimo admitido (0-1)
	 */
	PORCENT_HOMOGENEIDAD_AEROLÍNEAS,
	/*
	 * El grado de homogeneidad porcentual entre los aeropuertos debe ser mayor o igual que el umbral indicado
	 * Parámetros:
	 * 	min (float): Valor porcentual mínimo admitido (0-1)
	 */
	PORCENT_HOMOGENEIDAD_AEROPUERTOS,
	/*
	 * Ninguna aerolínea puede perder más de un cierto umbral porcentual de vuelos
	 * Parámetros:
	 * 	max (float): Valor porcentual máximo admitido (0-1)
	 */
	PORCENT_VUELOS_PERDIDOS_AEROLÍNEAS,
	/*
	 * Ningún aeropuerto puede perder más de un cierto umbral porcentual de vuelos
	 * Parámetros:
	 * 	max (float): Valor porcentual máximo admitido (0-1)
	 */
	PORCENT_VUELOS_PERDIDOS_AEROPUERTOS,
	/*
	 * El porcentaje de conectividad restante debe ser mayor o igual que umbral indicado
	 * Parámetros:
	 * 	min (float): Valor porcentual mínimo admitido (0-1)
	 */
	PORCENT_CONECTIVIDAD
}
