package org.brapi.schematools.core.sql;

import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.response.Response;

import java.util.List;


public interface CreateTableDDLGenerator {
    Response<String> generateDDLForObjectType(BrAPIObjectType brAPIObjectType);

    Response<String> generateDropScript();

    Response<String> generateForeignKeyConstraintScript();

    /**
     * Returns and clears any controlled-vocabulary / link-table scripts that were staged
     * for separate output files during primary-table generation.
     */
    Response<List<SeparateTableScript>> drainSeparateTableScripts();

    /**
     * A SQL script intended for its own output file.
     * @param fileName base file name without path (for example {@code ObservationLevels.sql})
     * @param title optional header title written as a leading comment line
     * @param sql the CREATE TABLE DDL body
     */
    record SeparateTableScript(String fileName, String title, String sql) {
    }
}
