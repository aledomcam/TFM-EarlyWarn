package earlywarn.main.modelo.datoid;

import earlywarn.definiciones.ETLOperationRequiredException;
import earlywarn.definiciones.Propiedad;
import earlywarn.etl.Añadir;
import earlywarn.main.Propiedades;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Representa un aeropuerto almacenado en la BD y su actividad durante un cierto periodo de tiempo.
 * Esta clase permite consultar ciertas propiedades del aeropuerto (como su valor de conectividad base o el número de
 * vuelos que salen del mismo) minimizando el número de accesos a la BD (solo se accede la primera vez que se consulta
 * un dato, después el dato se cachea).
 */
public class Aeropuerto {
	private final GraphDatabaseService db;
	// Código IATA del aeropuerto
	public final String id;
	private final String díaInicio;
	private final String díaFin;
	private final Propiedades propiedades;

	// Valores cacheados
	private Integer conectividadBase;
	private Long numVuelosSalida;
	private String idPaís;

	/**
	 * Crea una instancia de la clase
	 * @param id Código IATA que identifica al aeropuerto.
	 * @param díaInicio Primer día a tener en cuenta al obtener datos de vuelos que pasan por este aeropuerto
	 * @param díaFin Último día a tener en cuenta al obtener datos de vuelos que pasan por este aeropuerto
	 * @param db Conexión a la BD
	 */
	public Aeropuerto(String id, LocalDate díaInicio, LocalDate díaFin, GraphDatabaseService db) {
		this.id = id;
		this.db = db;
		this.díaInicio = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.díaFin = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		propiedades = new Propiedades(db);
	}

	/**
	 * Obtiene el valor de conectividad base del aeropuerto. Requiere que se haya ejecutado la operación ETL que
	 * carga los datos de conectividad en la BD.
	 * @return Conectividad base del aeropuerto
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#añadirConectividad(String)}
	 */
	public int getConectividadBase() {
		if (conectividadBase == null) {
			if (propiedades.getBool(Propiedad.ETL_CONECTIVIDAD)) {
				try (Transaction tx = db.beginTx()) {
					try (Result res = tx.execute(
						"MATCH (a:Airport {iata: \"" + id + "\"}) " +
						"RETURN a.connectivity")) {

						Map<String, Object> row = res.next();
						Object valor = row.get(res.columns().get(0));
						if (valor == null) {
							// Este aeropuerto no tiene datos de conectividad
							conectividadBase = 0;
						} else {
							conectividadBase = ((Long) valor).intValue();
						}
					}
				}
			} else {
				throw new ETLOperationRequiredException("Esta operación requiere que se haya ejecutado la operación ETL " +
					" que carga los datos de conectividad en la BD");
			}
		}
		return conectividadBase;
	}

	/**
	 * Devuelve el número de vuelos que salen de este aeropuerto en el rango de fechas correspondiente.
	 * @return Número de vuelos que salen de este aeropuerto en el rango de fechas especificado al crearlo
	 */
	public long getNumVuelosSalida() {
		if (numVuelosSalida == null) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(
					"MATCH (:Airport {iata: \"" + id + "\"})-[]-(:AirportOperationDay)-[]->(f:FLIGHT) " +
					"WHERE date(\"" + díaInicio + "\") <= f.dateOfDeparture <= date(\"" + díaFin + "\") " +
					"RETURN count(f)")) {

					Map<String, Object> row = res.next();
					numVuelosSalida = (Long) row.get(res.columns().get(0));
				}
			}
		}
		return numVuelosSalida;
	}

	/**
	 * Devuelve el ID del país en el que se encuentra este aeropuerto. Requiere que se haya ejecutado la operación
	 * ETL que añade las relaciones faltantes entre aeropuerto y país.
	 * @return ID del país en el que se encuentra este aeropuerto
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#añadirConexionesAeropuertoPaís()}
	 */
	public String getIdPaís() {
		if (idPaís == null) {
			if (propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS)) {
				try (Transaction tx = db.beginTx()) {
					try (Result res = tx.execute(
						"MATCH (:Airport {iata: \"" + id + "\"})-[]-(c:Country) " +
						"RETURN c.countryId")) {

						Map<String, Object> row = res.next();
						idPaís = (String) row.get(res.columns().get(0));
					}
				}
			} else {
				throw new ETLOperationRequiredException("Esta operación requiere que se haya ejecutado la operación ETL " +
					"que añade las relaciones faltantes entre aeropuerto y país antes de ejecutarla.");
			}
		}
		return idPaís;
	}
}
