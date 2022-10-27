package earlywarn.mh.vnsrs;

import org.neo4j.logging.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que almacena diversas estadísticas durante la ejecución de la metaheurística
 */
public class Estadísticas {
	private final Log log;
	public final List<EstadísticasIteración> listaEstadísticas;

	public Estadísticas(Log log) {
		this.log = log;
		listaEstadísticas = new ArrayList<>();
	}

	/**
	 * Registra las estadísticas de una sola iteración al final de la misma
	 * @param estadísticasIteración Estadísticas de la iteración
	 */
	public void registrarIteración(EstadísticasIteración estadísticasIteración) {
		listaEstadísticas.add(estadísticasIteración);
	}

	/**
	 * Convierte el registro de estadísticas almacenado a un fichero CSV. El fichero tendrá una línea por cada
	 * iteración sobre la que se hayan almacenado estadísticas, además de una cabecera listando los campos en la
	 * primera línea.
	 * @param rutaFichero Ruta al fichero de salida
	 */
	public void toCsv(String rutaFichero) {
		try {
			Files.createDirectory(Paths.get(rutaFichero).getParent());
		} catch (FileAlreadyExistsException e) {
			// OK
		} catch (IOException e) {
			log.warn("No se ha podido crear el directorio para almacenar las estadísticas de la metaheurística.\n" + e);
			return;
		}

		try (FileWriter fSalida = new FileWriter(rutaFichero)) {
			fSalida.write(EstadísticasIteración.cabecera() + "\n");
			for (EstadísticasIteración entrada : listaEstadísticas) {
				fSalida.write(entrada.toString() + "\n");
			}
		} catch (IOException e) {
			log.warn("No se han podido guardar las estadísticas de la metaheurística.\n" + e);
		}
	}
}
