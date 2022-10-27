package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.Utils;

import java.util.List;
import java.util.Map;

/**
 * Versión lineal del cálculo de la homogeneidad sobre aerolíneas. En vez de usar la desviación típica para el cálculo,
 * tiene en cuenta la desviación en el porcentaje de pasajeros restantes con respecto a la media de todas
 * las aerolíneas.
 * Este criterio tiene el mismo ID que su versión no lineal.
 */
public class HomogeneidadAerolíneasLineal extends HomogeneidadAerolíneas {

	public HomogeneidadAerolíneasLineal(Map<String, Long> pasajerosPorAerolíneaInicial) {
		super(pasajerosPorAerolíneaInicial);
		id = IDCriterio.HOMOGENEIDAD_AEROLÍNEAS_LINEAL;
	}

	@Override
	protected double getPorcentajeFinal(List<Double> porcentajes) {
		/*
		 * Primero calculamos la media de los porcentajes y luego la desviación media de los diferentes elementos
		 * con respecto a esta media calculada.
		 */
		double pasajerosRestantesMedio = Utils.getMedia(porcentajes);
		return 1 - Utils.getDesviaciónMedia(porcentajes, pasajerosRestantesMedio);
	}
}
