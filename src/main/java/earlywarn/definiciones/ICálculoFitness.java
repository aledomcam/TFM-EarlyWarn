package earlywarn.definiciones;

import earlywarn.main.modelo.criterio.Criterio;

import java.util.Collection;

/**
 * Interfaz que permite calcular el fitness de una solución dados diferentes criterios con un cierto valor para cada
 * uno
 */
public interface ICálculoFitness {
	double calcularFitness(Collection<Criterio> criterios);
}
