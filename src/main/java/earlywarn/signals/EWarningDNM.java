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
 * Specialization fo the EWarningGeneral general class for the generation of early warning signals and markers
 * to early detect outbreaks.
 * Notes: It assumes that every country has the same number of reports and that there is no gap between the first date
 * with covid reports and the last one. Also, it assumes tha all countries have the same date for the first report,
 * and hence all countries have the same date for its last report. (All things has been proved)
 */
public class EWarningDNM extends EWarningGeneral{
    /* Default values */
    protected final static int windowSizeDefault = 0;
    protected final static boolean cumulativeDataDefault = false;
    private final static List<List<String>> pathsSPDefault = new ArrayList<>(List.of(
            new ArrayList<>(List.of("NO", "IT")),
            new ArrayList<>(List.of("IE", "UA")),
            new ArrayList<>(List.of("IS", "AZ")),
            new ArrayList<>(List.of("PT", "FI"))
    ));

    /* Class properties */
    private boolean cumulativeData;

    /**
     * Secondary constructor for the Class that receive fewer parameters than the main one.
     * @param db Neo4j database instance annotated with the @Context from the main procedure function.
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size. If there aren't enough dates for a non window size configuration.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningDNM(GraphDatabaseService db) throws RuntimeException {
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
    public EWarningDNM(GraphDatabaseService db, LocalDate startDate, LocalDate endDate) throws RuntimeException {
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
    public EWarningDNM(GraphDatabaseService db, LocalDate startDate, LocalDate endDate, int windowSize,
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
    public EWarningDNM(GraphDatabaseService db, LocalDate startDate, LocalDate endDate, List<String> countries,
                       int windowSize, String correlation, boolean cumulativeData) throws RuntimeException {
        super(db, startDate, endDate, countries, windowSize, correlation);
        this.cumulativeData = cumulativeData;
    }

    /**
     * Assures that the user establish a start date of study previous to the end date. Also, it assures that the
     * database contains reports of covid confirmed cases for both dates, which means that it also will contain reports
     * for all the dates in the interval between the selected dates of study. Finally, it checks that the interval
     * between the start date and the end date is equal or greater than the windows size. The new incorporation is that
     * it checks if there are enough dates in case that no window size is selected (windowSize = 0).
     * @throws DateOutRangeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size. If there aren't enough dates for a non window size configuration.
     * @author Angel Fragua
     */
    @Override
    protected void checkDates() throws DateOutRangeException {
        super.checkDates();
        if (ChronoUnit.DAYS.between(this.startDate, this.endDate) < 3 - 1 && this.windowSize == 0) {
            throw new DateOutRangeException("The interval between <startDate> and <endDate> must be at least " +
                                            "of 3 days.");
        }
    }

    /**
     * Main method, that makes sure that all the data is correctly imported and transformed to subsequently generate
     * the corresponding networks matrices with its adjacencies for each instance of time between the start and
     * end date. In case that there isn't enough reports previous to the start date to fill the window size, it shifts
     * the start date enough dates to fulfill it. In case that the window size is fixed to 0 it will use all posible
     * past data between the data of study and the start date, also shifting in case of need.
     * @author Angel Fragua
     */
    @Override
    public void checkWindows() {
        Queries queries = new Queries(this.db);
        LocalDate minDate = queries.minReportDate();
        long restDays = ChronoUnit.DAYS.between(minDate, this.startDate);
        if (this.windowSize == 0) {
            LocalDate startDateWindow;
            if (restDays >= 2) {
                startDateWindow = this.startDate.minusDays(2);
            }
            else {
                startDateWindow = this.startDate.minusDays(restDays);
                this.startDate = this.startDate.plusDays(2 - restDays);
            }
            this.dataOriginal = importData(startDateWindow);
            this.data = transformData(startDateWindow);
            this.adjacencies = generateAdjacenciesNoWindow(startDateWindow);
            this.networks = generateNetworksNoWindow(startDateWindow);
        }
        else {
            this.windowSize += 1;
            super.checkWindows();
            this.windowSize -= 1;
        }
    }

    /**
     * Transform the original data of cumulative confirmed covid cases to its desired form. In this specialized case,
     * depending on the class property cumulativeData, it will leave the covid confirmed cases as cumulative data
     * (False) or will make the discrete difference to contain the daily new confirmed cases of covid (True).
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return long[][] Matrix with the transformed data. Each Row represents a country, and each Column contains
     * the cases from the first date of study until the end date.
     * @author Angel Fragua
     */
    @Override
    protected double[][] transformData(LocalDate startDateWindow) {
        int numCountries = this.countries.size();
        Queries queries = new Queries(this.db);

        if (!this.cumulativeData) {
            long[] extraDate = new long[numCountries];
            if (startDateWindow.isAfter(queries.minReportDate())) {
                for (int i = 0; i < numCountries; i++) {
                    extraDate[i] = queries.getReportConfirmed(this.countries.get(i), startDateWindow.minusDays(1),
                                                              startDateWindow.minusDays(1))[0];
                }
            }

            double[][] tmpData = new double[numCountries][this.dataOriginal[0].length + 1];

            for (int i = 0; i < numCountries; i++) {
                tmpData[i][0] = extraDate[i];
            }
            for (int i = 0; i < numCountries; i++) {
                for (int j = 0; j < this.dataOriginal[0].length; j++) {
                    tmpData[i][j+1] = this.dataOriginal[i][j];
                }
            }
            return super.diffData(tmpData);
        }
        else {
            return super.transformData(startDateWindow);
        }
    }

    /**
     * Generates an adjacency matrix for each instant of study between the start date and the end date. By default,
     * the matrix generated represents a complete graph, which means that each node can be connected to every other node
     * except itself. This means that all adjacency matrices will be filled with 1's except the main diagonal
     * (top-left to bottom-right) that will be filled with 0's. This class will have one adjacency more than networks,
     * because each network is compose of two different windows. This method is oriented to instances with window size
     * greater than zero.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return int[][][] List of the adjacency matrices for each temporal instant from the start date to the end date.
     * @author Angel Fragua
     */
    @Override
    protected int[][][] generateAdjacencies(LocalDate startDateWindow) {
        this.windowSize -= 1;
        int[][][] adjacencies = super.generateAdjacencies(startDateWindow);
        this.windowSize += 1;
        return adjacencies;
    }

    /**
     * Generates an adjacency matrix for each instant of study between the start date and the end date. By default,
     * the matrix generated represents a complete graph, which means that each node can be connected to every other node
     * except itself. This means that all adjacency matrices will be filled with 1's except the main diagonal
     * (top-left to bottom-right) that will be filled with 0's. This class will have one adjacency more than networks,
     * because each network is compose of two different windows. This method is oriented to instances with no window
     * size, which is the same as window size equal to zero.
     * @param startDateWindow Start date corresponding to the first window's date.
     * @return int[][][] List of the adjacency matrices for each temporal instant from the start date to the end date.
     * @author Angel Fragua
     */
    private int[][][] generateAdjacenciesNoWindow(LocalDate startDateWindow) {
        return super.generateAdjacencies(startDateWindow.plusDays(2));
    }

    /**
     * Generates a correlation matrix for each instant of study between the start date and the end date. This means
     * that for every pair of windows containing the confirmed covid cases for each possible pair of countries,
     * are used to calculate its correlation coefficient which will determinate the weight of the edge that
     * connects them both in the graph. The new incorporation is that for each network it is required a total of two
     * windows for each country instead of one. This method is oriented to instances with window size greater than zero.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return double[][][] List of the correlation matrices for each temporal instant from the start date to
     * the end date.
     * @author Angel Fragua
     */
    @Override
    protected double[][][] generateNetworks(LocalDate startDateWindow) {
        int numCountries = this.countries.size();
        List<double[][]> networks = new ArrayList<>();
        double[][] windowT0 = new double[numCountries][this.windowSize - 1];
        double[][] windowT1 = new double[numCountries][this.windowSize - 1];

        int i = 0;
        while (startDateWindow.plusDays(this.windowSize).compareTo(this.endDate.plusDays(1)) <= 0) {
            for (int j = 0; j < numCountries; j ++) {
                for (int k = 0; k < this.windowSize - 1; k++) {
                    windowT0[j][k] = this.data[j][k + i];
                }
                for (int k = 0; k < this.windowSize - 1; k++) {
                    windowT1[j][k] = this.data[j][k + i + 1];
                }
            }

            networks.add(windowToNetwork(windowT0, windowT1, this.adjacencies[i], this.adjacencies[i + 1]));
            startDateWindow = startDateWindow.plusDays(1);
            i = i + 1;
        }
        return networks.toArray(new double[networks.size()][][]);
    }

    /**
     * Generates a correlation matrix for each instant of study between the start date and the end date. This means
     * that for every pair of windows containing the confirmed covid cases for each possible pair of countries,
     * are used to calculate its correlation coefficient which will determinate the weight of the edge that
     * connects them both in the graph. The new incorporation is that for each network it is required a total of two
     * windows for each country instead of one. This method is oriented to instances with no window size,
     * which is the same as window size equal to zero.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return double[][][] List of the correlation matrices for each temporal instant from the start date to
     * the end date.
     * @author Angel Fragua
     */
    protected double[][][] generateNetworksNoWindow(LocalDate startDateWindow) {
        int numCountries = this.countries.size();
        List<double[][]> networks = new ArrayList<>();
        List<List<Double>> windowT0 = new ArrayList<>();
        List<List<Double>> windowT1 = new ArrayList<>();
        for (int i = 0; i < numCountries; i++) {
            windowT0.add(new ArrayList<>());
            windowT1.add(new ArrayList<>());
        }

        for (int i = 0; startDateWindow.plusDays(i + 2).compareTo(this.endDate) <= 0; i++) {
            for (int j = 0; j < numCountries; j ++) {
                windowT0.get(j).clear();
                windowT1.get(j).clear();
            }
            for (int j = 0; j < numCountries; j ++) {
                for (int k = 0; startDateWindow.plusDays(k).compareTo(startDateWindow.plusDays(i + 2)) < 0; k++) {
                    windowT0.get(j).add(this.data[j][k]);
                }
                for (int k = 0; startDateWindow.plusDays(k).compareTo(startDateWindow.plusDays(i + 2)) <= 0; k++) {
                    windowT1.get(j).add(this.data[j][k]);
                }
            }
            networks.add(windowToNetwork(
                    windowT0.stream()
                            .map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
                            .toArray(double[][]::new),
                    windowT1.stream()
                            .map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
                            .toArray(double[][]::new),
                    this.adjacencies[i], this.adjacencies[i + 1]));
        }
        return networks.toArray(new double[networks.size()][][]);
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
        double xT0Sd, xT1Sd, yT0Sd, yT1Sd;
        double cc, sd;
        for (int i = 0; i < numCountries; i++) {
            xT0 = windowT0[i];
            xT1 = windowT1[i];
            xT0Sd = sdFunction.evaluate(xT0);
            xT1Sd = sdFunction.evaluate(xT1);
            for (int j = 0; j < numCountries; j++) {
                if (j > i) {
                    yT0 = windowT0[j];
                    yT1 = windowT1[j];
                    yT0Sd = sdFunction.evaluate(yT0);
                    yT1Sd = sdFunction.evaluate(yT1);

                    cc = Math.abs(super.calculateCorrelation(xT1, yT1)) -
                         Math.abs(super.calculateCorrelation(xT0, yT0));
                    sd = (xT1Sd + yT1Sd) / 2 - (xT0Sd + yT0Sd) / 2;
                    network[i][j] = Math.abs(cc) * Math.abs(sd);
                    network[j][i] = network[i][j];
                }
            }
        }

        return network;
    }

    /**
     * Calculates the early warning signals based on the Minimum Spanning Tree - Dynamic Network Marker (MST-DNM).
     * @return List<Double> List of all the values of the Minimum Spanning Tree - Dynamic Network Marker (MST-DNM) of
     * each network between the established dates.
     * @author Angel Fragua
     */
    public List<Double> MST() {
        List<Double> mstDNMs = new ArrayList<>();
        for (double[][] net : this.networks) {
            net = Arrays.stream(net)
                    .map(x -> Arrays.stream(x).map(y -> (y < 1.0E-12) ? 0 : y).toArray())
                    .toArray(double[][]::new);
            Graph<String, DefaultWeightedEdge> g = super.networkToGraph(net);
            KruskalMinimumSpanningTree mst = new KruskalMinimumSpanningTree<>(g);

            mstDNMs.add(mst.getSpanningTree().getWeight());
        }

        return mstDNMs;
    }

    /**
     * Calculates the early warning signals based on the Shortest Path - Dynamic Network Marker (SP-DNM).
     * @param paths List of the pair of countries from which the shortest path will be searched. This pair of countries
     * will also be lists but in this case of size two, where the first element is a ISO-3166-Alpha2 of the origin
     * country and the second one is another ISO-3166-Alpha2 reference of the destination country.
     * @return List<List<Double>> List of all the values of the Shortest Path - Dynamic Network Marker (SP-DNM) of
     * each network between the established dates.
     * @author Angel Fragua
     */
    public List<List<Double>> SP(List<List<String>> paths) {
        List<List<Double>> spDNMs = new ArrayList<>();

        /* If paths is empty, then default paths for default countries are established: northeast-southeast,
        easter-gestear, northeaster-southwester, northwester-southeaster*/
        if (paths.isEmpty()) {
            paths = this.pathsSPDefault;
        }
        /* Check if all iso2 references are established and get its index */
        int[][] pathsIdx = new int[paths.size()][2];
        for (int i = 0; i < paths.size(); i++) {
            pathsIdx[i][0] = countries.indexOf(paths.get(i).get(0));
            pathsIdx[i][1] = countries.indexOf(paths.get(i).get(1));
            if (pathsIdx[i][0] == -1 || pathsIdx[i][1] == -1) {
                throw new CountryUndefinedException("Some ISO-3166-Alpha2 references for the paths are incorrect or " +
                        "not established in the Class.");
            }
            spDNMs.add(new ArrayList<>());
        }

        double pathLenght = 0;
        double[][] net;
        for (int i = 0; i < this.networks.length; i++) {
            net = Arrays.stream(this.networks[i])
                    .map(x -> Arrays.stream(x).map(y -> (y < 1.0E-12) ? 0 : y).toArray())
                    .toArray(double[][]::new);
            Graph<String, DefaultWeightedEdge> g = super.networkToGraph(net);
            DijkstraShortestPath<String, DefaultWeightedEdge> sp = new DijkstraShortestPath(g);

            for (int j = 0; j < pathsIdx.length; j++) {
                pathLenght = sp.getPathWeight(String.valueOf(pathsIdx[j][0] + 1), String.valueOf(pathsIdx[j][1] + 1));
                spDNMs.get(j).add(pathLenght == Double.POSITIVE_INFINITY ? 0: pathLenght);
            }
        }

        return spDNMs;
    }
}
