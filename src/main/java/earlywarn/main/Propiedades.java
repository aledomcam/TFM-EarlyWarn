package earlywarn.main;

import earlywarn.definiciones.Propiedad;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

/**
 * Clase usada para interactuar con el nodo de propiedades de la BD.
 * Este nodo almacena una serie de valores que se pueden consultar para saber en qué estado están los datos de la misma.
 * Por ejemplo, se pueden almacenar propiedades para indicar qué operaciones ETL se han completado ya.
 */
public class Propiedades {
	/*
	 * La instancia de la base de datos.
	 * Debe ser obtenida usando la anotación @Context en un procedimiento o función
	 */
	private final GraphDatabaseService db;

	public Propiedades(GraphDatabaseService db) {
		this.db = db;
	}

	/**
	 * @return True si las propiedades de la BD han sido inicializadas (si se ha insertado al menos una).
	 */
	public boolean inicializadas() {
		try (Transaction tx = db.beginTx()) {
			try (Result res = tx.execute(
				"MATCH (p:Properties) RETURN p")) {
				return res.hasNext();
			}
		}
	}

	/**
	 * Devuelve el valor de una propiedad booleana.
	 * @param propiedad Propiedad a leer de la BD
	 * @return Valor de la propiedad
	 */
	public boolean getBool(Propiedad propiedad) {
		if (!inicializadas()) {
			return false;
		} else {
			try (Transaction tx = db.beginTx()) {
				try (Result res = tx.execute("MATCH (p:Properties) RETURN p." + propiedad.name())) {
					Map<String, Object> row = res.next();

					Object valor = row.get(res.columns().get(0));
					if (valor == null) {
						return false;
					} else {
						return (boolean) valor;
					}
				}
			}
		}
	}

	/**
	 * Fija el valor de una propiedad booleana. Si existe, se sobrescribirá el valor anterior. Si no, se añadirá
	 * una nueva propiedad con el valor indicado.
	 * @param propiedad Propiedad a fijar
	 * @param valor Nuevo valor de la propiedad
	 */
	public void setBool(Propiedad propiedad, boolean valor) {
		try (Transaction tx = db.beginTx()) {
			tx.execute(
				"MERGE (p:Properties) " +
				"SET p." + propiedad.name() + " = " + valor);
			tx.commit();
		}
	}
}
