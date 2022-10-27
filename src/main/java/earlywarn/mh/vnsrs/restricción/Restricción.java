package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.mh.vnsrs.config.ListaParámetros;

import java.util.List;

/**
 * Representa una restricción que limita el rango de soluciones factibles durante la ejecución del algoritmo.
 * Cada restricción se representa con una subclase, que se identifica con un valor del enum {@link IDRestricción}.
 */
public abstract class Restricción {
	/**
	 * Crea una nueva instancia de una restricción en base al identificador especificado y una serie de parámetros.
	 * @param idRestricción Identificador de restricción. Debe corresponder con uno de los valores del enum
	 * {@link IDRestricción}.
	 * @param parámetros Lista de parámetros asociados al criterio a crear
	 * @throws IllegalArgumentException Si el identificador especificado no se corresponde con ningún valor del
	 * enum {@link IDRestricción}.
	 * @return Nueva instancia de alguna de las subclases de la actual
	 */
	public static Restricción crear(String idRestricción, ListaParámetros parámetros) {
		IDRestricción idRestricciónEnum;
		try {
			idRestricciónEnum = IDRestricción.valueOf(idRestricción);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El ID de restricción especificado (" + idRestricción + ") no se " +
				"corresponde con ninguna restricción", e);
		}

		switch (idRestricciónEnum) {
			case PORCENT_RIESGO:
				return new PorcentRiesgo(parámetros.getParamFloat("max"));
			case PORCENT_PASAJEROS_PERDIDOS:
				return new PorcentPasajerosPerdidos(parámetros.getParamFloat("max"));
			case PORCENT_INGRESOS:
				return new PorcentIngresos(parámetros.getParamFloat("min"));
			case INGRESOS:
				return new Ingresos(parámetros.getParamFloat("min"));
			case PORCENT_HOMOGENEIDAD_AEROLÍNEAS:
				return new PorcentHomogeneidadAerolíneas(parámetros.getParamFloat("min"));
			case PORCENT_HOMOGENEIDAD_AEROPUERTOS:
				return new PorcentHomogeneidadAeropuertos(parámetros.getParamFloat("min"));
			case PORCENT_VUELOS_PERDIDOS_AEROLÍNEAS:
				return new PorcentVuelosPerdidosAerolíneas(parámetros.getParamFloat("max"));
			case PORCENT_VUELOS_PERDIDOS_AEROPUERTOS:
				return new PorcentVuelosPerdidosAeropuertos(parámetros.getParamFloat("max"));
			case PORCENT_CONECTIVIDAD:
				return new PorcentConectividad(parámetros.getParamFloat("min"));
			default:
				throw new IllegalStateException("El ID de restricción " + idRestricción + " no se ha asociado con " +
					"ninguna subclase de Restricción");
		}
	}

	/**
	 * Comprueba si el estado actual de una serie de criterios cumple la restricción
	 * @param criterios Lista de criterios a comprobar
	 * @return True si los criterios de la lista cumplen la restricción, false en caso contrario.
	 */
	public abstract boolean cumple(List<Criterio> criterios);

	/**
	 * Devuelve una lista con los criterios necesarios para el cálculo de esta restricción. La restricción solo
	 * necesitará que al menos uno de los criterios listados esté presente para poder ser calculada.
	 * @return Lista que contiene los criterios necesarios para el cálculo de esta restricción
	 */
	public abstract IDCriterio[] getCriteriosAsociados();
}
