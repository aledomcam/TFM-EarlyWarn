package earlywarn.definiciones;

public class Globales {

    public static double defaultAlpha = (1.0/(9*96));
    public static double defaultBeta = (0.253/96);

    public static void updateAlpha(double alpha) {
        defaultAlpha = alpha;
    }

    public static void updateBeta(double beta) {
        defaultBeta = beta;
    }
}
