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
 * JUnit Class used to test the class EWarningLDNM. It builds a temporal Neo4j database witch is loaded with
 * the nodes declared in the resources files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) /* This annotation is needed for creating a JUnit Class*/
public class EWarningLDNMTest {

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
     * Tests that the method landscape() from the EWarningLDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and windows size is set to zero. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void landscape1() {
        List<List<Double>> landscapeDNMs = new ArrayList<>();
        for (int i = 0; i < EWarningGeneral.countriesDefault.size(); i++) {
            landscapeDNMs.add(new ArrayList<>(List.of(0.000011863036681,0.,0.,0.000007642530867,0.000006034859571,
                    0.000009953238075,0.000000221066513,0.000102550508965,0.000088403409273,0.000001129690545,
                    0.000023687043113,0.000013444386873,0.000001570611236,0.000000682750741,0.00000187341205,
                    0.000016846456327,0.000004822556097,0.000000528549688,0.000000818424048,0.000000562115942,
                    0.000000455110661,0.000000375205925,0.000000472706489,0.000000245752487,0.000000265087771,
                    0.000000193461975,0.000000206419309,0.000000148622063,0.000019367030426,0.00003227135604,
                    0.000066569803422,0.000016939376628,0.001318208722786,0.007778593570201,0.013559930606921,
                    0.007742223144324,0.016110445922347,0.061287925180331)));
        }

        EWarningLDNM ew = new EWarningLDNM(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                EWarningGeneral.countriesDefault, 0, "kendall", false);
        ew.checkWindows();

        assertThat(
                ew.landscape().stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
                .isEqualTo(landscapeDNMs
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
    }

    /**
     * Tests that the method landscape() from the EWarningLDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = false which means that the data will be converted from the cumulative covid
     * cases to daily cases, and windows size is set to a positive value. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void landscape2() {
        List<List<Double>> landscapeDNMs = new ArrayList<>();
        for (int i = 0; i < EWarningGeneral.countriesDefault.size(); i++) {
            landscapeDNMs.add(new ArrayList<>(List.of(0.000000013161801,0.,0.000005912536171,0.000057556783082,
                    0.000002826621263,0.000000105682256,0.000001620965031,0.0000099122146,0.000000063806636,
                    0.000003496999266,0.000038837435061)));
        }

        EWarningLDNM ew = new EWarningLDNM(this.db, LocalDate.of(2020, 1, 28), LocalDate.of(2020, 2, 15),
                EWarningGeneral.countriesDefault, 14, "pearson", false);
        ew.checkWindows();

        assertThat(
                ew.landscape()
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
                .isEqualTo(landscapeDNMs
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
    }

    /**
     * Tests that the method landscape() from the EWarningLDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and windows size is set to zero. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void landscape3() {
        List<List<Double>> landscapeDNMs = new ArrayList<>();
        for (int i = 0; i < EWarningGeneral.countriesDefault.size(); i++) {
            landscapeDNMs.add(new ArrayList<>(List.of(0.000266918325322,0.000609081192421,0.000018601351863,
                    0.000029240395202,0.000076492312918,0.000010137063512,0.000003121619645,0.000003473536302,
                    0.000031572406594,0.00001717419956,0.000006368192246,0.000004576305073,0.000002625915345,
                    0.000001641195599,0.000001139032885,0.000001013287448,0.000000611780599,0.000000629225697,
                    0.000000446130553,0.000000464875362,0.000000613468751,0.000010278246686)));
        }

        EWarningLDNM ew = new EWarningLDNM(this.db, LocalDate.of(2020, 1, 31), LocalDate.of(2020, 2, 21),
                EWarningGeneral.countriesDefault, 0, "spearman", true);
        ew.checkWindows();

        assertThat(
                ew.landscape()
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
                .isEqualTo(landscapeDNMs
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
    }

    /**
     * Tests that the method landscape() from the EWarningLDNM returns the correct List with a 10 decimal precision.
     * This test checks cumulativeData = true which means that the data won't be converted from the cumulative covid
     * cases to daily cases, and windows size is set to a positive value. Rest of parameters are being changed to assure
     * its correctness.
     * @author Angel Fragua
     */
    @Test
    void landscape4() {
        List<List<Double>> landscapeDNMs = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            landscapeDNMs.add(new ArrayList<>(List.of(0.004480301562216,0.011752350148134,0.000860539608296,
                    0.001583483616029,0.037615215671743,0.011256133196882,0.019398643416164,0.005384043864648,
                    0.000029331272521,0.000064827691768,0.00071055911702,0.02426952489304,0.006655255433282,
                    0.000082002717571,0.000119059685632,0.000048550162268,0.000054577086739,0.000714100504822)));
        }

        EWarningLDNM ew = new EWarningLDNM(this.db, LocalDate.of(2020, 2, 3), LocalDate.of(2020, 2, 20),
                new ArrayList<>(Arrays.asList("AL", "BE", "FR", "ES", "SE", "CH", "GB", "UA")), 7, "pearson", true);
        ew.checkWindows();

        assertThat(
                ew.landscape()
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()))
                .isEqualTo(landscapeDNMs
                        .stream()
                        .map(x -> x.stream().map(y -> round(y, 10)).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
    }
}
