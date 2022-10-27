package earlywarn.mh.vnsrs.entornos;

import java.util.*;

/**
 * Almacena una serie de casos usados en el cambio de entorno horizontal
 */
public class MemoriaCasosX {
	private final Random random;
	/**
	 * Mapa que mapea número de líneas abiertas a conjuntos de casos que tienen dicho número de líneas abiertas
	 */
	private final NavigableMap<Integer, List<CasoEntornoX>> casos;
	/*
	 * Almacena el número de elementos que hay en cada uno de las listas dentro del mapa de casos.
	 * Se usa para poder obtener acceso O(1) al número de casos que tienen X número de líneas abiertas, lo que permite
	 * determinar el peso asignado a cada elemento de una forma un poco más rápida que no requiere
	 * recorrer todo el conjunto de casos.
	 * El elemento en la posición i representa el número de casos que tienen i líneas abiertas.
	 */
	private final List<Integer> numCasos;
	// Número de elementos actualmente en la memoria
	private int numElementos;
	// Tamaño máximo de la memoria
	private final int tamaño;

	/**
	 * Crea una nueva instancia
	 * @param tamaño Tamaño máximo de la memoria de casos
	 * @param valorMax Valor máximo posible para el número de líneas abiertas en los casos que se insertarán en la
	 *                 memoria
	 */
	public MemoriaCasosX(int tamaño, int valorMax) {
		random = new Random();
		casos = new TreeMap<>();
		numCasos = new ArrayList<>();
		for (int i = 0; i <= valorMax; i++) {
			numCasos.add(0);
		}
		numElementos = 0;
		this.tamaño = tamaño;
	}

	/**
	 * Añade un nuevo caso a la memoria de casos. Si está llena, se eliminará un caso antiguo.
	 * El caso a eliminar será elegido de forma aleatoria. Aquellos casos que tengan un número de líneas con una
	 * mayor diferencia con respecto al caso añadido tendrán más probabilidades de ser eliminados.
	 * @param caso Caso a añadir
	 * @param numLíneasAbiertas Número de líneas abiertas que tiene este caso
	 */
	public void añadir(CasoEntornoX caso, int numLíneasAbiertas) {
		List<CasoEntornoX> listaActual = casos.get(numLíneasAbiertas);
		if (listaActual == null) {
			listaActual = new ArrayList<>();
			listaActual.add(caso);
			casos.put(numLíneasAbiertas, listaActual);
		} else {
			listaActual.add(caso);
		}
		numCasos.set(numLíneasAbiertas, numCasos.get(numLíneasAbiertas) + 1);
		numElementos++;

		if (numElementos > tamaño) {
			// Hay que borrar un caso de la memoria
			int numLíneasCasoABorrar = getNumLíneasCasoABorrar(numLíneasAbiertas);
			// Borramos un caso al azar de entre todos los que tengan el número de líneas elegido
			List<CasoEntornoX> casosCandidatos = casos.get(numLíneasCasoABorrar);
			casosCandidatos.remove(random.nextInt(casosCandidatos.size()));

			numCasos.set(numLíneasCasoABorrar, numCasos.get(numLíneasCasoABorrar) - 1);
			numElementos--;
		}
	}

	/**
	 * Devuelve una lista con todos los casos almacenados en la memoria que tiene un número de líneas abiertas entre los
	 * dos valores indicados
	 * @param minLíneas Número mínimo de líneas
	 * @param maxLíneas Número máximo de líneas
	 * @return Lista de todos los casos en la memoria que tenen un número de líneas abierto entre los dos
	 * valores indicados
	 */
	public List<CasoEntornoX> getCasosEnRango(int minLíneas, int maxLíneas) {
		List<CasoEntornoX> ret = new ArrayList<>();
		for (List<CasoEntornoX> lista : casos.subMap(minLíneas, maxLíneas + 1).values()) {
			ret.addAll(lista);
		}
		return ret;
	}

	/**
	 * Elige de qué subconjunto de casos saldrá el que será eliminado de la memoria. El subconjunto elegido se
	 * identifica por el número de líneas abiertas que tienen sus casos. Aquellos subconjuntos con un número de
	 * líneas abiertas similar al recién insertado tendrán menos probabilidades de ser elegidos.
	 * @param numLíneasCasoInsertado Número de líneas abiertas que tenía el caso recién insertado en la memoria
	 * @return Número de líneas abiertas que tendrá el caso a borrar de la memoria
	 */
	private int getNumLíneasCasoABorrar(int numLíneasCasoInsertado) {
		/*
		 * Cada conjunto tendrá un peso igual a (nº elementos) * ((distancia al nº de líneas del caso insertado) + 1).
		 * Creamos una lista que almacene pares con el número de líneas y el peso acumulado de cada conjunto.
		 * Esta lista queda ordenada ascendentemente por peso acumulado, lo que nos permite iterarla directamente
		 * para elegir un elemento al azar teniendo en cuenta sus pesos.
		 */
		List<Map.Entry<Integer, Integer>> pesosAcumulados = new ArrayList<>();
		int casosProcesados = 0;
		int variaciónNumLíneas = 0;
		int pesoTotal = 0;
		/*
		 * Vamos recorriendo la lista numCasos, empezando por el mismo número de líneas que para el caso insertado,
		 * luego pasamos a ese número +1, -1, +2, -2... hasta procesar todos los casos.
		 */
		while (casosProcesados < numElementos) {
			int numLíneas = numLíneasCasoInsertado + variaciónNumLíneas;
			if (numLíneas < numCasos.size()) {
				int numCasosActual = numCasos.get(numLíneas);
				if (numCasosActual > 0) {
					pesoTotal += numCasosActual * (variaciónNumLíneas + 1);
					pesosAcumulados.add(new AbstractMap.SimpleEntry<>(numLíneas, pesoTotal));
					casosProcesados += numCasosActual;
				}
			}

			if (variaciónNumLíneas > 0) {
				numLíneas = numLíneasCasoInsertado - variaciónNumLíneas;
				if (numLíneas >= 0) {
					int numCasosActual = numCasos.get(numLíneas);
					if (numCasosActual > 0) {
						pesoTotal += numCasosActual * (variaciónNumLíneas + 1);
						pesosAcumulados.add(new AbstractMap.SimpleEntry<>(numLíneas, pesoTotal));
						casosProcesados += numCasosActual;
					}
				}
			}
			variaciónNumLíneas++;
		}
		// Ahora seleccionamos uno de los valores al azar, teniendo en cuenta el peso de cada subconjunto
		int valorElegido = random.nextInt(pesoTotal);
		for (Map.Entry<Integer, Integer> entrada : pesosAcumulados) {
			if (entrada.getValue() > valorElegido) {
				return entrada.getKey();
			}
		}
		throw new IllegalStateException();
	}
}
