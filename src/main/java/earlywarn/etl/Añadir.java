package earlywarn.etl;

import earlywarn.definiciones.ETLOperationRequiredException;
import earlywarn.definiciones.Propiedad;
import earlywarn.main.Consultas;
import earlywarn.main.Propiedades;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;

/**
 * En esta clase se encuentran los procedimientos encargados de añadir datos a la BD antes de empezar a trabajar
 * con la misma.
 */
public class Añadir {
	@Context
	public GraphDatabaseService db;

	/**
	 * Requerido por Neo4J
	 * @deprecated Este constructor no debe utilizarse. Usar {@link Añadir#Añadir(GraphDatabaseService)} en su lugar.
	 */
	@Deprecated
	public Añadir() {

	}
	// Requerido para poder llamar a procedimientos desde otra clase
	public Añadir(GraphDatabaseService db) {
		this.db = db;
	}

	/**
	 * Añade las conexiones entre aeropuertos y países que faltan en la BD.
	 * Estas conexiones se obtienen buscando aeropuertos situados en un ProvinceState pero que no están relacionados
	 * con Country.
	 * Fija la propiedad {@link Propiedad#ETL_AEROPUERTO_PAÍS} a true en la BD.
	 */
	@Procedure(mode = Mode.WRITE)
	public void añadirConexionesAeropuertoPaís() {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"MATCH (a:Airport)-[]-(ps:ProvinceState)-[]-(c:Country) " +
				"MERGE (a)<-[:INFLUENCE_ZONE]-(c)");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_AEROPUERTO_PAÍS, true);
		}
	}

	/**
	 * Añade el valor de conectividad a los diferentes aeropuertos.
	 * Los datos se obtienen de un CSV importado con el nombre indicado. El formato esperado para el CSV es
	 * "códigoIATAAeropuerto,conectividad". El CSV no debe contener una cabecera. El valor de conectividad es un entero
	 * sin separador de miles.
	 * Fija la propiedad {@link Propiedad#ETL_CONECTIVIDAD} a true en la BD.
	 * @param rutaFichero Ruta al fichero CSV, relativa a la carpeta de import definida en la configuración de Neo4J.
	 */
	@Procedure(mode = Mode.WRITE)
	public void añadirConectividad(@Name("rutaFichero") String rutaFichero) {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"LOAD CSV FROM 'file:///" + rutaFichero + "' AS line " +
				"MATCH (a:Airport) " +
				"WHERE a.iata = line[0] " +
				"SET a.connectivity = toInteger(line[1])");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_CONECTIVIDAD, true);
		}
	}

	/**
	 * Añade un campo a cada vuelo que incluye su número de pasajeros. El valor se calcula el número de asientos y
	 * el porcentaje de ocupación del vuelo.
	 * Los valores faltantes del número de asientos y el porcentaje de ocupación se rellenarán con la media de todo
	 * el dataset.
	 * Fija la propiedad {@link Propiedad#ETL_PASAJEROS} a true en la BD.
	 */
	@Procedure(mode = Mode.WRITE)
	public void calcularNúmeroPasajeros() {
		try (Transaction tx = db.beginTx()) {
			int mediaAsientos;
			double mediaOcupación;
			try (Result res = tx.execute("MATCH (f:FLIGHT) RETURN avg(f.seatsCapacity)")) {
				Map<String, Object> row = res.next();
				mediaAsientos = (int) Math.round((double) row.get(res.columns().get(0)));
			}
			try (Result res = tx.execute("MATCH (f:FLIGHT) RETURN avg(f.occupancyPercentage)")) {
				Map<String, Object> row = res.next();
				mediaOcupación = (double) row.get(res.columns().get(0));
			}

			// Rellenar valores faltantes
			tx.execute(
				"MATCH (f:FLIGHT) " +
				"WHERE f.seatsCapacity IS NULL " +
				"SET f.seatsCapacity = " + mediaAsientos);
			tx.execute(
				"MATCH (f:FLIGHT) " +
				"WHERE f.occupancyPercentage IS NULL " +
				"SET f.occupancyPercentage = " + mediaOcupación);

			// Insertar número de pasajeros
			tx.execute(
				"MATCH (f:FLIGHT) " +
				"SET f.passengers = toInteger(round(f.seatsCapacity * f.occupancyPercentage / 100))");

			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_PASAJEROS, true);
		}
	}

	/**
	 * Añade el ratio de turistas por país (o región) y fecha (año y mes).
	 * Los datos se obtienen de un CSV importado con el nombre indicado. El formato esperado para el CSV es
	 * "countryID,provinceStateId,año,mes,porcentaje", donde:
	 * 		- countryID es el identificador del país, igual que está almacenado en la BD
	 * 		- provinceStateId es el identificador de la región, igual que está almacenado en la BD. Debe ir entre
	 * 		comillas dobles ya que contiene una coma. Si el dato es para el país completo, este campo debe estar vacío.
	 * 		- año es el año del dato
	 * 		- mes es el mes del dato
	 * 		- porcentaje es un valor entre 0 y 1 indicando el ratio de visitantes por motivos turísticos para este
	 * 		territorio y mes. El separador de decimales debe ser un punto.
	 * El CSV no debe contener una cabecera.
	 * Fija la propiedad {@link Propiedad#ETL_RATIO_TURISTAS} a true en la BD.
	 * @param rutaFichero Ruta al fichero CSV, relativa a la carpeta de import definida en la configuración de Neo4J.
	 */
	@Procedure(mode = Mode.WRITE)
	public void añadirRatioTuristas(@Name("rutaFichero") String rutaFichero) {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"LOAD CSV FROM 'file:///" + rutaFichero + "' AS line " +
				"CALL apoc.do.when(line[1] IS NULL, \"" +
					"MATCH (c:Country {countryId: line[0]}) " +
					"MERGE (c)-[:TURIST_RATIO]->" +
					"(tr:TuristRatio {year: toInteger(line[2]), month: toInteger(line[3])}) " +
					"SET tr.ratio = toFloat(line[4])" +
				"\", \"" +
					"MATCH (:Country {countryId: line[0]})<-[:BELONGS_TO]-(ps:ProvinceState {provinceStateId: line[1]}) " +
					"MERGE (ps)-[:TURIST_RATIO]->" +
					"(tr:TuristRatio {year: toInteger(line[2]), month: toInteger(line[3])}) " +
					"SET tr.ratio = toFloat(line[4])" +
				"\", {line:line}) YIELD value RETURN value");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_RATIO_TURISTAS, true);
		}
	}

	/**
	 * Añade el gasto medio por persona en materia de turismo por país de origen, país de destino y fecha (año y mes).
	 * Los datos se obtienen de un CSV importado con el nombre indicado. El formato esperado para el CSV es
	 * "countryID1,countryID2,año,mes,gasto", donde:
	 * 		- countryID1 es el identificador del país del que provienen los turistas, igual que está almacenado en la BD.
	 * 		Si está vacío, se considera que este es el dato por defecto para este mes para el país de destino (se usará
	 * 		para los vuelos que provengan de países que no tienen datos concretos).
	 * 		- countryID2 es el identificador del país al que van los turistas, igual que está almacenado en la BD
	 * 		- año es el año del dato
	 * 		- mes es el mes del dato
	 * 		- gasto es el gasto por persona para los turistas que viajan del primer país al segundo en el mes indicado.
	 * 		El separador de decimales debe ser un punto.
	 * El CSV no debe contener una cabecera.
	 * Fija la propiedad {@link Propiedad#ETL_GASTO_TURÍSTICO} a true en la BD.
	 * @param rutaFichero Ruta al fichero CSV, relativa a la carpeta de import definida en la configuración de Neo4J.
	 */
	@Procedure(mode = Mode.WRITE)
	public void añadirGastoTurístico(@Name("rutaFichero") String rutaFichero) {
		try (Transaction tx = db.beginTx()) {
			// Insertar un nodo para representar los datos genéricos para otros países que no están en el CSV
			tx.execute("MERGE (:DefaultCountry)");

			tx.execute(
				"LOAD CSV FROM 'file:///" + rutaFichero + "' AS line " +
				"CALL apoc.do.when(line[0] IS NULL, \"" +
					"MATCH (c1:DefaultCountry) " +
					"MATCH (c2:Country {countryId: line[1]}) " +
					"MERGE (c1)-[:TURIST_EXPENSE]->" +
					"(te:TuristExpense {year: toInteger(line[2]), month: toInteger(line[3])})" +
					"-[:TURIST_EXPENSE]->(c2) " +
					"SET te.expense = toInteger(line[4])" +
				"\", \"" +
					"MATCH (c1:Country {countryId: line[0]}) " +
					"MATCH (c2:Country {countryId: line[1]}) " +
					"MERGE (c1)-[:TURIST_EXPENSE]->" +
					"(te:TuristExpense {year: toInteger(line[2]), month: toInteger(line[3])})" +
					"-[:TURIST_EXPENSE]->(c2) " +
					"SET te.expense = toInteger(line[4])" +
				"\", {line:line}) YIELD value RETURN value");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_GASTO_TURÍSTICO, true);
		}
	}

	/**
	 * Añade una estimación del número de turistas a bordo de cada vuelo de llegada usando los datos de turismo.
	 * Requiere que se hayan ejecutado las operaciones ETL que añaden el ratio de turistas por región y el número
	 * de pasajeros por vuelo, así como la operación ETL que convierte las fechas de los vuelos a tipo date y la
	 * que añade las relaciones faltantes entre aeropuerto y país.
	 * Fija la propiedad {@link Propiedad#ETL_TURISTAS_VUELO} a true en la BD.
	 * @param mismaFecha Si es true, para cada vuelo se intentará buscar datos de turismo en su fecha de llegada.
	 *                   Útil si se está trabajando con vuelos pasados y se sabe que se dispone de datos de turismo
	 *                   para los mismos.
	 * @param aproximarFaltantes Si es true, los vuelos que no tengan datos asignados (porque no había datos en su
	 *                           fecha o porque mismaFecha es false) calcularán su número de turistas usando los datos
	 *                           de turismo del año más reciente de su mes de llegada. Útil si se trabaja con vuelos
	 *                           futuros y se sabe que no se dispone de datos de turismo para los mismos.
	 * @throws ETLOperationRequiredException Si no se ha ejecutado alguna de las siguientes operaciones ETL:
	 * {@link Modificar#convertirRelacionesAOD()}, {@link Añadir#calcularNúmeroPasajeros()},
	 * {@link Modificar#convertirFechasVuelos()} o {@link Añadir#añadirConexionesAeropuertoPaís()}.
	 * @throws IllegalArgumentException Si mismaFecha y aproximarFaltantes son ambos false.
	 */
	@Procedure(mode = Mode.WRITE)
	public void añadirTuristasVuelo(@Name("mismaFecha") Boolean mismaFecha,
									@Name("aproximarFaltantes") Boolean aproximarFaltantes) {
		if (!mismaFecha && !aproximarFaltantes) {
			throw new IllegalArgumentException("No tiene sentido llamar a añadirTuristasVuelo() si no se quiere ni " +
				"usar los datos de turismo presentes ni aproximar los futuros, ya que entonces el método no hace nada.");
		}

		Propiedades p = new Propiedades(db);
		if (p.getBool(Propiedad.ETL_RATIO_TURISTAS) && p.getBool(Propiedad.ETL_PASAJEROS) &&
				p.getBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS) && p.getBool(Propiedad.ETL_AEROPUERTO_PAÍS)) {
			try (Transaction tx = db.beginTx()) {
				/*
				 * Antes de nada, limpiar los datos de número de turistas que pudiera haber de antes, ya que este
				 * código necesita saber qué vuelos han recibido datos y qué vuelos no durante la ejecución
				 */
				tx.execute("MATCH (f:FLIGHT) SET f.turists = null");

				if (mismaFecha) {
					/*
					 * Primero fijamos el valor para los vuelos que llegan a un aeropuerto cuya región tiene datos
					 * de turismo en esas fechas
					 */
					tx.execute(
						"MATCH (f:FLIGHT) " +
						"CALL { " +
							"WITH f " +
							"MATCH (f)-[]->(:AirportOperationDay)<-[]-(:Airport)<-[]-(:ProvinceState)-[]->(tr:TuristRatio) " +
							"WHERE tr.year = f.dateOfArrival.year AND tr.month = f.dateOfArrival.month " +
							"RETURN tr.ratio as ratio " +
						"} " +
						"SET f.turists = f.passengers * ratio");

					/*
					 * Habrá vuelos que se habrán quedado sin asignar porque su región no tenía datos. Para esos vuelos,
					 * probamos a usar los datos a nivel nacional.
					 */
					tx.execute(
						"MATCH (f:FLIGHT) " +
						"WHERE f.turists IS NULL " +
						"CALL { " +
							"WITH f " +
							"MATCH (f)-[]->(:AirportOperationDay)<-[]-(:Airport)<-[]-(:Country)-[]->(tr:TuristRatio) " +
							"WHERE tr.year = f.dateOfArrival.year AND tr.month = f.dateOfArrival.month " +
							"RETURN tr.ratio as ratio " +
						"} " +
						"SET f.turists = f.passengers * ratio");
				}

				/*
				 * Ahora tenemos que ver qué hacemos con los vuelos que aún no tienen datos, que serán aquellos para
				 * los que no hay datos de turismo en su país para su fecha de destino (o todos si mismaFecha era
				 * false).
				 */
				if (aproximarFaltantes) {
					/*
					 * Para cada vuelo, tenemos que buscar los datos de turismo del mismo mes más recientes
					 * que tengamos.
					 */
					Consultas consultas = new Consultas(db);
					int primerAño = consultas.getPrimerAñoDatosTurismo();
					int últimoAño = consultas.getÚltimoAñoDatosTurismo();

					for (int añoActual = últimoAño; añoActual >= primerAño; añoActual--) {
						// Probar con los datos de la región primero
						tx.execute(
							"MATCH (f:FLIGHT) " +
							"WHERE f.turists IS NULL " +
							"CALL { " +
								"WITH f " +
								"MATCH (f)-[]->(:AirportOperationDay)<-[]-(:Airport)<-[]-(:ProvinceState)-[]->(tr:TuristRatio) " +
								"WHERE tr.year = " + añoActual + " AND tr.month = f.dateOfArrival.month " +
								"RETURN tr.ratio as ratio " +
							"} " +
							"SET f.turists = f.passengers * ratio");

						// Si no hay datos, usar los del país
						tx.execute(
							"MATCH (f:FLIGHT) " +
							"WHERE f.turists IS NULL " +
							"CALL { " +
								"WITH f " +
								"MATCH (f)-[]->(:AirportOperationDay)<-[]-(:Airport)<-[]-(:Country)-[]->(tr:TuristRatio) " +
								"WHERE tr.year = " + añoActual + " AND tr.month = f.dateOfArrival.month " +
								"RETURN tr.ratio as ratio " +
							"} " +
							"SET f.turists = f.passengers * ratio");
					}
				}

				/*
				 * Llegados a este punto, no hay nada más que hacer. Los vuelos que aún no tengan un valor de turistas
				 * no tienen datos disponibles que puedan usar.
				 * Fijamos su número de turistas a 0.
				 */
				tx.execute(
					"MATCH (f:FLIGHT) " +
					"WHERE f.turists IS NULL " +
					"SET f.turists = 0");

				tx.commit();
				new Propiedades(db).setBool(Propiedad.ETL_TURISTAS_VUELO, true);
			}
		} else {
			throw new ETLOperationRequiredException("Esta operación requiere que se haya ejecutado la operación ETL " +
				"que añade los datos de turismo, la operación ETL que calcula los pasajeros de cada vuelo, la " +
				"operación ETL que convierte las fechas de vuelos a tipo date y la operación ETL que añade conexiones " +
				"faltantes entre aeropuertos y países antes de ejecutarla.");
		}
	}

	/**
	 * Añade una estimación de los ingresos derivados del turismo que generan los pasajeros de cada vuelo de llegada
	 * usando los datos de gasto turístico.
	 * Requiere que se haya ejecutado la operación ETL que añade el número de turistas a cada vuelo, la operación
	 * ETL que añade los datos de gasto turístico a la BD y la operación ETL que añade las relaciones faltantes entre
	 * aeropuerto y país.
	 * Fija la propiedad {@link Propiedad#ETL_INGRESOS_VUELO} a true en la BD.
	 * @param mismaFecha Si es true, para cada vuelo se intentará buscar datos de gasto turístico en su fecha de llegada.
	 *                   Útil si se está trabajando con vuelos pasados y se sabe que se dispone de datos de gasto
	 *                   turístico para los mismos.
	 * @param aproximarFaltantes Si es true, los vuelos que no tengan datos asignados (porque no había datos en su
	 *                           fecha o porque mismaFecha es false) calcularán sus ingresos usando los datos de gasto
	 *                           turístico del año más reciente de su mes de llegada. Útil si se trabaja con vuelos
	 *                           futuros y se sabe que no se dispone de datos de gasto turístico para los mismos.
	 * @throws ETLOperationRequiredException Si no se ha ejecutado la operación ETL
	 * {@link Añadir#añadirTuristasVuelo}, la operación ETL {@link Añadir#añadirGastoTurístico(String)} o la operación
	 * ETL {@link Añadir#añadirConexionesAeropuertoPaís()}.
	 * @throws IllegalArgumentException Si mismaFecha y aproximarFaltantes son ambos false.
	 */
	@Procedure(mode = Mode.WRITE)
	public void añadirIngresosVuelo(@Name("mismaFecha") Boolean mismaFecha,
									@Name("aproximarFaltantes") Boolean aproximarFaltantes) {
		if (!mismaFecha && !aproximarFaltantes) {
			throw new IllegalArgumentException("No tiene sentido llamar a añadirIngresosVuelo() si no se quiere ni " +
				"usar los datos de gasto presentes ni aproximar los futuros, ya que entonces el método no hace nada.");
		}

		Propiedades p = new Propiedades(db);
		if (p.getBool(Propiedad.ETL_TURISTAS_VUELO) && p.getBool(Propiedad.ETL_GASTO_TURÍSTICO) &&
		p.getBool(Propiedad.ETL_AEROPUERTO_PAÍS)) {
			try (Transaction tx = db.beginTx()) {
				/*
				 * Antes de nada, limpiar los datos de ingresos que pudiera haber de antes, ya que este
				 * código necesita saber qué vuelos han recibido datos y qué vuelos no durante la ejecución
				 */
				tx.execute("MATCH (f:FLIGHT) SET f.incomeFromTurism = null");

				if (mismaFecha) {
					/*
					 * Primero buscamos vuelos para los que existan datos de gasto entre su país de origen y de destino
					 */
					tx.execute(
						"MATCH (c1:Country)-[]-(:Airport)-[]-(:AirportOperationDay)-[]->(f:FLIGHT)" +
						"-[]->(:AirportOperationDay)-[]-(:Airport)-[]-(c2:Country) " +
						"WHERE c1 <> c2 " +
						"CALL { " +
							"WITH f, c1, c2 " +
							"MATCH (c1)-[:TURIST_EXPENSE]->(te:TuristExpense)-[:TURIST_EXPENSE]->(c2) " +
							"WHERE te.year = f.dateOfArrival.year AND te.month = f.dateOfArrival.month " +
							"RETURN te.expense as expense " +
						"} " +
						"SET f.incomeFromTurism = f.turists * expense");

					/*
					 * Habrá vuelos que se habrán quedado sin asignar porque no hay datos entre su país de origen y de
					 * destino. Para esos vuelos, probamos a usar los datos genéricos del país de destino, si existen.
					 */
					tx.execute(
						"MATCH (c1:Country)-[]-(:Airport)-[]-(:AirportOperationDay)-[]->(f:FLIGHT)" +
						"-[]->(:AirportOperationDay)-[]-(:Airport)-[]-(c2:Country) " +
						"WHERE c1 <> c2 AND f.incomeFromTurism IS NULL " +
						"CALL { " +
							"WITH f, c2 " +
							"MATCH (:DefaultCountry)-[:TURIST_EXPENSE]->(te:TuristExpense)-[:TURIST_EXPENSE]->(c2) " +
							"WHERE te.year = f.dateOfArrival.year AND te.month = f.dateOfArrival.month " +
							"RETURN te.expense as expense " +
						"} " +
						"SET f.incomeFromTurism = f.turists * expense");
				}

				/*
				 * Ahora tenemos que ver qué hacemos con los vuelos que aún no tienen datos, que serán aquellos para
				 * los que no hay datos de gasto turístico para su país de destino en su fecha de destino (o todos si
				 * mismaFecha era false).
				 */
				if (aproximarFaltantes) {
					/*
					 * Para cada vuelo, tenemos que buscar los datos de gasto turístico del mismo mes más recientes
					 * que tengamos.
					 */
					Consultas consultas = new Consultas(db);
					int primerAño = consultas.getPrimerAñoDatosGastoTurístico();
					int últimoAño = consultas.getÚltimoAñoDatosGastoTurístico();

					for (int añoActual = últimoAño; añoActual >= primerAño; añoActual--) {
						// Probar primero con los datos entre el país de origen y el de destino
						tx.execute(
							"MATCH (c1:Country)-[]-(:Airport)-[]-(:AirportOperationDay)-[]->(f:FLIGHT)" +
							"-[]->(:AirportOperationDay)-[]-(:Airport)-[]-(c2:Country) " +
							"WHERE c1 <> c2 AND f.incomeFromTurism IS NULL " +
							"CALL { " +
								"WITH f, c1, c2 " +
								"MATCH (c1)-[:TURIST_EXPENSE]->(te:TuristExpense)-[:TURIST_EXPENSE]->(c2) " +
								"WHERE te.year = " + añoActual + " AND te.month = f.dateOfArrival.month " +
								"RETURN te.expense as expense " +
							"} " +
							"SET f.incomeFromTurism = f.turists * expense");

						// Si no hay datos, usar el país genérico como origen
						tx.execute(
							"MATCH (c1:Country)-[]-(:Airport)-[]-(:AirportOperationDay)-[]->(f:FLIGHT)" +
							"-[]->(:AirportOperationDay)-[]-(:Airport)-[]-(c2:Country) " +
							"WHERE c1 <> c2 AND f.incomeFromTurism IS NULL " +
							"CALL { " +
								"WITH f, c2 " +
								"MATCH (:DefaultCountry)-[:TURIST_EXPENSE]->(te:TuristExpense)-[:TURIST_EXPENSE]->(c2) " +
								"WHERE te.year = " + añoActual + " AND te.month = f.dateOfArrival.month " +
								"RETURN te.expense as expense " +
							"} " +
							"SET f.incomeFromTurism = f.turists * expense");
					}
				}

				/*
				 * Llegados a este punto, no hay nada más que hacer. Los vuelos que aún no tengan un valor de ingresos
				 * no tienen datos disponibles que puedan usar.
				 * Fijamos sus ingresos a 0.
				 */
				tx.execute(
					"MATCH (f:FLIGHT) " +
					"WHERE f.incomeFromTurism IS NULL " +
					"SET f.incomeFromTurism = 0");

				tx.commit();
				new Propiedades(db).setBool(Propiedad.ETL_INGRESOS_VUELO, true);
			}
		} else {
			throw new ETLOperationRequiredException("Esta operación requiere que se haya ejecutado la operación ETL " +
				"que añade el número de turistas a cada vuelo, la operación ETL que carga los datos de gasto " +
				"turístico y la operación ETL que añade conexiones faltantes entre aeropuertos y países antes " +
				"de ejecutarla.");
		}
	}
}
