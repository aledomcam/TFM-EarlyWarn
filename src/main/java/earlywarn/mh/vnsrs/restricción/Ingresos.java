package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.IngresosTurísticos;

import java.util.List;

/**
 * Restricción que comprueba que los ingresos por turismo están dentro de un umbral mínimo
 */
public class Ingresos extends Restricción {
	private final float min;

	public Ingresos(float min) {
		if (min < 0) {
			throw new IllegalArgumentException("El valor de la restricción del umbral de ingresos debe " +
				"ser al menos 0");
		}
		this.min = min;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof IngresosTurísticos) {
				IngresosTurísticos ingresosTurísticos = (IngresosTurísticos) c;
				return ingresosTurísticos.getValorActual() >= min;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.INGRESOS_TURÍSTICOS};
	}
}
