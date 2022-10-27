package earlywarn.main;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDate;
import java.util.*;

/**
 * Clase que almacena utilidades varias
 */
public class Utils {
	private static final double LN_2 = 0.6931471805599453;

	/**
	 * Convierte un resultado de una consulta de Neo4J que puede ser un Long o un Double a un Double.
	 * Esto puede pasar al usar funciones de agregación sobre un campo con decimales, ya que un resultado de 0 se
	 * devuelve como un Long (por alguna razón).
	 * @param resultado Resultado obtenido de la consulta de Neo4J. Debe ser un Long o un Double.
	 * @return El resultado como tipo Double
	 */
	public static Double resultadoADouble(Object resultado) {
		//noinspection ChainOfInstanceofChecks
		if (resultado instanceof Double) {
			return (Double) resultado;
		} else if (resultado instanceof Long) {
			return ((Long) resultado).doubleValue();
		} else {
			throw new IllegalArgumentException("El resultado especificado no es un Long ni un Double");
		}
	}

	/**
	 * Calcula la media de una lista de valores decimales
	 * @param valores Lista de valores
	 * @return Media de la lista de valores
	 */
	public static double getMedia(List<Double> valores) {
		double total = 0;
		for (Double valor : valores) {
			total += valor;
		}
		return total / valores.size();
	}

	/**
	 * Calcula la desviación típica de una lista de valores decimales
	 * @param valores Lista de valores
	 * @return Desvuación típica de la lista de valores
	 */
	public static double getStd(List<Double> valores) {
		double totalCuadrados = 0;
		double media = getMedia(valores);
		for (Double valor : valores) {
			totalCuadrados += Math.pow(valor - media, 2);
		}
		return Math.sqrt(totalCuadrados / valores.size());
	}

	/**
	 * Calcula la desviación típica máxima posible en un conjunto de datos del tamaño indicado, asumiendo que todos los
	 * elementos toman valores entre 0 y 1.
	 * @param númeroDatos Número de elementos en el conjunto de datos
	 * @return Desviación típica máxima que puede obtenerse variando los valores del conjunto de datos entre 0 y 1
	 */
	public static double getStdMáxima(int númeroDatos) {
		int numElemAltos = (int) Math.floor(númeroDatos / 2.0f);
		int numElemBajos = (int) Math.ceil(númeroDatos / 2.0f);
		float media = (float) numElemAltos / númeroDatos;

		/*
		 * Distancia de los elementos altos a la media: 1 - media
		 * Distancia de los elementos bajos a la media: media
		 * Suma de los cuadrados de todas las distancias:
		 * 	(dist. elementos altos)^2 * (nº elementos altos) + (dist. elementos bajos)^2 * (nº elementos bajos)
		 * Desviación típica: sqrt(Suma de cuadrados / total elementos)
		 */
		return Math.sqrt((Math.pow(1 - media, 2) * numElemAltos + Math.pow(media, 2) * numElemBajos) / númeroDatos);
	}

	/**
	 * Calcula la desviación (distancia) de cada elemento de la lista proporcionada con respecto al valor
	 * indicado y luego calcula la media de esas desviaciones y la devuelve.
	 * @param elementos Lista de elementos sobre los que calcular la desviación media
	 * @param valor Valor con el que comparar cada elemento
	 * @return Desviación media de los diferentes elementos de la lista con respecto al valor indicado
	 */
	public static double getDesviaciónMedia(List<Double> elementos, double valor) {
		double ret = 0;
		for (Double elem : elementos) {
			ret += Math.abs(elem - valor);
		}
		return ret / elementos.size();
	}

	/**
	 * Convierte una lista de nodos XML en una lista de instancias de Element, manteniendo solo los nodos de ese
	 * tipo.
	 * @param lista Lista de nodos a procesar
	 * @return Lista con los elementos contenidos en la lista de entrada
	 */
	public static List<Element> toLista(NodeList lista) {
		List<Element> res = new ArrayList<>();
		Node actual;
		for (int i = 0; i < lista.getLength(); i++) {
			actual = lista.item(i);
			if (actual.getNodeType() == Node.ELEMENT_NODE) {
				res.add((Element) actual);
			}
		}
		return res;
	}

	public static double log2(double valor) {
		return Math.log(valor) / LN_2;
	}

	/**
	 * Redondea el valor especificado a la potencia de 2 más cercana y devuelve el exponente de dicha potencia.
	 * Nótese que este cálculo no es lo mismo que round(log2(valor)), ya que en ese caso primero se calcula el
	 * exponente y luego se redondea.
	 * @param valor Valor a redondear
	 * @return Exponente de la potencia de dos más cercana al valor indicado
	 */
	public static int redondearAPotenciaDeDosExponente(float valor) {
		int i = 0;
		float distanciaPrevia = Float.MAX_VALUE;
		while (true) {
			int valorActual = (int) Math.pow(2, i);
			float distancia = Math.abs(valor - valorActual);
			if (distancia < distanciaPrevia) {
				distanciaPrevia = distancia;
				i++;
			} else {
				return i - 1;
			}
		}
	}

	/**
	 * Crea una instancia de LocalDate en base a una fecha especificada como año-mes-día
	 * @param string String con la fecha en formato año-mes-día
	 * @return LocalDate que representa la fecha indicada
	 */
	public static LocalDate stringADate(String string) {
		String[] split =  string.split("-");
		return LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
	}

	/**
	 * Devuelve una cierta cantidad de números aleatorios entre 0 y el valor máximo especificado, sin repetición.
	 * @param max Límite superior usado para generar los valores (exclusivo)
	 * @param cantidad Número de números aleatorios a generar
	 * @return Lista con (cantidad) números aleatorios entre 0 y (max) - 1, sin repetición. Si (cantidad) >= (max),
	 * devuelve una lista con los números desde 0 hasta (max) - 1.
	 */
	public static List<Integer> múltiplesAleatorios(int max, int cantidad) {
		Random random = new Random();
		int maxActual = max;
		Set<Integer> númerosElegidos = new TreeSet<>();

		while (númerosElegidos.size() < cantidad && maxActual > 0) {
			int valorRandom = random.nextInt(maxActual);
			/*
			 * Funcionamiento del algoritmo: Por cada número que ya haya salido elegido que sea menor o igual al random
			 * generado, tenemos que incrementar dicho random en 1. Así tenemos en cuenta las posiciones de los números
			 * que ya han salido.
			 * La comparación se hace teniendo en cuenta los incrementos de iteraciones anteriores.
			 */
			for (Integer elegidoActual : númerosElegidos) {
				if (elegidoActual <= valorRandom) {
					valorRandom++;
				} else {
					break;
				}
			}
			númerosElegidos.add(valorRandom);
			maxActual--;
		}
		return new ArrayList<>(númerosElegidos);
	}

	/**
	 * Devuelve una string que incluye todos los elementos de la lista especificada convertidos a string y separados
	 * por comas. Opcionalmente se pueden incluir corchetes al inicio y al final de la lista.
	 * @param lista Lista de elementos a convertir
	 * @param corchetes Si es true, la string que representa la lista se encerrará entre corchetes
	 * @param espacios Si es true, se insertará un espacio después de cada coma.
	 * @return String que incluye todas las líneas en la lista
	 */
	public static String listaToString(List<?> lista, boolean corchetes, boolean espacios) {
		StringBuilder sb = new StringBuilder();
		if (corchetes) {
			sb.append("[");
		}
		boolean primero = true;
		for (Object elemento : lista) {
			if (primero) {
				primero = false;
			} else {
				if (espacios) {
					sb.append(", ");
				} else {
					sb.append(",");
				}
			}
			sb.append(elemento);
		}
		if (corchetes) {
			sb.append("]");
		}
		return sb.toString();
	}

	/**
	 * Devuelve una representación como string de los elementos del mapa indicado. Cada elemento se mostrará en
	 * una línea.
	 * @param mapa Mapa a mostrar
	 * @return Representación del mapa indicado como string
	 */
	public static String mapaAString(Map<?, ?> mapa) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
			sb.append(entrada.getKey()).append(": ").append(entrada.getValue()).append("\n");
		}
		return sb.toString();
	}
}
