package earlywarn.mh.vnsrs.entornos;

import earlywarn.definiciones.OperaciónLínea;

/**
 * Almacena un caso usado para el cambio de entorno horizontal. El caso indica si se abrieron o cerraron líneas y
 * si se logró una mejora en la función objetivo como resultado.
 */
public class CasoEntornoX {
	public final int numLíneasAbiertas;
	public final boolean abrir;
	public final boolean mejora;

	public CasoEntornoX(int numLíneasAbiertas, boolean abrir, boolean mejora) {
		this.numLíneasAbiertas = numLíneasAbiertas;
		this.abrir = abrir;
		this.mejora = mejora;
	}

	/**
	 * Devuelve la oepración que este caso recomienda en base a su experiencia. Si hubo una mejora en la función
	 * objetivo, será la acción realizada. Si no, será la contraria.
	 * @return Operación recomendada para este caso
	 */
	public OperaciónLínea getOperación() {
		if (abrir ^ mejora) {
			return OperaciónLínea.CERRAR;
		} else {
			return OperaciónLínea.ABRIR;
		}
	}
}
