package earlywarn.signals;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {
    @Context
    public GraphDatabaseService db;

    @UserFunction
    @Description("Test query")
    public String test(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                       @Name("windowSize") long windowSize, @Name("cumulativeData") boolean cumulativeData) {
        EWarningGeneral eWarn = new EWarningDNM(this.db, startDay, endDay, (int) windowSize, cumulativeData);
        eWarn.checkWindows();
        return eWarn.toString();
    }

    @UserFunction
    @Description("Abbreviation Density Function")
    public List<Double> density1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                                 @Name("cumulativeData") boolean cumulativeData,
                                 @Name("squareRootData") boolean squareRootData, @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, cumulativeData, squareRootData,
                                                      threshold);
        eWarn.checkWindows();
        return eWarn.density();
    }

    @UserFunction
    @Description("Complete Density Function")
    public List<Double> density(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                                @Name("countries") List<String> countries, @Name("windowSize") long windowSize,
                                @Name("correlation") String correlation, @Name("cumulativeData") boolean cumulativeData,
                                @Name("squareRootData") boolean squareRootData, @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, countries, (int) windowSize,
                                                      correlation, cumulativeData, squareRootData, threshold);
        eWarn.checkWindows();
        return eWarn.density();
    }

    @UserFunction
    @Description("Abbreviation Clustering Coefficient Function")
    public List<Double> clusteringCoefficient1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                                               @Name("cumulativeData") boolean cumulativeData,
                                               @Name("squareRootData") boolean squareRootData,
                                               @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, cumulativeData, squareRootData,
                                                      threshold);
        eWarn.checkWindows();
        return eWarn.clusteringCoefficient();
    }

    @UserFunction
    @Description("Complete Clustering Coefficient Function")
    public List<Double> clusteringCoefficient(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                                              @Name("countries") List<String> countries,
                                              @Name("windowSize") long windowSize,
                                              @Name("correlation") String correlation,
                                              @Name("cumulativeData") boolean cumulativeData,
                                              @Name("squareRootData") boolean squareRootData,
                                              @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, countries, (int) windowSize,
                                                      correlation, cumulativeData, squareRootData, threshold);
        eWarn.checkWindows();
        return eWarn.clusteringCoefficient();
    }

    @UserFunction
    @Description("Abbreviation Degree Assortativity Coefficient Function")
    public List<Double> assortativityCoefficient1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                                                  @Name("cumulativeData") boolean cumulativeData,
                                                  @Name("squareRootData") boolean squareRootData,
                                                  @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, cumulativeData, squareRootData,
                                                      threshold);
        eWarn.checkWindows();
        return eWarn.assortativityCoefficient();
    }

    @UserFunction
    @Description("Complete Degree Assortativity Coefficient Function")
    public List<Double> assortativityCoefficient(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                                                 @Name("countries") List<String> countries,
                                                 @Name("windowSize") long windowSize,
                                                 @Name("correlation") String correlation,
                                                 @Name("cumulativeData") boolean cumulativeData,
                                                 @Name("squareRootData") boolean squareRootData,
                                                 @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, countries, (int) windowSize,
                                                      correlation, cumulativeData, squareRootData, threshold);
        eWarn.checkWindows();
        return eWarn.assortativityCoefficient();
    }

    @UserFunction
    @Description("Abbreviation Number of Edges Function")
    public List<Long> numberEdges1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                                     @Name("cumulativeData") boolean cumulativeData,
                                     @Name("squareRootData") boolean squareRootData,
                                     @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, cumulativeData, squareRootData,
                                                      threshold);
        eWarn.checkWindows();
        return eWarn.numberEdges();
    }

    @UserFunction
    @Description("Complete Number of Edges Function")
    public List<Long> numberEdges(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                                    @Name("countries") List<String> countries, @Name("windowSize") long windowSize,
                                    @Name("correlation") String correlation,
                                    @Name("cumulativeData") boolean cumulativeData,
                                    @Name("squareRootData") boolean squareRootData,
                                    @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, countries, (int) windowSize,
                                                      correlation, cumulativeData, squareRootData, threshold);
        eWarn.checkWindows();
        return eWarn.numberEdges();
    }

    @UserFunction
    @Description("Abbreviation Preparedness Risk Score (PRS) Function")
    public List<Long> PRS1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                           @Name("cumulativeData") boolean cumulativeData,
                           @Name("squareRootData") boolean squareRootData, @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, cumulativeData, squareRootData,
                                                      threshold);
        eWarn.checkWindows();
        return eWarn.PRS();
    }

    @UserFunction
    @Description("Complete Preparedness Risk Score (PRS) Function")
    public List<Long> PRS(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                          @Name("countries") List<String> countries, @Name("windowSize") long windowSize,
                          @Name("correlation") String correlation, @Name("cumulativeData") boolean cumulativeData,
                          @Name("squareRootData") boolean squareRootData, @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, countries, (int)windowSize,
                                                      correlation, cumulativeData, squareRootData, threshold);
        eWarn.checkWindows();
        return eWarn.PRS();
    }

    @UserFunction
    @Description("Abbreviation Forman Ricci Curvature Function")
    public List<Double> formanRicciCurvature1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                                              @Name("cumulativeData") boolean cumulativeData,
                                              @Name("squareRootData") boolean squareRootData,
                                              @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, cumulativeData, squareRootData,
                                                      threshold);
        eWarn.checkWindows();
        return eWarn.formanRicciCurvature();
    }

    @UserFunction
    @Description("Complete Forman Ricci Curvature Function")
    public List<Double> formanRicciCurvature(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                                             @Name("countries") List<String> countries,
                                             @Name("windowSize") long windowSize,
                                             @Name("correlation") String correlation,
                                             @Name("cumulativeData") boolean cumulativeData,
                                             @Name("squareRootData") boolean squareRootData,
                                             @Name("threshold") double threshold) {
        EWarningSpecific eWarn = new EWarningSpecific(this.db, startDay, endDay, countries, (int) windowSize,
                                                      correlation, cumulativeData, squareRootData, threshold);
        eWarn.checkWindows();
        return eWarn.formanRicciCurvature();
    }

    /* Dynamical Network Markers (DNM) */
    @UserFunction
    @Description("Abbreviation Minimum Spanning Tree - Dynamic Network Marker (MSP-DNM) Function")
    public List<Double> MST1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                             @Name("windowSize") long windowSize, @Name("cumulativeData") boolean cumulativeData) {
        EWarningDNM eWarn = new EWarningDNM(this.db, startDay, endDay, (int) windowSize, cumulativeData);
        eWarn.checkWindows();
        return eWarn.MST();
    }

    @UserFunction
    @Description("Complete Minimum Spanning Tree - Dynamic Network Marker (MSP-DNM) Function")
    public List<Double> MST(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                            @Name("countries") List<String> countries, @Name("windowSize") long windowSize,
                            @Name("correlation") String correlation, @Name("cumulativeData") boolean cumulativeData) {
        EWarningDNM eWarn = new EWarningDNM(this.db, startDay, endDay, countries, (int) windowSize, correlation,
                                            cumulativeData);
        eWarn.checkWindows();
        return eWarn.MST();
    }

    @UserFunction
    @Description("Abbreviation Shortest Path - Dynamic Network Marker (SP-DNM) Function")
    public List<List<Double>> SP1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                                  @Name("windowSize") long windowSize, @Name("cumulativeData") boolean cumulativeData) {
        EWarningDNM eWarn = new EWarningDNM(this.db, startDay, endDay, (int) windowSize, cumulativeData);
        eWarn.checkWindows();
        return eWarn.SP(new ArrayList<>());
    }

    @UserFunction
    @Description("Complete Shortest Path - Dynamic Network Marker (SP-DNM) Function")
    public List<List<Double>> SP(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                                 @Name("countries") List<String> countries, @Name("windowSize") long windowSize,
                                 @Name("correlation") String correlation,
                                 @Name("cumulativeData") boolean cumulativeData,
                                 @Name("paths") List<List<String>> paths) {
        EWarningDNM eWarn = new EWarningDNM(this.db, startDay, endDay, countries, (int) windowSize, correlation,
                                            cumulativeData);
        eWarn.checkWindows();
        return eWarn.SP(paths);
    }

    @UserFunction
    @Description("Abbreviation Landscape - Dynamic Network Marker (L-DNM) Function")
    public List<List<Double>> landscape1(@Name("startDay") LocalDate startDay, @Name("endDay")LocalDate endDay,
                                         @Name("windowSize") long windowSize,
                                         @Name("cumulativeData") boolean cumulativeData) {
        EWarningLDNM eWarn = new EWarningLDNM(this.db, startDay, endDay, (int) windowSize, cumulativeData);
        eWarn.checkWindows();
        return eWarn.landscape();
    }

    @UserFunction
    @Description("Complete Landscape - Dynamic Network Marker (L-DNM) Function")
    public List<List<Double>> landscape(@Name("startDay") LocalDate startDay, @Name("endDay") LocalDate endDay,
                                        @Name("countries") List<String> countries, @Name("windowSize") long windowSize,
                                        @Name("correlation") String correlation,
                                        @Name("cumulativeData") boolean cumulativeData) {
        EWarningLDNM eWarn = new EWarningLDNM(this.db, startDay, endDay, countries, (int) windowSize, correlation,
                                              cumulativeData);
        eWarn.checkWindows();
        return eWarn.landscape();
    }
}
