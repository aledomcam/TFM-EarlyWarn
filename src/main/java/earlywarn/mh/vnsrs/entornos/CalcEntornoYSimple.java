package earlywarn.mh.vnsrs.entornos;

import earlywarn.main.Utils;
import earlywarn.mh.vnsrs.config.ConfigVNS;

import java.util.List;

/**
 * Determina qué entorno vertical usar en función del porcentaje de temperatura restante
 */
public class CalcEntornoYSimple implements ICalcEntornoY {
	/*
	 * Constante por la que se multiplica el porcentaje de temperatura restante para el cálculo del entorno
	 * vertical simple
	 */
	private static final float MULT_PORCENT_TEMPERATURA_ENTORNO_Y_SIMPLE = 3.0f;

	private final ConfigVNS config;
	// Temperatura inicial del RS
	private final double temperaturaInicial;
	// Número total de líneas
	private final int numLíneas;

	public CalcEntornoYSimple(ConfigVNS configVNS, int numLíneas, double temperaturaInicial) {
		config = configVNS;
		this.temperaturaInicial = temperaturaInicial;
		this.numLíneas = numLíneas;
	}

	/**
	 * Determina el número de entorno a usar en función del porcentaje de temperatura restante.
	 * Más concretamente, se usa la raíz cuadrada de la temperatura multiplicada por la constante
	 * {@link #MULT_PORCENT_TEMPERATURA_ENTORNO_Y_SIMPLE}.
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 * @return Número de entorno vertical al que cambiar
	 */
	@Override
	public int numEntornoY(double temperaturaActual) {
		int numMaxLíneas = (int) Math.pow(2, config.getMaxEntornoY(numLíneas));
		double coeficienteTemperatura =
			Math.min(1, Math.sqrt(temperaturaActual / temperaturaInicial) * MULT_PORCENT_TEMPERATURA_ENTORNO_Y_SIMPLE);
		float numLíneasAVariar = (float) (numMaxLíneas * coeficienteTemperatura);
		return Utils.redondearAPotenciaDeDosExponente(numLíneasAVariar);
	}

	@Override
	public void registrarNuevaPosición(List<String> líneasVariadas) {
		// Nada que registrar ya que esta versión no usa una memoria
	}
}
