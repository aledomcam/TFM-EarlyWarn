package earlywarn.funciones;

import java.util.List;

import earlywarn.definiciones.SentidoVuelo;
import earlywarn.main.Consultas;
import earlywarn.definiciones.Propiedad;
import earlywarn.main.modelo.datoid.Línea;
import earlywarn.main.Propiedades;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * Contiene las funciones y procedimientos ejecutables desde Neo4J
 */
public class Funciones {
	@Context
	public GraphDatabaseService db;

	// -- Consultas --

	@UserFunction
	@Description("Devuelve el nº de vuelos que salen del aeropuerto indicado en el rango de días indicado")
	public Long vuelosSalida(@Name("idAeropuerto") String idAeropuerto, @Name("fechaInicio") LocalDate fechaInicio,
							 @Name("fechaFin") LocalDate fechaFin) {
		Consultas consultas = new Consultas(db);
		return consultas.getVuelosAeropuerto(idAeropuerto, fechaInicio, fechaFin, SentidoVuelo.SALIDA);
	}

	@UserFunction
	@Description("Devuelve el año más antiguo del que hay datos de turismo")
	public Long últimoAñoDatosTurismo() {
		Consultas consultas = new Consultas(db);
		return (long) consultas.getÚltimoAñoDatosTurismo();
	}

	@UserFunction
	@Description("Devuelve el número de pasajeros totales entre todos los vuelos de llegada al país indicado en el " +
		"rango de fechas indicado. Si el país se deja en blanco, se tienen en cuenta todos los vuelos.")
	public Long getPasajerosTotales(@Name("idPaís") String idPaís, @Name("fechaInicio") LocalDate fechaInicio,
									@Name("fechaFin") LocalDate fechaFin) {
		Consultas consultas = new Consultas(db);
		return (long) consultas.getPasajerosTotales(fechaInicio, fechaFin, idPaís);
	}

	@UserFunction
	@Description("Devuelve los ingresos turísticos totales entre todos los vuelos de llegada al país indicado en el " +
		"rango de fechas indicado. Si el país se deja en blanco, se tienen en cuenta todos los vuelos.")
	public Double getIngresosTurísticosTotales(@Name("idPaís") String idPaís, @Name("fechaInicio") LocalDate fechaInicio,
											   @Name("fechaFin") LocalDate fechaFin) {
		Consultas consultas = new Consultas(db);
		return consultas.getIngresosTurísticosTotales(fechaInicio, fechaFin, idPaís);
	}

	@UserFunction
	@Description("Devuelve el valor de conectividad total entre todos los aeropuertos")
	public Long getConectividadTotal() {
		Consultas consultas = new Consultas(db);
		return (long) consultas.getConectividadTotal();
	}

	@UserFunction
	@Description("Devuelve la parte de la conectividad total que proviene de los vuelos hacia el país indicado en el " +
		"rango de fechas indicado")
	public Long getConectividadPaís(@Name("idPaís") String idPaís, @Name("fechaInicio") LocalDate fechaInicio,
									@Name("fechaFin") LocalDate fechaFin) {
		Consultas consultas = new Consultas(db);
		return (long) consultas.getConectividadPaís(fechaInicio, fechaFin, idPaís);
	}

	@UserFunction
	@Description("Obtiene el número de pasajeros que viajan con cada aerolínea entre todos los vuelos de llegada " +
		"al país indicado en el rango de fechas indicado. Si el país se deja en blanco, se tienen en cuenta " +
		"todos los vuelos.")
	public Map<String, Long> getPasajerosPorAerolínea(@Name("idPaís") String idPaís,
														 @Name("fechaInicio") LocalDate fechaInicio,
														 @Name("fechaFin") LocalDate fechaFin) {
		Consultas consultas = new Consultas(db);
		return consultas.getPasajerosPorAerolínea(fechaInicio, fechaFin, idPaís);
	}

	@UserFunction
	@Description("Obtiene el número de pasajeros que viajan desde y hacia cada aeropuerto entre todos los vuelos de " +
		"llegada al país indicado en el rango de fechas indicado. Solo se incluyen aeropuertos del país indicado. " +
		"Si el país se deja en blanco, se tienen en cuenta todos los vuelos y aeropuertos.")
	public Map<String, Long> getPasajerosPorAeropuerto(@Name("idPaís") String idPaís,
														 @Name("fechaInicio") LocalDate fechaInicio,
														 @Name("fechaFin") LocalDate fechaFin) {
		Consultas consultas = new Consultas(db);
		return consultas.getPasajerosPorAeropuerto(fechaInicio, fechaFin, idPaís);
	}

	// -- Líneas --

	@UserFunction
	@Description("Obtiene todas las líneas existentes en el periodo indicado que terminan en un aeropuerto del país " +
		"indicado. Si el país se deja en blanco, se obtienen todas las líneas.")
	public List<String> getLíneas(@Name("idPaís") String idPaís,
								  @Name("fechaInicio") LocalDate fechaInicio,
								  @Name("fechaFin") LocalDate fechaFin) {
		Consultas consultas = new Consultas(db);
		return consultas.getLíneas(fechaInicio, fechaFin, idPaís);
	}

	@UserFunction
	@Description("Devuelve los pasajeros que circulan por la línea indicada en el periodo indicado")
	public Long getPasajerosLínea(@Name("idLínea") String idLínea, @Name("díaInicio") LocalDate díaInicio,
								  @Name("díaFin") LocalDate díaFin) {
		Línea línea = new Línea(idLínea, díaInicio, díaFin, db);
		return línea.getPasajeros();
	}

	@UserFunction
	@Description("Devuelve los ingresos turísticos totales de la línea indicada en el periodo indicado")
	public Double getIngresosTurísticosLínea(@Name("idLínea") String idLínea, @Name("díaInicio") LocalDate díaInicio,
										   @Name("díaFin") LocalDate díaFin) {
		Línea línea = new Línea(idLínea, díaInicio, díaFin, db);
		return línea.getIngresosTurísticos();
	}

	@UserFunction
	@Description("Devuelve el número de vuelos que circulan por la línea indicada en el periodo indicado")
	public Long getNumVuelosLínea(@Name("idLínea") String idLínea, @Name("díaInicio") LocalDate díaInicio,
								  @Name("díaFin") LocalDate díaFin) {
		Línea línea = new Línea(idLínea, díaInicio, díaFin, db);
		return línea.getNumVuelos();
	}

	@UserFunction
	@Description("Devuelve el riesgo importado total de la línea indicada en el periodo indicado")
	public Double getRiesgoImportadoLínea(@Name("idLínea") String idLínea, @Name("díaInicio") LocalDate díaInicio,
										@Name("díaFin") LocalDate díaFin) {
		Línea línea = new Línea(idLínea, díaInicio, díaFin, db);
		return línea.getRiesgoImportado();
	}

	@UserFunction
	@Description("Devuelve los pasajeros por aerolínea que circulan por la línea indicada en el periodo indicado")
	public Map<String, Long> getPasajerosPorAerolíneaLínea(@Name("idLínea") String idLínea,
														   @Name("díaInicio") LocalDate díaInicio,
														   @Name("díaFin") LocalDate díaFin) {
		Línea línea = new Línea(idLínea, díaInicio, díaFin, db);
		return línea.getPasajerosPorAerolínea();
	}

	// -- Propiedades --

	@UserFunction
	@Description("Prueba para Propiedades.inicializadas()")
	public Boolean propInit() {
		return new Propiedades(db).inicializadas();
	}

	@UserFunction
	@Description("Prueba para Propiedades.getBool()")
	public Boolean propGetBool(@Name("nombreProp") String nombreProp) {
		return new Propiedades(db).getBool(Propiedad.valueOf(nombreProp));
	}

	@Procedure(mode = Mode.WRITE)
	@Description("Prueba para Propiedades.setBool()")
	public void propSetBool(@Name("nombreProp") String nombreProp, @Name("valor") boolean valor) {
		new Propiedades(db).setBool(Propiedad.valueOf(nombreProp), valor);
	}
}
