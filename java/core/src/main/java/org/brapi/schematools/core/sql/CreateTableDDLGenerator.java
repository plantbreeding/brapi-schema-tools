package org.brapi.schematools.core.sql;

import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.response.Response;

import java.util.List;


public interface CreateTableDDLGenerator {
    Response<String> generateDDLForObjectType(BrAPIObjectType brAPIObjectType);

    Response<String> generateDropScript();

    Response<String> generateForeignKeyConstraintScript();
}
