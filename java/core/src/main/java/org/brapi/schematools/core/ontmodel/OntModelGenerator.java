package org.brapi.schematools.core.ontmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.ontmodel.metadata.OntModelGeneratorMetadata;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.response.Response;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates an Ontology Model from a BrAPI Json Schema.
 */
@AllArgsConstructor
public class OntModelGenerator {

    private final BrAPISchemaReader schemaReader;
    private final OntModelGeneratorOptions options;

    /**
     * Creates a OntModelGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link OntModelGeneratorOptions}.
     */
    public OntModelGenerator() {
        this(new BrAPISchemaReader(), OntModelGeneratorOptions.load()) ;
    }

    /**
     * Creates a OntModelGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link OntModelGeneratorOptions}.
     * @param options The options to be used in the generation.
     */
    public OntModelGenerator(OntModelGeneratorOptions options) {
        this(new BrAPISchemaReader(), options) ;
    }

    /**
     * Generates the {@link OntModel} from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @return the {@link OntModel} from the complete BrAPI Specification
     */
    public Response<OntModel> generate(Path schemaDirectory) {
        return generate(schemaDirectory, new OntModelGeneratorMetadata()) ;
    }

    /**
     * Generates the RDF {@link OntModel} from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @param metadata additional metadata that is used in the generation
     * @return the RDF {@link OntModel} from the complete BrAPI Specification
     */
    public Response<OntModel> generate(Path schemaDirectory, OntModelGeneratorMetadata metadata) {
        return options.validate().asResponse().merge(
            schemaReader.readDirectories(schemaDirectory).mapResultToResponse(brAPISchemas -> new Generator(options, metadata, brAPISchemas).generate())) ;
    }

    @Getter
    private static class Generator {
        private final OntModelGeneratorOptions options;
        private final OntModelGeneratorMetadata metadata;
        private final Map<String, BrAPIClass> brAPISchemas;
        private String namespace = "http://basf.org/";

        public Generator(OntModelGeneratorOptions options, OntModelGeneratorMetadata metadata, List<BrAPIClass> brAPISchemas) {
            this.options = options;
            this.metadata = metadata;
            Map<String, BrAPIClass> map = new HashMap<>();
            for (BrAPIClass brAPISchema : brAPISchemas) {
                if (map.put(brAPISchema.getName(), brAPISchema) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            this.brAPISchemas = map;
        }

        public Response<OntModel> generate() {
            OntModel model = OntModelFactory.createModel();

            brAPISchemas.values().forEach(schema -> createClass(model, schema));

            return Response.success(model) ;
            //return brAPISchemas.values().stream().
             ///   map(this::createSchema);
        }

        private void createClass(OntModel model, BrAPIClass schema) {
            OntClass.Named ontClass = model.createOntClass(namespace + schema.getName());

            //ontClass.addProperty()
        }
    }

}
