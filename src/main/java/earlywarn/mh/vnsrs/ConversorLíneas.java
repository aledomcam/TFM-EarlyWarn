package earlywarn.mh.vnsrs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Clase capaz de convertir un ID de línea a un ID numérico y de realizar conversiones entre arrays de booleanos que
 * representan el estado de las líneas y listas con IDs de línea.
 */
public class ConversorLíneas {
	// Mapea IDs de líneas a IDs numéricos
	private final Map<String, Integer> idANúmero;
	// Mapea IDs numéricas a IDs de líneas
	private final Map<Integer, String> númeroAId;

	public ConversorLíneas(List<String> líneas) {
		idANúmero = new TreeMap<>();
		númeroAId = new TreeMap<>();
		int i = 0;
		for (String línea : líneas) {
			idANúmero.put(línea, i);
			númeroAId.put(i, línea);
			i++;
		}
	}

	/**
	 * Devuelve el identificador numérico de la línea con el ID indicado
	 * @param idLínea ID de la línea
	 * @return ID numérico correspondiente a la línea indicada
	 */
	public int getIDNumérico(String idLínea) {
		return idANúmero.get(idLínea);
	}

	/**
	 * Devuelve el identificador de línea que corresponde al ID numérico indicado
	 * @param idNumérico ID numérico de la línea que se desea obtener
	 * @return ID de la línea correspondiente al ID numérico indicado
	 */
	public String getIDLínea(int idNumérico) {
		return númeroAId.get(idNumérico);
	}

	/**
	 * Devuelve una lista con los IDs de todas las líneas que se encuentran abiertas en la lista indicada
	 * @param líneas Array de booleanos que representa el estado de cada línea según su ID numérico
	 * @return Lista con los IDs de todas las líneas abiertas en el array indicado
	 */
	public List<String> getAbiertas(boolean[] líneas) {
		return getLíneasPorEstado(líneas, true);
	}

	/**
	 * Devuelve una lista con los IDs de todas las líneas que se encuentran cerradas en la lista indicada
	 * @param líneas Array de booleanos que representa el estado de cada línea según su ID numérico
	 * @return Lista con los IDs de todas las líneas cerradas en el array indicado
	 */
	public List<String> getCerradas(boolean[] líneas) {
		return getLíneasPorEstado(líneas, false);
	}

	private List<String> getLíneasPorEstado(boolean[] líneas, boolean abiertas) {
		List<String> ret = new ArrayList<>();
		for (int i = 0; i < líneas.length; i++) {
			if (líneas[i] == abiertas) {
				ret.add(getIDLínea(i));
			}
		}
		return ret;
	}
}
