package earlywarn.mh.vnsrs.sensibilidad;

import earlywarn.definiciones.IDCriterio;
import earlywarn.definiciones.OperaciónLínea;
import earlywarn.main.GestorLíneas;
import earlywarn.main.GestorLíneasBuilder;
import earlywarn.main.modelo.FitnessPorPesos;
import earlywarn.main.modelo.datoid.Línea;
import earlywarn.main.modelo.datoid.RegistroDatoID;
import earlywarn.mh.vnsrs.ConversorLíneas;
import earlywarn.mh.vnsrs.CriterioFactory;
import earlywarn.mh.vnsrs.config.Config;
import org.neo4j.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * Clase que representa una solución al problema del tráfico aéreo que puede ser evaluada en un momento cualquiera
 * dados unos pesos para cada criterio que también se pueden modificar en cualquier momento.
 */
public class SoluciónEvaluable {
	private final GestorLíneas gestor;
	private final FitnessPorPesos fitnessPorPesos;

	/**
	 * Crea una instancia de la clase
	 * @param líneas Lista de todas las líneas a considerar
	 * @param líneasCerradas Lista de líneas cerradas en la solución que se quiere reperesentar
	 * @param config Configuración de Vns-Rs con los criterios a utilizar
	 * @param fCriterios Factoría de criterios que permita crearlos en base a su ID
	 * @param registroLíneas Registro que permita obtener datos de líneas en base a su ID
	 * @param conversorLíneas Conversor que permita convertir IDs de líneas a valores numéricos
	 * @param log Log de Neo4J
	 */
	public SoluciónEvaluable(List<String> líneas, List<String> líneasCerradas, Config config,
							 CriterioFactory fCriterios, RegistroDatoID<Línea> registroLíneas,
							 ConversorLíneas conversorLíneas, Log log) {
		fitnessPorPesos = new FitnessPorPesos(config.pesos);
		gestor = new GestorLíneasBuilder(líneas, registroLíneas, conversorLíneas, log)
			.añadirCriterios(config.criterios, fCriterios)
			.añadirCálculoFitness(fitnessPorPesos)
			.build();
		gestor.abrirCerrarLíneas(líneasCerradas, OperaciónLínea.CERRAR);
	}

	/**
	 * Cambia los pesos usados para evaluar la solución representada por esta instancia
	 * @param pesos Nuevos pesos a utilizar en la evaluación
	 */
	public void setPesos(Map<IDCriterio, Float> pesos) {
		for (Map.Entry<IDCriterio, Float> entrada : pesos.entrySet()) {
			fitnessPorPesos.setPesoCriterio(entrada.getKey(), entrada.getValue());
		}
	}

	/**
	 * @return Fitness de la solución representada por esta clase dados los pesos actuales
	 */
	public double getFitness() {
		return gestor.getFitness();
	}
}
