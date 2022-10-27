package earlywarn.mh.vnsrs.config;

import java.util.Map;
import java.util.TreeMap;

/**
 * Representa una lista de parámetros leída del fichero de configuración
 */
public class ListaParámetros {
	private final Map<String,String> parámetros;

	public ListaParámetros() {
		parámetros = new TreeMap<>();
	}

	/**
	 * Añade un nuevo parámetro a la lista, identificado por un ID
	 * @param id ID del parámetro. Debería corresponder con el ID de alguno de los parámetros que puede tener el
	 * elemento al que corresponde esta lista de parámetros.
	 * @param valor Valor del parámetro
	 */
	public void añadir(String id, String valor) {
		parámetros.put(id, valor);
	}

	/**
	 * Devuelve un parámetro dado su id, como string
	 * @param id ID del parámetro a recuperar
	 * @return String con el valor del parámetro identificado por el id indicado, o null si el parámetro especificado
	 * no está en esta lista
	 */
	public String getParam(String id) {
		return parámetros.get(id);
	}

	/**
	 * Devuelve un parámetro dado su id, como entero
	 * @param id ID del parámetro a recuperar
	 * @throws IllegalArgumentException Si el parámetro indicado no es de tipo entero
	 * @return Integer con el valor del parámetro identificado por el id indicado, o null si el parámetro especificado
	 * no está en esta lista
	 */
	public Integer getParamInt(String id) {
		String paramStr = getParam(id);
		if (paramStr == null) {
			return null;
		} else {
			try {
				return Integer.parseInt(paramStr);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("El parámetro \"" + id + "\" no es de tipo entero", e);
			}
		}
	}

	/**
	 * Devuelve un parámetro dado su id, como float
	 * @param id ID del parámetro a recuperar
	 * @throws IllegalArgumentException Si el parámetro indicado no es de tipo float
	 * @return Float con el valor del parámetro identificado por el id indicado, o null si el parámetro especificado
	 * no está en esta lista
	 */
	public Float getParamFloat(String id) {
		String paramStr = getParam(id);
		if (paramStr == null) {
			return null;
		} else {
			try {
				return Float.parseFloat(paramStr);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("El parámetro \"" + id + "\" no es de tipo float", e);
			}
		}
	}
}
