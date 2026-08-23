package org.brapi.schematools.core.openapi.comparator;

import org.brapi.schematools.core.openapi.comparator.options.OpenAPIComparatorOptions;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Test;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression: equivalent nullable reference encodings must not produce false type diffs.
 *
 * Hand-authored OpenAPI 3.1 style:
 *   coordinates: anyOf: [ { $ref: geoJSONSearchArea }, { type: null } ]
 *
 * Schema-generated style:
 *   coordinates: nullable: true, allOf: [ { $ref: geoJSONSearchArea } ]
 */
class OpenAPIComparatorNullableSchemaTest {

    @Test
    void equivalentNullableRefEncodingsAreNotReportedAsTypeChange() throws Exception {
        OpenAPIComparatorOptions options = OpenAPIComparatorOptions.load()
            .setIgnoreDescriptions(true)
            .setNormalizeNullableSchemas(true);

        OpenAPIComparator comparator = new OpenAPIComparator(options);

        Path anyOf = Path.of(ClassLoader.getSystemResource("OpenAPIComparator/nullable-ref-anyof.json").toURI());
        Path allOf = Path.of(ClassLoader.getSystemResource("OpenAPIComparator/nullable-ref-allof.json").toURI());

        Response<ChangedOpenApi> response = comparator.openApiCompare(anyOf, allOf);
        assertFalse(response.hasErrors(), () -> response.getMessagesCombined(","));

        Path markdownOut = Path.of("build/test-output/OpenAPIComparator/nullable-ref-equivalent.md");
        Files.createDirectories(markdownOut.getParent());
        Response<Path> rendered = comparator.compare(anyOf, allOf, markdownOut, ComparisonOutputFormat.MARKDOWN);
        assertFalse(rendered.hasErrors(), () -> rendered.getMessagesCombined(","));

        String markdown = Files.readString(markdownOut, StandardCharsets.UTF_8);
        assertFalse(markdown.contains("Type changed"),
            "equivalent nullable $ref encodings must not report Type changed; was:\n" + markdown);
        assertFalse(markdown.toLowerCase().contains("object") && markdown.toLowerCase().contains("`null`")
                && markdown.contains("Type changed"),
            "must not report object -> null for coordinates");
        assertFalse(containsCoordinatesTypeChange(markdown),
            "coordinates must not appear as a type change; was:\n" + markdown);

        ChangedOpenApi diff = response.getResult();
        assertTrue(diff.isCompatible() || isEmptyDiff(diff),
            "equivalent nullable encodings should be compatible or empty; changedOps="
                + size(diff.getChangedOperations()) + " changedSchemas=" + size(diff.getChangedSchemas()));
    }

    @Test
    void genuineNullabilityDifferenceIsStillReported() throws Exception {
        OpenAPIComparatorOptions options = OpenAPIComparatorOptions.load()
            .setIgnoreDescriptions(true)
            .setNormalizeNullableSchemas(true);

        OpenAPIComparator comparator = new OpenAPIComparator(options);

        Path anyOf = Path.of(ClassLoader.getSystemResource("OpenAPIComparator/nullable-ref-anyof.json").toURI());
        Path required = Path.of(ClassLoader.getSystemResource("OpenAPIComparator/nullable-ref-required.json").toURI());

        Path markdownOut = Path.of("build/test-output/OpenAPIComparator/nullable-ref-genuine.md");
        Files.createDirectories(markdownOut.getParent());
        Response<Path> rendered = comparator.compare(anyOf, required, markdownOut, ComparisonOutputFormat.MARKDOWN);
        assertFalse(rendered.hasErrors(), () -> rendered.getMessagesCombined(","));

        String markdown = Files.readString(markdownOut, StandardCharsets.UTF_8);
        assertTrue(
            markdown.toLowerCase().contains("nullable")
                || markdown.contains("Type changed")
                || markdown.contains("coordinates")
                || markdown.toLowerCase().contains("what's changed")
                || markdown.toLowerCase().contains("changed"),
            "nullable vs non-nullable coordinates must still be reported; was:\n" + markdown);

        Response<ChangedOpenApi> response = comparator.openApiCompare(anyOf, required);
        assertFalse(response.hasErrors(), () -> response.getMessagesCombined(","));
        ChangedOpenApi diff = response.getResult();
        assertFalse(isEmptyDiff(diff) && diff.isCompatible(),
            "genuine nullability difference must not produce an empty compatible diff");
    }

    private static boolean containsCoordinatesTypeChange(String markdown) {
        String lower = markdown.toLowerCase();
        if (!lower.contains("coordinates")) {
            return false;
        }
        return lower.contains("type changed") || lower.contains("object -> null") || lower.contains("`object` -> `null`");
    }

    private static boolean isEmptyDiff(ChangedOpenApi diff) {
        return size(diff.getChangedOperations()) == 0
            && size(diff.getChangedSchemas()) == 0
            && size(diff.getMissingEndpoints()) == 0
            && size(diff.getNewEndpoints()) == 0;
    }

    private static int size(java.util.Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }
}
