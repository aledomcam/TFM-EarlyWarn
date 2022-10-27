package earlywarn.main.modelo.datoid;

import org.neo4j.graphdb.GraphDatabaseService;

import java.time.LocalDate;

/**
 * Factoría que instancia una línea de vuelo dada su ID
 */
public class LíneaFactory implements IDatoIDFactory<Línea> {
	private final GraphDatabaseService db;
	private final LocalDate díaInicio;
	private final LocalDate díaFin;

	public LíneaFactory(LocalDate díaInicio, LocalDate díaFin, GraphDatabaseService db) {
		this.db = db;
		this.díaInicio = díaInicio;
		this.díaFin = díaFin;
	}

	@Override
	public Línea crear(String id) {
		return new Línea(id, díaInicio, díaFin, db);
	}
}
