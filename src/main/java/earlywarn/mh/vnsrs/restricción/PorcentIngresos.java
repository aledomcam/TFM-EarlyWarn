package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.IngresosTurísticos;

import java.util.List;

/**
 * Restricción que comprueba que los ingresos por turismo están dentro de un umbral percentual mínimo
 */
public class PorcentIngresos extends Restricción {
	private final float min;

	public PorcentIngresos(float min) {
		if (min < 0 || min > 1) {
			throw new IllegalArgumentException("El valor de la restricción del umbral porcentual de ingresos debe " +
				"estar entre 0 y 1");
		}
		this.min = min;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof IngresosTurísticos) {
				IngresosTurísticos ingresosTurísticos = (IngresosTurísticos) c;
				return ingresosTurísticos.getValorActual() / ingresosTurísticos.getValorInicial() >= min;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.INGRESOS_TURÍSTICOS};
	}
}
