package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.datoid.Línea;

/**
 * Representa el número de pasajeros que vuelan en la red de tráfico aéreo. Usado como aproximación para calcular las
 * pérdidas económicas derivadas de la pérdida de pasajeros.
 */
public class NumPasajeros extends Criterio {
	private final long valorInicial;
	private long valorActual;

	public NumPasajeros(long valorInicial) {
		this.valorInicial = valorInicial;
		valorActual = valorInicial;
		id = IDCriterio.NÚMERO_PASAJEROS;
	}

	public long getValorInicial() {
		return valorInicial;
	}

	public long getValorActual() {
		return valorActual;
	}

	@Override
	public double getPorcentaje() {
		return (double) valorActual / valorInicial;
	}

	@Override
	public void recalcular(Línea línea, boolean abrir) {
		if (abrir) {
			valorActual += línea.getPasajeros();
		} else {
			valorActual -= línea.getPasajeros();
		}
	}
}
