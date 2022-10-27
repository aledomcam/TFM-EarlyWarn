package earlywarn.mh.vnsrs.config;

/**
 * Clase que almacena los valores necesarios para configurar RS
 */
public class ConfigRS {
	// Temperatura inicial
	public float tInicial;
	// Factor de reducción de la temperatura (recocido geométrico)
	public float alfa;
	// Cada cuántas iteraciones se reduce la temperatura
	public int itReducciónT;
}
