package earlywarn.main;

import earlywarn.definiciones.*;
import earlywarn.etl.Modificar;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Clase usada para realizar consultas sencillas a la base de datos
 */
public class Consultas {
	// Aerolínea que tienen asignada los vuelos de la BD para los que no se conoce quién opera el vuelo
	public static final String AEROLÍNEA_DESCONOCIDA = "UNKNOWN";

	/*
	 * La instancia de la base de datos.
	 * Debe ser obtenida usando la anotación @Context en un procedimiento o función
	 */
	private final GraphDatabaseService db;

	// Primer año del que se tienen datos de turismo. Null si aún no se ha consultado la BD para obtener el valor.
	private Integer primerAñoDatosTurismo;
	// Último año del que se tienen datos de turismo. Null si aún no se ha consultado la BD para obtener el valor.
	private Integer últimoAñoDatosTurismo;
	// Primer año del que se tienen datos de gasto turístico. Null si aún no se ha consultado la BD para obtener el valor.
	private Integer primerAñoDatosGastoTurístico;
	// Último año del que se tienen datos de gasto turístico. Null si aún no se ha consultado la BD para obtener el valor.
	private Integer últimoAñoDatosGastoTurístico;

	public Consultas(GraphDatabaseService db) {
		this.db = db;
		primerAñoDatosTurismo = null;
		últimoAñoDatosTurismo = null;
		primerAñoDatosGastoTurístico = null;
		últimoAñoDatosGastoTurístico = null;
	}

	/**
	 * Devuelve el número de vuelos que entran y/o salen del aeropuerto indicado en el rango de días indicados.
	 * Requiere que se haya llevado a cabo la operación ETL que convierte las relaciones entre Airport y AOD.
	 * @param idAeropuerto Código IATA del aeropuerto
	 * @param díaInicio Primer día en el que buscar vuelos (inclusivo)
	 * @param díaFin Último día en el que buscar vuelos (inclusivo)
	 * @param sentido Sentido de los vuelos a considerar
	 * @return Número de vuelos que salen del aeropuerto en el día indicado
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Modificar#convertirRelacionesAOD()}.
	 */
	public long getVuelosAeropuerto(String idAeropuerto, LocalDate díaInicio, LocalDate díaFin, SentidoVuelo sentido) {
		String díaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String díaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		if (new Propiedades(db).getBool(Propiedad.ETL_RELACIONES_AOD)) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(
					"MATCH (a:Airport)-[o:OPERATES_ON]->(:AirportOperationDay)" +
					sentido.operadorAODVuelo("") + "(f:FLIGHT) " +
					"WHERE a.iata = \"" + idAeropuerto + "\" " +
					"AND date(\"" + díaInicioStr + "\") <= o.date <= date(\"" + díaFinStr + "\") " +
					"RETURN count(f)")) {
					Map<String, Object> row = res.next();
					return (long) row.get(res.columns().get(0));
				}
			}
		} else {
			throw new ETLOperationRequiredException("Esta operación requiere que se haya ejecutado la operación ETL " +
				"de conversión de relaciones AOD antes de ejecutarla.");
		}
	}

	/**
	 * Devuelve el año más antiguo del que se tienen datos acerca del porcentaje de visitantes por motivos turísticos.
	 * El valor está cacheado, por lo que una instancia de esta clase siempre devolverá el mismo. La instancia actual
	 * solo consultará la base de datos la primera vez que se llame a este método.
	 * @return Año más antiguo del que hay registros en la BD acerca del porcentaje de turistas
	 */
	public int getPrimerAñoDatosTurismo() {
		if (primerAñoDatosTurismo == null) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute("MATCH (tr:TuristRatio) RETURN min(tr.year)")) {
					Map<String, Object> row = res.next();
					primerAñoDatosTurismo = Math.toIntExact((Long) row.get(res.columns().get(0)));
				}
			}
		}
		return primerAñoDatosTurismo;
	}

	/**
	 * Devuelve el año más reciente del que se tienen datos acerca del porcentaje de visitantes por motivos turísticos.
	 * El valor está cacheado, por lo que una instancia de esta clase siempre devolverá el mismo. La instancia actual
	 * solo consultará la base de datos la primera vez que se llame a este método.
	 * @return Año más reciente del que hay registros en la BD acerca del porcentaje de turistas
	 */
	public int getÚltimoAñoDatosTurismo() {
		if (últimoAñoDatosTurismo == null) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute("MATCH (tr:TuristRatio) RETURN max(tr.year)")) {
					Map<String, Object> row = res.next();
					últimoAñoDatosTurismo = Math.toIntExact((Long) row.get(res.columns().get(0)));
				}
			}
		}
		return últimoAñoDatosTurismo;
	}

	/**
	 * Devuelve el año más antiguo del que se tienen datos acerca del gasto turístico por persona y país de origen.
	 * El valor está cacheado, por lo que una instancia de esta clase siempre devolverá el mismo. La instancia actual
	 * solo consultará la base de datos la primera vez que se llame a este método.
	 * @return Año más antiguo del que hay registros en la BD acerca del gasto turístico
	 */
	public int getPrimerAñoDatosGastoTurístico() {
		if (primerAñoDatosGastoTurístico == null) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute("MATCH (te:TuristExpense) RETURN min(te.year)")) {
					Map<String, Object> row = res.next();
					primerAñoDatosGastoTurístico = Math.toIntExact((Long) row.get(res.columns().get(0)));
				}
			}
		}
		return primerAñoDatosGastoTurístico;
	}

	/**
	 * Devuelve el año más reciente del que se tienen datos acerca del gasto turístico por persona y país de origen.
	 * El valor está cacheado, por lo que una instancia de esta clase siempre devolverá el mismo. La instancia actual
	 * solo consultará la base de datos la primera vez que se llame a este método.
	 * @return Año más reciente del que hay registros en la BD acerca del gasto turístico
	 */
	public int getÚltimoAñoDatosGastoTurístico() {
		if (últimoAñoDatosGastoTurístico == null) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute("MATCH (te:TuristExpense) RETURN max(te.year)")) {
					Map<String, Object> row = res.next();
					últimoAñoDatosGastoTurístico = Math.toIntExact((Long) row.get(res.columns().get(0)));
				}
			}
		}
		return últimoAñoDatosGastoTurístico;
	}

	/**
	 * Dado una fecha de inicio, una fecha de fin y un país devuelve el valor de riesgo importado para el país.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @param idPaís Identificador del país tal y como aparece en la base de datos
	 * @param díaInicio Primer día a tener en cuenta
	 * @param díaFin Último día a tener en cuenta
	 * @return Valor del riesgo importado (SIR total) para el país indicado teniendo en cuenta todos los vuelos entrantes
	 * en el periodo especificado.
	 */
	public Double getRiesgoPorPaís(LocalDate díaInicio, LocalDate díaFin, String idPaís) {
		String diaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String diaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Propiedades propiedades = new Propiedades(db);

		if (propiedades.getBool(Propiedad.ETL_BORRAR_VUELOS_SIN_SIR) &&
		propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS) &&
		propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(
					"MATCH (:Airport)-[]->(:AirportOperationDay)-[]->(f:FLIGHT)" +
					"-[]->(:AirportOperationDay)<-[]-(:Airport)-[]-(c:Country) " +
					"WHERE c.countryId=\"" + idPaís + "\" " +
					"AND date(\"" + diaInicioStr + "\") <= f.dateOfDeparture <= date(\"" + diaFinStr + "\") " +
					"RETURN sum(f.flightIfinal)")) {
					Map<String, Object> row = res.next();
					return Utils.resultadoADouble(row.get(res.columns().get(0)));
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo");
		}
	}

	/**
	 * Obtiene el número total de pasajeros que viajan en un rango de fechas,
	 * opcionalmente filtrando por país de destino.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @param díaInicio Primer día a tener en cuenta
	 * @param díaFin Último día a tener en cuenta
	 * @param idPaís Solo se tendrán en cuenta los vuelos que tienen este país como destino, o todos
	 *               si se deja en blanco.
	 * @return Número total de pasajeros en el rango de fechas indicado.
	 */
	public int getPasajerosTotales(LocalDate díaInicio, LocalDate díaFin, String idPaís) {
		String díaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String díaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Propiedades propiedades = new Propiedades(db);

		if (propiedades.getBool(Propiedad.ETL_PASAJEROS) && propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS) &&
		propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			try (Transaction tx = db.beginTx()) {
				if (idPaís.isEmpty()) {
					try (Result res = tx.execute(
						"MATCH (f:FLIGHT) WHERE date(\"" + díaInicioStr + "\") <= f.dateOfDeparture <= " +
						"date(\"" + díaFinStr + "\") RETURN sum(f.passengers)")) {
						Map<String, Object> row = res.next();
						return Math.toIntExact((Long) row.get(res.columns().get(0)));
					}
				} else {
					try (Result res = tx.execute(
						"MATCH (:Airport)-[]->(:AirportOperationDay)-[]->(f:FLIGHT)" +
						"-[]->(:AirportOperationDay)-[]-(:Airport)-[]-(c:Country) " +
						"WHERE c.countryId = \"" + idPaís + "\" AND date(\"" + díaInicioStr + "\") <= " +
						"f.dateOfDeparture <= date(\"" + díaFinStr + "\") RETURN sum(f.passengers)")) {
						Map<String, Object> row = res.next();
						return Math.toIntExact((Long) row.get(res.columns().get(0)));
					}
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo.");
		}
	}
	/**
	 * @see #getPasajerosTotales(LocalDate, LocalDate, String)
	 */
	public int getPasajerosTotales(LocalDate díaInicio, LocalDate díaFin) {
		return getPasajerosTotales(díaInicio, díaFin, "");
	}

	/**
	 * Obtiene los ingresos turísticos totales en un rango de fechas, opcionalmente filtrando por país de destino.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @param díaInicio Primer día a tener en cuenta
	 * @param díaFin Último día a tener en cuenta
	 * @param idPaís Solo se tendrán en cuenta los vuelos que tienen este país como destino, o todos
	 *               si se deja en blanco.
	 * @return Ingresos totales (en euros) entre todos los vuelos en el periodo indicado
	 */
	public double getIngresosTurísticosTotales(LocalDate díaInicio, LocalDate díaFin, String idPaís) {
		String díaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String díaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Propiedades propiedades = new Propiedades(db);

		if (propiedades.getBool(Propiedad.ETL_INGRESOS_VUELO) && propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS) &&
		propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			try (Transaction tx = db.beginTx()) {
				if (idPaís.isEmpty()) {
					try (Result res = tx.execute(
						"MATCH (f:FLIGHT) WHERE date(\"" + díaInicioStr + "\") <= f.dateOfDeparture <= " +
						"date(\"" + díaFinStr + "\") RETURN sum(f.incomeFromTurism)")) {
						Map<String, Object> row = res.next();
						return (double) row.get(res.columns().get(0));
					}
				} else {
					try (Result res = tx.execute(
						"MATCH (:Airport)-[]->(:AirportOperationDay)-[]->(f:FLIGHT)" +
						"-[]->(:AirportOperationDay)-[]-(:Airport)-[]-(c:Country) " +
						"WHERE c.countryId = \"" + idPaís + "\" AND date(\"" + díaInicioStr + "\") <= " +
						"f.dateOfDeparture <= date(\"" + díaFinStr + "\") RETURN sum(f.incomeFromTurism)")) {
						Map<String, Object> row = res.next();
						return Utils.resultadoADouble(row.get(res.columns().get(0)));
					}
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo.");
		}
	}
	/**
	 * @see #getIngresosTurísticosTotales(LocalDate, LocalDate, String)
	 */
	public double getIngresosTurísticosTotales(LocalDate díaInicio, LocalDate díaFin) {
		return getIngresosTurísticosTotales(díaInicio, díaFin, "");
	}

	/**
	 * Obtiene el valor total de conectividad entre todos los aeropuertos.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @return Conectividad total entre todos los aeropuertos
	 */
	public int getConectividadTotal() {
		Propiedades propiedades = new Propiedades(db);

		if (propiedades.getBool(Propiedad.ETL_CONECTIVIDAD) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(
					"MATCH (a:Airport) RETURN sum(a.connectivity)")) {
					Map<String, Object> row = res.next();
					return Math.toIntExact((Long) row.get(res.columns().get(0)));
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo.");
		}
	}

	/**
	 * Obtiene la parte del valor de conectividad total que representan los vuelos hacia un cierto país tomando
	 * un rango de fechas como referencia.
	 * Es decir, este método permite conocer cuánto se reduciría la conectividad global si se cerrasen todos los vuelos
	 * al país indicado en el rango de fechas indicado.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @param díaInicio Primer día a tener en cuenta
	 * @param díaFin Último día a tener en cuenta
	 * @param idPaís ID del país de destino. Solo se tendrán en cuenta los vuelos hacia este país
	 * @return Porcentaje de la conectividad total que depende de los vuelos al país indicado en el rango de fechas
	 * indicado
	 */
	public int getConectividadPaís(LocalDate díaInicio, LocalDate díaFin, String idPaís) {
		String díaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String díaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Propiedades propiedades = new Propiedades(db);

		if (propiedades.getBool(Propiedad.ETL_CONECTIVIDAD) && propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS)
		&& propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(
					"MATCH (a:Airport)-[]-(:AirportOperationDay)-[]->(f:FLIGHT) " +
					"WHERE date(\"" + díaInicioStr + "\") <= f.dateOfDeparture <= date(\"" + díaFinStr + "\") " +
					"WITH a, count(f) as cf " +
					"MATCH (a)-[]-(:AirportOperationDay)-[]->(f2:FLIGHT)-[]->(:AirportOperationDay)-[]-(:Airport)" +
					"-[]-(c:Country) " +
					"WHERE date(\"" + díaInicioStr + "\") <= f2.dateOfDeparture <= date(\"" + díaFinStr + "\") " +
					"AND c.countryId = \"" + idPaís + "\" " +
					"WITH a, cf, count(f2) as cf2 " +
					"RETURN sum(a.connectivity * cf2 / cf)")) {
					Map<String, Object> row = res.next();
					return (int) Math.round(Utils.resultadoADouble(row.get(res.columns().get(0))));
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo.");
		}
	}

	/**
	 * Obtiene el número de pasajeros que viajan con cada aerolínea en un rango de fechas,
	 * opcionalmente filtrando por país de destino.
	 * Se excluyen los pasajeros de vuelos cuya aerolínea se desconoce.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @param díaInicio Primer día a tener en cuenta
	 * @param díaFin Último día a tener en cuenta
	 * @param idPaís Solo se tendrán en cuenta los vuelos que tienen este país como destino, o todos
	 *               si se deja en blanco.
	 * @return Mapa que relaciona códigos de aerolíneas con el número de pasajeros que viajan con cada una
	 * en el rango de fechas indicado. No incluye aerolínas con 0 pasajeros.
	 */
	public TreeMap<String, Long> getPasajerosPorAerolínea(LocalDate díaInicio, LocalDate díaFin, String idPaís) {
		String díaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String díaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Propiedades propiedades = new Propiedades(db);
		TreeMap<String, Long> ret = new TreeMap<>();

		if (propiedades.getBool(Propiedad.ETL_PASAJEROS) && propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS)
		&& propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			String consulta;
			if (idPaís.isEmpty()) {
				consulta = "MATCH (f:FLIGHT) WHERE date(\"" + díaInicioStr + "\") <= f.dateOfDeparture <= " +
					"date(\"" + díaFinStr + "\") RETURN distinct(f.operator), sum(f.passengers)";
			} else {
				consulta = "MATCH (:Airport)-[]->(:AirportOperationDay)-[]->(f:FLIGHT)" +
					"-[]->(:AirportOperationDay)-[]-(:Airport)-[]-(c:Country) " +
					"WHERE c.countryId = \"" + idPaís + "\" AND date(\"" + díaInicioStr + "\") <= " +
					"f.dateOfDeparture <= date(\"" + díaFinStr + "\") " +
					"RETURN distinct(f.operator), sum(f.passengers)";
			}

			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(consulta)) {
					List<String> columnas = res.columns();
					while (res.hasNext()) {
						Map<String, Object> row = res.next();
						String aerolínea = (String) row.get(columnas.get(0));
						Long numPasajeros = (Long) row.get(columnas.get(1));
						if (!aerolínea.equals(AEROLÍNEA_DESCONOCIDA) && numPasajeros > 0) {
							ret.put(aerolínea, numPasajeros);
						}
					}
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo.");
		}

		return ret;
	}
	/**
	 * @see #getPasajerosPorAerolínea(LocalDate, LocalDate, String)
	 */
	public TreeMap<String, Long> getPasajerosPorAerolínea(LocalDate díaInicio, LocalDate díaFin) {
		return getPasajerosPorAerolínea(díaInicio, díaFin, "");
	}

	/**
	 * Obtiene el número de pasajeros que viajan desde y hasta cada aeropuerto en un rango de fechas,
	 * opcionalmente filtrando por país de destino.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @param díaInicio Primer día a tener en cuenta
	 * @param díaFin Último día a tener en cuenta
	 * @param idPaís Solo se tendrán en cuenta los vuelos que tienen este país como destino y solo se devolverán
	 *               aeropuertos de este país. Si se deja en blanco, la restricción no se aplica.
	 * @return Mapa que relaciona códigos IATA de aeropuertos con el número de pasajeros que viajan desde y hasta cada
	 * uno en el rango de fechas indicado. No incluye aeropuertos con 0 pasajeros.
	 */
	public TreeMap<String, Long> getPasajerosPorAeropuerto(LocalDate díaInicio, LocalDate díaFin, String idPaís) {
		String díaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String díaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Propiedades propiedades = new Propiedades(db);
		TreeMap<String, Long> ret = new TreeMap<>();

		if (propiedades.getBool(Propiedad.ETL_PASAJEROS) && propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS)
		&& propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			String consulta;
			if (idPaís.isEmpty()) {
				// Esta consulta cuenta a los pasajeros tanto a la llegada como a la salida
				consulta = "MATCH (f:FLIGHT)-[]-(:AirportOperationDay)-[]-(a:Airport) " +
					"WHERE date(\"" + díaInicioStr + "\") <= f.dateOfDeparture <= " +
					"date(\"" + díaFinStr + "\") RETURN distinct(a.iata), sum(f.passengers)";
			} else {
				/*
				 * Esta consulta cuenta primero los pasajeros del aeropuerto de destino (que es un aeropuerto del país
				 * indicado) y después los del aeropuerto de origen solo si éste es del país indicado.
				 */
				consulta = "MATCH (c1:Country)-[]-(a1:Airport)-[]-(aod1:AirportOperationDay)-[]->(f:FLIGHT)" +
					"-[]->(aod2:AirportOperationDay)-[]-(a2:Airport)-[]-(c2:Country) " +
					"WHERE c2.countryId = \"" + idPaís + "\" AND date(\"" + díaInicioStr + "\") <= " +
					"f.dateOfDeparture <= date(\"" + díaFinStr + "\") " +
					"CALL { " +
						"WITH f, a2 " +
						"RETURN distinct(a2.iata) AS iata, sum(f.passengers) AS p " +
						"UNION " +
						"WITH c1, a1, f " +
						"MATCH (c1) " +
						"WHERE c1.countryId = \"" + idPaís + "\" " +
						"RETURN distinct(a1.iata) AS iata, sum(f.passengers) AS p " +
					"} " +
					"RETURN distinct(iata), sum(p)";
			}

			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(consulta)) {
					List<String> columnas = res.columns();
					while (res.hasNext()) {
						Map<String, Object> row = res.next();
						String aeropuerto = (String) row.get(columnas.get(0));
						Long numPasajeros = (Long) row.get(columnas.get(1));
						// Parece que algunos resultados vienen sin aeropuerto
						if (!aeropuerto.isEmpty() && numPasajeros > 0) {
							ret.put(aeropuerto, numPasajeros);
						}
					}
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo.");
		}

		return ret;
	}
	/**
	 * @see #getPasajerosPorAeropuerto(LocalDate, LocalDate, String)
	 */
	public TreeMap<String, Long> getPasajerosPorAeropuerto(LocalDate díaInicio, LocalDate díaFin) {
		return getPasajerosPorAeropuerto(díaInicio, díaFin, "");
	}

	/**
	 * Devuelve todas las líneas (conexiones entre dos aeropuertos por los que circula al menos un vuelo) que existen
	 * en el periodo indicado, opcionalmente filtrando por país de destino.
	 * Requiere que se hayan ejecutado ciertas operaciones ETL.
	 * @param díaInicio Primer día a tener en cuenta
	 * @param díaFin Último día a tener en cuenta
	 * @param idPaís Solo se tendrán en cuenta los vuelos que tienen este país como destino. Si se deja en blanco,
	 *               la restricción no se aplica.
	 * @return Lista con los identificadores de todas las líneas (formados por el código IATA del aeropuerto de
	 * origen, un guión y el código IATA del aeropuerto de destino)
	 */
	public List<String> getLíneas(LocalDate díaInicio, LocalDate díaFin, String idPaís) {
		String díaInicioStr = díaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String díaFinStr = díaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Propiedades propiedades = new Propiedades(db);
		List<String> ret = new ArrayList<>();

		if (propiedades.getBool(Propiedad.ETL_AEROPUERTO_PAÍS) &&
		propiedades.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) &&
		propiedades.getBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA)) {
			String consulta;
			if (idPaís.isEmpty()) {
				consulta = "MATCH (c1:Country)-[]-(a1:Airport)-[]-(aod1:AirportOperationDay)-[]->(f:FLIGHT)" +
					"-[]->(aod2:AirportOperationDay)-[]-(a2:Airport)-[]-(c2:Country) " +
					"WHERE date(\"" + díaInicioStr + "\") <= f.dateOfDeparture <= date(\"" + díaFinStr + "\") " +
					"RETURN distinct([a1.iata, a2.iata])";
			} else {
				consulta = "MATCH (c1:Country)-[]-(a1:Airport)-[]-(aod1:AirportOperationDay)-[]->(f:FLIGHT)" +
					"-[]->(aod2:AirportOperationDay)-[]-(a2:Airport)-[]-(c2:Country) " +
					"WHERE c2.countryId = \"" + idPaís + "\" AND date(\"" + díaInicioStr + "\") <= " +
					"f.dateOfDeparture <= date(\"" + díaFinStr + "\") " +
					"RETURN distinct([a1.iata, a2.iata])";
			}

			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute(consulta)) {
					List<String> columnas = res.columns();
					while (res.hasNext()) {
						@SuppressWarnings("unchecked")
						List<String> row = (List<String>) res.next().get(columnas.get(0));
						ret.add(row.get(0) + "-" + row.get(1));
					}
				}
			}
		} else {
			throw new ETLOperationRequiredException("No se ha ejecutado una operación ETL requerida para realizar " +
				"este cálculo.");
		}

		return ret;
	}
}
