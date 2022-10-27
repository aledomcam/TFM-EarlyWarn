package earlywarn.mh.vnsrs.entornos;

import earlywarn.definiciones.OperaciónLínea;
import earlywarn.mh.vnsrs.*;
import earlywarn.mh.vnsrs.config.ConfigVNS;

import java.util.*;

/**
 * Clase que gestiona el cambio de entorno de VNS
 */
public class GestorEntornos {
	private final ConfigVNS config;
	// Número de iteraciones restantes hasta que se tenga que considerar otro posible cambio de entorno
	private int sigCambioEntorno;
	private final EntornoVNS entornoActual;

	private final ICalcEntornoX calcEntornoX;
	private final ICalcEntornoY calcEntornoY;

	public GestorEntornos(ConfigVNS configVNS, ConversorLíneas conversorLíneas, int numLíneas,
						  double temperaturaInicial) {
		config = configVNS;
		sigCambioEntorno = config.itCambioEntorno;
		entornoActual = new EntornoVNS(OperaciónLínea.CERRAR, config.getMaxEntornoY(numLíneas));

		if (configVNS.cambioEntornoXComplejo) {
			calcEntornoX = new CalcEntornoXMemoria(configVNS, numLíneas, temperaturaInicial);
		} else {
			calcEntornoX = new CalcEntornoXSimple();
		}
		if (configVNS.cambioEntornoYComplejo) {
			calcEntornoY = new CalcEntornoYEstancamiento(configVNS, conversorLíneas, numLíneas, temperaturaInicial);
		} else {
			calcEntornoY = new CalcEntornoYSimple(configVNS, numLíneas, temperaturaInicial);
		}
	}

	/*
	 * Devuelve el entorno de VNS en el que nos encontramos ahora mismo.
	 * Nota: Los valores de esta instancia se irán modificando según se cambie de entorno. Si se quiere mantener
	 * el estado íntegro de la misma a largo plazo, crear una copia de la misma.
	 */
	public EntornoVNS getEntorno() {
		return entornoActual;
	}

	/**
	 * Registra una nueva solución considerada
	 * @see ICalcEntornoX#registrarNuevaSolución(int, OperaciónLínea, double, double)
	 */
	public void registrarNuevaSolución(int numLíneasAbiertas, OperaciónLínea operaciónRealizada, double nuevoFitness,
									   double fitnessActual) {
		calcEntornoX.registrarNuevaSolución(numLíneasAbiertas, operaciónRealizada, nuevoFitness, fitnessActual);
	}

	/**
	 * Registra una nueva posición al final de una iteración
	 * @see ICalcEntornoY#registrarNuevaPosición(List) 
	 */
	public void registrarNuevaPosición(List<String> líneasVariadas) {
		calcEntornoY.registrarNuevaPosición(líneasVariadas);
	}

	/**
	 * Usado para indicar que se ha completado una iteración. Comprueba si es necesario realizar un cambio de
	 * entorno y lo realiza si es así.
	 * @param numLíneasAbiertas Número de líneas actualmente abiertas
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 */
	public void sigIter(int numLíneasAbiertas, double temperaturaActual) {
		sigCambioEntorno--;
		if (sigCambioEntorno <= 0) {
			cambioEntorno(numLíneasAbiertas, temperaturaActual);
			sigCambioEntorno += config.itCambioEntorno;
		}
	}

	/**
	 * Ejecuta el procedimiento de cambio de entorno, que podrá o no pasar a un entorno diferente del actual.
	 * @param numLíneasAbiertas Número de líneas actualmente abiertas
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 */
	private void cambioEntorno(int numLíneasAbiertas, double temperaturaActual) {
		// No fijamos los valores directamente para asegurarnos de que la operación es atómica
		OperaciónLínea entornoX = calcEntornoX.entornoX(numLíneasAbiertas, temperaturaActual);
		int numEntornoY = calcEntornoY.numEntornoY(temperaturaActual);
		entornoActual.operación = entornoX;
		entornoActual.numEntornoY = numEntornoY;
	}
}
