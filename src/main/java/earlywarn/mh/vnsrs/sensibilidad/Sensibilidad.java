package earlywarn.mh.vnsrs.sensibilidad;

import earlywarn.definiciones.IDCriterio;
import earlywarn.definiciones.IllegalOperationException;
import earlywarn.main.Consultas;
import earlywarn.main.modelo.datoid.Aeropuerto;
import earlywarn.main.modelo.ListaSoluciones;
import earlywarn.main.modelo.datoid.Línea;
import earlywarn.main.modelo.datoid.AeropuertoFactory;
import earlywarn.main.modelo.datoid.LíneaFactory;
import earlywarn.main.modelo.datoid.RegistroDatoID;
import earlywarn.mh.vnsrs.ConversorLíneas;
import earlywarn.mh.vnsrs.CriterioFactory;
import earlywarn.mh.vnsrs.config.Config;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.util.*;

/**
 * Clase que implementa el análisis de sensibilidad de pesos mediante simulación Monte Carlo.
 */
public class Sensibilidad {
	private final Config config;
	private final int numIteraciones;
	private final Log log;
	private final ListaSoluciones soluciones;
	private final Consultas consultas;
	private final List<String> líneas;
	private final RegistroDatoID<Aeropuerto> registroAeropuertos;
	private final RegistroDatoID<Línea> registroLíneas;
	/*
	 * Contiene los diferentes gestores usados para evaluar cada una de las posibles soluciones. Cada gestor tendrá
	 * las líneas abiertas y cerradas correspondiente a su solución.
	 */
	private final List<SoluciónEvaluable> solucionesEv;
	private final ConversorLíneas conversorLíneas;
	// Pesos actuales de los diferentes criterios
	private final ConjuntoPesos pesos;
	private Resultado resultado;

	/**
	 * Crea una instancia de la clase que permite ejecutar el análisis
	 * @param config Fichero de configuración de Vns-Rs que contiene la lista de criterios y sus pesos
	 * @param soluciones Lista con las diferentes soluciones que se usarán como referencia durante la ejecución
	 * @param numIteraciones Número de iteraciones de a ejectuar
	 * @param db Acceso a la base de datos
	 * @param log Log de Neo4J
	 */
	public Sensibilidad(Config config, ListaSoluciones soluciones, int numIteraciones, GraphDatabaseService db, Log log) {
		this.soluciones = soluciones;
		this.log = log;
		this.config = config;

		consultas = new Consultas(db);
		líneas = consultas.getLíneas(config.díaInicio, config.díaFin, config.país);
		conversorLíneas = new ConversorLíneas(líneas);
		solucionesEv = new ArrayList<>();
		resultado = null;
		pesos = new ConjuntoPesos(config.pesos);
		this.numIteraciones = numIteraciones;

		AeropuertoFactory fAeropuertos = new AeropuertoFactory(config.díaInicio, config.díaFin, db);
		registroAeropuertos = new RegistroDatoID<>(fAeropuertos);
		LíneaFactory fLíneas = new LíneaFactory(config.díaInicio, config.díaFin, db);
		registroLíneas = new RegistroDatoID<>(fLíneas);
	}

	/**
	 * Ejecuta el análisis de sensibilidad
	 */
	public void ejecutar() {
		resultado = new Resultado(soluciones.size(), log);
		CriterioFactory fCriterios = new CriterioFactory(consultas, config, registroAeropuertos);
		for (List<String> cerradasSolución : soluciones) {
			SoluciónEvaluable sol = new SoluciónEvaluable(líneas, cerradasSolución, config, fCriterios, registroLíneas,
				conversorLíneas, log);
			solucionesEv.add(sol);
		}

		for (int iteración = 0; iteración < numIteraciones; iteración++) {
			// La primera iteración usa los pesos iniciales
			if (iteración > 0) {
				pesos.randomizarPesos();
				actualizarPesos(pesos.pesosActuales);
			}

			// Calcular y almacenar el fitness de cada solución
			List<Double> fitnessSoluciones = new ArrayList<>();
			for (SoluciónEvaluable sol : solucionesEv) {
				fitnessSoluciones.add(sol.getFitness());
			}
			List<Double> fitnessSolucionesOrdenado = new ArrayList<>(fitnessSoluciones);
			fitnessSolucionesOrdenado.sort(null);
			Collections.reverse(fitnessSolucionesOrdenado);

			// Calcular el puesto en el ranking de cada solución
			List<Integer> rankings = new ArrayList<>();
			for (Double fitness : fitnessSoluciones) {
				int rank = fitnessSolucionesOrdenado.indexOf(fitness) + 1;
				rankings.add(rank);
			}
			resultado.registrarIteración(fitnessSoluciones, rankings);
		}
	}

	/**
	 * Almacena el resultado del análisis de sensibilidad en dos ficheros CSV. Cada uno contendrá una cabecera que
	 * lista las diferentes soluciones (identificadas cada una con un número que empeieza en 1, en el mismo orden en
	 * el que se especificaron en el fichero de soluciones) seguida de una línea por iteración.
	 * @param rutaFicheroFitness Fichero que contiene el fitness de cada solución en cada una de las iteraciones
	 * @param rutaFicheroRanking Fichero que contiene el ranking de cada solución en la lista de fitness ordenada de
	 *                           mayor a menor en cada una de las iteraciones
	 */
	public void guardarResultado(String rutaFicheroFitness, String rutaFicheroRanking) {
		if (resultado == null) {
			throw new IllegalOperationException("No se puede almacenar el resultado del análisis de sensibilidad si " +
				"aún no se ha ejecutado");
		} else {
			resultado.toCsv(rutaFicheroFitness, rutaFicheroRanking);
		}
	}

	/**
	 * Actualiza el valor de los pesos de cada criterio en todas las soluciones que se están considerando
	 * @param nuevosPesos Nuevos pesos para cada criterio
	 */
	private void actualizarPesos(Map<IDCriterio, Float> nuevosPesos) {
		for (SoluciónEvaluable solución : solucionesEv) {
			solución.setPesos(nuevosPesos);
		}
	}
}
