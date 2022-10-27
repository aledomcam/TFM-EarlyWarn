package earlywarn.main.modelo;

import java.util.TreeMap;

public class SIRAeropuerto {

    private static double riesgoTotalAeropuerto;
    private static TreeMap<String,Double> riesgoVuelos;

    public SIRAeropuerto(){
        riesgoTotalAeropuerto = 0;
        riesgoVuelos = new TreeMap<>();
    }

    public TreeMap<String,Double> getRiesgoTotalAeropuerto(){
        TreeMap<String,Double> ret = new TreeMap<>(riesgoVuelos);
        ret.put("RIESGO TOTAL AEROPUERTO", riesgoTotalAeropuerto);

        return ret;
    }

    public void añadirRiesgoVuelo(String idVuelo, Double riesgo) {
        riesgoVuelos.put(idVuelo, riesgo);
    }

    public void setRiesgoTotal(double riesgoTotal) {
        riesgoTotalAeropuerto = riesgoTotal;
    }
}
