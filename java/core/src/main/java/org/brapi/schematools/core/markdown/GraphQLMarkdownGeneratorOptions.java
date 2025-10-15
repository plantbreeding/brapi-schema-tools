package org.brapi.schematools.core.markdown;

import com.fasterxml.jackson.annotation.JsonIgnore;
import graphql.schema.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.brapi.schematools.core.markdown.GraphQLMarkdownGenerator.LIST_RESPONSE_PATTERN;
import static org.brapi.schematools.core.markdown.GraphQLMarkdownGenerator.SEARCH_RESPONSE_PATTERN;

/**
 * Options for the {@link GraphQLGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class GraphQLMarkdownGeneratorOptions implements Options {

    private Boolean overwrite;
    private String queryDefinitionsDirectory;
    private String typeDefinitionsDirectory;
    private String descriptionsDirectory;
    private String fieldsDirectory;
    private String argumentsDirectory;
    private Boolean createTopLevelFieldDefinitions;
    private Boolean createTopLevelInputFieldDefinitions;
    private Boolean createTopLeveArgumentDefinitions;
    private String introspectionQuery;
    private String introspectionQueryJsonPath;
    private String ignoreQueryNamePattern;
    private String ignoreTypeNamePattern;

    /**
     * Load the default options
     *
     * @return The default options
     */
    public static GraphQLMarkdownGeneratorOptions load() {
        try {
            return ConfigurationUtils.load("graphql-markdown-options.yaml", GraphQLMarkdownGeneratorOptions.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param optionsFile The path to the options file in YAML or Json.
     * @return The options loaded from the YAML or Json file.
     * @throws IOException if the options file can not be found or is incorrectly formatted.
     */
    public static GraphQLMarkdownGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, GraphQLMarkdownGeneratorOptions.class));
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static GraphQLMarkdownGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, GraphQLMarkdownGeneratorOptions.class));
    }

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(overwrite, "'overwrite' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(queryDefinitionsDirectory, "'queryDefinitionsDirectory' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(typeDefinitionsDirectory, "'typeDefinitionsDirectory' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(descriptionsDirectory, "'descriptionsDirectory' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(fieldsDirectory, "'fieldsDirectory' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(argumentsDirectory, "'argumentsDirectory' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(createTopLevelFieldDefinitions, "'createTopLevelFieldDefinitions' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(createTopLeveArgumentDefinitions, "'createTopLeveArgumentDefinitions' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(introspectionQuery, "'introspectionQuery' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public GraphQLMarkdownGeneratorOptions override(GraphQLMarkdownGeneratorOptions overrideOptions) {
        if (overrideOptions.overwrite != null) {
            overwrite = overrideOptions.overwrite;
        }

        if (overrideOptions.queryDefinitionsDirectory != null) {
            queryDefinitionsDirectory = overrideOptions.queryDefinitionsDirectory;
        }

        if (overrideOptions.typeDefinitionsDirectory != null) {
            typeDefinitionsDirectory = overrideOptions.typeDefinitionsDirectory;
        }

        if (overrideOptions.descriptionsDirectory != null) {
            descriptionsDirectory = overrideOptions.descriptionsDirectory;
        }

        if (overrideOptions.fieldsDirectory != null) {
            fieldsDirectory = overrideOptions.fieldsDirectory;
        }

        if (overrideOptions.argumentsDirectory != null) {
            argumentsDirectory = overrideOptions.argumentsDirectory;
        }

        if (overrideOptions.createTopLevelFieldDefinitions != null) {
            createTopLevelFieldDefinitions = overrideOptions.createTopLevelFieldDefinitions;
        }

        if (overrideOptions.createTopLeveArgumentDefinitions != null) {
            createTopLeveArgumentDefinitions = overrideOptions.createTopLeveArgumentDefinitions;
        }

        if (overrideOptions.introspectionQuery != null) {
            introspectionQuery = overrideOptions.introspectionQuery;
        }

        if (overrideOptions.introspectionQueryJsonPath != null) {
            introspectionQueryJsonPath = overrideOptions.introspectionQueryJsonPath;
        }

        if (overrideOptions.ignoreQueryNamePattern != null) {
            ignoreQueryNamePattern = overrideOptions.ignoreQueryNamePattern;
        }

        if (overrideOptions.ignoreTypeNamePattern != null) {
            ignoreTypeNamePattern = overrideOptions.ignoreTypeNamePattern;
        }

        return this;
    }

    /**
     * Determines if the Generator should Overwrite exiting files.
     *
     * @return {@code true} if the Generator should Overwrite exiting files, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isOverwritingExistingFiles() {
        return overwrite;
    }

    /**
     * Creates the description for a GraphQLObjectType
     *
     * @param type            the GraphQLObjectType
     * @param dataType        if the type is a Response with a data field, then this is the type of that field
     * @param queryDefinition the query associated with the response type
     * @return the description for a GraphQLObjectType
     */
    public String getDescriptionForObjectType(GraphQLObjectType type, GraphQLOutputType dataType, GraphQLFieldDefinition queryDefinition) {
        if (dataType != null) {
            if (queryDefinition != null) {
                return String.format("%s is the response for the %s query, which returns %s.", type.getName(), getMarkdownLink(queryDefinition, "../../" + queryDefinitionsDirectory + "/" + descriptionsDirectory + "/"), getMarkdownLink(dataType, ""));
            } else {
                return String.format("%s is an Response Object Type, which returns %s.", type.getName(), getMarkdownLink(dataType, ""));
            }
        } else {
            return String.format("%s is an Object Type.", type.getName());
        }
    }

    /**
     * Creates the description for a GraphQLInputObjectType
     *
     * @param type            the GraphQLInputObjectType
     * @param queryDefinition the query associated with the input field type
     * @return the description for a GraphQLInputObjectType
     */
    public String getDescriptionForInputObjectType(GraphQLInputObjectType type, GraphQLFieldDefinition queryDefinition) {
        if (queryDefinition != null) {
            return String.format("%s is used as an input for the %s query.", type.getName(), getMarkdownLink(queryDefinition, "../../" + queryDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
        } else {
            return String.format("%s is an Input Type.", type.getName());
        }

    }

    /**
     * Creates the query description for a Query
     *
     * @param queryDefinition the GraphQLFieldDefinition
     * @param dataType        if the type is a Response with a data field, then this is the type of that field
     * @param inputType       the query input type
     * @return the query description for a GraphQLFieldDefinition
     */
    public String getDescriptionForQuery(GraphQLFieldDefinition queryDefinition, GraphQLOutputType dataType, GraphQLInputType inputType) {

        if (queryDefinition.getType() instanceof GraphQLNamedType graphQLNamedType) {
            Matcher matcher = LIST_RESPONSE_PATTERN.matcher(graphQLNamedType.getName());

            if (matcher.matches()) {
                return getDescriptionForListQuery(queryDefinition, dataType, inputType);
            } else {
                matcher = SEARCH_RESPONSE_PATTERN.matcher(graphQLNamedType.getName());

                if (matcher.matches()) {
                    return getDescriptionForSearchQuery(queryDefinition, dataType, inputType);
                }
            }
        }

        if (inputType != null) {
            return String.format("The %s query returns a %s, that matches the filters provided %s Input.",
                queryDefinition.getName(),
                getMarkdownLink(queryDefinition.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"),
                getMarkdownLink(inputType, "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
        } else {
            return String.format("The %s query returns a %s.",
                queryDefinition.getName(),
                getMarkdownLink(queryDefinition.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));

        }
    }

    /**
     * Creates the query description for a List Query
     *
     * @param query     the GraphQLFieldDefinition
     * @param dataType  if the type is a Response with a data field, then this is the type of that field
     * @param inputType the query input type
     * @return the query description for a GraphQLFieldDefinition
     */
    public String getDescriptionForListQuery(GraphQLFieldDefinition query, GraphQLOutputType dataType, GraphQLInputType inputType) {
        if (dataType != null) {
            if (inputType != null) {
                return String.format("The %s query returns a %s, which is a paged list of %s values that matches the filters provided %s Input.",
                    query.getName(),
                    getMarkdownLink(query.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"),
                    getMarkdownLink(dataType, "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"),
                    getMarkdownLink(inputType, "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
            } else {
                return String.format("The %s query returns a %s, which is a paged list of %s values.",
                    query.getName(),
                    getMarkdownLink(query.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"),
                    getMarkdownLink(dataType, "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
            }
        } else {
            return String.format("The %s query returns a %s value.",
                query.getName(),
                getMarkdownLink(query.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));

        }
    }

    /**
     * Creates the query description for a Search Query
     *
     * @param query     the GraphQLFieldDefinition
     * @param dataType  if the type is a Response with a data field, then this is the type of that field
     * @param inputType the query input type
     * @return the query description for a GraphQLFieldDefinition
     */
    public String getDescriptionForSearchQuery(GraphQLFieldDefinition query, GraphQLOutputType dataType, GraphQLInputType inputType) {
        if (dataType != null) {
            if (inputType != null) {
                return String.format("The %s query returns a %s, which is a complete list of %s that matches the filters provided %s Input or a searchResultsDbId that can be used to lookup a results at a later point.",
                    query.getName(),
                    getMarkdownLink(query.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"),
                    getMarkdownLink(dataType, "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"),
                    getMarkdownLink(inputType, "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
            } else {
                return String.format("The %s query returns a %s, which is a complete list of %s that matches the filters provided by the Input or a searchResultsDbId that can be used to lookup a results at a later point.",
                    query.getName(),
                    getMarkdownLink(query.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"),
                    getMarkdownLink(dataType, "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
            }
        } else {
            return String.format("The %s query returns a %s value.",
                query.getName(),
                getMarkdownLink(query.getType(), "../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
        }
    }

    /**
     * Creates the description for a GraphQLInterfaceType
     *
     * @param type the GraphQLInterfaceType
     * @return the description for a GraphQLInterfaceType
     */
    public String getDescriptionForInterface(GraphQLInterfaceType type) {
        return String.format("%s is an Interface.", type.getName());
    }

    /**
     * Creates the description for a GraphQLEnumType
     *
     * @param type the GraphQLEnumType
     * @return the description for a GraphQLEnumType
     */
    public String getDescriptionForEnum(GraphQLEnumType type) {
        return String.format("%s is an Enumerated value.", type.getName());
    }

    /**
     * Creates the description for a GraphQLFieldDefinition
     *
     * @param type  the type to which this field belongs
     * @param field the GraphQLFieldDefinition
     * @return the description for a GraphQLFieldDefinition
     */
    public String getDescriptionForField(GraphQLNamedType type, GraphQLFieldDefinition field) {
        if (field.getArguments().isEmpty()) {
            if (type != null) {
                return String.format("The %s field in type %s returns a %s value.",
                    field.getName(),
                    getMarkdownLink(type, "../../" + descriptionsDirectory + "/"),
                    getMarkdownLink(field.getType(), "../../" + descriptionsDirectory + "/"));
            } else {
                return String.format("The %s field returns %s value.",
                    field.getName(),
                    getMarkdownLink(field.getType(), "../../" + descriptionsDirectory + "/"));
            }
        } else {
            if (type != null) {
                return String.format("The sub-query %s is a sub-query in type %s returns a %s value.",
                    field.getName(),
                    getMarkdownLink(type, "../../" + descriptionsDirectory + "/"),
                    getMarkdownLink(field.getType(), "../../" + descriptionsDirectory + "/"));
            } else {
                return String.format("The %s sub-query returns %s value.",
                    field.getName(),
                    getMarkdownLink(field.getType(), "../../" + descriptionsDirectory + "/"));
            }
        }
    }

    /**
     * Creates the description for a GraphQLInputObjectField
     *
     * @param type            the type to which this field belongs
     * @param queryDefinition the query associated with the input field type
     * @param field           the GraphQLInputObjectField
     * @return the description for a GraphQLInputObjectField
     */
    public String getDescriptionForInputField(GraphQLInputObjectType type, GraphQLFieldDefinition queryDefinition, GraphQLInputObjectField field) {
        if (type != null) {
            if (queryDefinition != null) {
                return String.format("The %s field in type %s is used in the %s query to filter the returned results.",
                    field.getName(),
                    getMarkdownLink(type, "../../" + descriptionsDirectory + "/"),
                    getMarkdownLink(queryDefinition, "../../../" + queryDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
            } else {
                return String.format("The %s field in type %s is used to filter query results.",
                    field.getName(),
                    getMarkdownLink(type, "../../" + descriptionsDirectory + "/"));
            }
        } else {
            return String.format("The %s field returns %s value.",
                field.getName(),
                getMarkdownLink(field.getType(), "../../" + descriptionsDirectory + "/"));
        }
    }

    /**
     * Creates the description for a GraphQLArgument
     *
     * @param queryDefinition the query definition to which this field belongs
     * @param argument        the GraphQLArgument
     * @return the description for a GraphQLArgument
     */
    public String getDescriptionForArgument(GraphQLFieldDefinition queryDefinition, GraphQLArgument argument) {
        if (queryDefinition != null) {
            return String.format("The %s argument for query %s accepts a value of %s.",
                argument.getName(),
                getMarkdownLink(queryDefinition, "../../" + descriptionsDirectory + "/"),
                getMarkdownLink(argument.getType(), "../../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
        } else {
            return String.format("The %s argument accepts a value of %s.",
                argument.getName(),
                getMarkdownLink(argument.getType(), "../../../" + typeDefinitionsDirectory + "/" + descriptionsDirectory + "/"));
        }
    }

    /**
     * Determines if the Generator should create top level field descriptions when there is more than one field with the same name.
     *
     * @return {@code true} if the Generator should create top level field descriptions when there is more than one field with the same name,
     * {@code false} otherwise
     */
    public boolean isCreatingTopLevelFieldDefinitions() {
        return createTopLevelFieldDefinitions != null && createTopLevelFieldDefinitions;
    }

    /**
     * Determines if the Generator should create top level input object field descriptions when there is more than one field with the same name.
     *
     * @return {@code true} if the Generator should create top level input object field descriptions when there is more than one field with the same name,
     * {@code false} otherwise
     */
    public boolean isCreatingTopLevelInputFieldDefinitions() {
        return createTopLevelInputFieldDefinitions != null && createTopLevelInputFieldDefinitions;
    }

    /**
     * Determines if the Generator should create top level argument descriptions when there is more than one argument with the same name.
     *
     * @return {@code true} if the Generator should create top level argument descriptions when there is more than one argument with the same name,
     * {@code false} otherwise
     */
    public boolean isCreatingTopLevelArgumentDefinitions() {
        return createTopLeveArgumentDefinitions != null && createTopLeveArgumentDefinitions;
    }

    private String getMarkdownLink(GraphQLType type, String prefix) {
        if (type instanceof GraphQLNamedType graphQLNamedType) {
            if (type instanceof GraphQLScalarType) {
                return graphQLNamedType.getName();
            } else {
                return String.format("[%s](%s%s.md)", graphQLNamedType.getName(), prefix, graphQLNamedType.getName());
            }
        } else if (type instanceof GraphQLList graphQLList) {
            return String.format("list of %s", getMarkdownLink(graphQLList.getWrappedType(), prefix));
        } else if (type instanceof GraphQLNonNull graphQLNonNull) {
            return String.format("%s", getMarkdownLink(graphQLNonNull.getWrappedType(), prefix));
        } else {
            return type.toString();
        }
    }

    private String getMarkdownLink(GraphQLFieldDefinition queryDefinition, String prefix) {
        return String.format("[%s](%s%s.md)", queryDefinition.getName(), prefix, queryDefinition.getName());
    }
}