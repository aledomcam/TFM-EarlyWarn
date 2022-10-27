package earlywarn.definiciones;

/**
 * Excepción que se lanza al intentar realizar una acción imposible dado el estado actual de un objeto
 */
public class IllegalOperationException extends RuntimeException {
	public IllegalOperationException() {}
	public IllegalOperationException(String message) {
		super(message);
	}
}
