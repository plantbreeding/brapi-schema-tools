package org.brapi.schematools.core.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import lombok.AllArgsConstructor;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderException;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.openapi.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
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

    public Response<List<OpenAPI>> generate(Path schemaDirectory, OpenAPIGeneratorOptions options) {

        try {
            return new OpenAPIGenerator.Generator(options, schemaReader.readDirectories(schemaDirectory)).generate();
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

        public Response<List<OpenAPI>> generate() {
            if (options.isSeparatingByModule()) {
                return brAPISchemas.values().stream().
                    filter(type -> Objects.nonNull(type.getModule())).
                    collect(Collectors.groupingBy(BrAPIObjectType::getModule, toList())).
                    entrySet().stream().map(entry -> generate(entry.getKey(), entry.getValue())).
                    collect(Response.toList());
            } else {
                return generate("BrAPI", brAPISchemas.values()).mapResult(Collections::singletonList);
            }
        }

        private Response<OpenAPI> generate(String title, Collection<BrAPIObjectType> types) {

            OpenAPI openAPI = new OpenAPI();

            Info info = new Info();

            info.setTitle(title);

            openAPI.setInfo(info);

            return Response.empty().
                mergeOnCondition(options.isGeneratingEndpoint(),
                    () -> types.stream().
                        filter(type -> options.getSingleGet().getGeneratingFor().getOrDefault(type.getName(), false)).
                        map(type -> generateEndpoint(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createEndpointNameWithId(type.getName()), pathItem);
                            })).collect(Response.toList())).
                mergeOnCondition(options.isGeneratingEndpointNameWithId(),
                    () -> types.stream().
                        filter(type -> options.getListGet().getGeneratingFor().getOrDefault(type.getName(), false)).
                        map(type -> generateSingleEndpointWithId(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createEndpointName(type.getName()), pathItem);
                            })).collect(Response.toList())).
                map(() -> success(openAPI));

        }

        private String createEndpointName(String entityName) {
            return String.format("/%s", toParameterCase(toPlural(entityName)));
        }

        public Response<PathItem> generateEndpoint(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();


            return success(pathItem);
        }

        private String createEndpointNameWithId(String entityName) {
            return String.format("/%s/{%s}", toParameterCase(toPlural(entityName)), String.format(options.getIds().getNameFormat(), toParameterCase(entityName)));
        }

        public Response<PathItem> generateSingleEndpointWithId(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            return success(pathItem);
        }


    }

}
