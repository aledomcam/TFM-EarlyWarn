package earlywarn.mh.vnsrs.config;

import earlywarn.definiciones.IDCriterio;
import earlywarn.main.Utils;
import earlywarn.mh.vnsrs.restricción.Restricción;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Clase que representa los datos de configuración para la metaheurística
 */
@SuppressWarnings("ProhibitedExceptionThrown")
public class Config {
	public ConfigVNS configVNS;
	public ConfigRS configRS;
	/*
	 * Parar la ejecución cuando transcurra este número de iteraciones sin una mejora significativa en
	 * la función objetivo
	 */
	public int itParada;
	// Porcentaje que debe haber mejorado el fitness para que una mejora se considere significativa
	public float porcentMejora;
	// País sobre el que se está trabajando
	public String país;
	// Rango de fechas sobre el que se está trabajando
	public LocalDate díaInicio;
	public LocalDate díaFin;
	// Lista de criterios leídos del XML
	public List<IDCriterio> criterios;
	// Pesos de los diferentes criterios
	public Map<IDCriterio, Float> pesos;
	// Restricciones a las soluciones factibles
	public ListaRestricciones restricciones;
	// True para permitir considerar soluciones infactibles
	public boolean permitirInfactibles;

	/**
	 * Instancia la configuración
	 * @param rutaFicheroConfig Ruta al fichero XML que contiene la configuración, relativa a la carpeta del DBMS
	 */
	public Config(String rutaFicheroConfig) {
		Document xml;
		try {
			xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rutaFicheroConfig);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Fichero de configuración vns-rs no encontrado", e);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException("Error al abrir el fichero de configuración vns-rs", e);
		}

		configVNS = new ConfigVNS();
		configRS = new ConfigRS();

		Element raíz = xml.getDocumentElement();

		Element elemItCambioEntorno = Utils.toLista(raíz.getElementsByTagName("itCambioEntorno")).get(0);
		configVNS.itCambioEntorno = Integer.parseInt(elemItCambioEntorno.getTextContent());

		Element elemItCambioEntornoXComplejo = Utils.toLista(raíz.getElementsByTagName("cambioEntornoXComplejo")).get(0);
		configVNS.cambioEntornoXComplejo = Boolean.parseBoolean(elemItCambioEntornoXComplejo.getTextContent());
		Element elemItCambioEntornoYComplejo = Utils.toLista(raíz.getElementsByTagName("cambioEntornoYComplejo")).get(0);
		configVNS.cambioEntornoYComplejo = Boolean.parseBoolean(elemItCambioEntornoYComplejo.getTextContent());

		Element elemEntornoXComplejo = Utils.toLista(raíz.getElementsByTagName("entornoXComplejo")).get(0);
		Element elemTamañoMemoriaX = Utils.toLista(elemEntornoXComplejo.getElementsByTagName("tamañoMemoriaX")).get(0);
		configVNS.tamañoMemoriaX = Float.parseFloat(elemTamañoMemoriaX.getTextContent());
		Element elemDistanciaMemoriaX = Utils.toLista(elemEntornoXComplejo.getElementsByTagName("distanciaMemoriaX")).get(0);
		configVNS.distanciaMemoriaX = Float.parseFloat(elemDistanciaMemoriaX.getTextContent());

		Element elemEntornoYComplejo = Utils.toLista(raíz.getElementsByTagName("entornoYComplejo")).get(0);
		Element elemMaxPorcentLíneas = Utils.toLista(elemEntornoYComplejo.getElementsByTagName("maxPorcentLíneas")).get(0);
		configVNS.maxPorcentLíneas = Float.parseFloat(elemMaxPorcentLíneas.getTextContent());
		Element elemNumComprobaciones = Utils.toLista(elemEntornoYComplejo.getElementsByTagName("numComprobaciones")).get(0);
		configVNS.numComprobaciones = Integer.parseInt(elemNumComprobaciones.getTextContent());
		Element elemPorcentLíneas = Utils.toLista(elemEntornoYComplejo.getElementsByTagName("porcentLíneas")).get(0);
		configVNS.porcentLíneas = Float.parseFloat(elemPorcentLíneas.getTextContent());
		Element elemIteraciones = Utils.toLista(elemEntornoYComplejo.getElementsByTagName("iteraciones")).get(0);
		configVNS.iteraciones = Integer.parseInt(elemIteraciones.getTextContent());
		Element elemLíneasPorIt = Utils.toLista(elemEntornoYComplejo.getElementsByTagName("líneasPorIt")).get(0);
		configVNS.líneasPorIt = Float.parseFloat(elemLíneasPorIt.getTextContent());
		Element elemVariaciónMax = Utils.toLista(elemEntornoYComplejo.getElementsByTagName("variaciónMax")).get(0);
		configVNS.variaciónMax = Float.parseFloat(elemVariaciónMax.getTextContent());

		Element elemTInicial = Utils.toLista(raíz.getElementsByTagName("tInicial")).get(0);
		configRS.tInicial = Float.parseFloat(elemTInicial.getTextContent());
		Element elemAlfa = Utils.toLista(raíz.getElementsByTagName("alfa")).get(0);
		configRS.alfa = Float.parseFloat(elemAlfa.getTextContent());
		Element elemItReducciónT = Utils.toLista(raíz.getElementsByTagName("itReducciónT")).get(0);
		configRS.itReducciónT = Integer.parseInt(elemItReducciónT.getTextContent());

		Element elemPaís = Utils.toLista(raíz.getElementsByTagName("país")).get(0);
		país = elemPaís.getTextContent();
		Element elemDíaInicio = Utils.toLista(raíz.getElementsByTagName("primerDía")).get(0);
		díaInicio = Utils.stringADate(elemDíaInicio.getTextContent());
		Element elemDíaFin = Utils.toLista(raíz.getElementsByTagName("últimoDía")).get(0);
		díaFin = Utils.stringADate(elemDíaFin.getTextContent());
		Element elemItParada = Utils.toLista(raíz.getElementsByTagName("itParada")).get(0);
		itParada = Integer.parseInt(elemItParada.getTextContent());
		Element elemPorcentMejora = Utils.toLista(raíz.getElementsByTagName("porcentMejora")).get(0);
		porcentMejora = Float.parseFloat(elemPorcentMejora.getTextContent());

		criterios = new ArrayList<>();
		pesos = new EnumMap<>(IDCriterio.class);
		Element elemCriterios = Utils.toLista(raíz.getElementsByTagName("criterios")).get(0);
		for (Element elemCriterio : Utils.toLista(elemCriterios.getElementsByTagName("criterio"))) {
			String textoIDCriterio = elemCriterio.getAttribute("id");
			String textoPesoCriterio = elemCriterio.getAttribute("peso");
			IDCriterio idCriterio;
			try {
				idCriterio = IDCriterio.valueOf(textoIDCriterio);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("El ID de criterio \"" + textoIDCriterio + "\", especificado en " +
					"la configuración del programa, no corresponde con ningún criterio.", e);
			}
			criterios.add(idCriterio);
			if (!textoPesoCriterio.isEmpty()) {
				pesos.put(idCriterio, Float.parseFloat(textoPesoCriterio));
			}
		}

		restricciones = new ListaRestricciones();
		Element elemRestricciones = Utils.toLista(raíz.getElementsByTagName("restricciones")).get(0);
		for (Element elemRestricción : Utils.toLista(elemRestricciones.getElementsByTagName("restricción"))) {
			ListaParámetros parámetros = new ListaParámetros();
			String idRestricción = elemRestricción.getAttribute("id");
			for (Element elemParámetro : Utils.toLista(elemRestricción.getElementsByTagName("param"))) {
				parámetros.añadir(elemParámetro.getAttribute("id"), elemParámetro.getTextContent());
			}
			restricciones.añadir(Restricción.crear(idRestricción, parámetros));
		}

		Element elemInfactibles = Utils.toLista(raíz.getElementsByTagName("permitirInfactibles")).get(0);
		permitirInfactibles = Boolean.parseBoolean(elemInfactibles.getTextContent());
	}
}
