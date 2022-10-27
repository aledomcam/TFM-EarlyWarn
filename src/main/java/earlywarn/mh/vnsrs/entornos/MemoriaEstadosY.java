package earlywarn.mh.vnsrs.entornos;

import earlywarn.mh.vnsrs.ConversorLíneas;
import earlywarn.mh.vnsrs.config.ConfigVNS;

import java.util.*;

/**
 * Clase que almacena los últimos estados por los que ha pasado el algoritmo. Usada para saber cuánto tiempo hace
 * que no se logra un cierto grado de variedad en las soluciones y así evitar estancamiento.
 */
public class MemoriaEstadosY {
	private final List<EstadoEntornoY> estados;
	// Mapea IDs de líneas a un ID numérico. Usado para direccionar los arrays de booleanos de los estados.
	private final ConversorLíneas conversorLíneas;
	/*
	 * Almacena el número de iteraciones que hace que no logramos una distancia de al menos (posición actual) líneas
	 * de diferencia con respecto a la solución actual.
	 */
	private final int[] tiempoSinDistancia;
	private final int numLíneas;
	private final int tamañoMemoria;

	public MemoriaEstadosY(ConfigVNS config, ConversorLíneas conversorLíneas, int numLíneas) {
		estados = new LinkedList<>();
		this.conversorLíneas = conversorLíneas;
		this.numLíneas = numLíneas;
		tamañoMemoria = config.getTamañoMemoriaY();
		tiempoSinDistancia = new int[numLíneas + 1];
	}

	/**
	 * Inserta un nuevo estado en la memoria
	 * @param líneasVariadas Líneas que se han modificado con respecto al estado anterior
	 */
	public void insertar(List<String> líneasVariadas) {
		// Primero necesitamos conocer de qué estado partimos
		EstadoEntornoY estadoPrevio;
		if (estados.isEmpty()) {
			// Estado inicial: Todo abierto
			estadoPrevio = new EstadoEntornoY(numLíneas, 0);
			estados.add(estadoPrevio);
		} else {
			estadoPrevio = estados.get(0);
		}

		// Traducir la lista de líneas variadas a IDs numéricos
		List<Integer> idNumLíneasVariadas = new ArrayList<>();
		for (String línea : líneasVariadas) {
			idNumLíneasVariadas.add(conversorLíneas.getIDNumérico(línea));
		}

		// Creamos el estado actual a partir del anterior y variamos los elementos de las líneas que han cambiado
		EstadoEntornoY estadoActual = new EstadoEntornoY(estadoPrevio);
		for (Integer idNumLínea : idNumLíneasVariadas) {
			estadoActual.líneas[idNumLínea] = !estadoActual.líneas[idNumLínea];
		}
		estados.add(0, estadoActual);
		if (estados.size() > tamañoMemoria) {
			estados.remove(estados.size() - 1);
		}

		if (!líneasVariadas.isEmpty()) {
			/*
			 * Ahora tenemos que recorrer la memoria y actualizar las distancias al primer elemento de todos los demás.
			 * Para ello, comprobamos si las líneas modificadas pasan a ser iguales (distancia -= 1) o diferentes
			 * (distancia += 1) para cada elemento de la memoria.
			 */
			// Nos saltamos el primer elemento ya que es el que acabamos de insertar
			boolean primero = true;
			for (EstadoEntornoY estado : estados) {
				if (primero) {
					primero = false;
				} else {
					for (Integer idNumérico : idNumLíneasVariadas) {
						boolean estadoActualLínea = estadoActual.líneas[idNumérico];
						boolean estadoPasadoLínea = estado.líneas[idNumérico];
						if (estadoActualLínea ^ estadoPasadoLínea) {
							/*
							 * Esta línea ha pasado a tener un estado diferente con respecto al que tenía en esta
							 * entrada anterior, por lo que la distancia entre ambas ha aumentado
							 */
							estado.distancia++;
						} else {
							/*
							 * Esta línea ha pasado a tener un estado igual que el que tenía en esta
							 * entrada anterior, por lo que la distancia entre ambas se ha reducido
							 */
							estado.distancia--;
						}
					}
				}
			}
		}

		/*
		 * Ahora que tenemos al día las distancias, podemos recalcular el tiempo que hace que no logramos
		 * cierto grado de variación en la solución.
		 */
		recalcularTiempoSinDistancia();
	}

	/**
	 * Devuelve el número de iteraciones que hace que no logramos una distancia de Hamming con la solución actual
	 * de al menos (distancia) líneas diferentes.
	 * @param distancia Distancia a comprobar
	 * @return Número de iteraciones que hace que no logramos la distancia indicada con respecto a la solución
	 * actual
	 */
	public int iteracionesSinDistancia(int distancia) {
		return tiempoSinDistancia[distancia];
	}

	/*
	 * Recorre todas las entradas de la memoria, recalculando cuánto tiempo hace que no logramos una distancia
	 * de X líneas diferentes con respecto al estado actual para X = 0...numLíneas
	 */
	private void recalcularTiempoSinDistancia() {
		/*
		 * Indica cuál es la posición más alta del vector tiempoSinDistancia que hemos escrito en esta llamada.
		 * Útil para saber qué valores están ya inicializados sin tener que hacer algo como recorrer el vector
		 * entero y rellenarlo con -1.
		 */
		int posMásAlta = 0;

		// Número de iteraciones de distancia en el tiempo que representa el elemento actual de la memoria
		int numIteraciones = 0;
		for (EstadoEntornoY estado : estados) {
			/*
			 * Este estado está a una distancia de Hamming de X líneas con respecto al actual y pasamos por él
			 * hace (numIteraciones) iteraciones. Por tanto, rellenamos todas las entradas de tiempoSinDistancia
			 * que representen una distancia menor o igual a la actual y no estén ya inicializadas con este valor
			 * de iteraciones.
			 */
			for (int i = posMásAlta + 1; i <= estado.distancia; i++) {
				tiempoSinDistancia[i] = numIteraciones;
			}
			posMásAlta = Math.max(posMásAlta, estado.distancia);

			numIteraciones++;
		}

		/*
		 * Una vez iterada toda la memoria, las entradas del array que aún no estén inicializadas representan
		 * distancias que nunca hemos alcanzado. Dado que tiene más sentido que al principio el algoritmo trate
		 * de variar muchas líneas, fijamos estas entradas al valor máximo posible.
		 */
		for (int i = posMásAlta + 1; i < tiempoSinDistancia.length; i++) {
			tiempoSinDistancia[i] = tamañoMemoria;
		}
	}
}
