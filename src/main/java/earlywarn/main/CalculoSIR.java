package earlywarn.main;

import earlywarn.main.modelo.SIR;
import earlywarn.main.modelo.SIRVuelo;

public class CalculoSIR {

    public static SIRVuelo calcularRiesgoVuelo(SIR initialSIR, double durationInSeconds, double seatsCapacity,
                                               double occupancyPercentage, double alpha, double beta) {
        SIRVuelo ret;
        double sFinal = initialSIR.getSusceptibles();
        double iFinal = initialSIR.getInfectados();
        double rFinal = initialSIR.getRecuperados();
        double flightOccupancy = seatsCapacity * (occupancyPercentage / 100);

        for (int i = 0; i < ((durationInSeconds / 60) / 15); i++) {
            double sAux = sFinal;
            double iAux = iFinal;
            double rAux = rFinal;
            sFinal = sAux - beta * sAux * iAux / flightOccupancy;
            iFinal = iAux + beta * sAux * iAux / flightOccupancy - alpha * iAux;
            rFinal = rAux + alpha * iAux;
        }
        // Añadir datos de cálculo
        ret = new SIRVuelo(initialSIR.getSusceptibles(), initialSIR.getInfectados(), initialSIR.getRecuperados(),
            sFinal, iFinal, rFinal, alpha, beta);

        return ret;
    }

    public static SIR calcularSirInicialVuelo(double occupancyPercentage, long seatsCapacity, long population,
                                              long confirmed, long recovered) {
        double flightOccupancy = seatsCapacity * (occupancyPercentage / 100);
        double susceptible = population - (confirmed - recovered);
        double s0 = flightOccupancy * susceptible / population;
        double i0 = flightOccupancy * confirmed / population;
        double r0 = flightOccupancy * recovered / population;
        return new SIR(s0, i0, r0);
    }
}
