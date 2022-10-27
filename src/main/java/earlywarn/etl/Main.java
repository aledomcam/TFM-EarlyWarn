package earlywarn.etl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

/**
 * Clase que contiene el método principal con el que se inician las operaciones ETL sobre la BD.
 */
public class Main {
	@Context
	public GraphDatabaseService db;

	/**
	 * Ejecuta todas las operaciones ETL.
	 * @param rutaFicheroConectividad Ruta al fichero CSV que contiene los datos de conectividad de aeropuertos,
	 *                                relativa a la carpeta de import definida en la configuración de Neo4J.
	 * @param rutaFicheroTurismo Ruta al fichero CSV que contiene los datos del ratio de turistas de las diferentes
	 *                           regiones, relativa a la carpeta de import definida en la configuración de Neo4J.
	 * @param mismaFechaTurismo True si al rellenar los datos de turistas en vuelos y gasto turístico de pasajeros
	 *                          deberían buscarse datos de turismo/gasto en la misma fecha. Para más detalles,
	 *                          ver {@link Añadir#añadirTuristasVuelo}.
	 *                          Si este parámetro es false, aproximarFaltantesTuristas debe ser true.
	 * @param aproximarFaltantesTurismo True si al rellenar los datos de turistas en vuelos y gasto turístico de
	 *                                  pasajeros deberían aproximarse los datos de turismo/gasto cuando no estén
	 *                                  disponibles. Para más detalles, ver {@link Añadir#añadirTuristasVuelo}.
	 *                                  Si este parámetro es false, mismaFechaTuristas debe ser true.
	 */
	@Procedure(mode = Mode.WRITE)
	public void mainETL(@Name("rutaFicheroConectividad") String rutaFicheroConectividad,
						@Name("rutaFicheroTurismo") String rutaFicheroTurismo,
						@Name("rutaFicheroGasto") String rutaFicheroGasto,
						@Name("mismaFechaTurismo") Boolean mismaFechaTurismo,
						@Name("aproximarFaltantesTurismo") Boolean aproximarFaltantesTurismo) {
		Modificar modificar = new Modificar(db);
		modificar.convertirRelacionesAOD();
		modificar.borrarVuelosSinSIR();
		modificar.borrarAeropuertosSinIATA();
		modificar.convertirFechasVuelos();

		Añadir añadir = new Añadir(db);
		añadir.añadirConexionesAeropuertoPaís();
		añadir.añadirConectividad(rutaFicheroConectividad);
		añadir.calcularNúmeroPasajeros();
		añadir.añadirRatioTuristas(rutaFicheroTurismo);
		añadir.añadirGastoTurístico(rutaFicheroGasto);
		añadir.añadirTuristasVuelo(mismaFechaTurismo, aproximarFaltantesTurismo);
		añadir.añadirIngresosVuelo(mismaFechaTurismo, aproximarFaltantesTurismo);

		modificar.convertirFechasReportes();

		Índices índices = new Índices(db);
		índices.indexarIataAeropuertos();
	}
}
