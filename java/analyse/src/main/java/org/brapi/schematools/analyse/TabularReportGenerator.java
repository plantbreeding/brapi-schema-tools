package org.brapi.schematools.analyse;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dflib.DataFrame;
import org.dflib.Printers;
import org.dflib.builder.DataFrameArrayAppender;

import java.util.List;

/**
 * Generates a tabular report from a list of AnalysisReports
 */
@NoArgsConstructor
public class TabularReportGenerator {
    private static final String[] COLUMN_NAMES = new String[]{
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
        DataFrameArrayAppender appender = DataFrame.byArrayRow(COLUMN_NAMES).appender();

        reports.forEach(analysisReport -> {
            if (analysisReport.getValidationReport().getMessages().isEmpty()) {
                appender.append(
                    analysisReport.getEndpoint().getPath(),
                    analysisReport.getStatusCode(),
                    DurationFormatUtils.formatDurationHMS(analysisReport.getTimeElapsed()),
                    null,
                    null,
                    null) ;
            } else {
                analysisReport.getValidationReport().getMessages().forEach(message -> {
                    appender.append(
                        analysisReport.getEndpoint().getPath(),
                        analysisReport.getStatusCode(),
                        DurationFormatUtils.formatDurationHMS(analysisReport.getTimeElapsed()),
                        message.getKey(),
                        message.getLevel(),
                        message.getMessage()) ;
                });
            }
        });

        return appender.toDataFrame().as("Report") ;
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
}
