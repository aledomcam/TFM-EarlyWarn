package earlywarn.main.modelo.datoid;

import org.neo4j.graphdb.GraphDatabaseService;

import java.time.LocalDate;

/**
 * Factoría que instancia un aeropuerto dado su ID
 */
public class AeropuertoFactory implements IDatoIDFactory<Aeropuerto> {
	private final GraphDatabaseService db;
	private final LocalDate díaInicio;
	private final LocalDate díaFin;

	public AeropuertoFactory(LocalDate díaInicio, LocalDate díaFin, GraphDatabaseService db) {
		this.db = db;
		this.díaInicio = díaInicio;
		this.díaFin = díaFin;
	}

	@Override
	public Aeropuerto crear(String id) {
		return new Aeropuerto(id, díaInicio, díaFin, db);
	}
}
