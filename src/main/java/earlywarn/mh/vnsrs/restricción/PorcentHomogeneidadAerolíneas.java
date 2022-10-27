package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.HomogeneidadAerolíneas;

import java.util.List;

/**
 * Restricción que comprueba que la homogeneidad sobre aerolíneas está dentro de un umbral percentual mínimo
 */
public class PorcentHomogeneidadAerolíneas extends Restricción {
	private final float min;

	public PorcentHomogeneidadAerolíneas(float min) {
		if (min < 0 || min > 1) {
			throw new IllegalArgumentException("El valor de la restricción del porcentaje de homogeneidad de " +
				"aerolíneas debe estar entre 0 y 1");
		}
		this.min = min;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof HomogeneidadAerolíneas) {
				HomogeneidadAerolíneas homogeneidadAerolíneas = (HomogeneidadAerolíneas) c;
				return homogeneidadAerolíneas.getPorcentaje() >= min;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.HOMOGENEIDAD_AEROLÍNEAS, IDCriterio.HOMOGENEIDAD_AEROLÍNEAS_LINEAL};
	}
}
