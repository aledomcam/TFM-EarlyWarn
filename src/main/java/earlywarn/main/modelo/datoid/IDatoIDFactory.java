package earlywarn.main.modelo.datoid;

/**
 * Interfaz que representa una factoría capaz de crear instancias de un objeto dado una serie de valores iniciales
 * (especificados en el constructor de la factoría) y un ID de objeto (especificado al instanciarlo)
 * @param <T> Tipo del objeto que producirá la factoría
 */
public interface IDatoIDFactory<T> {
	 T crear(String id);
}
