package earlywarn.main;

import earlywarn.definiciones.ICálculoFitness;
import earlywarn.definiciones.IDCriterio;
import earlywarn.definiciones.IllegalOperationException;
import earlywarn.definiciones.OperaciónLínea;
import earlywarn.main.modelo.datoid.Línea;
import earlywarn.main.modelo.criterio.Criterio;
import earlywarn.main.modelo.datoid.RegistroDatoID;
import earlywarn.mh.vnsrs.ConversorLíneas;
import org.neo4j.logging.Log;

import java.util.*;

/**
 * Permite abrir y cerrar líneas durante la ejecución del programa. Lleva un registro con los valores de todos los
 * criterios que dependen de las líneas abiertas y permite consultar su valor porcentual en cualquier momento.
 */
public class GestorLíneas {
	private final ConversorLíneas conversorLíneas;
	// Lista de criterios almacenados, cada uno identificado por un valor de un enum
	protected final Map<IDCriterio, Criterio> criterios;
	private final Log log;
	private final Random random;
	private final RegistroDatoID<Línea> registroLíneas;

	// Mapa que mapea el ID de cada línea con su estado actual (true = abierta, false = cerrada)
	private final Map<String, Boolean> líneas;
	// Array de booleanos que también almacena el estado de las líneas. Permite copiar una solución de forma rápida.
	private final boolean[] líneasBool;
	// Número de líneas actualmente abiertas
	private int numAbiertas;

	// Clase usada para calcular el fitness final. Puede ser null.
	private ICálculoFitness cálculoFitness;

	/**
	 * Crea una instancia del gestor. El método está protegido ya que se debe usar {@link GestorLíneasBuilder} para
	 * instanciar esta clase.
	 * @param líneas Lista con los IDs de todas las líneas. Debe haber sido creada con el mismo día de inicio y fin
	 *               que los especificados a continuación.
	 * @param conversorLíneas Conversor de líneas que permita obtener el ID numérico de una línea
	 * @param registroLíneas Registro que permite acceder a los datos de las líneas
	 * @param log Log de Neo4J
	 */
	protected GestorLíneas(List<String> líneas, RegistroDatoID<Línea> registroLíneas, ConversorLíneas conversorLíneas,
						   Log log) {
		this.log = log;
		this.conversorLíneas = conversorLíneas;
		random = new Random();
		this.líneas = new TreeMap<>();
		líneasBool = new boolean[líneas.size()];
		criterios = new EnumMap<>(IDCriterio.class);
		for (String idLínea : líneas) {
			this.líneas.put(idLínea, true);
		}
		for (int i = 0; i < líneas.size(); i++) {
			líneasBool[i] = true;
		}
		numAbiertas = this.líneas.size();
		this.registroLíneas = registroLíneas;
	}

	/**
	 * Añade un nuevo critero al gestor. Usado por {@link GestorLíneasBuilder} para añadir criterios al crear la
	 * instancia. No se pueden añadir más criterios una vez creada.
	 * @param criterio Criterio a añadir
	 */
	protected void _añadirCriterio(Criterio criterio) {
		criterios.put(criterio.id, criterio);
	}

	/**
	 * Añade un método de cálculo de fitness al gestor, lo que le permite agrupar los valores de cada criterio
	 * en uno solo. Usado por {@link GestorLíneasBuilder}.
	 * @param cálculoFitness Clase usada para calcular el fitness
	 */
	protected void _añadirCálculoFitness(ICálculoFitness cálculoFitness) {
		this.cálculoFitness = cálculoFitness;
	}

	/**
	 * Abre o cierra las líneas identificadas por los IDs incluidos en la lista indicada. Si alguna de las líneas
	 * indicadas ya estaba en el estado objetivo, se ignorará.
	 * @param líneas Lista con los IDs de las lineas que se quieren abrir o cerrar.
	 * @param operación Operación a realizar (apertura o cierre)
	 */
	public void abrirCerrarLíneas(List<String> líneas, OperaciónLínea operación) {
		for (String idLínea : líneas) {
			cambiarEstadoLínea(idLínea, operación);
		}
	}

	/**
	 * Varia el estado de cada una de las líneas en el gestor con una probabilidad del 50%
	 */
	public void variarAlAzar() {
		for (Map.Entry<String, Boolean> entrada : líneas.entrySet()) {
			if (random.nextBoolean()) {
				OperaciónLínea operación = entrada.getValue() ? OperaciónLínea.CERRAR : OperaciónLínea.ABRIR;
				cambiarEstadoLínea(entrada.getKey(), operación);
			}
		}
	}

	public List<String> getLíneas() {
		return new ArrayList<>(líneas.keySet());
	}

	/**
	 * @return Número de líneas totales
	 */
	public int getNumLíneas() {
		return líneas.size();
	}

	/**
	 * @return Número de líneas actualmente abiertas
	 */
	public int getNumAbiertas() {
		return numAbiertas;
	}

	/**
	 * @return Número de líneas actualmente cerradas
	 */
	public int getNumCerradas() {
		return líneas.size() - numAbiertas;
	}

	/**
	 * @return Array de booleanos que representa el estado de todas las líneas según su ID numérico
	 */
	public boolean[] getLíneasBool() {
		return líneasBool.clone();
	}

	/**
	 * Devuelve los identificadores de las líneas que ocupen cada una de las posiciones indicadas dentro del conjunto
	 * de todas las líneas que se encuentren en el estado indicado.
	 * Por ejemplo, si el estado especificado es "abierta" y las posiciones son 1, 3 y 12, se devolverán la primera
	 * línea abierta, la tercera y la decimosegunda.
	 * @param posiciones Lista con las posiciones de las líneas a devolver
	 * @param getAbiertas Si es true, solo se considerarán las líneas abiertas. Si es false, las cerradas.
	 * @return Lista con las líneas que ocupan las posiciones indicadas de entre todas las que tienen el estado
	 * indicado.
	 */
	public List<String> getPorPosiciónYEstado(List<Integer> posiciones, boolean getAbiertas) {
		List<String> ret = new ArrayList<>();

		List<Integer> posicionesOrdenadas = new ArrayList<>(posiciones);
		posicionesOrdenadas.sort(null);

		Iterator<Map.Entry<String, Boolean>> itLíneas = líneas.entrySet().iterator();
		// Lleva la cuenta de cuántas líneas hemos encontrado hasta ahora que estén en el estado que buscamos
		int procesadas = 0;
		for (Integer posActual : posicionesOrdenadas) {
			boolean encontrada = false;
			while (!encontrada) {
				Map.Entry<String, Boolean> entradaActual = itLíneas.next();
				if (entradaActual.getValue() == getAbiertas) {
					// Esta es la línea nº <procesadas> que está en el estado que buscamos
					if (procesadas == posActual) {
						ret.add(entradaActual.getKey());
						encontrada = true;
					}
					procesadas++;
				}
			}
		}
		return ret;
	}

	/**
	 * Obtiene el valor porcentual del criterio indicado. Un valor de 1 indica que el criterio tiene el mejor valor
	 * posible, un valor de 0 indica que el criterio tiene el peor valor posible.
	 * @param id ID del criterio a consultar
	 * @return Valor porcentual (entre 0 y 1) del criterio indicado
	 * @throws IllegalArgumentException Si el criterio indicado no se ha añadido a este gestor al instanciarlo
	 */
	public double getPorcentajeCriterio(IDCriterio id) {
		Criterio criterio = criterios.get(id);
		if (criterio != null) {
			return criterio.getPorcentaje();
		} else {
			throw new IllegalArgumentException("El criterio especificado no está incluido en este gestor");
		}
	}

	/**
	 * Devuelve el valor porcentual de todos los criterios registrados en el gestor.
	 * @return Mapa con el valor porcentual de todos los criterios
	 */
	public Map<IDCriterio, Double> getPorcentajeCriterios() {
		Map<IDCriterio, Double> ret = new EnumMap<>(IDCriterio.class);
		for (Map.Entry<IDCriterio, Criterio> entrada : criterios.entrySet()) {
			ret.put(entrada.getKey(), entrada.getValue().getPorcentaje());
		}
		return ret;
	}

	/**
	 * Obtiene el valor de fitness actual dados los valores de todos los criterios. Requiere que se haya especificado
	 * un método de cálculo de fitness al instanciar esta clase
	 * @return Valor de fitness (entre 0 y 1) que representa la calidad de la solución actual
	 * @throws IllegalOperationException Si no se ha especificado un método de cálculo de fitness al crear esta
	 * instancia
	 */
	public double getFitness() {
		if (cálculoFitness != null) {
			return cálculoFitness.calcularFitness(criterios.values());
		} else {
			throw new IllegalOperationException("No se puede calcular el fitness de la solución si no se ha " +
				"especificado un método de cálculo");
		}
	}

	/**
	 * @return Lista de criterios usados en el gestor
	 */
	public List<Criterio> getCriterios() {
		return new ArrayList<>(criterios.values());
	}

	/**
	 * Modifica el estado de una de las líneas almacenadas en el gestor, salvo que la línea ya esté en el estado
	 * deseado.
	 * @param idLínea ID de la línea a modificar
	 * @param operación Operación a realizar sobre la línea
	 */
	private void cambiarEstadoLínea(String idLínea, OperaciónLínea operación) {
		boolean abrir = operación == OperaciónLínea.ABRIR;
		Boolean abierta = líneas.get(idLínea);
		if (abierta != null) {
			if (abierta != abrir) {
				líneas.put(idLínea, !abierta);
				líneasBool[conversorLíneas.getIDNumérico(idLínea)] = abrir;
				// Recalcular los valores de todos los criteros
				for (Criterio criterio : criterios.values()) {
					criterio.recalcular(registroLíneas.get(idLínea), abrir);
				}

				if (operación == OperaciónLínea.ABRIR) {
					numAbiertas++;
				} else {
					numAbiertas--;
				}
			}
		} else {
			log.warn("No se puede variar el estado de la línea " + idLínea + " porque no está en " +
				"la lista de líneas");
		}
	}
}
