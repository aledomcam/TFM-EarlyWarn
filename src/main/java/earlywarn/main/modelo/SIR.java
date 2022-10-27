package earlywarn.main.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase representativa de los compartimentos que forman el método SIR.
 */

public class SIR {

    private static double susceptibles;
    private static double infectados;
    private static double recuperados;

    public SIR(){
        susceptibles = 0;
        infectados = 0;
        recuperados = 0;
    }

    public SIR(double s, double i, double r){
        susceptibles = s;
        infectados = i;
        recuperados = r;
    }

    public List<Double> getListaSIR(){
        List<Double> ret = new ArrayList<>();
        ret.add(susceptibles);
        ret.add(infectados);
        ret.add(recuperados);

        return ret;
    }

    public double getSusceptibles() {
        return susceptibles;
    }

    public double getInfectados() {
        return infectados;
    }

    public double getRecuperados() {
        return recuperados;
    }
}
