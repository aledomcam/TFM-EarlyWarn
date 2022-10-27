package earlywarn.definiciones;

/**
 * Lanzada al intentar ejecutar una operación sobre la BD que requiere que se haya ejecutado una cierta operación
 * ETL con anterioridad que no se ha ejecutado.
 */
public class ETLOperationRequiredException extends RuntimeException {
	public ETLOperationRequiredException(String message) {
		super(message);
	}
}
