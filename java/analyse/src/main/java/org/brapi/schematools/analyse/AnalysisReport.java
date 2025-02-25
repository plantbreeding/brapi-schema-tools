package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.report.ValidationReport;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Holds all the information from an Endpoint analysis.
 */
@Value
@Builder
public class AnalysisReport {
    APIRequest request;
    LocalDateTime startTime;
    LocalDateTime endTime;
    int statusCode;
    ValidationReport validationReport ;

    /**
     * Get the time elapsed from the start of the analysis to it completion in milliseconds
     * @return time elapsed from the start of the analysis to it completion in milliseconds
     */
    public long getTimeElapsed() {
        return endTime.atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli() -
            startTime.atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli() ;
    }

    /**
     * Gets the name of the report
     * @return the name of the report
     */
    public String getName() {
        return request.getName() ;
    }

    /**
     * Gets the entity name of the report
     * @return the entity name of the report
     */
    public String getEntityName() {
        return request.getEntityName() ;
    }
}
