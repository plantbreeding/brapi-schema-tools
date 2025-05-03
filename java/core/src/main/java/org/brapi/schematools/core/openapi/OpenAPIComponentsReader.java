package org.brapi.schematools.core.openapi;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.brapi.schematools.core.response.Response;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.find;
import static org.brapi.schematools.core.response.Response.fail;

/**
 * Utility class for reading OpenAPI Components from YAML files.
 */
public class OpenAPIComponentsReader {

    /**
     * Read OpenAPI Components from YAML files.
     * @param schemaDirectory The path to the directory containing the YAML files.
     * @return OpenAPI Components read from YAML files.
     */
    public Response<Components> readComponents(Path schemaDirectory) {

        Components components = new Components();
        try {
            return find(schemaDirectory, 3, this::schemaPathMatcher).map(this::readComponentFile).collect(Response.toList())
                .onSuccessDoWithResult(result -> result.forEach(c -> merge(components, c)))
                .merge(Response.success(components));
        } catch (IOException | RuntimeException e) {
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    private void merge(Components toComponents, Components fromComponents) {
        if (fromComponents.getParameters() != null) {
            fromComponents.getParameters().forEach(toComponents::addParameters);
        }

        if (fromComponents.getResponses() != null) {
            fromComponents.getResponses().forEach(toComponents::addResponses);
        }

        if (fromComponents.getSchemas() != null) {
            fromComponents.getSchemas().forEach(toComponents::addSchemas);
        }

        if (fromComponents.getSecuritySchemes() != null) {
            fromComponents.getSecuritySchemes().forEach(toComponents::addSecuritySchemes);
        }
    }

    private Response<Components> readComponentFile(Path path) {

        SwaggerParseResult result = new OpenAPIParser().readLocation(path.toFile().getPath(), null, null);

        return Response.success(result.getOpenAPI().getComponents()) ;
    }

    private boolean schemaPathMatcher(Path path, BasicFileAttributes basicFileAttributes) {
        return basicFileAttributes.isRegularFile() && path.toString().endsWith(".yaml");
    }
}
