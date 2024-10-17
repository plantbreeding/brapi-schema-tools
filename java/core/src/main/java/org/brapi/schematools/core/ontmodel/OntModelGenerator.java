package org.brapi.schematools.core.ontmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Property;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.ontmodel.metadata.OntModelGeneratorMetadata;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.response.Response;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.success;

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
        private final OntModel model ;

        private final String namespace ;

        public Generator(OntModelGeneratorOptions options, OntModelGeneratorMetadata metadata, List<BrAPIClass> brAPISchemas) {
            this.options = options;
            this.metadata = metadata;
            namespace = metadata.getNamespace() ;

            model = OntModelFactory.createModel();
            Map<String, BrAPIClass> map = new HashMap<>();
            for (BrAPIClass brAPISchema : brAPISchemas) {
                if (map.put(brAPISchema.getName(), brAPISchema) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            this.brAPISchemas = map;
        }

        public Response<OntModel> generate() {
            return brAPISchemas.values().stream().map(this::createClass).collect(Response.toList()).map(() -> success(model));
        }

        private Response<OntClass> createClass(BrAPIClass schema) {
            if (schema instanceof BrAPIObjectType brAPIObjectType) {
                return createObjectType(brAPIObjectType) ;
            } else if (schema instanceof BrAPIOneOfType brAPIOneOfType) {
                return createOneOfType(brAPIOneOfType) ;
            } else if (schema instanceof BrAPIEnumType brAPIEnumType) {
                return createEnumType(brAPIEnumType) ;
            } 

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown output type '%s'", schema.getName()));
        }

        private Response<OntClass> createObjectType(BrAPIObjectType brAPIObjectType) {
            OntClass.Named ontClass = model.createOntClass(createURIForBrAPIClass(brAPIObjectType));

            return brAPIObjectType.getProperties().stream()
                .map(property -> createProperty(ontClass, property))
                .collect(Response.toList())
                .map(() -> success(ontClass));
        }

        private Response<Property> createProperty(OntClass.Named ontClass, BrAPIObjectProperty property) {

            Property ontProperty = null ;

            BrAPIType type = property.getType();

            if (type instanceof BrAPIObjectType brAPIObjectType) {
                ontProperty = model.createObjectProperty(createURIForBrAPIObjectProperty(ontClass, property));
                ontClass.addProperty(ontProperty, createURIForBrAPIClass(brAPIObjectType)) ;

            } else if (type instanceof BrAPIArrayType brAPIArrayType) {
                ontProperty = model.createObjectProperty(String.format("%s/%s", ontClass.getNameSpace(), property.getName()));
                //ontClass.addProperty(ontProperty, brAPIArrayType.getItems()) ;

            } else if (type instanceof BrAPIReferenceType) {

            } else if (type instanceof BrAPIEnumType) {

            } else if (type instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
                ontProperty = model.createObjectProperty(createURIForBrAPIObjectProperty(ontClass, property));

                String typeURI = switch (brAPIPrimitiveType.getName()) {
                        case "string" -> XSDDatatype.XSDstring.getURI();
                        case "integer" -> XSDDatatype.XSDinteger.getURI();
                        case "number" -> XSDDatatype.XSDdouble.getURI();
                        case "boolean" -> XSDDatatype.XSDboolean.getURI();
                        default -> null;
                    };

                if (typeURI == null) {
                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find XSDDatatype for '%s' in property '%s'", brAPIPrimitiveType.getName(), property.getName()));
                }

                ontClass.addProperty(ontProperty, typeURI) ;
            }

            return success(ontProperty) ;
        }

        private Response<OntClass> createOneOfType(BrAPIOneOfType brAPIOneOfType) {
            return success(model.createOntClass(namespace + brAPIOneOfType.getName()));
        }

        private Response<OntClass> createEnumType(BrAPIEnumType brAPIEnumType) {
            OntClass.Named ontClass = model.createOntClass(namespace + brAPIEnumType.getName());

            //OntClass.OneOf oneOf = ontClass.as(OntClass.OneOf.class);

           // oneOf.setComponents(brAPIEnumType.getValues().stream().map(this::createOntIndividual).toList()) ;

            return success(ontClass);
        }

        private OntIndividual createOntIndividual(BrAPIEnumValue enumValue) {
            return model.createIndividual(namespace + enumValue.getName()) ;
        }

        private String createURIForBrAPIObjectProperty(OntClass.Named ontClass, BrAPIObjectProperty property) {
            return String.format("%s/%s", ontClass.getURI(), property.getName()) ;
        }

        private String createURIForBrAPIClass(BrAPIClass brAPIClass) {
            return String.format("%s/%s", namespace, brAPIClass.getName()) ;
        }
    }
}
