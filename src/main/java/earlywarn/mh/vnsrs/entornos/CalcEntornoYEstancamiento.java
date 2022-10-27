package earlywarn.mh.vnsrs.entornos;

import earlywarn.main.Utils;
import earlywarn.mh.vnsrs.config.ConfigVNS;
import earlywarn.mh.vnsrs.ConversorLíneas;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que determina el entorno vertical a usar (el número de líneas a abrir o cerrar) comprobando hasta qué punto
 * está estancada la búsqueda. También tiene en cuenta la temperatura.
 */
public class CalcEntornoYEstancamiento implements ICalcEntornoY {
	private final ConfigVNS config;
	// Temperatura inicial del RS
	private final double temperaturaInicial;
	// Número total de líneas
	private final int numLíneas;

	private final MemoriaEstadosY estadosY;

	public CalcEntornoYEstancamiento(ConfigVNS configVNS, ConversorLíneas conversorLíneas, int numLíneas,
									 double temperaturaInicial) {
		config = configVNS;
		this.temperaturaInicial = temperaturaInicial;
		this.numLíneas = numLíneas;
		estadosY = new MemoriaEstadosY(config, conversorLíneas, numLíneas);
	}

	public void registrarNuevaPosición(List<String> líneasVariadas) {
		// Añadir esta entrada a la memoria que almacena las últimas posiciones visitadas
		estadosY.insertar(líneasVariadas);
	}

	/**
	 * Calcula el número de entorno vertical al que cambiar en base al grado de estancamiento de la ejecución.
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 * @return Número de entorno vertical al que cambiar
	 */
	@Override
	public int numEntornoY(double temperaturaActual) {
		/*
		 * Tenemos que calcular el valor de estancamiento para cada porcentaje de líneas para el que se hace esta
		 * comprobación
		 */
		List<Double> valoresEstancamiento = new ArrayList<>();
		for (int i = 1; i <= config.numComprobaciones; i++) {
			int numLíneasActual = Math.round(config.getDistComprobacionesY() * i * numLíneas);
			// Iteraciones que hace que no logramos al menos (numLíneas) de distancia con respecto a la solución actual
			int iteraciones = estadosY.iteracionesSinDistancia(numLíneasActual);
			double estancamiento = iteraciones / (config.getUmbralIt() * i);

			// El valor de estancamiento se ajusta en función del % de temperatura restante
			double estancamientoAjustado = estancamiento * Math.min(1, Math.sqrt(temperaturaActual / temperaturaInicial));
			// También se tiene en cuenta la velocidad de variación de líneas especificada por el usuario
			estancamientoAjustado *= Utils.log2(config.líneasPorIt);

			valoresEstancamiento.add(estancamientoAjustado);
		}
		// El valor de estancamiento final es la media de todos los calculados
		double estancamientoMedio = Utils.getMedia(valoresEstancamiento);
		// El estancamiento medio se redondea hacia abajo para determinar el número de entorno vertical
		int numEntorno = (int) Math.floor(estancamientoMedio);
		// Comprobar que no excedemos el entorno máximo permitido
		int numEntornoMax = config.getMaxEntornoY(numLíneas);
		if (numEntorno > numEntornoMax) {
			numEntorno = numEntornoMax;
		}
		return numEntorno;
	}
}
