package earlywarn.signals;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.neo4j.graphdb.GraphDatabaseService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Specialization fo the EWarningDNM class for the generation of early warning signals and markers to early detect
 * outbreaks.
 * Notes: It assumes that every country has the same number of reports and that there is no gap between the first date
 * with covid reports and the last one. Also, it assumes tha all countries have the same date for the first report,
 * and hence all countries have the same date for its last report. (All things has been proved)
 */
public class EWarningLDNM extends EWarningDNM{
    /* Class properties */
    private List<List<Double>> landscapeDNMs = new ArrayList<>();

    /**
     * Secondary constructor for the Class that receive fewer parameters than the main one.
     * @param db Neo4j database instance annotated with the @Context from the main procedure function.
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size. If there aren't enough dates for a non window size configuration.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningLDNM(GraphDatabaseService db) throws RuntimeException {
        this(db, EWarningGeneral.startDateDefault, EWarningGeneral.endDateDefault, EWarningGeneral.countriesDefault,
             windowSizeDefault, EWarningGeneral.correlationDefault, cumulativeDataDefault);
    }

    /**
     * Secondary constructor for the Class that receive fewer parameters than the main one.
     * @param db Neo4j database instance annotated with the @Context from the main procedure function.
     * @param startDate First date of the range of days of interest.
     * @param endDate Last date of the range of days of interest.
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size. If there aren't enough dates for a non window size configuration.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningLDNM(GraphDatabaseService db, LocalDate startDate, LocalDate endDate) throws RuntimeException {
        this(db, startDate, endDate, EWarningGeneral.countriesDefault, windowSizeDefault,
             EWarningGeneral.correlationDefault, cumulativeDataDefault);
    }

    /**
     * Secondary constructor for the Class that receive fewer parameters than the main one.
     * @param db Neo4j database instance annotated with the @Context from the main procedure function.
     * @param startDate First date of the range of days of interest.
     * @param windowSize Size of the window to shift between startDate and endDate.
     * @param endDate Last date of the range of days of interest.
     * @param cumulativeData Boolean that determines whether to use cumulative confirmed covid cases (True) over
     * the time or new daily cases of confirmed covid cases (True).
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size. If there aren't enough dates for a non window size configuration.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningLDNM(GraphDatabaseService db, LocalDate startDate, LocalDate endDate, int windowSize,
                        boolean cumulativeData)
            throws RuntimeException {
        this(db, startDate, endDate, EWarningGeneral.countriesDefault, windowSize, EWarningGeneral.correlationDefault,
             cumulativeData);
    }

    /**
     * Main constructor for the Class that receive all possible parameters.
     * @param db Neo4j database instance annotated with the @Context from the main procedure function.
     * @param startDate First date of the range of days of interest.
     * @param endDate Last date of the range of days of interest.
     * @param countries List of countries to take into account in the ISO-3166-Alpha2 format (2 letters by country).
     * @param windowSize Size of the window to shift between startDate and endDate.
     * @param correlation Type of correlation to use for each window between each pair of countries.
     * List of possible correlation values:
     *      - "pearson": Pearson Correlation
     *      - "spearman": Spearman Correlation
     *      - "kendall":Kendall Correlation
     *      - any other value: Pearson Correlation
     * @param cumulativeData Boolean that determines whether to use cumulative confirmed covid cases (True) over
     * the time or new daily cases of confirmed covid cases (True).
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size. If there aren't enough dates for a non window size configuration.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningLDNM(GraphDatabaseService db, LocalDate startDate, LocalDate endDate, List<String> countries,
                        int windowSize, String correlation, boolean cumulativeData) throws RuntimeException {
        super(db, startDate, endDate, countries, windowSize, correlation, cumulativeData);
    }

    /**
     * Transform the original data of cumulative confirmed covid cases to its desired form. In this specialized case,
     * depending on the class property cumulativeData, it will leave the covid confirmed cases as cumulative data
     * (False) or will make the discrete difference to contain the daily new confirmed cases of covid (True).
     * Also, it inicializate the lists inside the list of the early warning marker of the Class.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return long[][] Matrix with the transformed data. Each Row represents a country, and each Column contains
     * the cases from the first date of study until the end date.
     * @author Angel Fragua
     */
    @Override
    protected double[][] transformData(LocalDate startDateWindow) {
        double[][] transformedData = super.transformData(startDateWindow);
        for (int i = 0; i < this.countries.size(); i++) {
            this.landscapeDNMs.add(new ArrayList<>());
        }
        return transformedData;
    }

    /**
     * Transform the data of the confirmed covid cases of the two fixed windows with one date of difference between them
     * to the graph matrix of the network, where the edges represent the coefficient correlation between
     * its pair of nodes, and the nodes represent each country.
     * @param windowT0 Data of the confirmed covid cases in a fixed period of time, where the Rows represent
     * each country and the columns represent each date from the latest to the new ones.
     * @param windowT1 Data of the confirmed covid cases in a fixed period of time, where the Rows represent
     * each country and the columns represent each date from the latest to the new ones.
     * @param adjacencyT0 Adjacency matrix as a 2d int array of the windowT0. In this case is not needed, so it will
     * be ignored.
     * @param adjacencyT1 Adjacency matrix as a 2d int array of the windowT1. In this case is not needed, so it will
     * be ignored.
     * @return double[][] The network's matrix created with the data of the two fixed time windows.
     * @author Angel Fragua
     */
    protected double[][] windowToNetwork(double[][] windowT0, double[][] windowT1, int[][] adjacencyT0,
                                         int[][]adjacencyT1) {
        int numCountries = this.countries.size();
        double[][] network = new double[numCountries][numCountries];
        StandardDeviation sdFunction = new StandardDeviation();

        double[] xT0, xT1, yT0, yT1;
        double cc;
        double ccIn = 0;
        double ccOut = 0;
        double sd = 0;
        Set<Integer> nodesIn = new TreeSet<>();
        for (int i = 0; i < numCountries; i++, nodesIn.clear(), sd = 0, ccIn = 0, ccOut = 0) {
            nodesIn.add(i);
            for (int j = 0; j < numCountries; j++) {
                if (adjacencyT1[i][j] == 1) {
                    nodesIn.add(j);
                }
            }
            /* Average Differential Standard Deviation of nodes in local network */
            for (int node: nodesIn) {
                sd += Math.abs(sdFunction.evaluate(windowT1[node]) - sdFunction.evaluate(windowT0[node]));
            }
            sd /= nodesIn.size();

            for (int j = 0; j < numCountries; j++) {
                xT0 = windowT0[j];
                xT1 = windowT1[j];
                for (int k = 0; k < numCountries; k++) {
                    yT0 = windowT0[k];
                    yT1 = windowT1[k];
                    /* Average Differential Correlation Coefficient within local network */
                    if (nodesIn.contains(j) && nodesIn.contains(k)) {
                        ccIn += Math.abs(super.calculateCorrelation(xT1, yT1) - super.calculateCorrelation(xT0, yT0));
                    }
                    /* Average Differential Correlation Coefficient between an inside node of the local network and
                    an outside one */
                    else if (nodesIn.contains(j) || nodesIn.contains(k)) {
                        ccOut += Math.abs(super.calculateCorrelation(xT1, yT1) - super.calculateCorrelation(xT0, yT0));
                    }
                }
            }
            ccIn /= nodesIn.size() * nodesIn.size();
            ccOut /= nodesIn.size() * nodesIn.size();

            this.landscapeDNMs.get(i).add(sd * (ccIn + ccOut));

            xT0 = windowT0[i];
            xT1 = windowT1[i];
            /* Maintaining some values in the networks */
            for (int j = 0; j < numCountries; j++) {
                if (j > i) {
                    yT0 = windowT0[j];
                    yT1 = windowT1[j];

                    cc = Math.abs(super.calculateCorrelation(xT1, yT1)) -
                         Math.abs(super.calculateCorrelation(xT0, yT0));
                    network[i][j] = Math.abs(cc);
                    network[j][i] = network[i][j];
                }
            }
        }

        return network;
    }

    /**
     * Calculates the early warning signals based on the Landscape - Dynamic Network Marker (L-DNM).
     * @return List<Double> List of all the values of the Minimum Landscape - Dynamic Network Marker (L-DNM) of each
     * network between the established dates.
     * @author Angel Fragua
     */
    public List<List<Double>> landscape() {
        return this.landscapeDNMs;
    }
}
