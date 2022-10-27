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
 * JUnit Class used to test the class EWarningDNM. It builds a temporal Neo4j database witch is loaded with
 * the nodes declared in the resources files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) /* This annotation is needed for creating a JUnit Class*/
public class EWarningDNMTest {

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
     * Tests that the method checkDates() from the EWarningDNM Class properly throws Exception when the dates are
     * wrong. The test will check that the interval of days between startDate and the endDate is not greater or equal
     * to three when no window size or window size equal to zero.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest1() {
        Exception thrown = Assertions.assertThrows(
                DateOutRangeException.class,
                () -> new EWarningDNM(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 1, 22), 0, false)
        );

        Assertions.assertTrue(
                thrown.getMessage().equals("The interval between <startDate> and <endDate> must be at least of 3 days.")
        );
    }

    /**
     * Tests that the method checkDates() from the EWarningDNM Class properly throws Exception when the dates are
     * wrong. This test will check that the interval of dates between the start date and the end date are equal or
     * greater than the window size.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest2() {

        Exception thrown = Assertions.assertThrows(
                DateOutRangeException.class,
                () -> new EWarningDNM(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 2, 3), 14, false)
        );

        Assertions.assertTrue(thrown.getMessage().equals("The interval between the first report date in the database " +
                "and the <endDate> must be equal or greater than <windowSize>."));
    }

    /**
     * Tests that the method checkDates() from the EWarningDNM Class is properly executed with no errors when none
     * window size or window size equal to zero.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest3() {
        LocalDate startDate = LocalDate.of(2020, 1, 22);
        LocalDate endDate = LocalDate.of(2020, 2, 22);

        EWarningDNM ew = new EWarningDNM(this.db, startDate, endDate, 0, false);

        assertThat(ew.startDate).isEqualTo(startDate);
        assertThat(ew.endDate).isEqualTo(endDate);
    }

    /**
     * Tests that the method checkDates() from the EWarningDNM Class is properly executed with no errors when window
     * size is greater than zero.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest4() {
        LocalDate startDate = LocalDate.of(2020, 1, 22);
        LocalDate endDate = LocalDate.of(2020, 3, 1);

        EWarningDNM ew = new EWarningDNM(this.db, startDate, endDate, 7, false);

        assertThat(ew.startDate).isEqualTo(startDate);
        assertThat(ew.endDate).isEqualTo(endDate);
    }

    /**
     * Tests that the method checkWindows() from the EWarningDNM Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. This test will use the first date with Report and none window size or window size equal to
     * zero.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest1() {
        EWarningDNM ew = new EWarningDNM(this.db,  LocalDate.of(2020, 1, 22), LocalDate.of(2020, 2, 10), 0, false);
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 1, 24));
        assertThat(ew.endDate).isEqualTo(LocalDate.of(2020, 2, 10));
        assertThat(ew.adjacencies.length).isEqualTo(ew.networks.length + 1);
    }

    /**
     * Tests that the method checkWindows() from the EWarningDNM Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. This test will use the first date with Report and none window size greater than zero.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest2() {
        EWarningDNM ew = new EWarningDNM(this.db,  LocalDate.of(2020, 1, 22), LocalDate.of(2020, 2, 10), 14, false);
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 5));
        assertThat(ew.endDate).isEqualTo(LocalDate.of(2020, 2, 10));
        assertThat(ew.adjacencies.length).isEqualTo(ew.networks.length + 1);
    }

    /**
     * Tests that the method checkWindows() from the EWarningDNM Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. This test will use a date between the first possible date the next three days, because
     * it is the minimum interval when no window size or window size equal to zero.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest3() {
        EWarningDNM ew = new EWarningDNM(this.db,  LocalDate.of(2020, 1, 23), LocalDate.of(2020, 3, 1), 0, false);
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 1, 24));
        assertThat(ew.endDate).isEqualTo(LocalDate.of(2020, 3, 1));
        assertThat(ew.adjacencies.length).isEqualTo(ew.networks.length + 1);
    }

    /**
     * Tests that the method checkWindows() from the EWarningDNM Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. This test will use a date between the first possible date and as many days passed as the
     * windowSize, when this is greater than zero.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest4() {
        EWarningDNM ew = new EWarningDNM(this.db,  LocalDate.of(2020, 1, 26), LocalDate.of(2020, 2, 25), 14, false);
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 5));
        assertThat(ew.endDate).isEqualTo(LocalDate.of(2020, 2, 25));
        assertThat(ew.adjacencies.length).isEqualTo(ew.networks.length + 1);
    }

    /**
     * Tests that the method checkWindows() from the EWarningDNM Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. This test will use a date that doesn't need any changes and no window size or
     * window size equal to zero.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest5() {
        EWarningDNM ew = new EWarningDNM(this.db,  LocalDate.of(2020, 2, 10), LocalDate.of(2020, 3, 1), 0, false);
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 10));
        assertThat(ew.endDate).isEqualTo(LocalDate.of(2020, 3, 1));
        assertThat(ew.adjacencies.length).isEqualTo(ew.networks.length + 1);
    }

    /**
     * Tests that the method checkWindows() from the EWarningDNM Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. Last test will use a date that doesn't need any changes and no window size is greater than
     * zero.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest6() {
        EWarningDNM ew = new EWarningDNM(this.db,  LocalDate.of(2020, 2, 10), LocalDate.of(2020, 2, 28), 7, false);
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 10));
        assertThat(ew.endDate).isEqualTo(LocalDate.of(2020, 2, 28));
        assertThat(ew.adjacencies.length).isEqualTo(ew.networks.length + 1);
    }

    /**
     * Tests that the method MST() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and windows size is set to zero. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void MST1() {
        List<Double> mstDNMs = new ArrayList<>(Arrays.asList(0.000000000000000,0.000000000000000,0.000000000000000,
                0.051305183464283,0.053836049047137,0.029628194681969,0.001414068969903,0.069203167857410,
                0.030282027403214,0.000859827077797,0.010225545191142,0.010092508014319,0.000486070437841,
                0.000425388885128,0.000416036617870,0.010130064916214,0.001087398192265,0.000293022974542,
                0.000192865489876,0.000153737215378,0.000131426842813,0.000113102434099,0.000147967728692,
                0.000127479995431,0.000081769446742,0.000100149137835,0.000065542585953,0.000052277103250,
                0.010397299490541,0.013620638913456,0.014977828959442,0.002274412369611,0.024562614365076,
                0.038631069651736,0.050467273266932,0.036249302444126,0.042147201938300,0.271143299140212));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                                         EWarningDNM.countriesDefault, 0, "pearson", false);
        ew.checkWindows();

        assertThat(ew.MST().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(mstDNMs.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method MST() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and windows size is set to a positive value. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void MST2() {
        List<Double> mstDNMs = new ArrayList<>(Arrays.asList(0.036629472661698,0.,0.136291419271724,0.175107261563823,
                0.008576622364481,0.764310386897689,0.049981768723534,0.048432272385102,0.,0.035618176586182,
                0.209745549505199,0.031102013112007,0.129821490911880,0.024595787993302,0.,0.011102003341613,
                0.046882693498788,0.350799388077512,0.032942804719868,0.006274407870875,0.067048040470598));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 24), LocalDate.of(2020, 2, 18),
                                         EWarningDNM.countriesDefault, 7, "kendall", false);
        ew.checkWindows();

        assertThat(ew.MST().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(mstDNMs.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method MST() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and windows size is set to zero. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void MST3() {
        List<Double> mstDNMs = new ArrayList<>(Arrays.asList(0.,0.153774955135062,0.379532124885078,0.008428922769769,
                0.040547222728089,0.070290910922934,0.008143919242808,0.002445317399995,0.000655069998889,
                0.007135320641056,0.006400457953679,0.002511946818106,0.001554370116456,0.000733201602980,
                0.000738918745483,0.000767574447598,0.000287889268119,0.000149248225176,0.000278348828202,
                0.000134841951530,0.000083471653601,0.000063399677175,0.000134980439481,0.000052352448395,
                0.002983244055570,0.003639433052130,0.030732732962041));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 30), LocalDate.of(2020, 2, 25),
                                         new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "UA")),
                                         0, "pearson", true);
        ew.checkWindows();

        assertThat(ew.MST().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(mstDNMs.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method MST() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and windows size is set to a positive value. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void MST4() {
        List<Double> mstDNMs = new ArrayList<>(Arrays.asList(0.006580081736144,0.001718433421641,0.001472432157785,
                0.003165327436239,0.003107189705678,0.004043997344271,0.030147817551941,0.003400256152265,
                0.010475631462965,0.040208207718277,0.002858962304332,0.008272405115297,0.047796689690430,
                0.008663079668945,0.022656624888362,0.118461848870236,0.233235197800657,0.169985795013954,
                0.313405560982561,0.140364957911194,2.077691521834900,1.972065313330318,1.713097773371550,
                1.335124042345958,1.049010009371482,1.517853665451508));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 2, 1), LocalDate.of(2020, 3, 1),
                                         EWarningDNM.countriesDefault, 14, "spearman", true);
        ew.checkWindows();

        assertThat(ew.MST().stream().map(x -> round(x, 10)).collect(Collectors.toList()))
                .isEqualTo(mstDNMs.stream().map(x -> round(x, 10)).collect(Collectors.toList()));
    }

    /**
     * Tests that the method SP() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and windows size is set to zero. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void SP1() {
        List<List<Double>> spDNMs = new ArrayList<>(List.of(
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.,0.,0.,0.017238722118873,0.023759499584101,0.023655781787522,0.017583634216697,
                        0.522768936212701)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.007032618126159)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.))
        ));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                EWarningDNM.countriesDefault, 0, "spearman", false);
        ew.checkWindows();

        assertThat(
                ew.SP(new ArrayList<>()).stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
            .isEqualTo(spDNMs.stream().map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                .collect(Collectors.toList()));
    }

    /**
     * Tests that the method SP() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and windows size is set to a positive value. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void SP2() {
        List<List<Double>> spDNMs = new ArrayList<>(List.of(
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.090949860124370,0.675773259978965,0.117675207896619)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.))
        ));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 24), LocalDate.of(2020, 2, 28),
                EWarningDNM.countriesDefault, 7, "kendall", false);
        ew.checkWindows();

        assertThat(
                ew.SP(new ArrayList<>()).stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
            .isEqualTo(spDNMs.stream().map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                .collect(Collectors.toList()));
    }

    /**
     * Tests that the method SP() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and windows size is set to zero. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void SP3() {
        List<List<Double>> spDNMs = new ArrayList<>(List.of(
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.042702121524257,0.003846034546913,
                        0.001119337823319,0.000402880702502,0.000369036770413,0.002188239661469,0.001029565834577,
                        0.000339814472014,0.000115711839843,0.000045142157285,0.000025450370381,0.000018786386760,
                        0.000016652459141,0.000016987798976,0.000009460436224,0.000002734270503,0.000001471795560,
                        0.000004063821179,0.000005610189028,0.000006474217550,0.000001800368954,0.018936014972885,
                        0.003128235937432,0.022229110032786,0.066580263845915,0.043595819100425,0.089345538447386)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.))
        ));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 25), LocalDate.of(2020, 3, 1),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "UA")),
                0, "pearson", true);
        ew.checkWindows();

        assertThat(
                ew.SP(new ArrayList<>(List.of(
                                        new ArrayList<>(List.of("ES","BE")),
                                        new ArrayList<>(List.of("GB","AL")))))
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
            .isEqualTo(spDNMs.stream().map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                .collect(Collectors.toList()));
    }

    /**
     * Tests that the method SP() from the EWarningDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and windows size is set to a positive value. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void SP4() {
        List<List<Double>> spDNMs = new ArrayList<>(List.of(
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.617001443359845,0.523786167497991,0.299032365160358,0.043992817973976,0.072603395363769)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.168380192825989)),
                new ArrayList<>(List.of(0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,
                        0.))
        ));

        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                EWarningDNM.countriesDefault, 14, "pearson", true);
        ew.checkWindows();

        assertThat(
                ew.SP(new ArrayList<>()).stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
            .isEqualTo(spDNMs.stream().map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                .collect(Collectors.toList()));
    }

    /**
     * Tests that the method SP() from the EWarningDNM properly throws Exception when the input is wrong.
     * This test will check that the paths passed as argument are real ISO-3166-Alpha2 references.
     * @author Angel Fragua
     */
    @Test
    void SP5() {
        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                                         EWarningDNM.countriesDefault, 14, "pearson", true);
        ew.checkWindows();

        Exception thrown = Assertions.assertThrows(
                CountryUndefinedException.class,
                () -> ew.SP(new ArrayList<>(List.of(new ArrayList<>(List.of("ES", "XX")))))
        );

        Assertions.assertTrue(thrown.getMessage().equals("Some ISO-3166-Alpha2 references for the paths are " +
                "incorrect or not established in the Class."));
    }

    /**
     * Tests that the method SP() from the EWarningDNM properly throws Exception when the input is wrong.
     * This test will check that the paths passed as argument are real ISO-3166-Alpha2 and contained in the list of
     * countries to be studied.
     * @author Angel Fragua
     */
    @Test
    void SP6() {
        EWarningDNM ew = new EWarningDNM(this.db, LocalDate.of(2020, 1, 25), LocalDate.of(2020, 3, 1),
                                         new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "UA")),
                                         0, "spearman", false);
        ew.checkWindows();

        Exception thrown = Assertions.assertThrows(
                CountryUndefinedException.class,
                () -> ew.SP(new ArrayList<>(List.of(
                                                new ArrayList<>(List.of("ES", "GB")),
                                                new ArrayList<>(List.of("FR", "IT")))))
        );

        Assertions.assertTrue(thrown.getMessage().equals("Some ISO-3166-Alpha2 references for the paths are " +
                "incorrect or not established in the Class."));
    }
}
