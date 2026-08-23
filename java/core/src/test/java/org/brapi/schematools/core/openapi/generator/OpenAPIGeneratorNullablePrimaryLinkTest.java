package org.brapi.schematools.core.openapi.generator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.brapi.schematools.core.openapi.generator.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAPIGeneratorNullablePrimaryLinkTest {

    @Test
    void generateObservationUsesIdLinksForNullableManyToOnePrimaryModel() throws URISyntaxException {
        Response<List<OpenAPI>> specifications = new OpenAPIGenerator(
            OpenAPIGeneratorOptions.load().setSeparateByModule(false))
            .generate(
                Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()),
                List.of("Observation", "ObservationVariable", "Season", "ObservationUnit", "Germplasm", "Study"));

        assertFalse(specifications.hasErrors(), () -> specifications.getMessagesCombined(","));
        assertNotNull(specifications.getResult());
        assertFalse(specifications.getResult().isEmpty());

        OpenAPI openAPI = specifications.getResult().getFirst();
        Schema requestSchema = openAPI.getComponents().getSchemas().get("ObservationNewRequest");
        assertNotNull(requestSchema, "ObservationNewRequest schema should be generated");

        @SuppressWarnings("unchecked")
        Map<String, Schema> properties = requestSchema.getProperties();
        assertNotNull(properties);

        assertTrue(properties.containsKey("observationVariableDbId"),
            "nullable many-to-one primary model should project observationVariableDbId");
        assertTrue(properties.containsKey("observationVariableName"),
            "nullable many-to-one primary model should project observationVariableName");
        assertFalse(properties.containsKey("observationVariable"),
            "nullable many-to-one primary model must not embed observationVariable object");

        assertNullable((Schema) properties.get("observationVariableDbId"));
        assertNullable((Schema) properties.get("observationVariableName"));

        // Non-primary many-to-one remains embedded and nullable
        assertTrue(properties.containsKey("season"));
        assertFalse(properties.containsKey("seasonDbId"));
        assertNullable((Schema) properties.get("season"));
    }

    private static void assertNullable(Schema schema) {
        assertNotNull(schema);
        boolean nullableFlag = Boolean.TRUE.equals(schema.getNullable());
        boolean nullType = schema.getTypes() != null && schema.getTypes().contains("null");
        assertTrue(nullableFlag || nullType, "projected link field should remain nullable");
    }
}
