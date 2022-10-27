package earlywarn.signals;

import org.junit.jupiter.api.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.math3.util.Precision.round;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit Class used to test the class EWarningSpecific. It builds a temporal Neo4j database witch is loaded with
 * the nodes declared in the resources files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) /* This annotation is needed for creating a JUnit Class*/
public class EWarningSpecificTest {

    private Neo4j embeddedDatabaseServer;
    private GraphDatabaseService db;

    /**
     * Initialize a temporal Neo4j instance Database for the current Class tests.
     * It reads a file containing the queries for the creation of some Country Nodes. It also reads a file with the
     * queries needed to create some Report Nodes of the previous Country Nodes between the date 22-1-2020 and 1-3-2020.
     * Last it creates and execute a query that creates a Relationship between each Country Node and its corresponding
     * Report Nodes. Furthermore, it saves a reference to the Database Service used to run queries in the Database.
     * Thanks to the @BeforeAll annotation is the first method of the test to be executed, so it works as a constructor.
     * @throws IOException If there is a problem reading any resource file.
     * @author Angel Fragua
     */
    @BeforeAll
    void initializeNeo4j() throws IOException {

        var countries = new StringWriter();
        try (var in = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/countries.cypher")))) {
            in.transferTo(countries);
            countries.flush();
        }

        /* 40 Reports for each country starting the 22/01/2020 until the 01/03/2020  */
        var reports = new StringWriter();
        try (var in = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/reports.cypher")))) {
            in.transferTo(reports);
            reports.flush();
        }

        this.embeddedDatabaseServer = Neo4jBuilders
                .newInProcessBuilder()
                /* Loads the Country Nodes */
                .withFixture(countries.toString())
                /* Loads the Report Nodes */
                .withFixture(reports.toString())
                /* Creates a :REPORTS Relationship between previous Nodes */
                .withFixture("MATCH (c:Country), (r:Report) " +
                             "WHERE c.countryName = r.country " +
                             "MERGE (c) - [:REPORTS] -> (r)")
                .build();

        this.db = this.embeddedDatabaseServer.defaultDatabaseService();
    }

    /**
     * This method is the last executed by the class thanks to the @AfterAll annotation, which can be used to close all
     * connections to the Database.
     * @author Angel Fragua
     */
    @AfterAll
    void closeNeo4j() {
        this.embeddedDatabaseServer.close();
    }

    /**
     * Tests that the method density() from the EWarningSpecific returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void density1() {
        List<Double> densities = new ArrayList<>(Arrays.asList(0.002898550724638,0.002898550724638,0.002898550724638,
                0.002898550724638,0.002898550724638,0.001932367149758,0.001932367149758,0.001932367149758,
                0.001932367149758,0.001932367149758,0.001932367149758,0.,0.,0.000966183574879,0.000966183574879,
                0.000966183574879,0.000966183574879,0.000966183574879,0.,0.000966183574879,0.000966183574879,
                0.02512077294686,0.065700483091787,0.085990338164251,0.085024154589372,0.114009661835749,
                0.150724637681159));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 2, 1), LocalDate.of(2020, 3, 1),
                                                   EWarningSpecific.countriesDefault, 14, "pearson", false, false, 0.5);
        ew.checkWindows();

        assertThat(ew.density().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(densities.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method density() from the EWarningSpecific returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void density2() {
        List<Double> densities = new ArrayList<>(Arrays.asList(0.001932367149758,0.001932367149758,0.001932367149758,
                0.001932367149758,0.001932367149758,0.001932367149758,0.003864734299517,0.003864734299517,
                0.003864734299517,0.002898550724638,0.,0.,0.000966183574879,0.000966183574879,0.000966183574879,
                0.000966183574879,0.000966183574879,0.,0.,0.,0.014492753623188,0.052173913043478,0.057971014492754,
                0.055072463768116,0.060869565217391,0.077294685990338));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 2, 5), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 14, "pearson", false, true, 0.6);
        ew.checkWindows();

        assertThat(ew.density().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(densities.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method density() from the EWarningSpecific returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void density3() {
        List<Double> densities = new ArrayList<>(Arrays.asList(0.002898550724638,0.009661835748792,0.018357487922705,
                0.018357487922705,0.018357487922705,0.016425120772947,0.016425120772947,0.011594202898551,
                0.004830917874396,0.007729468599034,0.010628019323671,0.009661835748792,0.009661835748792,
                0.009661835748792,0.005797101449275,0.002898550724638,0.001932367149758,0.001932367149758,
                0.000966183574879,0.000966183574879,0.000966183574879,0.000966183574879,0.000966183574879,
                0.000966183574879,0.000966183574879,0.000966183574879,0.027053140096618));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 30), LocalDate.of(2020, 2, 25),
                EWarningSpecific.countriesDefault, 7, "spearman", true, false, 0.4);
        ew.checkWindows();

        assertThat(ew.density().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(densities.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method density() from the EWarningSpecific returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void density4() {
        List<Double> densities = new ArrayList<>(Arrays.asList(0.166666666666667,0.166666666666667,0.166666666666667,
                0.194444444444444,0.194444444444444,0.194444444444444,0.166666666666667,0.166666666666667,
                0.111111111111111,0.083333333333333,0.083333333333333,0.083333333333333,0.083333333333333,
                0.083333333333333,0.027777777777778,0.027777777777778,0.027777777777778,0.027777777777778,
                0.027777777777778,0.,0.,0.027777777777778,0.111111111111111,0.166666666666667,0.25,0.277777777777778,
                0.277777777777778));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "TR", "UA")), 14, "kendall",
                true, true, 0.7);
        ew.checkWindows();

        assertThat(ew.density().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(densities.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method clusteringCoefficient() from EWarningSpecific returns the correct List with
     * a precision of 10 decimal.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void clusteringCoefficient1() {
        List<Double> clusteringCoefficients = new ArrayList<>(Arrays.asList(0.032608695652174,0.032608695652174,
                0.032608695652174,0.032608695652174,0.032608695652174,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                0.081780538302277,0.128623188405797,0.188255153840192,0.192595598845599,0.208019999324347,
                0.223542912401608));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 14, "pearson", false, false, 0.5);
        ew.checkWindows();

        assertThat(ew.clusteringCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(clusteringCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method clusteringCoefficient() from EWarningSpecific returns the correct List with
     * a precision of 10 decimal.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void clusteringCoefficient2() {
        List<Double> clusteringCoefficients = new ArrayList<>(Arrays.asList(0.,0.,0.,0.,0.,0.,0.,0.02536231884058,
                0.036231884057971,0.036231884057971,0.036231884057971,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.065217391304348));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 30), LocalDate.of(2020, 2, 25),
                EWarningSpecific.countriesDefault, 14, "spearman", false, true, 0.4);
        ew.checkWindows();

        assertThat(ew.clusteringCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(clusteringCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method clusteringCoefficient() from EWarningSpecific returns the correct List with
     * a precision of 10 decimal.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void clusteringCoefficient3() {
        List<Double> clusteringCoefficients = new ArrayList<>(Arrays.asList(0.,0.,0.,0.,0.,0.,0.053260869565217,
                0.058695652173913,0.03804347826087,0.03804347826087,0.,0.,0.,0.,0.032608695652174,0.032608695652174,0.,
                0.,0.,0.,0.));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 2, 15),
                EWarningSpecific.countriesDefault, 5, "pearson", true, false, 0.7);
        ew.checkWindows();

        assertThat(ew.clusteringCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(clusteringCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method clusteringCoefficient() from EWarningSpecific returns the correct List with
     * a precision of 10 decimal.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void clusteringCoefficient4() {
        List<Double> clusteringCoefficients = new ArrayList<>(Arrays.asList(0.277777777777778,0.277777777777778,
                0.277777777777778,0.277777777777778,0.277777777777778,0.277777777777778,0.277777777777778,
                0.277777777777778,0.277777777777778,0.277777777777778,0.222222222222222,0.222222222222222,
                0.222222222222222,0.166666666666667,0.166666666666667,0.166666666666667,0.166666666666667,0.,0.,0.,
                0.222222222222222,0.277777777777778,0.277777777777778));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 25), LocalDate.of(2020, 2, 27),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "TR", "UA")), 15, "kendall",
                true, true, 0.3);
        ew.checkWindows();

        assertThat(ew.clusteringCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(clusteringCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method assortativityCoefficient() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void assortativityCoefficient1() {
        List<Double> assortativityCoefficients = new ArrayList<>(Arrays.asList(Double.NaN,Double.NaN,Double.NaN,
                Double.NaN,Double.NaN,-1.,-1.,-1.,-1.,-1.,-1.,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,
                Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,-0.322033898305085,0.573667711598743,
                0.045115009746591,0.057075421092159,0.26727208309247,0.389198678232136));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 25), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 14, "pearson", false, false, 0.5);
        ew.checkWindows();

        assertThat(ew.assortativityCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(assortativityCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method assortativityCoefficient() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void assortativityCoefficient2() {
        List<Double> assortativityCoefficients = new ArrayList<>(Arrays.asList(0.999999999999998,0.999999999999998,
                Double.NaN,Double.NaN,Double.NaN,-0.5,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,
                Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,
                -0.253501400560229,-0.0756034310792,0.021009162472578));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 2, 5), LocalDate.of(2020, 2, 28),
                EWarningSpecific.countriesDefault, 7, "kendall", false, true, 0.4);
        ew.checkWindows();

        assertThat(ew.assortativityCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(assortativityCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method assortativityCoefficient() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void assortativityCoefficient3() {
        List<Double> assortativityCoefficients = new ArrayList<>(Arrays.asList(1.,-0.388888888888892,-0.373493975903611,
                -0.373493975903611,-0.373493975903611,-0.393939393939395,-0.382113821138214,-0.382113821138214,
                -0.545961002785514,-0.377880184331794,-0.454545454545452,-0.447368421052636,Double.NaN,Double.NaN,
                Double.NaN,Double.NaN,-0.714285714285714,-0.499999999999999,-1.,Double.NaN,Double.NaN,Double.NaN,
                -1.,Double.NaN,Double.NaN,1.,-0.193877551020413,-0.21274856987196,-0.126202407695415,0.063085571517803,
                0.173472881500324));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 10, "spearman", true, false, 0.6);
        ew.checkWindows();

        assertThat(ew.assortativityCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(assortativityCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method assortativityCoefficient() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void assortativityCoefficient4() {
        List<Double> assortativityCoefficients = new ArrayList<>(Arrays.asList(Double.NaN,Double.NaN,Double.NaN,
                -0.666666666666679,-0.714285714285714,-0.714285714285714,-0.714285714285714,-0.499999999999999,
                -0.499999999999999,-0.5,-1.,-1.,Double.NaN,Double.NaN,Double.NaN,-1.,Double.NaN,Double.NaN,Double.NaN,
                Double.NaN,Double.NaN,Double.NaN,-0.666666666666679,-0.548387096774194,Double.NaN,Double.NaN,
                Double.NaN));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 30), LocalDate.of(2020, 3, 1),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "TR", "UA")), 14, "pearson",
                true, true, 0.8);
        ew.checkWindows();

        assertThat(ew.assortativityCoefficient().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(assortativityCoefficients.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method numberEdges() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void numberEdges1() {
        List<Long> numberEdges = Arrays
                .stream(new long[]{0,0,1,1,1,1,1,0,1,1,26,68,89,88,118,156})
                .boxed().collect(Collectors.toList());

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 2, 15), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 14, "pearson", false, false, 0.5);
        ew.checkWindows();

        assertThat(ew.numberEdges()).isEqualTo(numberEdges);
    }

    /**
     * Tests that the method numberEdges() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void numberEdges2() {
        List<Long> numberEdges = Arrays
                .stream(new long[]{2,2,2,2,2,2,2,4,4,4,3,0,1,1,1,1,1,2,1,1,1,21,68,98,95,117,148})
                .boxed().collect(Collectors.toList());

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 2, 1), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 14, "pearson", false, true, 0.4);
        ew.checkWindows();

        assertThat(ew.numberEdges()).isEqualTo(numberEdges);
    }

    /**
     * Tests that the method numberEdges() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void numberEdges3() {
        List<Long> numberEdges = Arrays
                .stream(new long[]{4,13,19,19,18,18,17,17,13,12,12,10,10,10,9,6,4,3,2,1,1,1,1,1,1,16})
                .boxed().collect(Collectors.toList());

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 2, 25),
                EWarningSpecific.countriesDefault, 10, "kendall", true, false, 0.6);
        ew.checkWindows();

        assertThat(ew.numberEdges()).isEqualTo(numberEdges);
    }

    /**
     * Tests that the method numberEdges() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void numberEdges4() {
        List<Long> numberEdges = Arrays
                .stream(new long[]{7, 7, 7, 7, 6, 5, 4, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 3, 4})
                .boxed().collect(Collectors.toList());

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 30), LocalDate.of(2020, 2, 28),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "TR", "UA")), 20, "spearman",
                true, true, 0.8);
        ew.checkWindows();

        assertThat(ew.numberEdges()).isEqualTo(numberEdges);
    }

    /**
     * Tests that the method PRS() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void PRS1() {
        List<Long> PRSs = new ArrayList<>(Arrays.asList(10471290240433882L,10471290240433882L,10471290240433882L,
                18616740653558996L,10471290126725780L,2636691134803056L,2636691134803056L,10471289597835078L,
                19333601809043054L,19333601678496052L,19333601547949050L,8862311814342000L,20237821076579988L,
                8862311683795000L,8862311683795000L,8862311553248000L,8862311292154000L,8862311161607000L,0L,
                8208999364395696L,8208989075424852L,88899106287021166L,110980913577286700L,127885207163862106L,
                121923308115984516L));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 24), LocalDate.of(2020, 2, 28),
                EWarningSpecific.countriesDefault, 14, "pearson", false, false, 0.4);
        ew.checkWindows();

        assertThat(ew.PRS()).isEqualTo(PRSs);
    }

    /**
     * Tests that the method numberEdges() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void PRS2() {
        List<Long> PRSs = new ArrayList<>(Arrays.asList(9153401522428020L,9153401522428020L,9153400796886108L,
                9876724845573712L,8366504719054328L,8366504719054328L,8366504719054328L,0L,0L,18993771220170130L,0L,0L,
                0L,0L,11375509262237988L,0L,0L,0L,0L,0L,0L,0L,0L,0L,33701141012817926L,51591315325569034L,
                49911089957789978L,27742594003219352L,24164383863270450L,46911650488170382L));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 2, 1), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 7, "kendall", false, true, 0.6);
        ew.checkWindows();

        assertThat(ew.PRS()).isEqualTo(PRSs);
    }

    /**
     * Tests that the method numberEdges() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void PRS3() {
        List<Long> PRSs = new ArrayList<>(Arrays.asList(93600840705710854L,93600836418678050L,93600836418678050L,
                98434261144606154L,99947250753086534L,101348708207093462L,98367954699347218L,97644629027822710L,
                97644629027822710L,93125783072662342L,88266245710900464L,88266245175173162L,83349194247685660L,
                83349193729912618L,83349193729912618L,67247062259044298L,51461844979552798L,28072418983779370L,
                20237819884120556L,8862311161607000L,17071323757411892L,17071309873267696L,17071299323202852L,
                77082672184402506L,131908150863053092L,153207735620081924L));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 2, 27),
                EWarningSpecific.countriesDefault, 12, "spearman", true, false, 0.5);
        ew.checkWindows();

        assertThat(ew.PRS()).isEqualTo(PRSs);
    }

    /**
     * Tests that the method numberEdges() from EWarningSpecific returns the correct List with 8 Byte precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void PRS4() {
        List<Long> PRSs = new ArrayList<>(Arrays.asList(8663565998448308L,8663565884740206L,26521551258666608L,
                26521551258666608L,26521548941528812L,25203119097235420L,25203119097235420L,25203118829801088L,
                25203118562366756L,23831920606845418L,22887542138899446L,21313997026347132L,21313997026347132L,
                21313996802290570L,21313996802290570L,21313996578234008L,21313996130120884L,8862311161607000L,
                8862311161607000L,8862310508872000L));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 31), LocalDate.of(2020, 2, 23),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "TR", "UA")), 14, "pearson",
                true, true, 0.7);
        ew.checkWindows();

        assertThat(ew.PRS()).isEqualTo(PRSs);
    }

    /**
     * Tests that the method formanRicciCurvature() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void formanRicciCurvature1() {
        List<Double> formanRicciCurvatures = new ArrayList<>(Arrays.asList(Double.NaN,Double.NaN,Double.NaN,2.,2.75,
                1.714285714285714,1.714285714285714,1.714285714285714,3.714285714285714,3.714285714285714,4.,2.,2.,
                1.333333333333333,2.,2.,2.,2.,2.,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,
                Double.NaN,Double.NaN,2.,8.,8.865853658536585,7.316666666666666,4.589743589743589,2.902255639097744,
                4.721893491124260));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                EWarningSpecific.countriesDefault, 7, "spearman", false, false, 0.4);
        ew.checkWindows();

        assertThat(ew.formanRicciCurvature().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(formanRicciCurvatures.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method formanRicciCurvature() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void formanRicciCurvature2() {
        List<Double> formanRicciCurvatures = new ArrayList<>(Arrays.asList(2.,2.,2.,2.,2.,2.,2.,2.,2.,2.,2.,Double.NaN,
                Double.NaN,2.,2.,2.,2.,2.,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,
                6.));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 27), LocalDate.of(2020, 2, 25),
                EWarningSpecific.countriesDefault, 10, "pearson", false, true, 0.8);
        ew.checkWindows();

        assertThat(ew.formanRicciCurvature().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(formanRicciCurvatures.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method formanRicciCurvature() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = false which won't apply the square root to the original data to
     * smooth it. Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void formanRicciCurvature3() {
        List<Double> formanRicciCurvatures = new ArrayList<>(Arrays.asList(7.,6.227272727272728,5.75,5.96,5.96,
                6.227272727272728,7.,6.,3.8125,4.076923076923077,3.75,5.,5.,3.666666666666667,2.875,4.,1.75,
                0.666666666666667,2.,1.,1.,4.333333333333333,9.316455696202532,12.127659574468085,14.283333333333333));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 2, 1), LocalDate.of(2020, 2, 28),
                EWarningSpecific.countriesDefault, 14, "kendall", true, false, 0.6);
        ew.checkWindows();

        assertThat(ew.formanRicciCurvature().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(formanRicciCurvatures.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method formanRicciCurvature() from EWarningSpecific returns the correct List with
     * a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and squareRootData = true which apply the square root to the original data to smooth it.
     * Rest of parameters are being changed to assure its correctness.
     * @author Angel Fragua
     */
    @Test
    void formanRicciCurvature4() {
        List<Double> formanRicciCurvatures = new ArrayList<>(Arrays.asList(2.857142857142857,3.666666666666667,
                3.666666666666667,5.,5.,5.,5.,5.,3.666666666666667,2.857142857142857,2.4,1.75,3.,3.,3.,3.,3.,2.,2.,2.,
                2.,4.,5.,5.,5.,5.,6.));

        EWarningSpecific ew = new EWarningSpecific(this.db, LocalDate.of(2020, 1, 31), LocalDate.of(2020, 3, 1),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "TR", "UA")), 14, "pearson",
                true, true, 0.5);
        ew.checkWindows();

        assertThat(ew.formanRicciCurvature().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(formanRicciCurvatures.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }
}
