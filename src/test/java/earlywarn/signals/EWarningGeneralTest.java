package earlywarn.signals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Assertions;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit Class used to test the class EWarningGeneral. It builds a temporal Neo4j database witch is loaded with
 * the nodes declared in the resources files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) /* This annotation is needed for creating a JUnit Class*/
public class EWarningGeneralTest {

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
     * Tests that the method checkDates() from the EWarningGeneral Class properly throws Exception when the dates are
     * wrong. The first test will check that the startDate is not grater than the endDate.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest1() {
        Exception thrown = Assertions.assertThrows(
                DateOutRangeException.class,
                () -> new EWarningGeneral(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 1, 15))
        );

        Assertions.assertTrue(thrown.getMessage().equals("<startDate> must be older than <endDate>."));
    }

    /**
     * Tests that the method checkDates() from the EWarningGeneral Class properly throws Exception when the dates are
     * wrong. This test will check that the startDate has Reports for each Country Node in the database.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest2() {
        Exception thrown = Assertions.assertThrows(
                DateOutRangeException.class,
                () -> new EWarningGeneral(this.db, LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 25))
        );

        Assertions.assertTrue(thrown.getMessage().equals("Dates out of range. [2020-01-22 , 2020-03-01] " +
                                                         "(year-month-day)"));
    }

    /**
     * Tests that the method checkDates() from the EWarningGeneral Class properly throws Exception when the dates are
     * wrong. This test will check that the endDate has Reports for each Country Node in the database.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest3() {
        Exception thrown = Assertions.assertThrows(
                DateOutRangeException.class,
                () -> new EWarningGeneral(this.db, LocalDate.of(2020, 1, 25), LocalDate.of(2020, 4, 5))
        );

        Assertions.assertTrue(thrown.getMessage().equals("Dates out of range. [2020-01-22 , 2020-03-01] " +
                "(year-month-day)"));
    }

    /**
     * Tests that the method checkDates() from the EWarningGeneral Class is properly executed with no errors.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest4() {
        LocalDate startDate = LocalDate.of(2020, 1, 22);
        LocalDate endDate = LocalDate.of(2020, 3, 1);

        EWarningGeneral ew = new EWarningGeneral(this.db, startDate, endDate);

        assertThat(ew.startDate).isEqualTo(startDate);
        assertThat(ew.endDate).isEqualTo(endDate);
    }

    /**
     * Tests that the method checkDates() from the EWarningGeneral Class properly throws Exception when the dates are
     * wrong. This test will check that the interval of dates between the start date and the end date are equal or
     * greater than the window size.
     * @author Angel Fragua
     */
    @Test
    void checkDatesTest5() {

        Exception thrown = Assertions.assertThrows(
                DateOutRangeException.class,
                () -> new EWarningGeneral(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 2, 3))
        );

        Assertions.assertTrue(thrown.getMessage().equals("The interval between the first report date in the database " +
                "and the <endDate> must be equal or greater than <windowSize>."));
    }

    /**
     * Tests that the method checkCountries() from the EWarningGeneral Class properly throws Exception when the list of
     * country's references in the ISO-3166-Alpha2 format is incorrect. This test check that there are at least two
     * different countries in the countries list.
     * @author Angel Fragua
     */
    @Test
    void checkCountriesTest1() {
        Exception thrown = Assertions.assertThrows(
                CountryUndefinedException.class,
                () -> new EWarningGeneral(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                                          new ArrayList<>(Arrays.asList("ES","ES")), 14, "pearson")
        );

        Assertions.assertTrue(thrown.getMessage().equals("There must be at least two different ISO-3166-Alpha2 " +
                                                         "country references in <countries> and must be contained " +
                                                         "in the database."));
    }

    /**
     * Tests that the method checkCountries() from the EWarningGeneral Class properly throws Exception when the list of
     * country's references in the ISO-3166-Alpha2 format is incorrect. This test check that there are more than two
     * countries in the countries list.
     * @author Angel Fragua
     */
    @Test
    void checkCountriesTest2() {
        Exception thrown = Assertions.assertThrows(
                CountryUndefinedException.class,
                () -> new EWarningGeneral(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                        new ArrayList<>(Arrays.asList("ES")), 14, "pearson")
        );

        Assertions.assertTrue(thrown.getMessage().equals("There must be at least two different ISO-3166-Alpha2 " +
                                                         "country references in <countries> and must be contained " +
                                                         "in the database."));
    }

    /**
     * Tests that the method checkCountries() from the EWarningGeneral Class properly throws Exception when the list of
     * country's references in the ISO-3166-Alpha2 format is incorrect. This test check that incorrect country
     * references in the ISO-3166-Alpha2 format not contained in the database are detected.
     * @author Angel Fragua
     */
    @Test
    void checkCountriesTest3() {
        Exception thrown = Assertions.assertThrows(
                CountryUndefinedException.class,
                () -> new EWarningGeneral(this.db, LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                        new ArrayList<>(Arrays.asList("ES","FR","AL","GB","ERROR")), 14, "pearson")
        );

        Assertions.assertTrue(thrown.getMessage().equals("All ISO-3166-Alpha2 country references in <countries> must " +
                                                         "exist and be contained in the database. Errors: [ERROR]"));
    }

    /**
     * Tests that the method checkWindows() from the EWarningGeneral Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. First test will use the first date with Report.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest1() {
        EWarningGeneral ew = new EWarningGeneral(this.db,  LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1));
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 4));
    }

    /**
     * Tests that the method checkWindows() from the EWarningGeneral Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. First test will use a date between the first possible date and as many days passed as the
     * windowSize.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest2() {
        EWarningGeneral ew = new EWarningGeneral(this.db,  LocalDate.of(2020, 1, 26), LocalDate.of(2020, 3, 1));
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 4));
    }

    /**
     * Tests that the method checkWindows() from the EWarningGeneral Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. Last test will use a date that doesn't need any changes.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest3() {
        EWarningGeneral ew = new EWarningGeneral(this.db,  LocalDate.of(2020, 2, 10), LocalDate.of(2020, 3, 1));
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 10));
    }

    /**
     * Tests that the method checkWindows() from the EWarningGeneral Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. Same to checkWindowTest1() with different windowSize.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest4() {
        EWarningGeneral ew = new EWarningGeneral(this.db,  LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1),
                                                 new ArrayList<>(Arrays.asList("ES","FR","AL","GB")), 7, "pearson");
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 1, 28));
    }

    /**
     * Tests that the method checkWindows() from the EWarningGeneral Class properly changes the startDate based on the
     * dates with Report Node in the database. It assures that there are enough previous Reports to the startDate based
     * on the windows size. Same to checkWindowTest2() with different windowSize.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest5() {
        EWarningGeneral ew = new EWarningGeneral(this.db,  LocalDate.of(2020, 1, 26), LocalDate.of(2020, 3, 1),
                                                 new ArrayList<>(Arrays.asList("ES","FR","AL","GB")), 7, "pearson");
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 1, 28));
    }

    /**
     * Tests that the method checkWindows() from the EWarningGeneral Class properly changes the startDate based on the
     * dates with Report Node in the database. Same to checkWindowTest3() with different windowSize.
     * @author Angel Fragua
     */
    @Test
    void checkWindowTest6() {
        EWarningGeneral ew = new EWarningGeneral(this.db,  LocalDate.of(2020, 2, 10), LocalDate.of(2020, 3, 1),
                                                 new ArrayList<>(Arrays.asList("ES","FR","AL","GB")), 7, "pearson");
        ew.checkWindows();

        assertThat(ew.startDate).isEqualTo(LocalDate.of(2020, 2, 10));
    }
}
