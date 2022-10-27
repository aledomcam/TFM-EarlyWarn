package earlywarn.mh.vnsrs.sensibilidad;

import earlywarn.main.modelo.ListaSoluciones;
import earlywarn.mh.vnsrs.config.Config;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

/**
 * Clase desde la que se ejecuta el método de análisis de sensibilidad de pesos usados para evaluar el fitness de
 * una solución.
 */
public class Main {
	@Context
	public GraphDatabaseService db;
	@Context
	public Log log;

	private static final String RUTA_SOLUCIONES = "import/soluciones_sensibilidad.txt";
	private static final String RUTA_RESULTADO_FITNESS = "export/sensibilidad_fitness.csv";
	private static final String RUTA_RESULTADO_RANKING = "export/sensibilidad_ranking.csv";

	/**
	 * Ejecuta el análisis de sensibilidad sobre los pesos definidos en el fichero de configuración de Vns-Rs
	 * @param numIteraciones Número de iteraciones a ejecutar durante el análisis
	 */
	@Procedure
	public void sensibilidadPesos(@Name("numIteraciones") Long numIteraciones) {
		log.info("Inicio análisis de sensibilidad de pesos");
		Config config = new Config(earlywarn.mh.vnsrs.Main.RUTA_CONFIG);
		ListaSoluciones soluciones = new ListaSoluciones(RUTA_SOLUCIONES);
		Sensibilidad sensibilidad = new Sensibilidad(config, soluciones, numIteraciones.intValue(), db, log);
		sensibilidad.ejecutar();
		sensibilidad.guardarResultado(RUTA_RESULTADO_FITNESS, RUTA_RESULTADO_RANKING);
	}
}
