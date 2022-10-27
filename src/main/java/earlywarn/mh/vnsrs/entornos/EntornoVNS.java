package earlywarn.mh.vnsrs.entornos;

import earlywarn.definiciones.IllegalOperationException;
import earlywarn.definiciones.OperaciónLínea;

/**
 * Representa un entorno en la metaheurística que emplea VNS
 */
public class EntornoVNS {
	public OperaciónLínea operación;
	/*
	 * Número de entorno vertical en el que nos encontramos. Determina el número de líneas a abrir o cerrar (más
	 * cuanto mayor sea el valor).
	 */
	public int numEntornoY;

	public EntornoVNS(OperaciónLínea operación, int numEntornoY) {
		this.operación = operación;
		this.numEntornoY = numEntornoY;
	}

	public EntornoVNS(EntornoVNS otro) {
		operación = otro.operación;
		numEntornoY = otro.numEntornoY;
	}

	/**
	 * @return Número de líneas a abrir o cerrar en este entorno
	 */
	public int getNumLíneas() {
		return 1 << numEntornoY;
	}

	/**
	 * @return Número de líneas a abrir o cerrar en este entorno. El valor será positivo si la operación es
	 * {@link OperaciónLínea#ABRIR} o negativo si es {@link OperaciónLínea#CERRAR}.
	 */
	public int getNumLíneasConSigno() {
		switch (operación) {
			case ABRIR:
				return getNumLíneas();
			case CERRAR:
				return getNumLíneas() * -1;
			default:
				throw new IllegalOperationException();
		}
	}
}
