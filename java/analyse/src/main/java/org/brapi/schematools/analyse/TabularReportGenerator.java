package org.brapi.schematools.analyse;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dflib.DataFrame;
import org.dflib.Printers;
import org.dflib.builder.DataFrameArrayAppender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.dflib.Exp.$col;
import static org.dflib.Exp.count;

/**
 * Generates a tabular report from a list of AnalysisReports
 */
public class TabularReportGenerator {
    private static final String[] COLUMN_NAMES = new String[]{
        "Name",
        "Entity",
        "Path",
        "URI",
        "Status Code",
        "Duration",
        "Message Key",
        "Message Level",
        "Message"};

    private static final String[] SUMMARY_COLUMN_NAMES = new String[]{
        "Entity",
        "Path", "Status Code", "Message Key", "Message Level",
        "Count"} ;

    private boolean summariseAcrossReports =
        false ;

    private DataFrame summary;

    private TabularReportGenerator() {

    }

    /**
     * Creates the default Generator
     * @return the default Generator
     */
    public static TabularReportGenerator generator() {
        return new TabularReportGenerator().clearSummary() ;
    }

    /**
     * Summarise across multiple reports
     * @return the writer for method chaining
     */
    public TabularReportGenerator summariseAcrossReports() {
        summariseAcrossReports = true ;
        return this;
    }

    /**
     * Clears the summary for generator reuse
     * @return the writer for method chaining
     */
    public TabularReportGenerator clearSummary() {
        summary = DataFrame.empty(SUMMARY_COLUMN_NAMES) ;
        return this;
    }

    /**
     * Clear the summary for generator reuse
     * @return the writer for method chaining
     */
    public DataFrame getSummary() {
        return summary.as("Summary") ;
    }

    /**
     * Get the column names in the report
     * @return the column names in the report
     */
    public List<String> getColumnNames() {
        return List.of(COLUMN_NAMES);
    }

    /**
     * Get the summary column names in the report
     * @return the summary column names in the report
     */
    public List<String> getSummaryColumnNames() {
        return List.of(SUMMARY_COLUMN_NAMES);
    }

    /**
     * Generates a DataFrame from a list of AnalysisReport
     * @param reports a list of AnalysisReport
     * @return a data frame
     */
    public DataFrame generateReport(List<AnalysisReport> reports) {
        return generateReport("Report", reports);
    }

    /**
     * Generates DataFrames from a list of AnalysisReports with a separate Data Frame per entity.
     * @param reports a list of AnalysisReport
     * @return a data frame
     */
    public List<DataFrame> generateReportByEntity(List<AnalysisReport> reports) {
        return reports.stream().collect(Collectors.groupingBy(AnalysisReport::getEntityName))
            .entrySet().stream().map(this::generateReport).toList() ;
    }

    /**
     * Generates a table from a list of AnalysisReport
     * @param reports a list of AnalysisReport
     * @return a table of the AnalysisReports
     */
    public String generateReportTable(List<AnalysisReport> reports) {
        DataFrame dataFrame = generateReport(reports);
        return Printers.tabular.print(dataFrame);
    }

    private DataFrame generateReport(Map.Entry<String, List<AnalysisReport>> entry) {
        return generateReport(entry.getKey(), entry.getValue()) ;
    }

    private DataFrame generateReport(String name, List<AnalysisReport> reports) {
        DataFrameArrayAppender appender = DataFrame.byArrayRow(COLUMN_NAMES).appender();

        reports.forEach(analysisReport -> {
            if (analysisReport.getValidationReport() == null || analysisReport.getValidationReport().getMessages().isEmpty()) {
                appender.append(
                    analysisReport.getName(),
                    analysisReport.getEntityName(),
                    analysisReport.getRequest().getValidatorRequest().getPath(),
                    analysisReport.getUri(),
                    analysisReport.getStatusCode(),
                    DurationFormatUtils.formatDurationHMS(analysisReport.getTimeElapsed()),
                    analysisReport.getErrorKey(),
                    analysisReport.getErrorLevel(),
                    analysisReport.getErrorMessage()) ;
            } else {
                analysisReport.getValidationReport().getMessages().forEach(message -> {
                    appender.append(
                        analysisReport.getName(),
                        analysisReport.getEntityName(),
                        analysisReport.getRequest().getValidatorRequest().getPath(),
                        analysisReport.getUri(),
                        analysisReport.getStatusCode(),
                        DurationFormatUtils.formatDurationHMS(analysisReport.getTimeElapsed()),
                        message.getKey(),
                        message.getLevel(),
                        message.getMessage()) ;
                });
            }
        });

        return addToSummary(appender.toDataFrame().as(name));
    }

    private DataFrame addToSummary(DataFrame dataFrame) {
        if (summariseAcrossReports) {
            DataFrame agg = dataFrame
                .group("Path", "Status Code", "Message Key", "Message Level")
                .cols(SUMMARY_COLUMN_NAMES)
                .agg(
                    $col("Entity").first(),
                    $col("Path").first(),
                    $col("Status Code").first(),
                    $col("Message Key").first(),
                    $col("Message Level").first(),
                    count());

            summary = summary.vConcat(agg) ;
        }

        return dataFrame ;
    }
}
