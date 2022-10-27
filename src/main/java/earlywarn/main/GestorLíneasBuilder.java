package earlywarn.main;

import earlywarn.definiciones.ICálculoFitness;
import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.datoid.Línea;
import earlywarn.main.modelo.datoid.RegistroDatoID;
import earlywarn.mh.vnsrs.config.Config;
import earlywarn.mh.vnsrs.ConversorLíneas;
import earlywarn.mh.vnsrs.CriterioFactory;
import earlywarn.mh.vnsrs.restricción.Restricción;
import org.neo4j.logging.Log;

import java.util.List;

/**
 * Builder usado para inicializar un gestor de líneas con una serie de criterios
 */
public class GestorLíneasBuilder extends GestorLíneas {
	public GestorLíneasBuilder(List<String> líneas, RegistroDatoID<Línea> registroLíneas,
							   ConversorLíneas conversorLíneas, Log log) {
		super(líneas, registroLíneas, conversorLíneas, log);
	}

	public GestorLíneasBuilder añadirCriterio(Criterio criterio) {
		_añadirCriterio(criterio);
		return this;
	}

	/**
	 * Añade todos los criterios cuyo ID esté incluido en la lista especificada
	 * @param criterios Lista con los IDs de todos los criterios a añadir
	 * @param fCriterios Factoría de criterios que permita crear nuevos criterios dado únicamente su id
	 * @return this
	 */
	public GestorLíneasBuilder añadirCriterios(List<IDCriterio> criterios, CriterioFactory fCriterios) {
		for (IDCriterio c : criterios) {
			_añadirCriterio(fCriterios.criterio(c));
		}
		return this;
	}

	/**
	 * Añade todos los criterios necesarios para calcular las restricciones definidas en la configuración a la lista
	 * de criterios a tener en cuenta.
	 * A estos criterios se les asignará un peso de 0 si no estaban ya en la lista de pesos.
	 * @param config Configuración para la metaheurística de VNS-RS. Debe contener la lista de restricciones a
	 * considerar, así como los pesos de los diferentes criterios ya inicializados.
	 * @param fCriterios Factoría de criterios que permita crear nuevos criterios dado únicamente su id
	 * @return this
	 */
	public GestorLíneasBuilder añadirCriteriosRestricciones(Config config, CriterioFactory fCriterios) {
		for (Restricción restricción : config.restricciones) {
			// Comprobar si al menos uno de los criterios necesarios para calcular esta restricción está ya incluido
			IDCriterio[] criteriosAsociados = restricción.getCriteriosAsociados();
			if (criteriosAsociados.length > 0) {
				boolean alMenosUnCriterio = false;
				for (IDCriterio criterio : criteriosAsociados) {
					if (criterios.containsKey(criterio)) {
						alMenosUnCriterio = true;
						break;
					}
				}

				if (!alMenosUnCriterio) {
					// Insertamos el primer criterio necesario
					_añadirCriterio(fCriterios.criterio(criteriosAsociados[0]));
					config.pesos.put(criteriosAsociados[0], 0.0f);
				}
			}
		}
		return this;
	}

	public GestorLíneasBuilder añadirCálculoFitness(ICálculoFitness cálculoFitness) {
		_añadirCálculoFitness(cálculoFitness);
		return this;
	}

	public GestorLíneas build() {
		return this;
	}
}
