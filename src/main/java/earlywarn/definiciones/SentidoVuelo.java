package earlywarn.definiciones;

/**
 * Representa el sentido en el que vuela un vuelo con respecto a un aeropuerto
 */
public enum SentidoVuelo {
	ENTRADA, SALIDA, AMBOS;

	/**
	 * @return Sentido de vuelo contrario al actual
	 */
	public SentidoVuelo invertir() {
		switch (this) {
			case ENTRADA:
				return SALIDA;
			case SALIDA:
				return ENTRADA;
			case AMBOS:
				return AMBOS;
			default:
				throw new IllegalOperationException();
		}
	}

	/**
	 * Obtiene el operador de Cypher que debe usarse para representar este sentido de vuelo en una consulta del tipo
	 * (:AirportOperationDay)-[]-(:Flight).
	 * @param contenido String que colocar en el interior del operador de relación
	 * @return String que representa el operador de relación correcto dado este sentido de vuelo
	 */
	public String operadorAODVuelo(String contenido) {
		switch (this) {
			case ENTRADA:
				return "<-[" + contenido + "]-";
			case SALIDA:
				return "-[" + contenido + "]->";
			case AMBOS:
				return "-[" + contenido + "]-";
			default:
				throw new IllegalOperationException();
		}
	}

	/**
	 * Obtiene el operador de Cypher que debe usarse para representar este sentido de vuelo en una consulta del tipo
	 * (:Flight)-[]-(:AirportOperationDay).
	 * @param contenido String que colocar en el interior del operador de relación
	 * @return String que representa el operador de relación correcto dado este sentido de vuelo
	 */
	public String operadorVueloAOD(String contenido) {
		return invertir().operadorAODVuelo(contenido);
	}
}
