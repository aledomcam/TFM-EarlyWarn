package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.modelo.datoid.Aeropuerto;
import earlywarn.main.modelo.datoid.Línea;
import earlywarn.main.modelo.datoid.RegistroDatoID;

/**
 * Representa el grado de conectividad de todos los aeropuertos, usando como valores máximo y mínimo la conectividad
 * total de la red cuando no se cierran líneas y la que queda cuando se cierran todas las líneas hacia el país
 * que se está considerando, respectivamente.
 */
public class Conectividad extends Criterio {
	private final int conectividadPaís;
	private double conectividadPerdidaActual;
	private final RegistroDatoID<Aeropuerto> aeropuertos;

	/**
	 * @param conectividadPaís Valor de conectividad que representan los vuelos hacia el país para el que se están
	 *                         cerrando líneas
	 * @param aeropuertos Registro de aeropuertos que permite acceder a los datos de los mismos
	 */
	public Conectividad(int conectividadPaís, RegistroDatoID<Aeropuerto> aeropuertos) {
		this.conectividadPaís = conectividadPaís;
		conectividadPerdidaActual = 0;
		this.aeropuertos = aeropuertos;
		id = IDCriterio.CONECTIVIDAD;
	}

	/**
	 * @return valor total de conectividad que representan los vuelos hacia el país para el que se están cerrando líneas
	 */
	public int getConectividadPaís() {
		return conectividadPaís;
	}

	/**
	 * @return Cantidad total de conectividad perdida dadas las líneas cerradas actualmente
	 */
	public double getConectividadPerdidaActual() {
		return conectividadPerdidaActual;
	}

	@Override
	public double getPorcentaje() {
		return 1 - conectividadPerdidaActual / conectividadPaís;
	}

	@Override
	public void recalcular(Línea línea, boolean abrir) {
		Aeropuerto aeropuertoOrigen = aeropuertos.get(línea.idAeropuertoOrigen);
		/*
		 * La conectividad que gana o pierde este aeropuerto se calcula con el % de vuelos de salida ganados o perdidos
		 * en el mismo al abrir o cerrar esta línea
		 */
		double ratioVuelos = (float) línea.getNumVuelos() / aeropuertoOrigen.getNumVuelosSalida();
		double variaciónConectividad = aeropuertoOrigen.getConectividadBase() * ratioVuelos;
		if (abrir) {
			conectividadPerdidaActual -= variaciónConectividad;
		} else {
			conectividadPerdidaActual += variaciónConectividad;
		}
	}
}
