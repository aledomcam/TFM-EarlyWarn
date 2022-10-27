package earlywarn.mh.vnsrs.config;

import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.mh.vnsrs.restricción.Restricción;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Almacena una lista con múltiples restricciones
 */
public class ListaRestricciones implements Iterable<Restricción> {
	private final List<Restricción> restricciones;

	public ListaRestricciones() {
		restricciones = new ArrayList<>();
	}

	/**
	 * Añade una nueva restricción a la lista
	 * @param restricción Restricción a añadir
	 */
	public void añadir(Restricción restricción) {
		restricciones.add(restricción);
	}

	/**
	 * Comprueba si una lista de criterios cumple todas las restricciones de esta lista de restricciones
	 * @param criterios Lista de criterios a comprobar
	 * @return True si los criterios especificados cumplen todas las restricciones, false en caso contrario
	 */
	public boolean cumple(List<Criterio> criterios) {
		for (Restricción restricción : restricciones) {
			if (!restricción.cumple(criterios)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<Restricción> iterator() {
		return restricciones.iterator();
	}
}
