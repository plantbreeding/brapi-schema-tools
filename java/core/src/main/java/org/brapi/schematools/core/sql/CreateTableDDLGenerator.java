package org.brapi.schematools.core.sql;

import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.response.Response;


public interface CreateTableDDLGenerator {
    Response<String> generateDDLForObjectType(BrAPIObjectType brAPIObjectType);
}
