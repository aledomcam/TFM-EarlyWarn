package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Conectividad;
import earlywarn.main.modelo.criterio.Criterio;

import java.util.List;

/**
 * Restricción que comprueba que la conectividad restante está dentro de un umbral percentual mínimo
 */
public class PorcentConectividad extends Restricción {
	private final float min;

	public PorcentConectividad(float min) {
		if (min < 0 || min > 1) {
			throw new IllegalArgumentException("El valor de la restricción del umbral porcentual de conectividad " +
				"debe estar entre 0 y 1");
		}
		this.min = min;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof Conectividad) {
				Conectividad conectividad = (Conectividad) c;
				return conectividad.getPorcentaje() >= min;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.CONECTIVIDAD};
	}
}
