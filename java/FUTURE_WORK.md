# Future Work

Deferred features, known limitations, and items that need attention in future
versions of `brapi-schema-tools`.

---

## 1. `actionProperties` not implemented in non-OpenAPI generators

The `brapi-metadata.actionProperties` mechanism — which declares
collection-scoped POST endpoints on a primary model (e.g. `POST
/variantsets/extract`) — is currently implemented only in `OpenAPIGenerator`.
The other generators do not yet:

- Emit action endpoints / functions / DDL / sheets / docs for action
  properties, AND
- Filter action properties out when iterating
  `BrAPIObjectType.getProperties()` for regular data field generation.

Until they do, action properties may incorrectly appear as ordinary data
fields in their output.

### Where filtering should be added

Suggested skip-filter: `type.getMetadata() == null ||
!type.getMetadata().isActionProperty(property.getName())`

| Generator | File | Lines | Notes |
|-----------|------|------:|-------|
| GraphQLGenerator | `core/src/main/java/org/brapi/schematools/core/graphql/GraphQLGenerator.java` | 538, 684, 768 | Input types for mutations and queries iterate properties |
| PythonGenerator | `core/src/main/java/org/brapi/schematools/core/python/PythonGenerator.java` | 291, 386 | Class model generation |
| RGenerator (R6) | `core/src/main/java/org/brapi/schematools/core/r6/RGenerator.java` | 156–159 | Request object arguments |
| XSSFWorkbookGenerator (XLSX) | `core/src/main/java/org/brapi/schematools/core/xlsx/XSSFWorkbookGenerator.java` | 131 | Sheet field list |
| OntModelGenerator | `core/src/main/java/org/brapi/schematools/core/ontmodel/OntModelGenerator.java` | 137, 206 | Ontology classes/properties |
| MarkdownGenerator | `core/src/main/java/org/brapi/schematools/core/markdown/MarkdownGenerator.java` | 97, 154, 156 | Per-class field documentation |
| SQLGenerator | `core/src/main/java/org/brapi/schematools/core/sql/SQLGenerator.java` (delegates to `ANSICreateTableDDLGenerator`) | — | Verify behaviour in DDL generator |

`TabularReportGenerator` (in the `analyse` module) does not consume BrAPI
schemas and is unaffected.

---

## 2. `BrAPIMetadata` field coverage across generators

The following matrix shows which fields of `BrAPIMetadata` each generator
currently consults. Empty cells indicate the generator ignores that field even
when present in the source schema, which may produce sub-optimal output for
schemas that rely on it.

| Generator | `primaryModel` | `request` | `parameters` | `response` | `interfaceClass` | `controlledVocabularyProperties` | `subQueryProperties` | `updatableProperties` | `writableProperties` | `noSingularizeProperties` | `actionProperties` | `discriminatorPropertyName` |
|-----------|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| OpenAPIGenerator | ✅ | — | — | ✅ | — | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| GraphQLGenerator | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — | — | — |
| PythonGenerator | ✅ | — | — | — | — | — | — | — | — | — | — | — |
| RGenerator (R6) | ✅ | — | — | — | — | — | — | — | — | — | — | — |
| SQLGenerator | ✅ | — | — | — | — | — | — | — | — | — | — | — |
| XSSFWorkbookGenerator (XLSX) | — | ✅ | ✅ | — | — | — | — | — | — | — | — | — |
| OntModelGenerator | — | ✅ | ✅ | — | — | — | — | — | — | — | — | — |
| MarkdownGenerator | — | — | — | — | — | — | — | — | — | — | — | — |

### Notable gaps

- `discriminatorPropertyName` is not consulted by any generator yet — it is
  parsed by `BrAPISchemaReader` but unused downstream.
- `controlledVocabularyProperties`, `updatableProperties`, `writableProperties`,
  and `subQueryProperties` are honoured only by OpenAPIGenerator. Other
  generators may produce incorrect output for schemas that use them.
- `MarkdownGenerator` consults no `BrAPIMetadata` fields. Generated docs may
  not reflect intended schema semantics (e.g. requests vs primary models).

---

## 3. `/observationlevels` endpoint — not generated

The reference BrAPI spec exposes `/observationlevels`, which is produced
manually from a hard-coded enumeration in the upstream OpenAPI. The generator
has no mechanism to emit a path that is decoupled from any primary model.
Deferred per user.

---

## 4. `searchResultsDbId` query parameter on `GET /observations/table`

The reference spec includes a `searchResultsDbId` query parameter on
`GET /observations/table`. The user is removing this from the reference spec
rather than reproducing it in the generator.

---

## 5. Table response shape variants (3-column / 4-column)

The generator currently produces a single table-response shape. The reference
spec has historically supported variants (e.g. 3-column vs 4-column layouts
for `ObservationUnit`). Deferred.

---

## 6. `seedlots/transactions` — 7 of 10 query parameters missing

The new `AbstractListOptions.useSubQueryPropertiesFor` mechanism enables 3 of
the 10 query parameters expected by the reference spec. The remaining 7
(`crossDbId`, `crossName`, `commonCropName`, `programDbId`, `germplasmDbId`,
`germplasmName`, external-ref params) would require source-schema changes.
Deferred per options-first preference.

---

## 7. PythonGenerator known broken tests

`PythonGeneratorTest.generate()` and `generateNotebooks()` fail with an NPE
in PedigreeNode handling. Pre-existing; needs further work in the Python
generator itself.

---

## 8. Stale test fixtures

`RGeneratorTest.generate()` and `SQLGeneratorTest.generateWithOverwrite()`
fail because expected fixtures still embed version `0.68.0` while the project
is at `0.77.0`. Fixtures need refreshing.
