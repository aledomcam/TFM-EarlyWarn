package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.HomogeneidadAerolíneas;

import java.util.List;

/**
 * Restricción que comprueba que ninguna aerolínea pierde más vuelos que un cierto umbral porcentual máximo
 */
public class PorcentVuelosPerdidosAerolíneas extends Restricción {
	private final float max;

	public PorcentVuelosPerdidosAerolíneas(float max) {
		if (max < 0 || max > 1) {
			throw new IllegalArgumentException("El valor de la restricción del porcentaje de vuelos perdidos " +
				"en las aerolíneas debe estar entre 0 y 1");
		}
		this.max = max;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof HomogeneidadAerolíneas) {
				HomogeneidadAerolíneas homogeneidadAerolíneas = (HomogeneidadAerolíneas) c;
				return homogeneidadAerolíneas.getPérdidaMáxima() <= max;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.HOMOGENEIDAD_AEROLÍNEAS, IDCriterio.HOMOGENEIDAD_AEROLÍNEAS_LINEAL};
	}
}
