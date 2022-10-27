package earlywarn.etl;

import earlywarn.definiciones.Propiedad;
import earlywarn.main.Propiedades;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

/**
 * Almacena las modificaiones que se realizan en la BD antes de empezar a trabajar con ella relacionadas con la
 * creación de índices.
 * Estas operaciones ETL no son estrictamente necesarias para el funcionamiento del programa, pero el rendimiento
 * del mismo sin ellas será mucho peor.
 */
public class Índices {
	@Context
	public GraphDatabaseService db;

	/**
	 * Requerido por Neo4J
	 * @deprecated Este constructor no debe utilizarse. Usar {@link Índices#Índices(GraphDatabaseService)} en su lugar.
	 */
	@Deprecated
	public Índices() {

	}
	// Requerido para poder llamar a procedimientos desde otra clase
	public Índices(GraphDatabaseService db) {
		this.db = db;
	}

	/**
	 * Crea un índice sobre el campo Airport.iata para facilitar las consultas que trabajan sobre los aeropuertos
	 * de una cierta línea.
	 * Fija la propiedad {@link Propiedad#ETL_INDEXAR_IATA_AEROPUERTOS} a true en la BD.
	 */
	@Procedure(mode = Mode.SCHEMA)
	public void indexarIataAeropuertos() {
		try (Transaction tx = db.beginTx()) {
			tx.execute("CREATE INDEX airportIata IF NOT EXISTS FOR (n:Airport) ON (n.iata)");
			tx.commit();
			new Propiedades(db).setBool(Propiedad.ETL_INDEXAR_IATA_AEROPUERTOS, true);
		}
	}
}
