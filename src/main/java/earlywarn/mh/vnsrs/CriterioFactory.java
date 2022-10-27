package earlywarn.mh.vnsrs;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.Consultas;
import earlywarn.main.modelo.datoid.Aeropuerto;
import earlywarn.main.modelo.criterio.*;
import earlywarn.main.modelo.datoid.RegistroDatoID;
import earlywarn.mh.vnsrs.config.Config;

/**
 * Clase que permite crear instancias de criterios mediante la configuración del algoritmo VNS-RS
 */
public class CriterioFactory {
	private final Consultas consultas;
	private final Config config;
	private final RegistroDatoID<Aeropuerto> registroAeropuertos;

	/**
	 * Instancia la factoría
	 * @param consultas Clase usada para hacer consultas a la BD
	 * @param config Configuración para la metaheurística de VNS-RS
	 * @param registroAeropuertos Registro con los datos de aeropuertos
	 */
	public CriterioFactory(Consultas consultas, Config config, RegistroDatoID<Aeropuerto> registroAeropuertos) {
		this.consultas = consultas;
		this.config = config;
		this.registroAeropuertos = registroAeropuertos;
	}

	/**
	 * Crea un nuevo criterio del tipo especificado
	 * @param id Tipo de criterio a crear
	 * @return Instancia del nuevo criterio del tipo especificado
	 */
	public Criterio criterio(IDCriterio id) {
		switch (id) {
			case RIESGO_IMPORTADO:
				return new RiesgoImportado(consultas.getRiesgoPorPaís(config.díaInicio, config.díaFin, config.país));
			case NÚMERO_PASAJEROS:
				return new NumPasajeros(consultas.getPasajerosTotales(config.díaInicio, config.díaFin, config.país));
			case INGRESOS_TURÍSTICOS:
				return new IngresosTurísticos(
					consultas.getIngresosTurísticosTotales(config.díaInicio, config.díaFin, config.país));
			case HOMOGENEIDAD_AEROLÍNEAS:
				return new HomogeneidadAerolíneas(
					consultas.getPasajerosPorAerolínea(config.díaInicio, config.díaFin, config.país));
			case HOMOGENEIDAD_AEROLÍNEAS_LINEAL:
				return new HomogeneidadAerolíneasLineal(
					consultas.getPasajerosPorAerolínea(config.díaInicio, config.díaFin, config.país));
			case HOMOGENEIDAD_AEROPUERTOS:
				return new HomogeneidadAeropuertos(
					consultas.getPasajerosPorAeropuerto(config.díaInicio, config.díaFin, config.país), config.país,
					registroAeropuertos);
			case HOMOGENEIDAD_AEROPUERTOS_LINEAL:
				return new HomogeneidadAeropuertosLineal(
					consultas.getPasajerosPorAeropuerto(config.díaInicio, config.díaFin, config.país), config.país,
					registroAeropuertos);
			case CONECTIVIDAD:
				return new Conectividad(
					consultas.getConectividadPaís(config.díaInicio, config.díaFin, config.país), registroAeropuertos);
			default:
				throw new IllegalStateException("El ID de criterio " + id + " no se ha asociado con " +
					"ninguna subclase de Criterio");
		}
	}
}
