package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.Utils;
import earlywarn.main.modelo.datoid.Aeropuerto;
import earlywarn.main.modelo.datoid.Línea;
import earlywarn.main.modelo.datoid.RegistroDatoID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Representa el grado de homogeneidad entre el porcentaje de pasajeros que vuelan hacia o desde los diferentes
 * aeropuertos. El mejor valor para este criterio se obtiene cuando todos los aeropuertos tienen el mismo porcentaje
 * de pasajeros restantes.
 */
public class HomogeneidadAeropuertos extends Criterio {
	protected final Map<String, Long> pasajerosPorAeropuertoInicial;
	protected final Map<String, Long> pasajerosPorAeropuertoActual;
	private final String idPaís;
	private final RegistroDatoID<Aeropuerto> aeropuertos;

	public HomogeneidadAeropuertos(Map<String, Long> pasajerosPorAeropuertoInicial, String idPaís,
								   RegistroDatoID<Aeropuerto> aeropuertos) {
		this.pasajerosPorAeropuertoInicial = pasajerosPorAeropuertoInicial;
		pasajerosPorAeropuertoActual = new TreeMap<>(pasajerosPorAeropuertoInicial);
		this.idPaís = idPaís;
		this.aeropuertos = aeropuertos;
		id = IDCriterio.HOMOGENEIDAD_AEROPUERTOS;
	}

	public Map<String, Long> getPasajerosPorAeropuertoInicial() {
		return pasajerosPorAeropuertoInicial;
	}

	public Map<String, Long> getPasajerosPorAeropuertoActual() {
		return pasajerosPorAeropuertoActual;
	}

	@Override
	public double getPorcentaje() {
		return getPorcentajeFinal(getPorcentajes());
	}

	@Override
	public void recalcular(Línea línea, boolean abrir) {
		variarPasajerosAeropuerto(línea.idAeropuertoOrigen, línea.id, línea.getPasajeros(), abrir);
		variarPasajerosAeropuerto(línea.idAeropuertoDestino, línea.id, línea.getPasajeros(), abrir);
	}

	/**
	 * Si el aeropuerto indicado pertenece al país especificado al crear la clase, varía el número de pasajeros
	 * actuales por la cantidad indicada
	 * @param idAeropuerto Aeropuerto a comprobar y para el que se variará el número actual de pasajeros
	 * @param idLínea ID de la línea en la que está este aeropuerto
	 * @param cantidad Cantidad de pasajeros a incrementar
	 * @param incrementar True para incrementar el número de pasajeros, false para decrementarlos
	 */
	private void variarPasajerosAeropuerto(String idAeropuerto, String idLínea, long cantidad, boolean incrementar) {
		if (aeropuertos.get(idAeropuerto).getIdPaís().equals(idPaís)) {
			Long valorActual = pasajerosPorAeropuertoActual.get(idAeropuerto);
			if (valorActual != null) {
				if (incrementar) {
					valorActual += cantidad;
				} else {
					valorActual -= cantidad;
				}
				pasajerosPorAeropuertoActual.put(idAeropuerto, valorActual);
			} else {
				System.out.println("WARN: El aeropuerto \"" + idAeropuerto + "\", contenido en la línea \"" +
					idLínea + "\", no está en la lista global de pasajeros por aeropuerto y será ignorado");
			}
		}
	}

	/**
	 * @return Porcentaje de vuelos perdidos por el aeropuerto que más vuelos ha perdido, o null si no hay aeropuertos
	 * registrados.
	 */
	public Double getPérdidaMáxima() {
		Double menor = null;
		for (Double porcentaje : getPorcentajes()) {
			if (menor == null || porcentaje < menor) {
				menor = porcentaje;
			}
		}
		if (menor == null) {
			return menor;
		} else {
			return 1 - menor;
		}
	}

	/**
	 * @return Lista con el porcentaje de vuelos restantes para cada aeropuerto
	 */
	private List<Double> getPorcentajes() {
		List<Double> porcentajes = new ArrayList<>();
		for (Map.Entry<String, Long> entrada : pasajerosPorAeropuertoInicial.entrySet()) {
			String aeropuerto = entrada.getKey();
			Long valorInicial = entrada.getValue();
			Long valorActual = pasajerosPorAeropuertoActual.get(aeropuerto);

			if (valorActual != null) {
				porcentajes.add((double) valorActual / valorInicial);
			} else {
				throw new IllegalStateException("El número de pasajeros en el aeropuerto \"" + aeropuerto +
					"\" no está en el mapa de pasajeros por aeropuerto actual");
			}
		}
		return porcentajes;
	}

	/**
	 * Calcula el valor porcentual del criterio una vez que está calculada la lista con el porcentaje de pasajeros
	 * restantes por cada aeropuerto
	 * @param porcentajes Lista que contiene el porcentaje de pasajeros restantes para cada aeropuerto
	 * @return Valor porcentual del criterio
	 */
	protected double getPorcentajeFinal(List<Double> porcentajes) {
		/*
		 * Calculamos la desviación típica de estos porcentajes y obtenemos su ratio con respecto a la
		 * desviación máxima posible
		 */
		return 1 - Utils.getStd(porcentajes) / Utils.getStdMáxima(pasajerosPorAeropuertoInicial.size());
	}
}
