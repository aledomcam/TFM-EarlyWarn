package earlywarn.signals;

import org.apache.commons.math3.stat.correlation.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.csv.CSVFormat;
import org.jgrapht.nio.csv.CSVImporter;
import org.jgrapht.util.SupplierUtil;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * General Class for generating early warning signals and markers to early detect outbreaks.
 * Notes: It assumes that every country has the same number of reports and that there is no gap between the first date
 * with covid reports and the last one. Also, it assumes tha all countries have the same date for the first report,
 * and hence all countries have the same date for its last report. (All things has been proved)
 */
public class EWarningGeneral {
    /* Default values */
    protected final static LocalDate startDateDefault = LocalDate.of(2020, 2, 15);
    protected final static LocalDate endDateDefault = LocalDate.of(2020, 9, 15);
    protected final static int windowSizeDefault = 14;
    protected final static List<String> countriesDefault = new ArrayList<>(Arrays.asList(
            "AL", "AD", "AM", "AT", "AZ", "BE", "BA", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR",
            "GE", "DE", "GR", "HU", "IS", "IE", "IT", "LV", "LI", "LT", "LU", "MT", "MC", "ME", "NL",
            "MK", "NO", "PL", "PT", "MD", "RO", "SM", "RS", "SK", "SI", "ES", "SE", "CH", "GB", "TR",
            "UA"));
    protected final static String correlationDefault = "pearson";

    /* Class properties */
    protected GraphDatabaseService db;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected List<String> countries;
    protected int windowSize;
    protected String correlation;
    protected double[][][] networks;
    protected int[][][] adjacencies;
    protected long[][] dataOriginal;
    protected double[][] data;

    /**
     * Secondary constructor for the Class that receive fewer parameters than the main one.
     * @param db Neo4j database instance annotated with the @Context from the main procedure function.
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningGeneral(GraphDatabaseService db) throws RuntimeException {
        this(db, startDateDefault, endDateDefault, countriesDefault, windowSizeDefault, correlationDefault);
    }

    /**
     * Secondary constructor for the Class that receive fewer parameters than the main one.
     * @param db Neo4j database instance annotated with the @Context from the main procedure function.
     * @param startDate First date of the range of days of interest.
     * @param endDate Last date of the range of days of interest.
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningGeneral(GraphDatabaseService db, LocalDate startDate, LocalDate endDate) throws RuntimeException {
        this(db, startDate, endDate, countriesDefault, windowSizeDefault, correlationDefault);
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
     * @throws RuntimeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size.
     * If there are less than two selected countries. If any country inside the countries list isn't contain
     * in the database.
     * @author Angel Fragua
     */
    public EWarningGeneral(GraphDatabaseService db, LocalDate startDate, LocalDate endDate, List<String> countries,
                           int windowSize, String correlation) throws RuntimeException {
        this.db = db;
        this.startDate = startDate;
        this.endDate = endDate;
        this.countries = countries;
        Collections.sort(countries); /* The list is sorted to avoid using dictionaries */
        this.windowSize = windowSize;
        this.correlation = correlation;

        checkDates();
        checkCountries();
    }

    /**
     * Assures that the user establish a start date of study previous to the end date. Also, it assures that the
     * database contains reports of covid confirmed cases for both dates, which means that it also will contain reports
     * for all the dates in the interval between the selected dates of study. Finally, it checks that the interval
     * between the start date and the end date is equal or greater than the windows size.
     * @throws DateOutRangeException If startDate is greater than endDate or the database doesn't contain it. If there
     * aren't enough dates for the window size.
     * @author Angel Fragua
     */
    protected void checkDates() throws DateOutRangeException {
        if (this.startDate.isAfter(this.endDate)) {
            throw new DateOutRangeException("<startDate> must be older than <endDate>.");
        }
        Queries queries = new Queries(this.db);
        LocalDate maxDate = queries.maxReportDate();
        LocalDate minDate = queries.minReportDate();
        if (this.startDate.isBefore(minDate) || this.endDate.isAfter(maxDate)) {
            throw new DateOutRangeException("Dates out of range. [" + minDate + " , " + maxDate + "] (year-month-day)");
        }
        if (this.windowSize > 0 && ChronoUnit.DAYS.between(minDate, this.endDate) < this.windowSize - 1) {
            throw new DateOutRangeException("The interval between the first report date in the database and the " +
                                            "<endDate> must be equal or greater than <windowSize>.");
        }
    }

    /**
     * Assures that there are at least two selected countries. And also assures, that all the selected countries
     * are contained in the database.
     * @throws CountryUndefinedException If there are less than two selected countries. If any country inside
     * the countries list isn't contain in the database.
     * @author Angel Fragua
     */
    private void checkCountries() throws CountryUndefinedException {
        Set<String> countriesSelected = new HashSet<>(this.countries);
        if (countriesSelected.size() < 2) {
            throw new CountryUndefinedException("There must be at least two different ISO-3166-Alpha2 country " +
                                                "references in <countries> and must be contained in the database.");
        }

        Queries queries = new Queries(this.db);
        Set<String> countriesDb = queries.getConfirmedCountries(this.countries);

        if (!countriesDb.containsAll(countriesSelected)) {
            countriesSelected.removeAll(countriesDb);
            throw new CountryUndefinedException("All ISO-3166-Alpha2 country references in <countries> must exist " +
                                                "and be contained in the database. Errors: " + countriesSelected);
        }
    }

    /**
     * Main method, that makes sure that all the data is correctly imported and transformed to subsequently generate
     * the corresponding networks matrices with its adjacencies for each instance of time between the start and
     * end date. In case that there isn't enough reports previous to the start date to fill the window size, it shifts
     * the start date enough dates to fulfill it.
     * @author Angel Fragua
     */
    public void checkWindows() {
        Queries queries = new Queries(this.db);
        LocalDate minDate = queries.minReportDate();
        long restDays = ChronoUnit.DAYS.between(minDate, this.startDate);
        if (restDays >= this.windowSize) {
            LocalDate startDateWindow = this.startDate.minusDays(this.windowSize - 1);
            this.dataOriginal = importData(startDateWindow);
            this.data = transformData(startDateWindow);
            this.adjacencies = generateAdjacencies(startDateWindow);
            this.networks = generateNetworks(startDateWindow);
        }
        else {
            LocalDate startDateWindow = this.startDate.minusDays(restDays);
            this.dataOriginal = importData(startDateWindow);
            this.data = transformData(startDateWindow);
            this.adjacencies = generateAdjacencies(startDateWindow);
            this.networks = generateNetworks(startDateWindow);
            this.startDate = this.startDate.plusDays(this.windowSize - 1 - restDays);
        }
    }

    /**
     * Import for every country, the original cumulative data of confirmed covid cases between the established dates
     * of study taking into account the size of the window.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return long[][] Matrix with the original cumulative data of confirmed covid cases. Each Row represents
     * a country, and each Column contains the cases from the first date of study until the end date.
     * @author Angel Fragua
     */
    protected long[][] importData(LocalDate startDateWindow) {
        int numCountries = this.countries.size();
        long[][] data = new long[numCountries][(int)ChronoUnit.DAYS.between(startDateWindow, endDate)];
        Queries queries = new Queries(this.db);
        for (int i = 0; i < numCountries; i++) {
            data[i] = queries.getReportConfirmed(this.countries.get(i), startDateWindow, this.endDate);
        }
        return data;
    }

    /**
     * Calculates the discrete difference of the received data, which means that each column with index i will be
     * column[i] = column[i+1] - column[i]. Can be used to transform the original data.
     * @param data Data to be transformed.
     * @return double[][] The discrete difference of the data, where its first column will be lost.
     * @author Angel Fragua
     */
    protected double[][] diffData(double[][] data) {
        int numCols = data[0].length;
        int numRows = data.length;
        double[][] diffData = new double[numRows][numCols - 1];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols - 1; j++) {
                diffData[i][j] = data[i][j+1] - data[i][j];
            }
        }
        return diffData;
    }

    /**
     * Transform the original data of cumulative confirmed covid cases to its desired form. In this general case, it
     * returns a copy of the same data matrix.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return long[][] Matrix with the transformed data. Each Row represents a country, and each Column contains
     * the cases from the first date of study until the end date.
     * @author Angel Fragua
     */
    protected double[][] transformData(LocalDate startDateWindow) {
        Object[] arrayOfUntyped = Arrays.stream(this.dataOriginal)
                                    .map(longArray -> Arrays.stream(longArray).asDoubleStream().toArray()).toArray();
        return Arrays.copyOf(arrayOfUntyped, arrayOfUntyped.length, double[][].class);
    }

    /**
     * Generates an adjacency matrix for each instant of study between the start date and the end date. By default,
     * the matrix generated represents a complete graph, which means that each node can be connected to every other node
     * except itself. This means that all adjacency matrices will be filled with 1's except the main diagonal
     * (top-left to bottom-right) that will be filled with 0's.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return int[][][] List of the adjacency matrices for each temporal instant from the start date to the end date.
     * @author Angel Fragua
     */
    protected int[][][] generateAdjacencies(LocalDate startDateWindow) {
        int numCountries = this.countries.size();
        int[][] adjacency = new int[numCountries][numCountries];
        for (int[] row: adjacency) {
            Arrays.fill(row, 1);
        }
        /* Fill adjacency diagonal with zeros */
        for (int i = 0; i < numCountries; i++) {
            adjacency[i][i] = 0;
        }
        List<int[][]> adjacencies = new ArrayList<>();
        while (startDateWindow.plusDays(this.windowSize).compareTo(this.endDate.plusDays(1)) <= 0) {
            adjacencies.add(adjacency);
            startDateWindow = startDateWindow.plusDays(1);
        }
        return adjacencies.toArray(new int[adjacencies.size()][][]);
    }

    /**
     * Generates a correlation matrix for each instant of study between the start date and the end date. This means
     * that for every pair of windows containing the confirmed covid cases for each possible pair of countries,
     * are used to calculate its correlation coefficient which will determinate the weight of the edge that
     * connects them both in the graph.
     * @param startDateWindow Start date corresponding to the first window's date, which will be as many days prior to
     * the real start date of study as the size of the windows minus one.
     * @return double[][][] List of the correlation matrices for each temporal instant from the start date to
     * the end date.
     * @author Angel Fragua
     */
    protected double[][][] generateNetworks(LocalDate startDateWindow) {
        int numCountries = this.countries.size();
        List<double[][]> networks = new ArrayList<>();
        double[][] window = new double[numCountries][this.windowSize];

        int i = 0;
        while (startDateWindow.plusDays(this.windowSize).compareTo(this.endDate.plusDays(1)) <= 0) {
            for (int j = 0; j < numCountries; j ++) {
                for (int k = 0; k < this.windowSize; k++) {
                    window[j][k] = this.data[j][k+i];
                }
            }
            networks.add(windowToNetwork(window));
            startDateWindow = startDateWindow.plusDays(1);
            i = i + 1;
        }
        return networks.toArray(new double[networks.size()][][]);
    }

    /**
     * Transform the data of the confirmed covid cases in a fixed window time to the graph matrix of the network,
     * where the edges represent the coefficient correlation between its pair of nodes, and the nodes represent each
     * country.
     * @param window Data of the confirmed covid cases in a fixed period of time, where the Rows represent each country
     * and the columns represent each date from the latest to the new ones.
     * @return double[][] The network's matrix created with the data of a fixed time window.
     * @author Angel Fragua
     */
    protected double[][] windowToNetwork(double[][] window) {
        int numCountries = this.countries.size();
        double[][] network = new double[numCountries][numCountries];

        double cc;
        for (int i = 0; i < numCountries; i++) {
            for (int j = 0; j < numCountries; j++) {
                if (j > i) {
                    cc = calculateCorrelation(window[i], window[j]);
                    network[i][j] = cc;
                    network[j][i] = cc;
                }
            }
        }
        return network;
    }

    /**
     * Transforms a 2d int array representing a network into a String of its corresponding matrix.
     * @param network 2d int array of the network to be transformed.
     * @return String Representation of the network, where each row represent a row of the matrix and the same
     * happens with the columns. Values are separated by comas.
     */
    protected String networkToString(int[][] network) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, j = 0; i < network.length; i++, j = 0) {
            for (; j < network.length - 1; j++) {
                result.append(network[i][j] + ",");
            }
            result.append(network[i][j] + System.lineSeparator());
        }
        return result.toString();
    }

    /**
     * Transforms a 2d int array representing a network into a Graph of the JGraphT library.
     * @param network 2d int array of the network to be transformed.
     * @return Graph The corresponding Graph of the JGraphT library.
     */
    protected Graph<String, DefaultEdge> networkToGraph(int[][] network) {
        /* Graph Type builder */
        Graph<String, DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(false).allowingSelfLoops(false).weighted(false)
                .edgeClass(DefaultEdge.class).vertexSupplier(SupplierUtil.createStringSupplier(1))
                .buildGraph();
        /* Specification of the importation of the graph */
        CSVImporter gImporter = new CSVImporter(CSVFormat.MATRIX, ',');
        gImporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        /* Creation of graph by adjacency matrix */
        gImporter.importGraph(g, new StringReader(networkToString(network)));
        return g;
    }

    /**
     * Transforms a 2d double array representing a network into a String of its corresponding matrix.
     * @param network 2d double array of the network to be transformed.
     * @return String Representation of the network, where each row represent a row of the matrix and the same
     * happens with the columns. Values are separated by comas.
     */
    protected String networkToString(double[][] network) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, j = 0; i < network.length; i++, j = 0) {
            for (; j < network.length - 1; j++) {
                if (network[i][j] == 0 || j <= i) {
                    result.append(",");
                }
                else {
                    result.append(network[i][j] + ",");
                }
            }
            if (network[i][j] == 0) {
                result.append(System.lineSeparator());
            }
            else {
                result.append(network[i][j] + System.lineSeparator());
            }
        }
        return result.toString();
    }

    /**
     * Transforms a 2d double array representing a network into a Graph of the JGraphT library.
     * @param network 2d double array of the network to be transformed.
     * @return Graph The corresponding Graph of the JGraphT library.
     */
    protected Graph<String, DefaultWeightedEdge> networkToGraph(double[][] network) {
        /* Graph Type builder */
        Graph<String, DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(false).allowingSelfLoops(false).weighted(true)
                .edgeClass(DefaultWeightedEdge.class).vertexSupplier(SupplierUtil.createStringSupplier(1))
                .buildGraph();
        /* Specification of the importation of the graph */
        CSVImporter gImporter = new CSVImporter(CSVFormat.MATRIX, ',');
        gImporter.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
        /* Creation of graph by adjacency matrix */
        gImporter.importGraph(g, new StringReader(networkToString(network)));
        return g;
    }

    /**
     * Computes the correlation coefficient between two arrays. Depending on the value established on the class property
     * correlation different types of correlation will be used. List of possible correlation values:
     *      - "pearson": Pearson Correlation
     *      - "spearman": Spearman Correlation
     *      - "kendall":Kendall Correlation
     *      - any other value: Pearson Correlation
     * @param x First data array.
     * @param y Second data array.
     * @return double The computed correlation coefficient, or zero in case it returns NaN.
     * @author Angel Fragua
     * TODO: Granger Causality https://bmcecol.biomedcentral.com/articles/10.1186/s12898-016-0087-7
     */
    protected double calculateCorrelation(double[] x, double[] y) {
        double cc;
        switch (this.correlation) {
            case "pearson": {
                PearsonsCorrelation c = new PearsonsCorrelation();
                cc = c.correlation(x, y);
                break;
            }
            case "spearman": {
                SpearmansCorrelation c = new SpearmansCorrelation();
                cc = c.correlation(x, y);
                break;
            }
            case "kendall": {
                KendallsCorrelation c = new KendallsCorrelation();
                cc = c.correlation(x, y);
                break;
            }
            default: {
                PearsonsCorrelation c = new PearsonsCorrelation();
                cc = c.correlation(x, y);
                break;
            }
        }
        return Double.isNaN(cc) ? 0 : cc;
    }

    /**
     * Overrides the default method to transform the class to a string.
     * @return The generated string.
     * @author Angel Fragua
     */
    @Override
    public String toString() {
        return "EWarningGeneral{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", countries=" + countries +
                ", windowSize=" + windowSize +
                ", correlation=" + correlation +
                ", adjacencies=(" + adjacencies.length + "," + adjacencies[0].length + "," +
                                    adjacencies[0][0].length + ")" +
                ", networks=(" + networks.length + "," + networks[0].length + "," + networks[0][0].length + ")" +
                '}';
    }
}