# Changelog

All notable changes to this project will be documented in this file.
Changes are grouped by version, reconstructed from git history.
Versions correspond to Maven Central releases of `org.brapi:brapi-schema-tools-*`.

---

## [0.92.0] — 2026-08-25

### Added
- **PUT response-only embedded properties**: `put.embeddedResponsePropertiesFor` can add selected embedded properties to a PUT response without changing the PUT request body or shared response schemas.

---

## [0.91.0] — 2026-08-24

### Added
- **GET-by-ID response-only embedded properties**: `getWithId.embeddedResponsePropertiesFor` can add selected embedded properties to a GET response without changing shared request schemas or PUT request bodies.

### Changed
- **BrAPI OpenAPI defaults**: default options now preserve Call/Variant page tokens on nested collection paths, use compact embedded/omitted ReferenceSet relationship projections, and relax embedded Method/Trait primary-ID requirements for Attribute and ObservationVariable shapes.

---

## [0.90.0] — 2026-08-24

### Added
- **Nested collection page-token overrides**: new `get.subPathPagedToken` map lets nested/sub-path collection endpoints (e.g. `/callsets/{callSetDbId}/calls`) include a `pageToken` query parameter independently of the top-level list endpoint. When unset, nested paths fall back to the existing `get.pagedToken` / `pagedTokenDefault` setting via `GetOptions.hasSubPathPageTokenFor(...)`.
- **Embedded primary-model ID projections**: new `properties.embeddedPrimaryIdOptionalFor` map lets OpenAPI generation inline selected embedded primary models with their identifier removed from `required`. This supports compact embedded request/response shapes while leaving the canonical JSON Schema and standalone primary-model OpenAPI components unchanged.

---

## [0.89.0] — 2026-08-24

### Fixed
- **OpenAPI list GET query parameters are non-nullable**: generated query-parameter schemas now clear `nullable` / `type: null` so list filters match the hand-authored OpenAPI style (parameters remain optional via omission, not explicit null).

---

## [0.88.0] — 2026-08-24

### Fixed
- **OpenAPI comparator `allOf` inheritance**: before comparison, local component-schema `$ref`/`allOf` compositions are flattened by merging inherited `properties` and `required` fields. Equivalent flattened and composed schemas no longer produce false endpoint-wide deletions, such as `metadata.datafiles`, `metadata.pagination`, and `metadata.status`.
- **OpenAPI comparator request aliases**: pure local component aliases, such as `MethodNewRequest` referring only to `MethodBaseClass`, are dereferenced before comparison. Equivalent inline generated request schemas no longer report their inherited fields as additions.
- **OpenAPI table responses**: generated table endpoints now reference their configured `*Table` schema in the response `result`, rather than the primary entity schema.

### Added
- **Response token pagination metadata**: response classes can declare `brapi-metadata.tokenPagination: true`; generated OpenAPI wrappers then reference `metadataTokenPagination` instead of ordinary `metadata`.
- **Generic token-paginated list responses**: primary models configured with `pagedToken` now generate list-response wrappers referencing `metadataTokenPagination`, including `VariantListResponse`.

---

## [0.85.0] — 2026-08-18

### Added
- New Markdown options for interface and response class generation
- `BrAPINullType` model for explicit JSON Schema `null` union members
- OpenAPI comparator option `normalizeNullableSchemas` (default `true`) to canonicalise equivalent nullable `$ref` encodings before diffing

### Fixed
- Markdown Generator now only gererates files for primary classes. Response, Request, Parameter and Interface classes are only generated 
if explicitly included in the Markdown options. 
- **Nullable many-to-one primary-model references keep ID-link projection**: when a property is a nullable union of exactly one primary-model reference plus `null` and the relationship link type is `id`, OpenAPI generation emits the existing `*DbId` / `*Name` link fields (still nullable) instead of an embedded object/`allOf` `$ref`. Other nullable embeds, primitives, arrays, response wrappers, and multi-member `oneOf`/`anyOf` schemas are unchanged.
- **OpenAPI comparator nullable `$ref` equivalence**: `anyOf`/`oneOf` with a single non-null member plus `{type: null}`, `type: [T, "null"]`, and bare `$ref` + `nullable: true` are normalised to `allOf` + `nullable: true` before comparison, so equivalent encodings (e.g. hand-authored `geoJSONSearchArea` anyOf vs generated allOf) no longer report false `Type changed: object -> null`. Genuine nullable vs non-nullable differences are still reported.


---

## [0.84.0] — 2026-08-12

### Added
- **Selective property exclusion from generated POST/PUT request schemas**: new `propertyFromSchemaFor` option on the update sub-options (e.g. `post.propertyFromSchemaFor` / `put.propertyFromSchemaFor`) lets you omit specific properties — keyed by type and property name, with dot-notation for nested properties (e.g. `audit.personName`) — from the generated NewRequest/request-body schema, including their `required` entries. All properties are included by default unless a property is explicitly set to `false`.

---

## [0.83.0] — 2026-08-07

### Added
- **Deprecated property propagation**: properties marked `deprecated` in the source schema now emit `deprecated: true` in the generated OpenAPI, for both link properties and composite/embedded properties

### Fixed
- **Nullable `$ref` schemas preserved**: `makeNullable` now wraps a `$ref` schema in `allOf` with `nullable: true` (OpenAPI 3.0 disallows sibling keywords alongside `$ref`), so nullability is no longer lost on referenced schemas
- Non-link nullable properties now correctly emit `nullable: true`
- `PropertyOptions.requiredPropertyFor` / `nullablePropertyFor` now honour an explicit `false` entry in the options map instead of falling through to the default value

---

## [0.82.0] — 2026-08-05

### Added
- **Request property inclusion in GET endpoints**: GET endpoint generation now honours the `propertiesFromRequest` / `propertyFromRequestFor` options to select which request properties are included (previously available only for search/table endpoints)

### Changed
- Test comparison utility (`assertMultilineEqual`) now ignores the embedded `Generated by Schema Tools Generator Version:` header line, so generated-output fixtures no longer need regenerating on every version bump

### Fixed
- `OpenAPIGenerator` now returns an empty list on success when adding parameters, instead of yielding an incorrect result
- Improved validation of referenced attributes in `BrAPIObjectType`
- Corrected `nullable` property documentation in `PropertyOptions`

---

## [0.81.0] — 2026-08-04

### Fixed
- Schema directory search depth increased to `Integer.MAX_VALUE` so schemas in deeply nested directories are retrieved during generation

---

## [0.80.0] — 2026-07-30

### Fixed
- **`PropertyOptions.override` now adds new parent-type keys** for the nested `linkPropertyFor`, `requiredPropertyFor` and `nullablePropertyFor` maps. Previously an override entry whose top-level key (the parent BrAPI type) did not already exist in the default options was silently discarded, so per-relationship link/required/nullable suppressions could only be declared for parent types already present in the defaults. Renamed or newly added parent types (e.g. a `SeedLot` → `InventoryLot` rename) can now register these overrides via the options file.

---

## [0.79.0]

### Added
- **Multi-supplemental OpenAPI spec support**: `supplementalSpecifications` (list) option on `OpenAPIGeneratorOptions` allows multiple supplemental files to be merged into a single generation run; existing `supplementalSpecification` (single string) is still supported
- **Search-table endpoints**: new `POST /search/<entity>/table` endpoint type controlled by `SearchTableOptions` (`searchTable.generateFor`); supports configurable `searchTableResponseNameFormat` and `searchTableRequestNameFormat` options; request body schema is generated from the entity's Request class with per-property filtering via `propertiesFromRequest` / `propertyFromRequestFor` options
- **`AbstractRequestFilterOptions`**: new abstract base class (extracted from `AbstractListOptions`) providing `propertiesFromRequest` and `propertyFromRequestFor` options to control which request properties appear in a generated request body schema; `SearchTableOptions` extends this class

### Changed
- OpenAPI spec version is now always set to **3.1** regardless of the BrAPI version string; the previous regex-based version detection logic has been removed
- `additionalProperties` JSON Schema keyword is now correctly handled: schemas may carry multiple types, and `BrAPIAdditionalProperties` is read properly by `BrAPISchemaReader`

---

## [0.78.0] — 2026-06-27

### Fixed
- Added prefix for constraints in generated SQL output

---

## [0.77.0] — 2026-06-06

### Added
- **Action endpoints**: new `actions` mechanism for generating `POST /<entity>/<actionName>` endpoints (e.g. `POST /variantsets/extract`); controlled via `ActionsOptions` with `pathFormat`, `actionSummaryFormat`, `actionDescriptionFormat`, `actionRequestNameFormat`
- **Table endpoints**: new `table` options block to generate `GET /<entity>/table` returning `text/csv`; opt-in via `table.generateFor`
- **Bulk delete endpoints**: new `delete.bulkGenerateFor` option generates `POST /<entity>` bulk-delete paths with configurable path/summary/description/response name formats
- `singleAlsoFor` option on update sub-options to generate a single-entity PUT alongside multi-entity PUT (e.g. `PUT /observations/{observationDbId}`)
- `noSingularizeProperties` flag in BrAPI metadata
- `pattern` and `discriminatorPropertyName` fields added to schema models
- 404 Not Found responses across a large number of OpenAPI endpoints; per-verb and per-type `addNotFoundResponse` and `addNotFoundResponseFor` options
- Supplemental OpenAPI file handling improved: supplemental changes can now override generated objects

### Changed
- Schema definitions refactored: `anyOf` replaced with `allOf` + `nullable` for `season` and `validValues` properties

---

## [0.76.0] — 2026-06-02

### Changed
- SQL generator: removed embedded object links to parent tables

---

## [0.75.0] — 2026-05-29

### Added
- Composite description builder for array link properties in SQL DDL generation

### Changed
- DDL generation refactored to better handle link properties and prevent name clashes
- Enhanced link property generation for name equality edge cases

---

## [0.74.0] — 2026-05-24

### Changed
- SQL generation improvements (general updates to column generation)

---

## [0.73.0] — 2026-05-23

### Fixed
- Nullable and `required` field handling improved
- Nullable override applied when a property's type class is itself nullable

---

## [0.72.0] — 2026-05-21

### Changed
- Updated `isLinkForTypeOrProperty` logic for more accurate link detection

---

## [0.71.0] — 2026-05-21

### Added
- `ignoreDeprecatedSchemas: true` and `ignoreDeprecatedProperties: true` options in schema reader

---

## [0.70.0] — 2026-05-20

### Fixed
- Override handling for valid schema classes

---

## [0.69.0] — 2026-05-20

### Added
- `ServerInfo` added to the list of valid schema classes

---

## [0.68.0] — 2026-05-19

### Added
- BRAVA-Tools integration: new getters on model objects needed by BRAVA-Tools; options updated
- `NoAuthorizationProvider` use case now handled gracefully
- Conditional list handling added to `SearchOptions` and `Response` classes
- Conditional merging methods added to `Response`; `OpenAPIGenerator` updated accordingly
- Default `analyse-options.yaml` resource updated to fix breaking schema checks
- Dependencies updated to align with BRAVA-Tools

### Changed
- `SingleGet` renamed to `GetWithId` throughout
- `ListGetOptions` renamed to `GetOptions` throughout

### Fixed
- Nullable and external `$ref` handling fixed

---

## [0.67.0] — 2026-05-02

*Note: 0.66.0 was an intermediate development version and was not released as a standalone tag.*

### Fixed
- Map retrieval methods now handle null values and fall back to defaults
- Override methods no longer retain null entries in maps
- Maven Central publishing fixed (Sonatype S01 host configuration, vanniktech plugin classpath)
- Gradle setup action updated to v4

---

## [0.65.0] — 2026-05-01

### Changed
- GraphQL output types refactored to use named types; improved schema generation overall

---

## [0.64.0] — 2026-04-28

### Added
- `additionalInfo` and `KeyValuePair` support in BrAPI schema (temporary fix; improvement planned)

---

## [0.63.0] — 2026-04-28

### Added
- `BrAPIAdditionalProperties` class; integrated into OpenAPI schema generation
- `additionalProperties` support in OpenAPI generation options and Markdown rendering
- Option to ignore description fields during OpenAPI comparison

### Changed
- `BrAPISchemaReader` enhanced with additional validation and support for `anyOf` types
- Query options validation now includes superclass validation

---

## [0.62.0] — 2026-04-05

### Added
- SQL table properties organised into primary, link, and clustering categories
- SQL column definition generation groups properties for improved clarity and query performance
- `IF EXISTS` clause on SQL `ALTER TABLE` constraint statements for safer execution
- Conditional constraint support in `ALTER TABLE` statements
- Configurable constraint suppression for `ARRAY<STRUCT>` column types

### Changed
- SQL table comments improved for clarity and consistency
- JSON schemas updated with deprecation flags and improved property descriptions

---

## [0.61.0] — 2026-04-01

### Added
- SQL table generation: option to suppress constraints within `ARRAY<STRUCT>` types

---

## [0.60.0] — 2026-03-31

### Added
- SQL clustering columns include `studyType` and `studyCode`
- Primary key, foreign key, and NOT NULL constraint generation for SQL tables

---

## [0.59.0] — 2026-03-24

### Changed
- Link property handling enhanced in BrAPI object generation

---

## [0.58.0] — 2026-03-19

### Added
- Jupyter notebook generation for primary entities (`generateNotebooks` option)
- Notebook example arguments converted to single-quoted strings for consistency
- Updated Python package structure with new sub-directories

### Changed
- `toSnakeCase` method refined for better acronym boundary detection
- Python client generation and type handling improved
- BrAPI class caching and dependency management refactored
- REST generator options and endpoint validation enhanced
- `searchTable` options added; Python generator updated for new endpoint handling

---

## [0.57.0] — 2026-02-24

### Fixed
- SQL DDL now emits `DROP TABLE IF EXISTS` before `CREATE TABLE` for cleaner re-runs

---

## [0.56.0] — 2026-02-07

### Added
- `snakeCaseTableNames` option for SQL generation
- `pluralTableNames` option for SQL generation

---

## [0.55.0] — 2026-02-03

### Added
- New tests and options for SQL generation

---

## [0.54.0] — 2026-02-02

### Fixed
- Use absolute paths when writing SQL output files

---

## [0.53.0] — 2026-02-02

### Fixed
- Duplicate property issue in generated SQL output

---

## [0.52.0] — 2026-02-02

### Fixed
- BrAPI options parsing issue resolved

---

## [0.51.0] — 2026-02-01

*Maintenance release — test improvements.*

---

## [0.50.0] — 2026-02-01

*Maintenance/version bump release.*

---

## [0.49.0] — 2026-01-29

### Added
- R package generation: added `@family` and `@keywords` roxygen annotations
- R generation: improved `toSnakeCase` conversion

### Fixed
- `GraphQLGenerator` no longer creates `GraphQLUnion` types for parameter and request types

---

## [0.48.0] — 2026-01-29

### Added
- OpenAPI / GraphQL comparison: compare across directories
- Better output organisation — results written to separate files per comparison
- Improved generator and comparator internals

---

## [0.47.0] — 2026-01-29

*Version maintenance release.*

---

## [0.46.0] — 2026-01-28

### Fixed
- CLI version string fixed
- Minor bug fixes

---

## [0.45.0] — 2026-01-22

### Added
- Examples generator (generate example payloads)
- GraphQL comparison test

### Fixed
- Generator options parsing fix

---

## [0.44.0] — 2026-01-03

### Added
- Output directory deleted recursively before generation when overwrite is enabled

---

## [0.43.0] — 2026-01-03

### Added
- `version` sub-command added to CLI
- logback logging configuration scoped to CLI module only

---

## [0.42.0] — 2026-01-03

*Version/maintenance release.*

---

## [0.41.0] — 2026-01-03

### Fixed
- Generation options corrections

---

## [0.40.0] — 2026-01-02

### Added
- Initial SQL generation from JSON Schema (`generate -l SQL`)
- Improved property description generation
- Markdown generator options (`--overwrite` fix)
- Example data from BrAPI test fixtures

---

## [0.39.0] — 2025-10-20

### Added
- `version` command in CLI
- Improved documentation generation output
- Password and client-ID support for secured endpoint queries

---

## [0.38.0] — 2025-10-15

### Added
- Bearer authentication support for Markdown generation from GraphQL schema; auth handling refactored

---

## [0.37.0] — 2025-09-14

### Added
- Comparison endpoint filters
- Initial (untested) Controlled Vocabulary support
- Supplemental OpenAPI spec support: include hand-crafted paths/schemas that cannot be auto-generated

### Changed
- Generator output made more consistent

---

## [0.36.0] — 2025-08-05

Initial public release. Established Maven Central publishing workflow.

### Capabilities at initial release
- OpenAPI 3.0 JSON generation from BrAPI JSON Schema
- GraphQL schema generation
- OWL / OntModel generation
- Markdown documentation generation
- Compare generated OpenAPI against a reference spec
- Analyse sub-command for schema analysis
- CLI with `generate`, `compare`, `analyse`, `validate`, `markdown`, `examples` sub-commands
