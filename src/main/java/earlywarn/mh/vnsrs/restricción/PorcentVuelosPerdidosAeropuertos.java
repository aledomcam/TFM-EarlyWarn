package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.HomogeneidadAeropuertos;

import java.util.List;

/**
 * Restricción que comprueba que ningún aeropuerto pierde más vuelos que un cierto umbral porcentual máximo
 */
public class PorcentVuelosPerdidosAeropuertos extends Restricción {
	private final float max;

	public PorcentVuelosPerdidosAeropuertos(float max) {
		if (max < 0 || max > 1) {
			throw new IllegalArgumentException("El valor de la restricción del porcentaje de vuelos perdidos " +
				"en los aeropuertos debe estar entre 0 y 1");
		}
		this.max = max;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof HomogeneidadAeropuertos) {
				HomogeneidadAeropuertos homogeneidadAeropuertos = (HomogeneidadAeropuertos) c;
				return homogeneidadAeropuertos.getPérdidaMáxima() <= max;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.HOMOGENEIDAD_AEROPUERTOS, IDCriterio.HOMOGENEIDAD_AEROPUERTOS_LINEAL};
	}
}
