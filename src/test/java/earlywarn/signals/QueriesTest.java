package earlywarn.signals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit Class used to test the class Queries. It builds a temporal Neo4j database with is loaded with the nodes
 * declared in the resources files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) /* This annotation is needed for creating a JUnit Class*/
public class QueriesTest {

    private Neo4j embeddedDatabaseServer;
    private Queries queries;

    /**
     * Initialize a temporal Neo4j instance Database for the current Class tests.
     * It reads a file containing the queries for the creation of some Country Nodes. It also reads a file with the
     * queries needed to create some Report Nodes of the previous Country Nodes between the date 22-1-2020 and 1-3-2020.
     * Last it creates execute a query that creates a Relationship between each Country Node and its corresponding
     * Report Nodes. Furthermore, it creates an instance of the Queries Class to be tested.
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

        this.queries = new Queries(this.embeddedDatabaseServer.defaultDatabaseService());
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
     * Tests that the minReportDate() actually returns the first Report date of the Database.
     * @author Angel Fragua
     */
    @Test
    void minDateTest() {
        assertThat(this.queries.minReportDate()).isEqualTo(LocalDate.of(2020, 1, 22));
    }

    /**
     * Tests that the maxReportDate() actually returns the last Report date of the Database.
     * @author Angel Fragua
     */
    @Test
    void maxDateTest() {
        assertThat(this.queries.maxReportDate()).isEqualTo(LocalDate.of(2020, 3, 1));
    }

    /**
     * Tests that getReportConfirmed() gets the list of confirmed covid cases between two random dates for three
     * random countries Spain (ES), France (FR) and United Kingdom (GB).
     * @author Angel Fragua
     */
    @Test
    void getReportConfirmedTest() {
        long[] confirmedES = new long[]{0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,6,13,15,32,
                45,84};
        long[] confirmedFR = new long[]{6,6,6,6,6,6,6,11,11,11,11,11,11,11,12};
        long[] confirmedGB = new long[]{22,23,23,28,30,34,37,44,56,61,94};
        assertThat(this.queries.getReportConfirmed("ES", LocalDate.of(2020, 1, 22), LocalDate.of(2020, 3, 1)))
                .isEqualTo(confirmedES);
        assertThat(this.queries.getReportConfirmed("FR", LocalDate.of(2020, 2, 1), LocalDate.of(2020, 2, 15)))
                .isEqualTo(confirmedFR);
        assertThat(this.queries.getReportConfirmed("GB", LocalDate.of(2020, 2, 20), LocalDate.of(2020, 3, 1)))
                .isEqualTo(confirmedGB);
    }

    /**
     * Tests that getConfirmedCountries() returns only the Set of ISO-3166-Alpha2 references of the countries asked for
     * and contained in the Database. The first test only pass a group of real country references.
     * @author Angel Fragua
     */
    @Test
    void countryTest1() {
        List<String> countries = new ArrayList<>(Arrays.asList("ES","FR","NL","AD","GB","PL"));
        Set<String> countriesSet = new HashSet<>(countries);

        Set<String> countriesResponse = this.queries.getConfirmedCountries(countries);
        assertThat(countriesResponse).isEqualTo(countriesSet);
    }

    /**
     * Tests that getConfirmedCountries() returns only the Set of ISO-3166-Alpha2 references of the countries asked for
     * and contained in the Database. The second test pass both a group of real country references, and a fake reference
     * which will not be returned.
     * @author Angel Fragua
     */
    @Test
    void countryTest2() {
        List<String> countries = new ArrayList<>(Arrays.asList("ES","FR","NL","AD","GB","PL","XX"));
        Set<String> countriesSet = new HashSet<>(countries);

        Set<String> countriesResponse = this.queries.getConfirmedCountries(countries);

        assertThat(countriesResponse).isNotEqualTo(countriesSet);
        assertThat(countriesResponse).doesNotContain("XX");
        assertThat(countriesResponse.size() + 1).isEqualTo(countriesSet.size());
        countriesResponse.add("XX");
        assertThat(countriesResponse).isEqualTo(countriesSet);
    }

    /**
     * Tests that getPopulation() returns the population of a random country of the Database.
     * @author Angel Fragua
     */
    @Test
    void getPopulation() {
        assertThat(this.queries.getPopulation("ES")).isEqualTo(46754783);
    }

    /**
     * Tests that getPopulation() returns the population of each country passed as argument of the Database.
     * @author Angel Fragua
     */
    @Test
    void getPopulations() {
        long[] populations = {46754783,65273512,2877800,67886004,25459700,206139587,126476458};
        assertThat(this.queries.getPopulation(new ArrayList<>(Arrays.asList("ES","FR","AL","GB","AU","NG","JP"))))
                .isEqualTo(populations);
    }
}
