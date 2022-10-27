package earlywarn.etl;

import earlywarn.definiciones.Propiedad;
import earlywarn.main.Propiedades;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

/**
 * En esta clase se encuentran los procedimientos encargados de modificar datos en la BD antes de empezar a trabajar
 * con la misma. Estas modificaciones puden cambiar el formato de los datos o la estructura de la BD.
 */
public class Modificar {
	@Context
	public GraphDatabaseService db;

	/**
	 * Requerido por Neo4J
	 * @deprecated Este constructor no debe utilizarse. Usar {@link Modificar#Modificar(GraphDatabaseService)} en su lugar.
	 */
	@Deprecated
	public Modificar() {

	}
	public Modificar(GraphDatabaseService db) {
		this.db = db;
	}

	/**
	 * Modifica el formato de las relaciones entre aeropuertos y días de operación. En lugar de almacenar la fecha
	 * usando el tipo de la relación, pasa dicha fecha a un campo llamado "date" en la relación y cambia el tipo
	 * de todas estas relaciones a "OPERATES_ON".
	 * Fija la propiedad {@link Propiedad#ETL_RELACIONES_AOD} a true en la BD.
	 */
	@Procedure(mode = Mode.WRITE)
	public void convertirRelacionesAOD() {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"MATCH (a:Airport)-[r]->(aod:AirportOperationDay) " +
				"WHERE type(r) <> \"OPERATES_ON\" " +
				"CREATE (a)-[:OPERATES_ON {date: date(type(r))}]->(aod)");
			tx.execute(
				"MATCH (a:Airport)-[r]->(aod:AirportOperationDay) " +
				"WHERE type(r) <> \"OPERATES_ON\" " +
				"DELETE r");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_RELACIONES_AOD, true);
		}
	}

	/**
	 * Borra de la base de datos todos los vuelos que no tengan calculado su valor final de SIR.
	 * Fija la propiedad {@link Propiedad#ETL_BORRAR_VUELOS_SIN_SIR} a true en la BD.
	 */
	@Procedure(mode = Mode.WRITE)
	public void borrarVuelosSinSIR() {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"MATCH (f:FLIGHT) " +
				"WHERE f.flightIfinal IS NULL " +
				"DETACH DELETE f");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_BORRAR_VUELOS_SIN_SIR, true);
		}
	}

	/**
	 * Borra de la base de datos todos los aeropuertos que no tengan código IATA, así como los vuelos asociados
	 * a los mismos.
	 * Fija la propiedad {@link Propiedad#ETL_BORRAR_AEROPUERTOS_SIN_IATA} a true en la BD.
	 */
	@Procedure(mode = Mode.WRITE)
	public void borrarAeropuertosSinIATA() {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"MATCH (a:Airport) WHERE a.iata = \"\" OPTIONAL MATCH (a)-[]-(aod:AirportOperationDay) " +
				"OPTIONAL MATCH (aod)-[]-(f:FLIGHT) DETACH DELETE a,aod,f");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_BORRAR_AEROPUERTOS_SIN_IATA, true);
		}
	}

	/**
	 * Convierte las fechas de llegada y salida de los vuelos a tipo date. También convierte los instantes de
	 * llegada y salida a tipo datetime.
	 * Fija la propiedad {@link Propiedad#ETL_CONVERTIR_FECHAS_VUELOS} a true en la BD.
	 */
	@Procedure(mode = Mode.WRITE)
	public void convertirFechasVuelos() {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"MATCH (f:FLIGHT) " +
				"SET f.dateOfDeparture = date(f.dateOfDeparture) " +
				"SET f.dateOfArrival = date(f.dateOfArrival)" +
				"SET f.instantOfDeparture = datetime(f.instantOfDeparture)" +
				"SET f.instantOfArrival = datetime(f.instantOfArrival)");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_CONVERTIR_FECHAS_VUELOS, true);
		}
	}

	/**
	 * Convierte la fecha de publicación de los reportes de casos a tipo date.
	 * Fija la propiedad {@link Propiedad#ETL_CONVERTIR_FECHAS_REPORTES} a true en la BD.
	 */
	@Procedure(mode = Mode.WRITE)
	public void convertirFechasReportes() {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"MATCH (r:Report) " +
				"SET r.releaseDate = date(r.releaseDate)");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_CONVERTIR_FECHAS_REPORTES, true);
		}
	}
}
