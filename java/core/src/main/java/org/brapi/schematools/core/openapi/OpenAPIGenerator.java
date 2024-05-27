package org.brapi.schematools.core.openapi;

import graphql.schema.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import lombok.AllArgsConstructor;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderException;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.openapi.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;
import static org.brapi.schematools.core.utils.StringUtils.toPlural;

@AllArgsConstructor
public class OpenAPIGenerator {

    private final BrAPISchemaReader schemaReader;

    public OpenAPIGenerator() {
        this.schemaReader = new BrAPISchemaReader();
    }

    public Response<GraphQLSchema> generate(Path schemaDirectory, OpenAPIGeneratorOptions options) {

        try {
            return new GraphQLGenerator.Generator(options, schemaReader.readDirectories(schemaDirectory)).generate();
        } catch (BrAPISchemaReaderException e) {
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    public static class Generator {
        private final OpenAPIGeneratorOptions options;
        private final Map<String, BrAPIObjectType> brAPISchemas;

        public Generator(OpenAPIGeneratorOptions options, List<BrAPIObjectType> brAPISchemas) {
            this.options = options;
            this.brAPISchemas = brAPISchemas.stream().collect(Collectors.toMap(BrAPIObjectType::getName, Function.identity()));
        }

        public Response<OpenAPI> generate() {

            OpenAPI openAPI = new OpenAPI();

            return Response.empty().
                mergeOnCondition(options.isGeneratingEndpoint(),
                    () -> brAPISchemas.values().stream().
                        filter(type -> options.getSingleGet().getGeneratingFor().getOrDefault(type.getName(), false)).
                        map(type -> generateSingleGet(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createSingleGetName(type.getName()), pathItem);
                            })).collect(Response.toList())).
                mergeOnCondition(options.isGeneratingEndpointNameWithId(),
                    () -> brAPISchemas.values().stream().
                        filter(type -> options.getListGet().getGeneratingFor().getOrDefault(type.getName(), false)).
                        map(type -> generateListGet(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createListGetName(type.getName()), , pathItem);
                            })).collect(Response.toList())).
                map(() -> success(openAPI));

        }

        private String createEndpointName(String entityName) {
            return String.format("/%s", toParameterCase(toPlural(entityName))) ;
        }

        private String createEndpointNameWithId(String entityName) {
            return String.format("/%s/{%s}", toParameterCase(toPlural(entityName)), String.format(options.getIds().getNameFormat(), toParameterCase(name))) ;
        }

        public Response<PathItem> generateSingleEndpoint(BrAPIObjectType type) {
            PathItem pathItem = new PathItem() ;

            return success(pathItem) ;
        }

        public Response<PathItem> generateListEndpoint(BrAPIObjectType type) {
            PathItem pathItem = new PathItem() ;

            return success(pathItem) ;
        }
    }

}
