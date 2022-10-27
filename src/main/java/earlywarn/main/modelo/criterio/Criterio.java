package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.datoid.Línea;

/**
 * Representa un criterio usado durante la ejecución del programa. Tiene un valor inicial y otro actual (que cambia
 * según se van abriendo y cerrando líneas). Permite obtener un porcentaje con el valor final del criterio (entre 0 y
 * 1, siendo 1 el mejor valor posible).
 */
public abstract class Criterio {
	public IDCriterio id;

	/**
	 * @return Valor entre 0 y 1 que representa lo deseable que es el estado de este criterio (0 = peor, 1 = mejor).
	 * El porcentaje siempre debe poder valer 0 o 1 con una cierta combinación de líneas abiertas y cerradas.
	 */
	public abstract double getPorcentaje();

	/**
	 * Recalcula el valor del criterio tras abirar o cerrar una línea
	 * @param línea Línea que acaba de ser abierta o cerrada
	 * @param abrir True si la línea ha sido abierta, false si ha sido cerrada
	 */
	public abstract void recalcular(Línea línea, boolean abrir);
}
