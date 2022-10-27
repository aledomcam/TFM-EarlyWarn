package earlywarn.mh.vnsrs.sensibilidad;

import earlywarn.main.Utils;
import org.neo4j.logging.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que almacena el resultado de la ejecución del análisis de sensibilidad, iteración a iteración.
 */
public class Resultado {
	private final int numSoluciones;
	// Cabecera a añadir al inicio de los ficheros CSV de salida
	private final String cabeceraCsv;
	private final Log log;
	/*
	 * Lista que almacena el fitness de cada solución en cada iteración.
	 */
	private final List<List<Double>> fitness;
	/*
	 * Lista que almacena la posición el ranking de cada solución evaluada en cada iteración. El ranking se determina
	 * en función del fitness de las soluciones.
	 */
	private final List<List<Integer>> rankings;

	/**
	 * Instancia la clase
	 * @param numSoluciones Número de soluciones que serán evaluadas en cada iteración
	 * @param log Log de Neo4J
	 */
	public Resultado(int numSoluciones, Log log) {
		this.numSoluciones = numSoluciones;
		this.log = log;
		fitness = new ArrayList<>();
		rankings = new ArrayList<>();
		cabeceraCsv = getCabecera();
	}

	/**
	 * Registra una nueva iteración del análisis de sensiblidad
	 * @param fitness Lista con el fitness de cada solución considerada en la iteración actual
	 * @param rankings Posición en el ranking de fitness de esta iteración de la solución actual
	 */
	public void registrarIteración(List<Double> fitness, List<Integer> rankings) {
		this.fitness.add(fitness);
		this.rankings.add(rankings);
	}

	/**
	 * Exporta el resultado a dos ficheros CSV. Los ficheros tendrán una línea por cada iteración,
	 * además de una cabecera indicando el número de solución al que pertenece cada valor.
	 * @param rutaFicheroFitness Ruta al fichero de salida en el que almacenar los datos del fitness de cada solución
	 * @param rutaFicheroRankings Ruta al fichero de salida en el que almacenar los datos del ranking de cada solución
	 */
	public void toCsv(String rutaFicheroFitness, String rutaFicheroRankings) {
		if (!crearDirectorioPadre(rutaFicheroFitness) || !crearDirectorioPadre(rutaFicheroRankings)) {
			return;
		}

		generarCsv(fitness, rutaFicheroFitness);
		generarCsv(rankings, rutaFicheroRankings);
	}

	/**
	 * @return Cabecera a usar al inicio de los ficheros CSV. Contendrá un valor por cada solución, numeradas empezando
	 * en 1.
	 */
	private String getCabecera() {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= numSoluciones; i++) {
			if (i != 1) {
				sb.append(",");
			}
			sb.append("S").append(i);
		}
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Crea el directorio en el que deberá contenerse el fichero con la ruta especificada
	 * @param rutaFichero Ruta en la que colocar el fichero indicado
	 * @return True si se ha podido crear el directorio, false en caso contrario
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean crearDirectorioPadre(String rutaFichero) {
		try {
			Files.createDirectory(Paths.get(rutaFichero).getParent());
		} catch (FileAlreadyExistsException e) {
			// OK
		} catch (IOException e) {
			log.warn("No se ha podido crear el directorio para almacenar las estadísticas del análisis de " +
				"sensibilidad.\n" + e);
			return false;
		}
		return true;
	}

	/**
	 * Genera un fichero CSV que contiene los datos de la lista de datos indicada
	 * @param elementos Lista con los valores de cada iteración
	 * @param rutaFichero Ruta del fichero de salida
	 */
	private void generarCsv(List<? extends List<?>> elementos, String rutaFichero) {
		try (FileWriter fSalida = new FileWriter(rutaFichero)) {
			fSalida.write(cabeceraCsv);
			for (List<?> listaActual : elementos) {
				fSalida.write(Utils.listaToString(listaActual, false, false) + "\n");
			}
		} catch (IOException e) {
			log.warn("No se han podido guardar las estadísticas del análisis de sensibilidad.\n" + e);
		}
	}
}
