package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.datoid.Línea;

/**
 * Representa el riesgo importado total que llega a través de la red de tráfico aéreo
 */
public class RiesgoImportado extends Criterio {
	private final double valorInicial;
	private double valorActual;

	public RiesgoImportado(double valorInicial) {
		this.valorInicial = valorInicial;
		valorActual = valorInicial;
		id = IDCriterio.RIESGO_IMPORTADO;
	}

	public double getValorInicial() {
		return valorInicial;
	}

	public double getValorActual() {
		return valorActual;
	}

	@Override
	public double getPorcentaje() {
		return 1 - valorActual / valorInicial;
	}

	@Override
	public void recalcular(Línea línea, boolean abrir) {
		if (abrir) {
			valorActual += línea.getRiesgoImportado();
		} else {
			valorActual -= línea.getRiesgoImportado();
		}
	}
}
