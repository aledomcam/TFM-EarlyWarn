package earlywarn.mh.vnsrs.sensibilidad;

import earlywarn.definiciones.IDCriterio;

import java.util.*;

/**
 * Representa un conjunto de pesos que pueden modificarse de forma aleatoria
 */
public class ConjuntoPesos {
	private final Random random;
	// Valor actual del peso de cada criterio
	public final Map<IDCriterio, Float> pesosActuales;
	// Lista de criterios ordenados por su peso inicial, del menos al más importante
	private final List<IDCriterio> criteriosOrdenados;

	public ConjuntoPesos(Map<IDCriterio, Float> pesos) {
		random = new Random();

		// Copiar valores iniciales
		pesosActuales = new EnumMap<>(pesos);

		// Inicializar la lista de criterios ordenados por peso
		criteriosOrdenados = new ArrayList<>();
		Map<IDCriterio, Float> pesosTmp = new EnumMap<>(pesos);
		while (!pesosTmp.isEmpty()) {
			Map.Entry<IDCriterio, Float> menor = null;
			for (Map.Entry<IDCriterio, Float> actual : pesosTmp.entrySet()) {
				if (menor == null || actual.getValue() < menor.getValue()) {
					menor = actual;
				}
			}
			criteriosOrdenados.add(menor.getKey());
			pesosTmp.remove(menor.getKey());
		}
	}

	/**
	 * Modifica los pesos almacenados asignando un valor aleatorio a cada uno, pero siempre manteniendo el orden de
	 * importancia establecido al crear la clase (es decir, si se ordenan los pesos del más al menos importante, el
	 * orden será siempre el mismo).
	 */
	public void randomizarPesos() {
		List<Float> nuevosPesosOrdenados = new ArrayList<>();
		float total = 0;
		for (int i = 0; i < pesosActuales.size(); i++) {
			float rand = random.nextFloat();
			nuevosPesosOrdenados.add(rand);
			total += rand;
		}
		// Normalizar
		for (int i = 0; i < nuevosPesosOrdenados.size(); i++) {
			nuevosPesosOrdenados.set(i, nuevosPesosOrdenados.get(i) / total);
		}
		nuevosPesosOrdenados.sort(null);

		pesosActuales.clear();
		int i = 0;
		for (IDCriterio idCriterio : criteriosOrdenados) {
			pesosActuales.put(idCriterio, nuevosPesosOrdenados.get(i));
			i++;
		}
	}
}
