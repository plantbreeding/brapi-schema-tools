package org.brapi.schematools.core.ontmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.model.*;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.ontmodel.metadata.OntModelGeneratorMetadata;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.response.Response;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.brapi.schematools.core.response.Response.success;

/**
 * Generates an Ontology Model from a BrAPI JSON Schema.
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
        this(OntModelGeneratorOptions.load()) ;
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
     * the BrAPI JSON schema and BrAPI-Common that contains common schemas
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
     * the BrAPI JSON schema and BrAPI-Common that contains common schemas
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
        private final String language ;
        private final HashMap<String, BrAPIClass> types;
        private final HashMap<String, OntClass> ontClasses;

        public Generator(OntModelGeneratorOptions options, OntModelGeneratorMetadata metadata, List<BrAPIClass> brAPISchemas) {
            this.options = options;
            this.metadata = metadata;
            namespace = metadata.getNamespace() ;
            language = metadata.getLanguage() ;

            model = OntModelFactory.createModel();
            model.setID(namespace) ;

            Map<String, BrAPIClass> map = new HashMap<>();
            for (BrAPIClass brAPISchema : brAPISchemas) {
                if (map.put(brAPISchema.getName(), brAPISchema) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            this.brAPISchemas = map;
            this.types = new HashMap<>() ;
            this.ontClasses = new HashMap<>() ;
        }

        public Response<OntModel> generate() {
            return brAPISchemas.values()
                .stream()
                .filter(this::isGenerating)
                .map(this::createClass)
                .collect(Response.toList())
                .map(this::updateClasses)
                .map(() -> success(model));
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass.getMetadata() == null ||
                !(brAPIClass.getMetadata().isRequest() || brAPIClass.getMetadata().isParameters());
        }

        private Response<Boolean> createClass(BrAPIType brAPIType) {
            if (brAPIType instanceof BrAPIObjectType brAPIObjectType) {
                return createObjectType(brAPIObjectType) ;
            } else if (brAPIType instanceof BrAPIOneOfType brAPIOneOfType) {
                return createOneOfType(brAPIOneOfType) ;
            } else if (brAPIType instanceof BrAPIEnumType brAPIEnumType) {
                return createEnumType(brAPIEnumType) ;
            } 

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown supported BrAPI Type '%s'", brAPIType.getName()));
        }

        private Response<Boolean> createObjectType(BrAPIObjectType brAPIObjectType) {
            OntClass ontClass = createOntClass(brAPIObjectType) ;

            return brAPIObjectType.getProperties()
                    .stream()
                    .map(property -> createClassForType(property.getType()))
                    .collect(Response.toList())
                    .map (() -> success(true)) ;
        }

        private Response<Boolean> createOneOfType(BrAPIOneOfType brAPIOneOfType) {
            types.put(brAPIOneOfType.getName(), brAPIOneOfType) ;

            return brAPIOneOfType.getPossibleTypes()
                .stream()
                .map(this::createClass)
                .collect(Response.toList())
                .map(() -> success(false)) ;
        }

        private Response<Boolean> createEnumType(BrAPIEnumType brAPIEnumType) {
            OntClass ontClass = createOntClass(brAPIEnumType) ;

            return brAPIEnumType.getValues()
                .stream()
                .map(enumValue -> createOntIndividual(ontClass, enumValue))
                .collect(Response.toList())
                .mapResult(model::createObjectOneOf)
                .map(() -> success(true)) ;
        }

        private OntClass createOntClass(BrAPIClass brAPIClass) {
            types.put(brAPIClass.getName(), brAPIClass) ;

            OntClass ontClass = model.createOntClass(createURIForBrAPIType(brAPIClass));
            ontClass.addLabel(brAPIClass.getName(), language) ;

            if (brAPIClass.getDescription() != null) {
                ontClass.addComment(brAPIClass.getDescription(), language);
            }

            ontClasses.put(brAPIClass.getName(), ontClass) ;

            return ontClass ;
        }

        private Response<Boolean> createClassForType(BrAPIType type) {
            if (type instanceof BrAPIObjectType brAPIObjectType) {
                return createObjectType(brAPIObjectType) ;
            } else if (type instanceof BrAPIArrayType brAPIArrayType) {
                BrAPIType itemsType = brAPIArrayType.getItems();
                while (itemsType instanceof BrAPIArrayType brAPIArrayItemsType) {
                    itemsType = brAPIArrayItemsType.getItems() ;
                }

                return createClassForType(itemsType) ;
            } else if (type instanceof BrAPIEnumType brAPIEnumType) {
                return createEnumType(brAPIEnumType) ;
            }

            return Response.success(false) ;
        }
        private Response<List<OntClass>> updateClasses() {
            return this.ontClasses.values().stream()
                .map(this::updateClass)
                .collect(Response.toList()) ;
        }

        private Response<OntClass> updateClass(OntClass ontClass) {
            BrAPIClass brAPIClass = types.get(ontClass.getLabel());

            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                return brAPIObjectType.getProperties()
                    .stream()
                    .map(property -> createProperty(ontClass, property))
                    .collect(Response.toList())
                    .map (() -> success(ontClass)) ;
            } else if (brAPIClass instanceof BrAPIOneOfType brAPIOneOfType) {
                return success(ontClass) ;
            } else if (brAPIClass instanceof BrAPIEnumType brAPIEnumType) {
                return success(ontClass) ;
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown BrAPI Class '%s'", brAPIClass.getName()));
        }

        private Response<OntRelationalProperty> createProperty(OntClass ontClass, BrAPIObjectProperty property) {
            return createProperty(ontClass, property, property.getType()) ;
        }

        private Response<OntRelationalProperty> createProperty(OntClass ontClass, BrAPIObjectProperty property, BrAPIType brAPIType) {
            if (brAPIType instanceof BrAPIObjectType brAPIObjectType) {
                return findReferencedClass(brAPIObjectType.getName())
                    .mapResultToResponse(childClass -> createObjectProperty(ontClass, childClass, property)) ;
            } else if (brAPIType instanceof BrAPIArrayType brAPIArrayType) {
                BrAPIType itemsType = brAPIArrayType.getItems();
                AtomicInteger dimension = new AtomicInteger(1) ;

                while (itemsType instanceof BrAPIArrayType brAPIArrayItemsType) {
                    itemsType = brAPIArrayItemsType.getItems() ;
                    dimension.incrementAndGet() ;
                }

                return createProperty(ontClass, property, itemsType)
                    .mapResultToResponse(ontRelationalProperty -> updateProperty(ontRelationalProperty, dimension.get())) ;
            } else if (brAPIType instanceof BrAPIReferenceType brAPIReferenceType) {
                return findReferencedBrAPIType(brAPIReferenceType.getName())
                    .mapResultToResponse(referencedBrAPIType -> createProperty(ontClass, property, referencedBrAPIType)) ;
            } else if (brAPIType instanceof BrAPIOneOfType brAPIOneOfType) {
                return brAPIOneOfType.getPossibleTypes()
                    .stream()
                    .map(type -> findReferencedClass(type.getName()))
                    .collect(Response.toList())
                    .mapResultToResponse(childClasses -> createObjectProperty(ontClass, childClasses, property)) ;
            } else if (brAPIType instanceof BrAPIEnumType brAPIEnumType) {
                return findReferencedClass(brAPIEnumType.getName())
                    .mapResultToResponse(childClass -> createObjectProperty(ontClass, childClass, property)) ;
            } else if (brAPIType instanceof BrAPIPrimitiveType) {
                return createDataProperty(ontClass, property, brAPIType) ;
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown BrAPI Type '%s'", brAPIType.getName()));
        }

        private Response<OntRelationalProperty> updateProperty(OntRelationalProperty ontRelationalProperty, int dimension) {
            //TODO dimension
            return success(ontRelationalProperty) ;
        }

        private Response<OntRelationalProperty> createObjectProperty(OntClass domain, OntClass range, BrAPIObjectProperty property) {
            final OntObjectProperty objectProperty = model.createObjectProperty(createURIForBrAPIObjectProperty(domain, property));
            objectProperty.addDomain(domain);
            objectProperty.addRange(range);
            objectProperty.addLabel(property.getName(), language);

            if (property.getDescription() != null) {
                objectProperty.addComment(property.getDescription(), language);
            }

            return success(objectProperty) ;
        }

        private Response<OntRelationalProperty> createObjectProperty(OntClass domain, List<OntClass> ranges, BrAPIObjectProperty property) {
            final OntObjectProperty objectProperty = model.createObjectProperty(createURIForBrAPIObjectProperty(domain, property));
            objectProperty.addDomain(domain);
            ranges.forEach(objectProperty::addRange);
            objectProperty.addLabel(property.getName(), language);

            if (property.getDescription() != null) {
                objectProperty.addComment(property.getDescription(), language);
            }

            return success(objectProperty) ;
        }

        private Response<OntRelationalProperty> createDataProperty(OntClass domain, BrAPIObjectProperty property, BrAPIType type) {
            final OntDataProperty dataProperty = model.createDataProperty(createURIForBrAPIObjectProperty(domain, property));
            dataProperty.addDomain(domain);
            dataProperty.addLabel(property.getName(), language);

            if (property.getDescription() != null) {
                dataProperty.addComment(property.getDescription(), language);
            }

            OntDataRange.Named datatype = switch (type.getName()) {
                case "string" -> model.getDatatype(XSDDatatype.XSDstring.getURI()) ;
                case "integer" -> model.getDatatype(XSDDatatype.XSDinteger.getURI()) ;
                case "number" -> model.getDatatype(XSDDatatype.XSDdouble.getURI()) ;
                case "boolean" -> model.getDatatype(XSDDatatype.XSDboolean.getURI()) ;
                default -> null;
            };

            if (datatype == null) {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find XSDDatatype for '%s' in property '%s'", property.getType().getName(), property.getName()));
            }

            dataProperty.addRange(datatype) ;

            return success(dataProperty) ;
        }

        private Response<BrAPIType> findReferencedBrAPIType(String name) {
            BrAPIType referencedBrAPIType = types.get(name);

            if (referencedBrAPIType != null) {
                return success(referencedBrAPIType) ;
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find referenced BrAPI type '%s'", name));
            }
        }

        private Response<OntClass> findReferencedClass(String name) {
            OntClass referencedClass = ontClasses.get(name);

            if (referencedClass != null) {
                return success(referencedClass) ;
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find referenced class '%s'", name));
            }
        }

        private Response<OntIndividual> createOntIndividual(OntClass ontClass, BrAPIEnumValue enumValue) {
            return success(ontClass.createIndividual(createURIForBrAPIEnumValue(enumValue))) ;
        }

        private String createURIForBrAPIObjectProperty(OntClass ontClass, BrAPIObjectProperty property) {
            return String.format("%s/%s", ontClass.getURI(), property.getName()) ;
        }

        private String createURIForBrAPIType(BrAPIType brAPIType) {
            return String.format("%s/%s", namespace, brAPIType.getName()) ;
        }

        private String createURIForBrAPIEnumValue(BrAPIEnumValue enumValue) {
            return String.format("%s/%s", namespace, enumValue.getName()) ;
        }
    }
}
