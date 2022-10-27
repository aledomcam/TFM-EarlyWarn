package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.HomogeneidadAeropuertos;

import java.util.List;

/**
 * Restricción que comprueba que la homogeneidad sobre aeropuertos está dentro de un umbral percentual mínimo
 */
public class PorcentHomogeneidadAeropuertos extends Restricción {
	private final float min;

	public PorcentHomogeneidadAeropuertos(float min) {
		if (min < 0 || min > 1) {
			throw new IllegalArgumentException("El valor de la restricción del porcentaje de homogeneidad de " +
				"aeropuertos debe estar entre 0 y 1");
		}
		this.min = min;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof HomogeneidadAeropuertos) {
				HomogeneidadAeropuertos homogeneidadAeropuertos = (HomogeneidadAeropuertos) c;
				return homogeneidadAeropuertos.getPorcentaje() >= min;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.HOMOGENEIDAD_AEROPUERTOS, IDCriterio.HOMOGENEIDAD_AEROPUERTOS_LINEAL};
	}
}
