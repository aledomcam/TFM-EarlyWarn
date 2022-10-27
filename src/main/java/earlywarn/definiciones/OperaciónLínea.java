package earlywarn.definiciones;

/**
 * Enumera las operaciones que se pueden realizar sobre una línea de vuelo
 */
public enum OperaciónLínea {
	ABRIR, CERRAR;

	/**
	 * @return Valor opuesto del enum
	 */
	public OperaciónLínea invertir() {
		switch (this) {
			case ABRIR:
				return CERRAR;
			case CERRAR:
				return ABRIR;
			default:
				throw new IllegalOperationException();
		}
	}
}
