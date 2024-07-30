package org.brapi.schematools.core.brapischema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import graphql.com.google.common.collect.Streams;
import lombok.AllArgsConstructor;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.find;
import static java.util.Collections.singletonList;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.toSingular;

/**
 * Utility class for reading BrAPI JSON Schema.
 */
@AllArgsConstructor
public class BrAPISchemaReader {
    private static final Pattern REF_PATTERN = Pattern.compile("((?:\\.{1,2}+/)*(?:[\\w-]+\\/)*(?:\\w+).json)?#\\/\\$defs\\/(\\w+)");
    private static final List<String> COMMON_MODULES = List.of("Schemas", "Parameters", "Requests");

    private final JsonSchemaFactory factory;
    private final ObjectMapper objectMapper;

    /**
     * Creates schema reader with a basic {@link ObjectMapper} and V202012 JSonSchema version
     */
    public BrAPISchemaReader() {
        factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        objectMapper = new ObjectMapper();
    }

    /**
     * Reads the schema module directories within a parent directory, and validates between schemas.
     * Each directory in the parent directory is a module and the JSON schemas in the directories are object types
     *
     * @param schemaDirectory the parent directory that holds all the module directories
     * @return a response containing a list of BrAPIClass with one type per JSON Schema or validation errors
     */
    public Response<List<BrAPIClass>> readDirectories(Path schemaDirectory)  {
        try {
            return dereferenceAndValidate(find(schemaDirectory, 3, this::schemaPathMatcher).map(this::createBrAPISchemas).collect(Response.mergeLists())) ;
        } catch (RuntimeException | IOException e) {
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    /**
     * Reads a single object type from an JSON schema. If the JSON schema
     * contain more than one type definition only the first is returned. There is
     * no validation of referenced schemas
     *
     * @param schemaPath a JSON schema file
     * @param module     the module in which the object resides
     * @return a response containing the BrAPIClass for this schema or validation errors
     * @throws BrAPISchemaReaderException if there is a problem reading the JSON schema
     */
    public Response<BrAPIClass> readSchema(Path schemaPath, String module) throws BrAPISchemaReaderException {
        try {
            return createBrAPISchemas(schemaPath, module).mapResult(list -> list.get(0)) ;
        } catch (RuntimeException e) {
            throw new BrAPISchemaReaderException(e);
        }
    }

    /**
     * Reads a single object type from an JSON schema string. If the JSON schema
     * contain more than one type definition only the first is returned. There is
     * no validation of referenced schemas
     *
     * @param path   the path of the schema is used to check references, if not supplied then validation is not performed
     * @param schema a JSON schema string
     * @param module the module in which the object resides
     * @return a response containing the BrAPIClass for this schema or validation errors
     * @throws BrAPISchemaReaderException if there is a problem reading the JSON schema
     */
    public Response<BrAPIClass> readSchema(Path path, String schema, String module) throws BrAPISchemaReaderException {
        try {
            return createBrAPISchemas(path, objectMapper.readTree(schema), module).mapResult(list -> list.get(0)) ;
        } catch (RuntimeException| JsonProcessingException e) {
            throw new BrAPISchemaReaderException(String.format("Can not read schema at '%s' in module '%s' from '%s', due to '%s'", path, module, schema, e.getMessage()), e);
        }
    }

    private Response<List<BrAPIClass>> dereferenceAndValidate(Response<List<BrAPIClass>> types) {
        return types.mapResult(this::dereference).mapResultToResponse(this::validate) ;
    }

    private List<BrAPIClass> dereference(List<BrAPIClass> types) {

        Map<String, BrAPIType> typeMap = types.stream().collect(Collectors.toMap(BrAPIType::getName, Function.identity()));

        List<BrAPIClass> brAPIClasses = new ArrayList<>() ;

        types.forEach(type -> {
            if (type instanceof BrAPIAllOfType brAPIAllOfType) {
                brAPIClasses.add(BrAPIObjectType.builder()
                    .name(brAPIAllOfType.getName())
                    .description(brAPIAllOfType.getDescription())
                    .module(brAPIAllOfType.getModule())
                    .metadata(brAPIAllOfType.getMetadata() != null ? brAPIAllOfType.getMetadata().toBuilder().build() : null)
                    .interfaces(extractInterfaces(brAPIAllOfType, typeMap))
                    .properties(extractProperties(new ArrayList<>(), brAPIAllOfType, typeMap))
                    .build());
            } else {
                brAPIClasses.add(type);
            }
        });

        return brAPIClasses ;
    }

    private Response<List<BrAPIClass>> validate(List<BrAPIClass> brAPIClasses) {
        Map<String, BrAPIClass> classesMap = brAPIClasses.stream().collect(Collectors.toMap(BrAPIType::getName, Function.identity()));

        return brAPIClasses.stream().map(brAPIClass -> validateClass(classesMap, brAPIClass).mapResult(t -> (BrAPIClass)t)).collect(Response.toList()) ;
    }

    private Response<BrAPIType> validateClass(final Map<String, BrAPIClass> classesMap, BrAPIClass brAPIClass) {
        return validateBrAPIMetadata(brAPIClass).map(() -> {
                if (brAPIClass instanceof BrAPIAllOfType brAPIAllOfType) {
                    return fail(Response.ErrorType.VALIDATION, String.format("BrAPIAllOfType '%s' was not de-referenced", brAPIAllOfType.getName())) ;
                }

                if (brAPIClass instanceof BrAPIOneOfType brAPIOneOfType) {
                    return brAPIOneOfType.getPossibleTypes().stream().map(possibleType -> validateType(classesMap, possibleType)).collect(Response.toList()).withResult(brAPIClass) ;
                }

                if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                    return brAPIObjectType.getProperties().stream().map(property -> validateProperty(classesMap, brAPIObjectType, property)).collect(Response.toList()).withResult(brAPIClass) ;
                }

                return success(brAPIClass) ;
        }) ;
    }

    private Response<BrAPIType> validateType(final Map<String, BrAPIClass> classesMap, BrAPIType brAPIType) {
        if (brAPIType instanceof BrAPIClass brAPIAllOfType) {
            return validateClass(classesMap, brAPIAllOfType) ;
        } else {
            return success(brAPIType) ;
        }
    }

    private Response<BrAPIMetadata> validateBrAPIMetadata(BrAPIClass brAPIClass) {
        BrAPIMetadata metadata = brAPIClass.getMetadata();

        if (metadata != null) {
            int i = 0 ;
            if (metadata.isPrimaryModel()) {
                ++i;
            }
            if (metadata.isRequest()) {
                ++i;
            }
            if (metadata.isParameters()) {
                ++i;
            }
            if (metadata.isInterfaceClass()) {
                ++i;
            }
            if (i > 1) {
                return fail(Response.ErrorType.VALIDATION, String.format("In class '%s', 'primaryModel', 'request', 'parameters', 'interface' are mutually exclusive, only one can be set to to true", brAPIClass.getName())) ;
            }
        }

        return success(metadata) ;
    }

    private Response<BrAPIObjectProperty> validateProperty(Map<String, BrAPIClass> classesMap, BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property) {
        if (property.getReferencedAttribute() != null) {

            BrAPIType type = unwrapType(property.getType());

            BrAPIClass referencedType = classesMap.get(type.getName()) ;

            if (referencedType == null) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Property '%s' in type '%s' has a Referenced Attribute '%s', but the referenced type '%s' is not available",
                        property.getName(), brAPIObjectType.getName(), property.getReferencedAttribute(), property.getType().getName()));
            }

            if (referencedType instanceof BrAPIObjectType referencedObjectType) {
                if (referencedObjectType.getProperties().stream().noneMatch(childProperty -> property.getReferencedAttribute().equals(childProperty.getName()))) {
                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Property '%s' in type '%s' has a Referenced Attribute '%s', but the property does not exist in the referenced type '%s'",
                        property.getName(), brAPIObjectType.getName(), property.getReferencedAttribute(), referencedType.getName()));
                }
            } else {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Property '%s' in type '%s' has a Referenced Attribute '%s', but the referenced type '%s' is not a BrAPIObjectType",
                        property.getName(), brAPIObjectType.getName(), property.getReferencedAttribute(), referencedType.getName()));
            }
        }

        return Response.success(property) ;
    }

    private BrAPIType unwrapType(BrAPIType type) {
        if (type instanceof BrAPIArrayType brAPIArrayType) {
            return unwrapType(brAPIArrayType.getItems()) ;
        }

        return type ;
    }

    private List<BrAPIObjectProperty> extractProperties(List<BrAPIObjectProperty> properties, BrAPIType brAPIType, Map<String, BrAPIType> typeMap) {

        if (brAPIType instanceof BrAPIObjectType brAPIObjectType) {
            properties.addAll(brAPIObjectType.getProperties()) ;
        } else {
            if (brAPIType instanceof BrAPIAllOfType brAPIAllOfType) {
                brAPIAllOfType.getAllTypes().forEach(type -> extractProperties(properties, type, typeMap)) ;
            } else {
                if (brAPIType instanceof BrAPIReferenceType brAPIReferenceType) {
                    extractProperties(properties, typeMap.get(brAPIReferenceType.getName()), typeMap) ;
                }
            }
        }

        return properties ;
    }

    private List<BrAPIObjectType> extractInterfaces(BrAPIAllOfType brAPIAllOfType, Map<String, BrAPIType> typeMap) {

        List<BrAPIObjectType> interfaces = new ArrayList<>() ;

        brAPIAllOfType.getAllTypes().forEach(type -> {
            BrAPIType allType = typeMap.get(type.getName());

            if (allType instanceof BrAPIObjectType && isInterface((BrAPIObjectType)allType)) {
                interfaces.add((BrAPIObjectType) allType) ;
            }
        });

        return interfaces ;
    }

    private boolean isInterface(BrAPIClass brAPIClass) {
        return brAPIClass.getMetadata() != null && brAPIClass.getMetadata().isInterfaceClass() ;
    }


    private Response<List<BrAPIClass>> createBrAPISchemas(Path path) {
        return createBrAPISchemas(path, findModule(path));
    }

    private String findModule(Path path) {
        String module = path != null ? path.getParent().getFileName().toString() : null;

        return module != null && COMMON_MODULES.contains(module) ? null : module;
    }

    private Response<List<BrAPIClass>> createBrAPISchemas(Path path, String module) {
        try {
            JsonSchema schema = factory.getSchema(path.toUri());

            JsonNode json = schema.getSchemaNode();

            return createBrAPISchemas(path, json, module);
        } catch (RuntimeException e) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not read schemas from for module '%s' from path '%s' due to '%s'", module, path, e.getMessage())) ;
        }
    }

    private Response<List<BrAPIClass>> createBrAPISchemas(Path path, JsonNode json, String module) {
        JsonNode defs = json.get("$defs");

        if (defs != null) {
            json = defs;
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = json.fields();

        return Stream.generate(() -> null)
            .takeWhile(x -> iterator.hasNext())
            .map(n -> iterator.next()).map(entry -> createBrAPIClass(path, entry.getValue(), entry.getKey(), module)).collect(Response.toList());
    }

    private Response<BrAPIClass> createBrAPIClass(Path path, JsonNode jsonNode, String fallbackName, String module) {
        try {
            return createType(path, jsonNode, fallbackName, module).mapResult(type -> (BrAPIClass) type);
        } catch (ClassCastException e) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not cast type '%s' to BrAPIClass!", fallbackName)) ;
        }
    }

    private boolean schemaPathMatcher(Path path, BasicFileAttributes basicFileAttributes) {
        return basicFileAttributes.isRegularFile();
    }

    private Response<BrAPIType> createType(Path path, JsonNode jsonNode, String fallbackName, String module) {
        if (jsonNode.has("allOf")) {
            return createAllOfType(path, jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
        }

        if (jsonNode.has("oneOf")) {
            return createOneOfType(path, jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
        }

        boolean isEnum = jsonNode.has("enum");

        return findChildNode(jsonNode, "$ref", false).ifPresentMapResultToResponseOr(
            ref -> createReferenceType(path, ref),

            () -> findStringList(jsonNode, "type", true).
                mapResultToResponse(types -> {
                    if (types.contains("object")) {
                        if (isEnum) {
                            return fail(Response.ErrorType.VALIDATION, String.format("Object Type '%s' can not be an enum!", fallbackName));
                        } else {
                            return createObjectType(path, jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                        }
                    }

                    if (types.contains("array")) {
                        if (isEnum) {
                            return fail(Response.ErrorType.VALIDATION, String.format("Array Type '%s' can not be an enum!", fallbackName));
                        }

                        return createArrayType(path, jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                    }

                    if (types.contains("string")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "string", module);
                        } else {
                            return success(BrAPIPrimitiveType.STRING);
                        }
                    }

                    if (types.contains("integer")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "integer", module);
                        } else {
                            return success(BrAPIPrimitiveType.INTEGER);
                        }
                    }

                    if (types.contains("number")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "number", module);
                        } else {
                            return success(BrAPIPrimitiveType.NUMBER);
                        }
                    }

                    if (types.contains("boolean")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "boolean", module);
                        } else {
                            return success(BrAPIPrimitiveType.BOOLEAN);
                        }
                    }

                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type(s) '%s' in node '%s'", types, jsonNode));

                }));

    }

    private Response<String> findNameFromTitle(JsonNode jsonNode) {
        return findString(jsonNode, "title", false).mapResult(name -> name != null ? name.replace(" ", "") : null);
    }

    private Response<BrAPIType> createReferenceType(Path path, JsonNode jsonNode) {

        BrAPIReferenceType.BrAPIReferenceTypeBuilder builder = BrAPIReferenceType.builder();

        return findString(jsonNode).
            mapResultToResponse(ref -> parseRef(path, ref)).
            onSuccessDoWithResult(builder::name).
            map(() -> success(builder.build()));
    }

    private Response<String> parseRef(Path path, String ref) {
        Matcher matcher = REF_PATTERN.matcher(ref);

        if (matcher.matches()) {

            if (path != null && matcher.group(1) != null) {
                Path refPath = path.getParent().resolve(matcher.group(1));

                if (!refPath.toFile().isFile()) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Can not find json file '%s' referenced in '%s'", refPath, path));
                }
            }
            return success(matcher.group(2));
        } else {
            return fail(Response.ErrorType.VALIDATION, String.format("Ref '%s' does not match ref pattern '%s'", ref, REF_PATTERN));
        }
    }

    private Response<BrAPIType> createArrayType(Path path, JsonNode jsonNode, String name, String module) {

        BrAPIArrayType.BrAPIArrayTypeBuilder builder = BrAPIArrayType.builder().name(name);

        return findChildNode(jsonNode, "items", true).
            mapResultToResponse(childNode -> createType(path, childNode, toSingular(name), module).
                onSuccessDoWithResult(builder::items)).
            map(() -> success(builder.build()));
    }

    private Response<BrAPIType> createObjectType(Path path, JsonNode jsonNode, String name, String module) {

        BrAPIObjectType.BrAPIObjectTypeBuilder builder = BrAPIObjectType.builder()
            .name(name)
            .module(module)
            .interfaces(new ArrayList<>());

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        List<String> required = findStringList(jsonNode, "required", false).getResultIfPresentOrElseResult(Collections.emptyList());

        List<BrAPIObjectProperty> properties = new ArrayList<>();
        return Response.empty()
            .mapOnCondition(jsonNode.has("additionalProperties"), () -> findChildNode(jsonNode, "additionalProperties", true).
                mapResultToResponse(additionalPropertiesNode -> createProperty(path, additionalPropertiesNode, "additionalProperties",
                    module, required.contains("additionalProperties")).onSuccessDoWithResult(properties::add)))
            .mapOnCondition(jsonNode.has("properties"), () -> findChildNode(jsonNode, "properties", true)
                    .mapResult(JsonNode::fields)
                .mapResultToResponse(fields -> createProperties(path, fields, module, required))
                .onSuccessDoWithResult(properties::addAll))
            .onSuccessDo(() -> builder.properties(properties))
            .merge(validateRequiredProperties(required, properties, name))
            .mapOnCondition(jsonNode.has("brapi-metadata"), () -> findChildNode(jsonNode, "brapi-metadata", true)
                .mapResultToResponse(this::parseMetadata).onSuccessDoWithResult(builder::metadata))
            .map(() -> success(builder.build()));
    }

    private Response<List<BrAPIObjectProperty>> validateRequiredProperties(List<String> requiredPropertyNames, List<BrAPIObjectProperty> properties, String objectName) {
        return requiredPropertyNames.stream().map(name -> validateRequiredProperty(name, properties, objectName)).collect(Response.toList());
    }

    private Response<BrAPIObjectProperty> validateRequiredProperty(String requiredPropertyName, List<BrAPIObjectProperty> properties, String objectName) {
        return properties.stream().filter(property -> property.getName().equals(requiredPropertyName))
            .findAny().map(Response::success)
            .orElse(fail(Response.ErrorType.VALIDATION,
                String.format("The required property '%s' is not found in the list of properties of '%s', expecting one of '%s'", requiredPropertyName, objectName,
                    properties.stream().map(BrAPIObjectProperty::getName).collect(Collectors.joining(", "))))) ;
    }

    private Response<List<BrAPIObjectProperty>> createProperties(Path path, Iterator<Map.Entry<String, JsonNode>> fields, String module, List<String> required) {
        return Streams.stream(fields).map(field -> createProperty(path, field.getValue(), field.getKey(), module, required.contains(field.getKey()))).collect(Response.toList());
    }

    private Response<BrAPIObjectProperty> createProperty(Path path, JsonNode jsonNode, String name, String module, boolean required) {

        BrAPIObjectProperty.BrAPIObjectPropertyBuilder builder = BrAPIObjectProperty.builder().
            name(name).
            required(required);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        findString(jsonNode, "referencedAttribute", false).
            onSuccessDoWithResult(builder::referencedAttribute);

        return createType(path, jsonNode, StringUtils.toSentenceCase(name), module).
            onSuccessDoWithResult(builder::type).
            mapOnCondition(jsonNode.has("relationshipType"), () -> findString(jsonNode, "relationshipType", true).
                mapResultToResponse(BrAPIRelationshipType::fromNameOrLabel).
                onSuccessDoWithResult(builder::relationshipType)).
            map(() -> success(builder.build()));
    }

    private Response<BrAPIMetadata> parseMetadata(JsonNode metadata) {
        BrAPIMetadata.BrAPIMetadataBuilder builder = BrAPIMetadata.builder();

        return findBoolean(metadata, "primaryModel", false, false).
            onSuccessDoWithResult(builder::primaryModel).
            merge(findBoolean(metadata, "request", false, false)).
            onSuccessDoWithResult(builder::request).
            merge(findBoolean(metadata, "parameters", false, false)).
            onSuccessDoWithResult(builder::parameters).
            merge(findBoolean(metadata, "interface", false, false)).
            onSuccessDoWithResult(builder::interfaceClass).
            map(() -> success(builder.build()));
    }

    private Response<BrAPIType> createAllOfType(Path path, JsonNode jsonNode, String name, String module) {

        BrAPIAllOfType.BrAPIAllOfTypeBuilder builder = BrAPIAllOfType.builder().
            name(name).
            module(module);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        return findChildNode(jsonNode, "allOf", true).
            mapResult(this::childNodes).
            mapResultToResponse(childNodes -> childNodes.mapResultToResponse(nodes -> createAllTypes(path, nodes, name, module))).
            onSuccessDoWithResult(builder::allTypes).
            mapOnCondition(jsonNode.has("brapi-metadata"), () -> findChildNode(jsonNode, "brapi-metadata", true).
                mapResultToResponse(this::parseMetadata).onSuccessDoWithResult(builder::metadata)).
            map(() -> success(builder.build()));
    }

    private Response<List<BrAPIType>> createAllTypes(Path path, List<JsonNode> jsonNodes, String fallbackNamePrefix, String module) {

        AtomicInteger i = new AtomicInteger();

        return jsonNodes.stream().map(jsonNode -> createType(path, jsonNode, String.format("%s%d", fallbackNamePrefix, i.incrementAndGet()), module)).collect(Response.toList());
    }

    private Response<BrAPIType> createOneOfType(Path path, JsonNode jsonNode, String name, String module) {

        BrAPIOneOfType.BrAPIOneOfTypeBuilder builder = BrAPIOneOfType.builder().
            name(name).
            module(module);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        return findChildNode(jsonNode, "oneOf", true).
            mapResult(this::childNodes).
            mapResultToResponse(childNodes -> childNodes.mapResultToResponse(nodes -> createPossibleTypes(path, nodes, name, module))).
            onSuccessDoWithResult(builder::possibleTypes).
            mapOnCondition(jsonNode.has("brapi-metadata"), () -> findChildNode(jsonNode, "brapi-metadata", true).
                mapResultToResponse(this::parseMetadata).onSuccessDoWithResult(builder::metadata)).
            map(() -> success(builder.build()));
    }

    private Response<List<BrAPIType>> createPossibleTypes(Path path, List<JsonNode> jsonNodes, String fallbackNamePrefix, String module) {

        AtomicInteger i = new AtomicInteger();

        return jsonNodes.stream().map(jsonNode -> createType(path, jsonNode, String.format("%s%d", fallbackNamePrefix, i.incrementAndGet()), module)).collect(Response.toList());
    }

    private Response<BrAPIType> createEnumType(JsonNode jsonNode, String name, String type, String module) {

        BrAPIEnumType.BrAPIEnumTypeBuilder builder = BrAPIEnumType.builder().
            name(name).
            type(type).
            module(module);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        return findStringList(jsonNode, "enum", true).
            mapResultToResponse(strings -> createEnumValues(strings, type)).
            onSuccessDoWithResult(builder::values).
            mapOnCondition(jsonNode.has("brapi-metadata"), () -> findChildNode(jsonNode, "brapi-metadata", true).
                mapResultToResponse(this::parseMetadata).onSuccessDoWithResult(builder::metadata)).
            map(() -> success(builder.build()));
    }

    private Response<List<BrAPIEnumValue>> createEnumValues(List<String> strings, String type) {
        return strings.stream().map(string -> createEnumValue(string, type)).collect(Response.toList());
    }

    private Response<BrAPIEnumValue> createEnumValue(String string, String type) {
        BrAPIEnumValue.BrAPIEnumValueBuilder builder = BrAPIEnumValue.builder().
            name(string);

        try {
            return switch (type) {
                case "string" -> success(builder.value(string).build());
                case "integer" -> success(builder.value(Integer.valueOf(string)).build());
                case "number" -> success(builder.value(Float.valueOf(string)).build());
                case "boolean" -> success(builder.value(Boolean.valueOf(string)).build());
                default ->
                    Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown primitive type '%s'", type));
            };
        } catch (NumberFormatException e) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not convert '%s' to type '%s'", string, type));
        }
    }

    private Response<String> findString(JsonNode parentNode, String fieldName, boolean required) {
        return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
            if (jsonNode instanceof TextNode textNode) {
                return success(textNode.asText());
            }
            return required ?
                fail(Response.ErrorType.VALIDATION,
                    String.format("Child node type '%s' was not TextNode with field name '%s' for parent node '%s'", jsonNode.getClass().getName(), parentNode, fieldName)) :
                Response.empty();
        });
    }

    private Response<Boolean> findBoolean(JsonNode parentNode, String fieldName, boolean required, boolean defaultValue) {
        return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
            if (jsonNode instanceof BooleanNode booleanNode) {
                return success(booleanNode.asBoolean());
            }
            return required ?
                fail(Response.ErrorType.VALIDATION,
                    String.format("Child node type '%s' was not BooleanNode with field name '%s' for parent node '%s'", jsonNode.getClass().getName(), parentNode, fieldName)) :
                Response.success(defaultValue);
        });
    }

    private Response<List<String>> findStringList(JsonNode parentNode, String fieldName, boolean required) {

        return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
            if (jsonNode instanceof ArrayNode arrayNode) {
                return StreamSupport.stream(arrayNode.spliterator(), false).
                    filter(childNode -> !(childNode instanceof NullNode)).
                    map(this::findString).filter(stringResponse -> stringResponse.getResult() != null).
                    collect(Response.toList());
            }

            if (jsonNode instanceof TextNode textNode) {
                return success(singletonList(textNode.asText()));
            }

            return required ?
                fail(Response.ErrorType.VALIDATION,
                    String.format("Unknown child node type '%s' with field name '%s' for parent node '%s'", jsonNode.getClass().getName(), parentNode, fieldName)) :
                Response.empty();
        });
    }

    private Response<String> findString(JsonNode jsonNode) {
        if (jsonNode instanceof TextNode textNode) {
            return success(textNode.asText());
        }

        if (jsonNode != null) {
            return fail(Response.ErrorType.VALIDATION, String.format("Node type '%s' is not string", jsonNode.getClass().getName()));
        } else {
            return Response.empty();
        }
    }

    private Response<JsonNode> findChildNode(JsonNode parentNode, String fieldName, boolean required) {
        JsonNode jsonNode = parentNode.get(fieldName);

        if (jsonNode != null) {
            return success(jsonNode);
        }

        if (required) {
            return fail(Response.ErrorType.VALIDATION, String.format("Parent node '%s' does not have child node with field name '%s'", parentNode, fieldName));
        } else {
            return Response.empty();
        }
    }

    private Response<List<JsonNode>> childNodes(JsonNode parentNode) {

        if (parentNode instanceof ArrayNode arrayNode) {
            return success(StreamSupport.stream(arrayNode.spliterator(), false).toList());
        }

        return fail(Response.ErrorType.VALIDATION,
            String.format("Parent Node type '%s' is not ArrayNode", parentNode.getClass().getName()));
    }

}
