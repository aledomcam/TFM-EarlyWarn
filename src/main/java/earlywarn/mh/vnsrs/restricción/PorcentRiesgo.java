package earlywarn.mh.vnsrs.restricción;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.criterio.RiesgoImportado;

import java.util.List;

/**
 * Restricción que comprueba que el riesgo importado está dentro de un umbral percentual máximo
 */
public class PorcentRiesgo extends Restricción {
	private final float max;

	public PorcentRiesgo(float max) {
		if (max < 0 || max > 1) {
			throw new IllegalArgumentException("El valor de la restricción del umbral de riesgo máximo debe estar " +
				"entre 0 y 1");
		}
		this.max = max;
	}

	@Override
	public boolean cumple(List<Criterio> criterios) {
		for (Criterio c : criterios) {
			if (c instanceof RiesgoImportado) {
				RiesgoImportado riesgoImportado = (RiesgoImportado) c;
				return riesgoImportado.getValorActual() / riesgoImportado.getValorInicial() <= max;
			}
		}
		return true;
	}

	@Override
	public IDCriterio[] getCriteriosAsociados() {
		return new IDCriterio[] {IDCriterio.RIESGO_IMPORTADO};
	}
}
