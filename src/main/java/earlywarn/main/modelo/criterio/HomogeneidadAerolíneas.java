package earlywarn.main.modelo.criterio;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.Utils;
import earlywarn.main.modelo.datoid.Línea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Representa el grado de homogeneidad entre el porcentaje de pasajeros que vuelan con las diferentes aerolíneas.
 * El mejor valor para este criterio se obtiene cuando todas las aerolíneas tienen el mismo porcentaje de pasajeros
 * restantes.
 */
public class HomogeneidadAerolíneas extends Criterio {
	protected final Map<String, Long> pasajerosPorAerolíneaInicial;
	protected final Map<String, Long> pasajerosPorAerolíneaActual;

	public HomogeneidadAerolíneas(Map<String, Long> pasajerosPorAerolíneaInicial) {
		this.pasajerosPorAerolíneaInicial = pasajerosPorAerolíneaInicial;
		pasajerosPorAerolíneaActual = new TreeMap<>(pasajerosPorAerolíneaInicial);
		id = IDCriterio.HOMOGENEIDAD_AEROLÍNEAS;
	}

	public Map<String, Long> getPasajerosPorAerolíneaInicial() {
		return pasajerosPorAerolíneaInicial;
	}

	public Map<String, Long> getPasajerosPorAerolíneaActual() {
		return pasajerosPorAerolíneaActual;
	}

	@Override
	public double getPorcentaje() {
		return getPorcentajeFinal(getPorcentajes());
	}

	@Override
	public void recalcular(Línea línea, boolean abrir) {
		Map<String, Long> variaciónPasajerosPorAerolínea = línea.getPasajerosPorAerolínea();
		for (Map.Entry<String, Long> variación : variaciónPasajerosPorAerolínea.entrySet()) {
			String aerolínea = variación.getKey();
			Long variaciónPasajeros = variación.getValue();

			Long valorActual = pasajerosPorAerolíneaActual.get(aerolínea);
			if (valorActual != null) {
				if (abrir) {
					valorActual += variaciónPasajeros;
				} else {
					valorActual -= variaciónPasajeros;
				}
				pasajerosPorAerolíneaActual.put(aerolínea, valorActual);
			} else {
				System.out.println("WARN: La aerolínea \"" + aerolínea + "\", contenida en la línea \"" +
					línea.id + "\", no está en la lista global de pasajeros por aerolínea y será ignorada");
			}
		}
	}

	/**
	 * @return Porcentaje de vuelos perdidos por la aerolínea que más vuelos ha perdido, o null si no hay aerolíneas
	 * registradas.
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
	 * @return Lista con el porcentaje de vuelos restantes para cada aerolínea
	 */
	private List<Double> getPorcentajes() {
		List<Double> porcentajes = new ArrayList<>();
		for (Map.Entry<String, Long> entrada : pasajerosPorAerolíneaInicial.entrySet()) {
			String aerolínea = entrada.getKey();
			Long valorInicial = entrada.getValue();
			Long valorActual = pasajerosPorAerolíneaActual.get(aerolínea);

			if (valorActual != null) {
				porcentajes.add((double) valorActual / valorInicial);
			} else {
				throw new IllegalStateException("El número de pasajeros en la aerolínea \"" + aerolínea +
					"\" no está en el mapa de pasajeros por aerolínea actual");
			}
		}
		return porcentajes;
	}

	/**
	 * Calcula el valor porcentual del criterio una vez que está calculada la lista con el porcentaje de pasajeros
	 * restantes por cada aerolínea
	 * @param porcentajes Lista que contiene el porcentaje de pasajeros restantes para cada aerolínea
	 * @return Valor porcentual del criterio
	 */
	protected double getPorcentajeFinal(List<Double> porcentajes) {
		/*
		 * Calculamos la desviación típica de estos porcentajes y obtenemos su ratio con respecto a la
		 * desviación máxima posible
		 */
		return 1 - Utils.getStd(porcentajes) / Utils.getStdMáxima(pasajerosPorAerolíneaInicial.size());
	}
}
