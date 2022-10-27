package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.Utils;
import earlywarn.main.modelo.datoid.Aeropuerto;
import earlywarn.main.modelo.datoid.RegistroDatoID;

import java.util.List;
import java.util.Map;

/**
 * Versión lineal del cálculo de la homogeneidad sobre aeropuertos. En vez de usar la desviación típica para el cálculo,
 * tiene en cuenta la desviación en el porcentaje de pasajeros restantes con respecto a la media de todos
 * los aeropuertos.
 * Este criterio tiene el mismo ID que su versión no lineal.
 */
public class HomogeneidadAeropuertosLineal extends HomogeneidadAeropuertos {

	public HomogeneidadAeropuertosLineal(Map<String, Long> pasajerosPorAeropuertoInicial, String idPaís,
										 RegistroDatoID<Aeropuerto> aeropuertos) {
		super(pasajerosPorAeropuertoInicial, idPaís, aeropuertos);
		id = IDCriterio.HOMOGENEIDAD_AEROPUERTOS_LINEAL;
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
