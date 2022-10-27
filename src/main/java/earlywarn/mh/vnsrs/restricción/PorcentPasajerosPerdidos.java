package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.NumPasajeros;

import java.util.List;

/**
 * Restricción que comprueba que el porcentaje de pasajeros perdidos está dentro de un umbral percentual máximo
 */
public class PorcentPasajerosPerdidos extends Restricción {
	private final float max;

	public PorcentPasajerosPerdidos(float max) {
		if (max < 0 || max > 1) {
			throw new IllegalArgumentException("El valor de la restricción del umbral de pasajeros perdidos debe " +
				"estar entre 0 y 1");
		}
		this.max = max;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof NumPasajeros) {
				NumPasajeros pasajeros = (NumPasajeros) c;
				return 1 - (pasajeros.getValorActual() / (float) pasajeros.getValorInicial()) <= max;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.NÚMERO_PASAJEROS};
	}
}
