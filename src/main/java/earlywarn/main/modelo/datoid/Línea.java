package earlywarn.main.modelo.datoid;

import earlywarn.definiciones.ETLOperationRequiredException;
import earlywarn.definiciones.Propiedad;
import earlywarn.etl.Añadir;
import earlywarn.etl.Modificar;
import earlywarn.main.Consultas;
import earlywarn.main.Propiedades;
import earlywarn.main.Utils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Representa una línea de conexión dirigida entre dos aeropuertos que engloba todos los vuelos entre ellos en un cierto
 * rango de fechas.
 * Esta clase permite consultar ciertas propiedades de la línea (como el número total de pasajeros o de vuelos)
 * minimizando el número de accesos a la BD.
 */
public class Línea {
	private final GraphDatabaseService db;
	public final String id;
	public final String idAeropuertoOrigen;
	public final String idAeropuertoDestino;
	private final String díaInicio;
	private final String díaFin;
	private final Propiedades propiedades;

	// True si los valores simples (todos salvo los pasajeros por aerolínea) se han cargado
	private boolean valoresSimplesCargados;

	// Valores cacheados
	private Long pasajeros;
	private Double ingresosTurísticos;
	private Map<String, Long> pasajerosPorAerolínea;
	private Long numVuelos;
	private Double riesgoImportado;

	/**
	 * Crea una instancia de la clase
	 * @param id Identificador de la línea. Formado por el código IATA del aeropuerto de origen, un guión y el
	 *           código IATA del aerpuerto destino.
	 * @param díaInicio Primer día a tener en cuenta al obtener datos de vuelos que viajan por esta línea
	 * @param díaFin Último día a tener en cuenta al obtener datos de vuelos que viajan por esta línea
	 * @param db Conexión a la BD
	 */
	public Línea(String id, LocalDate díaInicio, LocalDate díaFin, GraphDatabaseService db) {
		this.id = id;
		this.db = db;
		this.díaInicio = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.díaFin = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		propiedades = new Propiedades(db);

		String[] split = id.split("-");
		idAeropuertoOrigen = split[0];
		idAeropuertoDestino = split[1];
		valoresSimplesCargados = false;
	}

	/**
	 * Obtiene el número total de pasajeros que circulan por esta línea en el periodo establecido.
	 * Requiere que se haya ejecutado la operación ETL que calcula el número de pasajeros por vuelo, la operación ETL
	 * que calcula los ingresos por turismo de cada vuelo y la operación ETL que convierte las fechas de vuelos a
	 * tipo date.
	 * @return Número de pasajeros que circulan por esta línea en el periodo establecido.
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#calcularNúmeroPasajeros()}, la operación ETL {@link Modificar#convertirFechasVuelos()}
	 * o la operación ETL {@link Añadir#añadirIngresosVuelo(Boolean, Boolean)}.
	 */
	public long getPasajeros() {
		if (!valoresSimplesCargados) {
			cargarValoresSimples();
		}
		return pasajeros;
	}

	/**
	 * Obtiene los ingresos totales por turismo derivados de los vuelos que circulan por esta línea en el periodo
	 * establecido.
	 * Requiere que se haya ejecutado la operación ETL que calcula el número de pasajeros por vuelo, la operación ETL
	 * que calcula los ingresos por turismo de cada vuelo y la operación ETL que convierte las fechas de vuelos a
	 * tipo date.
	 * @return Ingresos totales derivados del turismo de los vuelos de esta línea
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#calcularNúmeroPasajeros()}, la operación ETL {@link Modificar#convertirFechasVuelos()}
	 * o la operación ETL {@link Añadir#añadirIngresosVuelo(Boolean, Boolean)}.
	 */
	public double getIngresosTurísticos() {
		if (!valoresSimplesCargados) {
			cargarValoresSimples();
		}
		return ingresosTurísticos;
	}

	/**
	 * Obtiene el número total de vuelos que circulan por esta línea en el periodo establecido.
	 * Requiere que se haya ejecutado la operación ETL que calcula el número de pasajeros por vuelo, la operación ETL
	 * que calcula los ingresos por turismo de cada vuelo y la operación ETL que convierte las fechas de vuelos a
	 * tipo date.
	 * @return Número de pasajeros que circulan por esta línea en el periodo establecido.
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#calcularNúmeroPasajeros()}, la operación ETL {@link Modificar#convertirFechasVuelos()}
	 * o la operación ETL {@link Añadir#añadirIngresosVuelo(Boolean, Boolean)}.
	 */
	public long getNumVuelos() {
		if (!valoresSimplesCargados) {
			cargarValoresSimples();
		}
		return numVuelos;
	}

	/**
	 * Obtiene el riesgo importado total de los vuelos que circulan por esta línea en el periodo establecido.
	 * Requiere que se haya ejecutado la operación ETL que calcula el número de pasajeros por vuelo, la operación ETL
	 * que calcula los ingresos por turismo de cada vuelo y la operación ETL que convierte las fechas de vuelos a
	 * tipo date.
	 * @return Riesgo importado total de los vuelos de esta línea
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#calcularNúmeroPasajeros()}, la operación ETL {@link Modificar#convertirFechasVuelos()}
	 * o la operación ETL {@link Añadir#añadirIngresosVuelo(Boolean, Boolean)}.
	 */
	public double getRiesgoImportado() {
		if (!valoresSimplesCargados) {
			cargarValoresSimples();
		}
		return riesgoImportado;
	}

	/**
	 * Obtiene el número de pasajeros por aerolínea de los vuelos que circulan por esta línea en el periodo establecido.
	 * Requiere que se haya ejecutado la operación ETL que calcula el número de pasajeros a bordo de cada vuelo y la
	 * operación ETL que convierte las fechas de vuelos a tipo date.
	 * Se excluyen los pasajeros de vuelos cuya aerolínea se desconoce.
	 * @return Mapa que relaciona códigos de aerolíneas con el número de pasajeros que viajan con cada una
	 * en el rango de fechas establecido. No incluye aerolínas con 0 pasajeros.
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#calcularNúmeroPasajeros()} o la operación ETL {@link Modificar#convertirFechasVuelos()}.
	 */
	public Map<String, Long> getPasajerosPorAerolínea() {
		if (pasajerosPorAerolínea == null) {
			if (propiedades.getBool(Propiedad.ETL_PASAJEROS) &&
			propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS)) {
				try (Transaction tx = db.beginTx()) {
					try (Result res = tx.execute(
						"MATCH (a1:Airport)-[]-(aod1:AirportOperationDay)-[]->(f:FLIGHT)-[]->" +
						"(aod2:AirportOperationDay)-[]-(a2:Airport) " +
						"WHERE a1.iata = \"" + idAeropuertoOrigen + "\" AND a2.iata = \"" + idAeropuertoDestino +
						"\" AND date(\"" + díaInicio + "\") <= f.dateOfDeparture <= date(\"" + díaFin + "\") " +
						"RETURN distinct(f.operator), sum(f.passengers)")) {

						pasajerosPorAerolínea = new TreeMap<>();
						List<String> columnas = res.columns();
						while (res.hasNext()) {
							Map<String, Object> row = res.next();
							String aerolínea = (String) row.get(columnas.get(0));
							Long numPasajeros = (Long) row.get(columnas.get(1));
							if (!aerolínea.equals(Consultas.AEROLÍNEA_DESCONOCIDA) && numPasajeros > 0) {
								pasajerosPorAerolínea.put(aerolínea, numPasajeros);
							}
						}
					}
				}
			} else {
				throw new ETLOperationRequiredException("Esta operación requiere que se haya ejecutado la operación ETL " +
					"que calcula el número de pasajeros de cada vuelo y la operación ETL que convierte las fechas de " +
					"vuelos a tipo date antes de ejecutarla.");
			}
		}
		return pasajerosPorAerolínea;
	}

	/**
	 * Carga los valores simples de la línea.
	 * Requiere que se haya ejecutado la operación ETL que calcula el número de pasajeros por vuelo, la operación ETL
	 * que calcula los ingresos por turismo de cada vuelo y la operación ETL que convierte las fechas de vuelos a
	 * tipo date.
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#calcularNúmeroPasajeros()}, la operación ETL {@link Modificar#convertirFechasVuelos()}
	 * o la operación ETL {@link Añadir#añadirIngresosVuelo(Boolean, Boolean)}.
	 */
	private void cargarValoresSimples() {
		if (propiedades.getBool(Propiedad.ETL_PASAJEROS) &&
		propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_INGRESOS_VUELO)) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(
					"MATCH (a1:Airport)-[]-(aod1:AirportOperationDay)-[]->(f:FLIGHT)-[]->" +
					"(aod2:AirportOperationDay)-[]-(a2:Airport) " +
					"WHERE a1.iata = \"" + idAeropuertoOrigen + "\" AND a2.iata = \"" + idAeropuertoDestino +
					"\" AND date(\"" + díaInicio + "\") <= f.dateOfDeparture <= date(\"" + díaFin + "\") " +
					"RETURN sum(f.passengers) AS numPasajeros, sum(f.incomeFromTurism) AS ingresosTurismo, " +
					"count(f) AS numVuelos, sum(f.flightIfinal) AS riesgoImportado")) {

					Map<String, Object> row = res.next();
					pasajeros = (Long) row.get("numPasajeros");
					ingresosTurísticos = Utils.resultadoADouble(row.get("ingresosTurismo"));
					numVuelos = (Long) row.get("numVuelos");
					riesgoImportado = Utils.resultadoADouble(row.get("riesgoImportado"));
					valoresSimplesCargados = true;
				}
			}
		} else {
			throw new ETLOperationRequiredException("Esta operación requiere que se haya ejecutado la operación ETL " +
				"que calcula el número de pasajeros de cada vuelo, la operación ETL que calcula los ingresos por " +
				"turismo de cada vuelo y la operación ETL que convierte las fechas de vuelos a tipo date " +
				"antes de ejecutarla.");
		}
	}
}
