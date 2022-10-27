package earlywarn.main.modelo.datoid;

import java.util.Map;
import java.util.TreeMap;

/**
 * Permite almacenar una serie de elementos identificados por un ID según se va necesitando acceder a sus datos.
 * La primera vez que se trate de acceder a los datos de un elemento, éste se insertará en el registro. A partir de
 * entonces, los accesos al mismo elemento devolverán la misma instancia.
 *
 * @param <T> Tipo de dato a almacenar
 */
public class RegistroDatoID<T> {
	private final Map<String, T> elementos;
	private final IDatoIDFactory<T> factoría;

	/**
	 * Crea un nuevo registro
	 * @param factoría Factoría que permite crear instancias de los elementos a almacenar en el registro usando solo
	 *                 su id.
	 */
	public RegistroDatoID(IDatoIDFactory<T> factoría) {
		elementos = new TreeMap<>();
		this.factoría = factoría;
	}

	public T get(String id) {
		T ret = elementos.get(id);
		if (ret == null) {
			ret = factoría.crear(id);
			elementos.put(id, ret);
		}
		return ret;
	}
}
