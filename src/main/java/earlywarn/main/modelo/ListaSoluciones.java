package earlywarn.main.modelo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contiene una lista de soluciones al problema de apertura y cierre de líneas
 */
public class ListaSoluciones implements Iterable<List<String>> {
	/*
	 * Lista que almacena todas las soluciones en la lista. Cada una está representada por una lista con los IDs de
	 * las líneas cerradas en la solución.
	 */
	public List<List<String>> soluciones;

	/**
	 * Crea una nueva instancia de la clase en base a los datos especificados en un fichero
	 * @param rutaFichero Ruta a un fichero que contenga la lista de soluciones. Cada una debe encontrarse en una
	 *                    línea del mismo, identificada como una lista de IDs de líneas cerradas (formato "IATA1-IATA2")
	 *                    separada por comas.
	 */
	@SuppressWarnings("ProhibitedExceptionThrown")
	public ListaSoluciones(String rutaFichero) {
		soluciones = new ArrayList<>();

		try (BufferedReader input = new BufferedReader(new FileReader(rutaFichero))) {
			String líneaFichero;
			while ((líneaFichero = input.readLine()) != null) {
				líneaFichero = líneaFichero.replace(", ", ",");
				String[] líneas = líneaFichero.split(",");
				List<String> listaLíneas = new ArrayList<>(List.of(líneas));
				soluciones.add(listaLíneas);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("No se ha encontrado el fichero con la lista de soluciones", e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterator<List<String>> iterator() {
		return soluciones.iterator();
	}

	public int size() {
		return soluciones.size();
	}
}
