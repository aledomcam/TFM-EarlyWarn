package earlywarn.mh.vnsrs.entornos;

import java.util.List;

/**
 * Interfaz implementada por las clases que permiten calcular el siguiente entorno vertical al que cambiar
 */
public interface ICalcEntornoY {
	/**
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 * @return Número de entorno vertical (afecta al número de líneas a abrir o cerrar) del próximo entorno
	 */
	int numEntornoY(double temperaturaActual);

	/**
	 * Registra la posición actual en la que se encuentra la búsqueda (la solución actual), especificando qué líneas
	 * han variado con respecto a la última posición. Debe llamarse al final de cada iteración.
	 * @param líneasVariadas Líneas que han cambiado de estado con respecto a la posición anterior
	 */
	void registrarNuevaPosición(List<String> líneasVariadas);
}
