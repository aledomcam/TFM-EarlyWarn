package earlywarn.main.modelo;

import java.util.TreeMap;

public class SIRVuelo {

    public double sInicial;
    public double iInicial;
    public double rInicial;
    public double sFinal;
    public double iFinal;
    public double rFinal;
    public double alpha;
    public double beta;

    public SIRVuelo(){
        sInicial = 0;
        iInicial = 0;
        rInicial = 0;
        sFinal = 0;
        iFinal = 0;
        rFinal = 0;
        alpha = 0;
        beta = 0;
    }

    public SIRVuelo(double s0, double i0, double r0, double sFin, double iFin, double rFin, double alphaVal, double betaVal){
        sInicial = s0;
        iInicial = i0;
        rInicial = r0;
        sFinal = sFin;
        iFinal = iFin;
        rFinal = rFin;
        alpha = alphaVal;
        beta = betaVal;
    }

    public TreeMap<String, Double> getValoresSIRVuelo(){
        TreeMap<String,Double> ret = new TreeMap<>();
        ret.put("S_Inicial", sInicial);
        ret.put("I_Inicial", iInicial);
        ret.put("R_Inicial", rInicial);
        ret.put("S_Final", sFinal);
        ret.put("I_Final", iFinal);
        ret.put("R_Final", rFinal);
        ret.put("Alpha_Recuperacion", alpha);
        ret.put("Beta_Transmision", beta);

        return ret;
    }

    public double getInfectadosFinales() {
        return iFinal;
    }
}
