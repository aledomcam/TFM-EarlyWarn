package earlywarn.mh.vnsrs.entornos;

import earlywarn.definiciones.OperaciónLínea;
import earlywarn.mh.vnsrs.config.ConfigVNS;

import java.util.List;
import java.util.Random;

/**
 * Determina el entorno horizontal a usar a continuación (la operación a realizar) usando un razonamiento basado en
 * casos que además tiene en cuenta la temperatura actual.
 */
public class CalcEntornoXMemoria implements ICalcEntornoX {
	// Constante por la que se multiplica el porcentaje de temperatura restante para el cálculo del entorno horizontal
	private static final float MULT_PORCENT_TEMPERATURA_ENTORNO_X = 3.0f;

	private final Random random;
	private final ConfigVNS config;
	// Temperatura inicial del RS
	private final double temperaturaInicial;
	// Número total de líneas
	private final int numLíneas;

	private final MemoriaCasosX casosX;

	public CalcEntornoXMemoria(ConfigVNS configVNS, int numLíneas, double temperaturaInicial) {
		random = new Random();
		config = configVNS;
		this.temperaturaInicial = temperaturaInicial;
		this.numLíneas = numLíneas;
		casosX = new MemoriaCasosX(Math.round(config.tamañoMemoriaX * numLíneas), numLíneas);
	}

	@Override
	public void registrarNuevaSolución(int numLíneasAbiertas, OperaciónLínea operaciónRealizada, double nuevoFitness,
									   double fitnessActual) {
		// Creamos un nuevo caso con esta información y lo añadimos a la memoria de casos
		CasoEntornoX nuevoCaso = new CasoEntornoX(numLíneasAbiertas, operaciónRealizada == OperaciónLínea.ABRIR,
			nuevoFitness > fitnessActual);
		casosX.añadir(nuevoCaso, nuevoCaso.numLíneasAbiertas);
	}

	/**
	 * Determina cuál será la opreración de variación de líneas (apertura o cierre) que se ejecutará en el siguiente
	 * entorno. Para ello emplea una memoria de casos que recuerdan si abrir/cerrar líneas fue positivo o no en
	 * situaciones que tenían un número de líneas abiertas similar al actual.
	 * @param numLíneasAbiertas Número de líneas actualmente abiertas
	 * @param temperaturaActual Temperatura actual del recocido simulado tras finalizar la iteración actual
	 * @return Operación horizontal (cierre o apertura de líneas) que se debería realizar en el próximo entorno
	 */
	@Override
	public OperaciónLínea entornoX(int numLíneasAbiertas, double temperaturaActual) {
		// Seleccionar los casos que votarán cuál debe ser la siguiente operación
		int numMinLíneas = Math.round(numLíneasAbiertas - numLíneas * config.distanciaMemoriaX);
		int numMaxLíneas = Math.round(numLíneasAbiertas + numLíneas * config.distanciaMemoriaX);
		if (numMaxLíneas < 0) {
			numMinLíneas = 0;
		}
		if (numMaxLíneas > numLíneas) {
			numMaxLíneas = numLíneas;
		}
		List<CasoEntornoX> casos = casosX.getCasosEnRango(numMinLíneas, numMaxLíneas);

		/*
		 * Recorremos los casos. Cada caso tiene un peso que depende de la diferencia entre su número de líneas
		 * abiertas y el número de líneas actualmente abiertas. Los casos que tengan la mayor diferencia tendrán un
		 * peso de 1 solo voto. Por cada unidad más cerca del número de líneas actual que esté el caso, éste gana 1
		 * voto más.
		 */
		int diferenciaMax = Math.max(numLíneasAbiertas - numMinLíneas, numMaxLíneas - numLíneasAbiertas);
		int votosAbrir = 0;
		int votosCerrar = 0;
		for (CasoEntornoX caso : casos) {
			int diferencia = Math.abs(caso.numLíneasAbiertas - numLíneasAbiertas);
			int peso = diferenciaMax - diferencia + 1;
			if (caso.getOperación() == OperaciónLínea.ABRIR) {
				votosAbrir += peso;
			} else {
				votosCerrar += peso;
			}
		}
		// El ratio de votos determina la probabilidad de abrir líneas (en este cálculo basado en casos)
		double probabilidadAbrirCasos = (double) votosAbrir / (votosAbrir + votosCerrar);

		/*
		 * En función del porcentaje de temperatura restante, se va transicionando de una elección completamente
		 * aleatoria a una basada en la probabilidad antes calculada.
		 */
		double coeficienteTemperatura =
			Math.min(1, temperaturaActual / temperaturaInicial * MULT_PORCENT_TEMPERATURA_ENTORNO_X);
		double probabilidadFinal = 0.5 * coeficienteTemperatura + probabilidadAbrirCasos * (1 - coeficienteTemperatura);

		if (random.nextDouble() < probabilidadFinal) {
			return OperaciónLínea.ABRIR;
		} else {
			return OperaciónLínea.CERRAR;
		}
	}
}
