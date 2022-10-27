package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.datoid.Línea;

/**
 * Representa los ingresos por turismo derivados de los pasajeros que viajan en la red de tráfico aéreo
 */
public class IngresosTurísticos extends Criterio {
	private final double valorInicial;
	private double valorActual;

	public IngresosTurísticos(double valorInicial) {
		this.valorInicial = valorInicial;
		valorActual = valorInicial;
		id = IDCriterio.INGRESOS_TURÍSTICOS;
	}

	public double getValorInicial() {
		return valorInicial;
	}

	public double getValorActual() {
		return valorActual;
	}

	@Override
	public double getPorcentaje() {
		return valorActual / valorInicial;
	}

	@Override
	public void recalcular(Línea línea, boolean abrir) {
		if (abrir) {
			valorActual += línea.getIngresosTurísticos();
		} else {
			valorActual -= línea.getIngresosTurísticos();
		}
	}
}
