package org.brapi.schematools.analyse;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dflib.DataFrame;
import org.dflib.Printers;
import org.dflib.builder.DataFrameArrayAppender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a tabular report from a list of AnalysisReports
 */
@NoArgsConstructor
public class TabularReportGenerator {
    private static final String[] COLUMN_NAMES = new String[]{
        "Name",
        "Entity",
        "Path",
        "Status Code",
        "Duration",
        "Message Key",
        "Message Level",
        "Message"};

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
        return Printers.tabular.toString(dataFrame);
    }

    private DataFrame generateReport(Map.Entry<String, List<AnalysisReport>> entry) {
        return generateReport(entry.getKey(), entry.getValue()) ;
    }

    private DataFrame generateReport(String name, List<AnalysisReport> reports) {
        DataFrameArrayAppender appender = DataFrame.byArrayRow(COLUMN_NAMES).appender();

        reports.forEach(analysisReport -> {
            if (analysisReport.getValidationReport().getMessages().isEmpty()) {
                appender.append(
                    analysisReport.getName(),
                    analysisReport.getEntityName(),
                    analysisReport.getRequest().getValidatorRequest().getPath(),
                    analysisReport.getStatusCode(),
                    DurationFormatUtils.formatDurationHMS(analysisReport.getTimeElapsed()),
                    null,
                    null,
                    null) ;
            } else {
                analysisReport.getValidationReport().getMessages().forEach(message -> {
                    appender.append(
                        analysisReport.getName(),
                        analysisReport.getEntityName(),
                        analysisReport.getRequest().getValidatorRequest().getPath(),
                        analysisReport.getStatusCode(),
                        DurationFormatUtils.formatDurationHMS(analysisReport.getTimeElapsed()),
                        message.getKey(),
                        message.getLevel(),
                        message.getMessage()) ;
                });
            }
        });

        return appender.toDataFrame().as(name) ;
    }
}
