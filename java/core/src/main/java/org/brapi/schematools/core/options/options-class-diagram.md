# BrAPI Schema Tools — Options Class Diagram

```mermaid
classDiagram
    direction TB

    %% ── Core interfaces / base classes ─────────────────────────────────────
    class Options {
        <<interface>>
        +getSchemaToolsVersion() String
    }

    class Validatable {
        <<interface>>
        +validate() Validation
    }

    Options --|> Validatable

    class AbstractGeneratorOptions {
        <<abstract>>
        -Boolean generate
        -String descriptionFormat
        -Map~String,Boolean~ generateFor
        -Map~String,String~ pluralFor
        +validate() Validation
        +override(AbstractGeneratorOptions)
        +isGenerating() boolean
        +isGeneratingFor(String) boolean
        +getPluralFor(String) String
    }
    AbstractGeneratorOptions ..|> Options

    class AbstractMainGeneratorOptions {
        <<abstract>>
        -BrAPISchemaReaderOptions brAPISchemaReader
        +validate() Validation
        +override(AbstractMainGeneratorOptions)
        #loadBrAPISchemaReaderOptions()$
    }
    AbstractMainGeneratorOptions --|> AbstractGeneratorOptions

    %% ── Sub-options base ───────────────────────────────────────────────────
    class AbstractSubOptions {
        <<abstract>>
        -String summaryFormat
        +validate() Validation
        +override(AbstractSubOptions)
        +getSummaryFor(String) String
    }
    AbstractSubOptions --|> AbstractGeneratorOptions

    %% ── REST generator base ─────────────────────────────────────────────────
    class AbstractRESTGeneratorOptions {
        <<abstract>>
        -Boolean overwrite
        -Boolean addGeneratorComments
        -Map~String,String~ pathItemNameFor
        -Map~String,Map~ pathItemNameForProperty
        +validate() Validation
        +override(AbstractRESTGeneratorOptions)
        +isOverwritingExistingFiles() boolean
        +isAddingGeneratorComments() boolean
        +getPathItemNameFor(String) String
        +getSearchPathItemNameFor(String) String
        +getPathItemNameForProperty(String,String) String
        +setPathItemNameForProperty(String,String,String)
        +getSubPathItemNameFor(String,BrAPIObjectProperty) String
        +isGeneratingSubPathFor()*
        +isGeneratingControlledVocabularyEndpoints()*
    }
    AbstractRESTGeneratorOptions --|> AbstractMainGeneratorOptions

    %% ── Generator Options (top-level) ───────────────────────────────────────
    class OpenAPIGeneratorOptions {
        -SingleGetOptions singleGet
        -ListGetOptions listGet
        -PostOptions post
        -PutOptions put
        -DeleteOptions delete
        -SearchOptions search
        -PropertiesOptions properties
        -ControlledVocabularyOptions controlledVocabulary
        -Boolean separateByModule
        -Boolean generateNewRequest
        -String newRequestNameFormat
        -String singleResponseNameFormat
        -String listResponseNameFormat
        -String searchRequestNameFormat
        -Map tagFor
        -Map supplementalSpecificationFor
        +load()$
        +override(OpenAPIGeneratorOptions)
        +validate() Validation
        +isSeparatingByModule() boolean
        +getTagFor(String) String
        +getNewRequestNameFor(String) String
    }
    OpenAPIGeneratorOptions --|> AbstractRESTGeneratorOptions

    class RGeneratorOptions {
        -SingleGetOptions singleGet
        -ListGetOptions listGet
        -PostOptions post
        -PutOptions put
        -DeleteOptions delete
        -SearchOptions search
        -PropertiesOptions properties
        -ControlledVocabularyOptions controlledVocabulary
        +load()$
        +override(RGeneratorOptions)
        +validate() Validation
        +isGeneratingFor(String) boolean
        +getSingularForProperty(String) String
    }
    RGeneratorOptions --|> AbstractRESTGeneratorOptions

    class PythonGeneratorOptions {
        -String entitiesDirectory
        -SingleGetOptions singleGet
        -ListGetOptions listGet
        -TableOptions table
        -PostOptions post
        -PutOptions put
        -DeleteOptions delete
        -SearchOptions search
        -PropertiesOptions properties
        -ControlledVocabularyOptions controlledVocabulary
        +load()$
        +override(PythonGeneratorOptions)
        +validate() Validation
        +isGeneratingFor(String) boolean
    }
    PythonGeneratorOptions --|> AbstractRESTGeneratorOptions

    class GraphQLGeneratorOptions {
        -InputOptions input
        -QueryTypeOptions queryType
        -MutationTypeOptions mutationType
        -PropertiesOptions properties
        -Boolean mergeOneOfType
        -Map mergingOneOfTypeFor
        +load()$
        +override(GraphQLGeneratorOptions)
        +validate() Validation
        +isGeneratingQueryType() boolean
        +isGeneratingMutationType() boolean
    }
    GraphQLGeneratorOptions --|> AbstractMainGeneratorOptions

    class MarkdownGeneratorOptions {
        -Boolean overwrite
        -Boolean addGeneratorComments
        -Boolean generateProperties
        -Boolean generateDuplicateProperties
        -Boolean generateParameterClasses
        -Boolean generateRequestClasses
        +load()$
        +override(MarkdownGeneratorOptions)
        +validate() Validation
    }
    MarkdownGeneratorOptions --|> AbstractMainGeneratorOptions

    class OntModelGeneratorOptions {
        -String name
        +load()$
        +override(OntModelGeneratorOptions)
    }
    OntModelGeneratorOptions --|> AbstractMainGeneratorOptions

    class SQLGeneratorOptions {
        -Boolean overwrite
        -Boolean addGeneratorComments
        -Boolean clustering
        -Boolean ifNotExists
        -Boolean dropTable
        -Boolean generateLinkTables
        -Boolean snakeCaseTableNames
        -PropertiesOptions properties
        -ControlledVocabularyOptions controlledVocabulary
        +load()$
        +override(SQLGeneratorOptions)
        +validate() Validation
    }
    SQLGeneratorOptions --|> AbstractMainGeneratorOptions

    class XSSFWorkbookGeneratorOptions {
        -List~ColumnOption~ dataClassProperties
        -List~String~ dataClassFieldHeaders
        -List~ColumnOption~ dataClassFieldProperties
        +load()$
        +override(XSSFWorkbookGeneratorOptions)
        +validate() Validation
    }
    XSSFWorkbookGeneratorOptions --|> AbstractMainGeneratorOptions

    %% ── Stand-alone options ─────────────────────────────────────────────────
    class GraphQLMarkdownGeneratorOptions {
        -Boolean overwrite
        -String queryDefinitionsDirectory
        +load()$
        +override(GraphQLMarkdownGeneratorOptions)
        +validate() Validation
    }
    GraphQLMarkdownGeneratorOptions ..|> Options

    class OpenAPIComparatorOptions {
        -String tempFilePrefix
        -AsciiDocOutputOptions asciiDoc
        -MarkdownOutputOptions markdown
        -JSONOutputOptions json
        -HTMLOutputOptions html
        +load()$
        +override(OpenAPIComparatorOptions)
        +validate() Validation
    }
    OpenAPIComparatorOptions ..|> Options

    %% ── OpenAPI sub-options ──────────────────────────────────────────────────
    class AbstractOpenAPISubOptions {
        <<abstract>>
    }
    AbstractOpenAPISubOptions --|> AbstractSubOptions

    class openapi_SingleGetOptions["SingleGetOptions"] {
    }
    openapi_SingleGetOptions --|> AbstractOpenAPISubOptions

    class openapi_ListGetOptions["ListGetOptions"] {
        -Boolean pagedDefault
        -Map paged
        -Boolean pagedTokenDefault
        -Map pagedToken
        -Map inputFor
        -Boolean propertiesFromRequest
    }
    openapi_ListGetOptions --|> AbstractOpenAPISubOptions

    class openapi_PostOptions["PostOptions"] {
    }
    openapi_PostOptions --|> AbstractOpenAPISubOptions

    class openapi_PutOptions["PutOptions"] {
        -Boolean multiple
        -Map multipleFor
    }
    openapi_PutOptions --|> AbstractOpenAPISubOptions

    class openapi_DeleteOptions["DeleteOptions"] {
    }
    openapi_DeleteOptions --|> AbstractOpenAPISubOptions

    class openapi_SearchOptions["SearchOptions"] {
    }
    openapi_SearchOptions --|> AbstractOpenAPISubOptions

    class openapi_ControlledVocabularyOptions["ControlledVocabularyOptions"] {
        -Boolean generate
        -String summaryFormat
        -String descriptionFormat
        -Map generateFor
    }
    openapi_ControlledVocabularyOptions ..|> Options

    %% ── R sub-options ────────────────────────────────────────────────────────
    class AbstractRGeneratorSubOptions {
        <<abstract>>
    }
    AbstractRGeneratorSubOptions --|> AbstractSubOptions

    class r_SingleGetOptions["SingleGetOptions"] {
    }
    r_SingleGetOptions --|> AbstractRGeneratorSubOptions

    class r_ListGetOptions["ListGetOptions"] {
    }
    r_ListGetOptions --|> AbstractRGeneratorSubOptions

    class r_PostOptions["PostOptions"] {
    }
    r_PostOptions --|> AbstractRGeneratorSubOptions

    class r_PutOptions["PutOptions"] {
        -Boolean multiple
        -Map multipleFor
    }
    r_PutOptions --|> AbstractRGeneratorSubOptions

    class r_DeleteOptions["DeleteOptions"] {
    }
    r_DeleteOptions --|> AbstractRGeneratorSubOptions

    class r_SearchOptions["SearchOptions"] {
    }
    r_SearchOptions --|> AbstractRGeneratorSubOptions

    class r_ControlledVocabularyOptions["ControlledVocabularyOptions"] {
        -Boolean generate
        -String summaryFormat
        -String descriptionFormat
        -Map generateFor
    }
    r_ControlledVocabularyOptions ..|> Options

    %% ── Python sub-options ───────────────────────────────────────────────────
    class AbstractPythonGeneratorSubOptions {
        <<abstract>>
    }
    AbstractPythonGeneratorSubOptions --|> AbstractSubOptions

    class py_SingleGetOptions["SingleGetOptions"] {
    }
    py_SingleGetOptions --|> AbstractPythonGeneratorSubOptions

    class py_ListGetOptions["ListGetOptions"] {
    }
    py_ListGetOptions --|> AbstractPythonGeneratorSubOptions

    class py_TableOptions["TableOptions"] {
    }
    py_TableOptions --|> AbstractPythonGeneratorSubOptions

    class py_PostOptions["PostOptions"] {
    }
    py_PostOptions --|> AbstractPythonGeneratorSubOptions

    class py_PutOptions["PutOptions"] {
    }
    py_PutOptions --|> AbstractPythonGeneratorSubOptions

    class py_DeleteOptions["DeleteOptions"] {
    }
    py_DeleteOptions --|> AbstractPythonGeneratorSubOptions

    class py_SearchOptions["SearchOptions"] {
    }
    py_SearchOptions --|> AbstractPythonGeneratorSubOptions

    class py_ControlledVocabularyOptions["ControlledVocabularyOptions"] {
        -Boolean generate
        -String summaryFormat
        -String descriptionFormat
        -Map generateFor
    }
    py_ControlledVocabularyOptions ..|> Options

    %% ── GraphQL sub-options ──────────────────────────────────────────────────
    class AbstractGraphQLOptions {
        <<abstract>>
        -Boolean pluralisingName
        -String nameFormat
        +validate() Validation
        +override(AbstractGraphQLOptions)
        +getNameFor(String) String
    }
    AbstractGraphQLOptions --|> AbstractGeneratorOptions

    class AbstractGraphQLQueryOptions {
        -String responseTypeNameFormat
        -Map input
        +validate() Validation
    }
    AbstractGraphQLQueryOptions --|> AbstractGraphQLOptions

    class SingleQueryOptions {
    }
    SingleQueryOptions --|> AbstractGraphQLOptions

    class ListQueryOptions {
        -String dataFieldName
    }
    ListQueryOptions --|> AbstractGraphQLQueryOptions

    class SearchQueryOptions {
        -String searchIdFieldName
    }
    SearchQueryOptions --|> AbstractGraphQLQueryOptions

    class CreateMutationOptions {
        -Boolean multiple
    }
    CreateMutationOptions --|> AbstractGraphQLOptions

    class UpdateMutationOptions {
        -Boolean multiple
    }
    UpdateMutationOptions --|> AbstractGraphQLOptions

    class DeleteMutationOptions {
        -Boolean multiple
    }
    DeleteMutationOptions --|> AbstractGraphQLOptions

    class QueryTypeOptions {
        -String name
        -Boolean partitionedByCrop
        -SingleQueryOptions singleQuery
        -ListQueryOptions listQuery
        -SearchQueryOptions searchQuery
    }
    QueryTypeOptions ..|> Options

    class MutationTypeOptions {
        -String name
        -CreateMutationOptions createMutation
        -UpdateMutationOptions updateMutation
        -DeleteMutationOptions deleteMutation
    }
    MutationTypeOptions ..|> Options

    class InputOptions {
        -String name
        -String nameFormat
    }
    InputOptions ..|> Options

    class graphql_PropertiesOptions["PropertiesOptions"] {
        -IdsOptions ids
    }
    graphql_PropertiesOptions --|> AbstractPropertiesOptions

    class IdsOptions {
        -String nameFormat
    }
    IdsOptions ..|> Options

    %% ── Shared properties options ────────────────────────────────────────────
    class AbstractPropertiesOptions {
        <<abstract>>
        -Map~String,Map~ linkTypeFor
        +validate() Validation
        +override(AbstractPropertiesOptions)
        +getLinkTypeFor(BrAPIObjectType,BrAPIObjectProperty)
    }
    AbstractPropertiesOptions ..|> Options

    class core_PropertiesOptions["PropertiesOptions"] {
        -String descriptionFormat
        -PropertyOptions id
        -PropertyOptions name
        -PropertyOptions pui
        +validate() Validation
        +override(AbstractPropertiesOptions)
        +getLinkPropertiesFor(BrAPIObjectType)
        +getIdPropertyNameFor(BrAPIType)
    }
    core_PropertiesOptions --|> AbstractPropertiesOptions

    class PropertyOptions {
        -String nameFormat
        -Boolean link
        -Map linkFor
        -Map nameFormatFor
    }
    PropertyOptions ..|> Options

    %% ── XLSX sub-options ─────────────────────────────────────────────────────
    class ValuePropertyOption {
        -String name
        -String key
        -Object defaultValue
    }
    ValuePropertyOption ..|> Options

    class ColumnOption {
        -String label
    }
    ColumnOption --|> ValuePropertyOption

    %% ── Comparator output options ────────────────────────────────────────────
    class AsciiDocOutputOptions {
    }
    AsciiDocOutputOptions ..|> Options

    class MarkdownOutputOptions {
    }
    MarkdownOutputOptions ..|> Options

    class JSONOutputOptions {
    }
    JSONOutputOptions ..|> Options

    class HTMLOutputOptions {
    }
    HTMLOutputOptions ..|> Options

    %% ── Composition links ────────────────────────────────────────────────────
    OpenAPIGeneratorOptions o-- openapi_SingleGetOptions : singleGet
    OpenAPIGeneratorOptions o-- openapi_ListGetOptions : listGet
    OpenAPIGeneratorOptions o-- openapi_PostOptions : post
    OpenAPIGeneratorOptions o-- openapi_PutOptions : put
    OpenAPIGeneratorOptions o-- openapi_DeleteOptions : delete
    OpenAPIGeneratorOptions o-- openapi_SearchOptions : search
    OpenAPIGeneratorOptions o-- core_PropertiesOptions : properties
    OpenAPIGeneratorOptions o-- openapi_ControlledVocabularyOptions : controlledVocabulary

    RGeneratorOptions o-- r_SingleGetOptions : singleGet
    RGeneratorOptions o-- r_ListGetOptions : listGet
    RGeneratorOptions o-- r_PostOptions : post
    RGeneratorOptions o-- r_PutOptions : put
    RGeneratorOptions o-- r_DeleteOptions : delete
    RGeneratorOptions o-- r_SearchOptions : search
    RGeneratorOptions o-- core_PropertiesOptions : properties
    RGeneratorOptions o-- r_ControlledVocabularyOptions : controlledVocabulary

    PythonGeneratorOptions o-- py_SingleGetOptions : singleGet
    PythonGeneratorOptions o-- py_ListGetOptions : listGet
    PythonGeneratorOptions o-- py_TableOptions : table
    PythonGeneratorOptions o-- py_PostOptions : post
    PythonGeneratorOptions o-- py_PutOptions : put
    PythonGeneratorOptions o-- py_DeleteOptions : delete
    PythonGeneratorOptions o-- py_SearchOptions : search
    PythonGeneratorOptions o-- core_PropertiesOptions : properties
    PythonGeneratorOptions o-- py_ControlledVocabularyOptions : controlledVocabulary

    GraphQLGeneratorOptions o-- InputOptions : input
    GraphQLGeneratorOptions o-- QueryTypeOptions : queryType
    GraphQLGeneratorOptions o-- MutationTypeOptions : mutationType
    GraphQLGeneratorOptions o-- graphql_PropertiesOptions : properties

    QueryTypeOptions o-- SingleQueryOptions : singleQuery
    QueryTypeOptions o-- ListQueryOptions : listQuery
    QueryTypeOptions o-- SearchQueryOptions : searchQuery

    MutationTypeOptions o-- CreateMutationOptions : createMutation
    MutationTypeOptions o-- UpdateMutationOptions : updateMutation
    MutationTypeOptions o-- DeleteMutationOptions : deleteMutation

    graphql_PropertiesOptions o-- IdsOptions : ids

    core_PropertiesOptions o-- PropertyOptions : id
    core_PropertiesOptions o-- PropertyOptions : name
    core_PropertiesOptions o-- PropertyOptions : pui

    SQLGeneratorOptions o-- core_PropertiesOptions : properties
    SQLGeneratorOptions o-- openapi_ControlledVocabularyOptions : controlledVocabulary

    XSSFWorkbookGeneratorOptions o-- ColumnOption : dataClassProperties
    XSSFWorkbookGeneratorOptions o-- ColumnOption : dataClassFieldProperties

    OpenAPIComparatorOptions o-- AsciiDocOutputOptions : asciiDoc
    OpenAPIComparatorOptions o-- MarkdownOutputOptions : markdown
    OpenAPIComparatorOptions o-- JSONOutputOptions : json
    OpenAPIComparatorOptions o-- HTMLOutputOptions : html
```

## Package layout

| Package | Classes |
|---|---|
| `core.options` | `Options`, `AbstractGeneratorOptions`, `AbstractMainGeneratorOptions`, `AbstractSubOptions`, `AbstractRESTGeneratorOptions`, `AbstractPropertiesOptions`, `PropertiesOptions`, `PropertyOptions`, `LinkType` |
| `core.openapi.generator.options` | `OpenAPIGeneratorOptions`, `AbstractOpenAPISubOptions`, `SingleGetOptions`, `ListGetOptions`, `PostOptions`, `PutOptions`, `DeleteOptions`, `SearchOptions`, `ControlledVocabularyOptions` |
| `core.r6.options` | `RGeneratorOptions`, `AbstractRGeneratorSubOptions`, `SingleGetOptions`, `ListGetOptions`, `PostOptions`, `PutOptions`, `DeleteOptions`, `SearchOptions`, `ControlledVocabularyOptions` |
| `core.python.options` | `PythonGeneratorOptions`, `AbstractPythonGeneratorSubOptions`, `SingleGetOptions`, `ListGetOptions`, `TableOptions`, `PostOptions`, `PutOptions`, `DeleteOptions`, `SearchOptions`, `ControlledVocabularyOptions` |
| `core.graphql.options` | `GraphQLGeneratorOptions`, `AbstractGraphQLOptions`, `AbstractGraphQLQueryOptions`, `SingleQueryOptions`, `ListQueryOptions`, `SearchQueryOptions`, `CreateMutationOptions`, `UpdateMutationOptions`, `DeleteMutationOptions`, `QueryTypeOptions`, `MutationTypeOptions`, `InputOptions`, `IdsOptions`, `PropertiesOptions` |
| `core.markdown.options` | `MarkdownGeneratorOptions`, `GraphQLMarkdownGeneratorOptions` |
| `core.ontmodel.options` | `OntModelGeneratorOptions` |
| `core.sql.options` | `SQLGeneratorOptions` |
| `core.xlsx.options` | `XSSFWorkbookGeneratorOptions`, `ValuePropertyOption`, `ColumnOption` |
| `core.openapi.comparator.options` | `OpenAPIComparatorOptions`, `AsciiDocOutputOptions`, `MarkdownOutputOptions`, `JSONOutputOptions`, `HTMLOutputOptions` |

