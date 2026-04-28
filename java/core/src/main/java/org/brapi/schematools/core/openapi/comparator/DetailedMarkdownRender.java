package org.brapi.schematools.core.openapi.comparator;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.openapidiff.core.model.ChangedApiResponse;
import org.openapitools.openapidiff.core.model.ChangedContent;
import org.openapitools.openapidiff.core.model.ChangedMediaType;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.model.ChangedOperation;
import org.openapitools.openapidiff.core.model.ChangedParameter;
import org.openapitools.openapidiff.core.model.ChangedParameters;
import org.openapitools.openapidiff.core.model.ChangedRequestBody;
import org.openapitools.openapidiff.core.model.ChangedResponse;
import org.openapitools.openapidiff.core.model.ChangedSchema;
import org.openapitools.openapidiff.core.model.Endpoint;
import org.openapitools.openapidiff.core.output.Render;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A detailed Markdown renderer for OpenAPI comparisons that explains exactly what
 * changed for each property (type, format, nullable, required, enum, etc.) rather
 * than only showing "Changed property X". Empty sections are suppressed entirely.
 */
public class DetailedMarkdownRender implements Render {

    @Override
    public void render(ChangedOpenApi diff, OutputStreamWriter outputStreamWriter) {
        try {
            outputStreamWriter.write(render(diff));
            outputStreamWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write comparison output", e);
        }
    }

    public String render(ChangedOpenApi diff) {
        StringBuilder sb = new StringBuilder();
        renderDeletedEndpoints(diff.getMissingEndpoints(), sb);
        renderNewEndpoints(diff.getNewEndpoints(), sb);
        renderChangedOperations(diff.getChangedOperations(), sb);
        return sb.toString();
    }

    // ── Deleted / New endpoints ──────────────────────────────────────────────

    private void renderDeletedEndpoints(List<Endpoint> endpoints, StringBuilder sb) {
        if (endpoints == null || endpoints.isEmpty()) return;
        sb.append("#### What's Deleted\n---\n\n");
        for (Endpoint e : endpoints) {
            sb.append(String.format("##### `%s` %s%n%n%n", e.getMethod().name(), e.getPathUrl()));
        }
    }

    private void renderNewEndpoints(List<Endpoint> endpoints, StringBuilder sb) {
        if (endpoints == null || endpoints.isEmpty()) return;
        sb.append("#### What's New\n---\n\n");
        for (Endpoint e : endpoints) {
            sb.append(String.format("##### `%s` %s%n%n%n", e.getMethod().name(), e.getPathUrl()));
        }
    }

    // ── Changed operations ───────────────────────────────────────────────────

    private void renderChangedOperations(List<ChangedOperation> ops, StringBuilder sb) {
        if (ops == null || ops.isEmpty()) return;
        sb.append("#### What's Changed\n---\n\n");
        for (ChangedOperation op : ops) {
            renderChangedOperation(op, sb);
        }
    }

    private void renderChangedOperation(ChangedOperation op, StringBuilder sb) {
        StringBuilder body = new StringBuilder();
        renderChangedParameters(op.getParameters(), body);
        renderChangedRequestBody(op.getRequestBody(), body);
        renderChangedResponses(op.getApiResponses(), body);
        if (body.length() > 0) {
            sb.append(String.format("##### `%s` %s%n%n%n", op.getHttpMethod().name(), op.getPathUrl()));
            sb.append(body);
        }
    }

    // ── Parameters ──────────────────────────────────────────────────────────

    private void renderChangedParameters(ChangedParameters params, StringBuilder sb) {
        if (params == null) return;
        StringBuilder body = new StringBuilder();

        if (params.getMissing() != null) {
            for (var p : params.getMissing()) {
                body.append(String.format("Deleted: `%s` in `%s`%n%n", p.getName(), p.getIn()));
            }
        }
        if (params.getIncreased() != null) {
            for (var p : params.getIncreased()) {
                body.append(String.format("Added: `%s` in `%s`%n%n", p.getName(), p.getIn()));
            }
        }
        if (params.getChanged() != null) {
            for (ChangedParameter p : params.getChanged()) {
                if (p.getSchema() != null) {
                    List<String> details = schemaChangeDetails(p.getSchema());
                    if (!details.isEmpty()) {
                        body.append(String.format("Changed: `%s` in `%s`:%n", p.getName(), p.getIn()));
                        for (String d : details) {
                            body.append("  - ").append(d).append("\n");
                        }
                        body.append("\n");
                    }
                }
            }
        }

        if (body.length() > 0) {
            sb.append("###### Parameters:\n\n").append(body);
        }
    }

    // ── Request body ────────────────────────────────────────────────────────

    private void renderChangedRequestBody(ChangedRequestBody body, StringBuilder sb) {
        if (body == null) return;
        String content = renderChangedContent(body.getContent());
        if (!content.isEmpty()) {
            sb.append("###### Request:\n\n").append(content);
        }
    }

    // ── Responses ───────────────────────────────────────────────────────────

    private void renderChangedResponses(ChangedApiResponse apiResponse, StringBuilder sb) {
        if (apiResponse == null) return;
        StringBuilder body = new StringBuilder();

        if (apiResponse.getMissing() != null) {
            apiResponse.getMissing().forEach((code, r) ->
                body.append(String.format("Deleted response : **%s**%n%n", code)));
        }
        if (apiResponse.getIncreased() != null) {
            apiResponse.getIncreased().forEach((code, r) ->
                body.append(String.format("New response : **%s**%n%n", code)));
        }
        if (apiResponse.getChanged() != null) {
            for (Map.Entry<String, ChangedResponse> entry : apiResponse.getChanged().entrySet()) {
                String contentStr = renderChangedContent(entry.getValue().getContent());
                if (!contentStr.isEmpty()) {
                    body.append(String.format("Changed response : **%s**%n%n", entry.getKey()));
                    body.append(contentStr);
                }
            }
        }

        if (body.length() > 0) {
            sb.append("###### Return Type:\n\n").append(body);
        }
    }

    // ── Content / media types ────────────────────────────────────────────────

    private String renderChangedContent(ChangedContent content) {
        if (content == null) return "";
        StringBuilder sb = new StringBuilder();

        if (content.getMissing() != null) {
            content.getMissing().keySet().forEach(mt ->
                sb.append(String.format("* Deleted content type : `%s`%n%n", mt)));
        }
        if (content.getIncreased() != null) {
            content.getIncreased().keySet().forEach(mt ->
                sb.append(String.format("* New content type : `%s`%n%n", mt)));
        }
        if (content.getChanged() != null) {
            for (Map.Entry<String, ChangedMediaType> entry : content.getChanged().entrySet()) {
                String schemaStr = renderChangedSchemaBlock(entry.getValue().getSchema(), "    ");
                if (!schemaStr.isEmpty()) {
                    sb.append(String.format("* Changed content type : `%s`%n%n", entry.getKey()));
                    sb.append(schemaStr);
                }
            }
        }
        return sb.toString();
    }

    // ── Schema rendering ─────────────────────────────────────────────────────

    /**
     * Renders a schema change block. Returns empty string if there is nothing visible to show.
     */
    private String renderChangedSchemaBlock(ChangedSchema schema, String indent) {
        if (schema == null) return "";
        StringBuilder sb = new StringBuilder();

        // New / removed properties
        if (schema.getIncreasedProperties() != null && !schema.getIncreasedProperties().isEmpty()) {
            sb.append(indent).append("New properties: ")
              .append(propertyNames(schema.getIncreasedProperties())).append("\n\n");
        }
        if (schema.getMissingProperties() != null && !schema.getMissingProperties().isEmpty()) {
            sb.append(indent).append("Deleted properties: ")
              .append(propertyNames(schema.getMissingProperties())).append("\n\n");
        }

        // Required list changes
        if (hasRequiredChanges(schema)) {
            var req = schema.getRequired();
            if (req.getIncreased() != null && !req.getIncreased().isEmpty()) {
                sb.append(indent).append("New required properties:\n");
                req.getIncreased().forEach(p -> sb.append(indent).append("- `").append(p).append("`\n"));
                sb.append("\n");
            }
            if (req.getMissing() != null && !req.getMissing().isEmpty()) {
                sb.append(indent).append("Removed required properties:\n");
                req.getMissing().forEach(p -> sb.append(indent).append("- `").append(p).append("`\n"));
                sb.append("\n");
            }
        }

        // Changed sub-properties — only show if sub-content is non-empty
        if (schema.getChangedProperties() != null) {
            for (Map.Entry<String, ChangedSchema> entry : schema.getChangedProperties().entrySet()) {
                String propContent = renderChangedProperty(entry.getKey(), entry.getValue(), indent);
                sb.append(propContent);
            }
        }

        // Array items
        if (schema.getItems() != null) {
            String itemsContent = renderChangedSchemaBlock(schema.getItems(), indent + "    ");
            if (!itemsContent.isEmpty()) {
                String typeLabel = schema.getType() != null ? " (" + schema.getType() + ")" : "";
                sb.append(indent).append("Changed items").append(typeLabel).append(":\n\n");
                sb.append(itemsContent);
            }
        }

        return sb.toString();
    }

    /**
     * Renders a single changed property entry. Returns empty string if nothing visible.
     */
    private String renderChangedProperty(String name, ChangedSchema schema, String indent) {
        List<String> details = schemaChangeDetails(schema);
        String subContent = renderChangedSchemaBlock(schema, indent + "    ");

        if (details.isEmpty() && subContent.isEmpty()) {
            return ""; // nothing to show — suppress entirely
        }

        StringBuilder sb = new StringBuilder();
        String typeStr = typeDescription(schema);
        if (!details.isEmpty()) {
            sb.append(String.format("%s* Changed property `%s`%s:%n",
                indent, name, typeStr.isEmpty() ? "" : " (" + typeStr + ")"));
            for (String detail : details) {
                sb.append(indent).append("  - ").append(detail).append("\n");
            }
            sb.append("\n");
        } else {
            sb.append(String.format("%s* Changed property `%s`%s%n%n",
                indent, name, typeStr.isEmpty() ? "" : " (" + typeStr + ")"));
        }
        sb.append(subContent);
        return sb.toString();
    }

    // ── Detail helpers ───────────────────────────────────────────────────────

    /**
     * Returns human-readable change details for a schema. Only reports values that actually differ.
     */
    private List<String> schemaChangeDetails(ChangedSchema schema) {
        List<String> details = new ArrayList<>();
        if (schema == null) return details;

        if (schema.isChangedType()) {
            String oldType = schema.getOldSchema() != null ? String.valueOf(schema.getOldSchema().getType()) : "?";
            String newType = schema.getNewSchema() != null ? String.valueOf(schema.getNewSchema().getType()) : "?";
            if (!oldType.equals(newType)) {
                details.add(String.format("Type changed: `%s` -> `%s`", oldType, newType));
            }
        }

        if (schema.isChangeFormat()) {
            String oldFmt = schema.getOldSchema() != null ? String.valueOf(schema.getOldSchema().getFormat()) : null;
            String newFmt = schema.getNewSchema() != null ? String.valueOf(schema.getNewSchema().getFormat()) : null;
            if (!Objects.equals(oldFmt, newFmt)) {
                details.add(String.format("Format changed: `%s` -> `%s`", oldFmt, newFmt));
            }
        }

        if (schema.getNullable() != null) {
            Boolean left = schema.getNullable().getLeft();
            Boolean right = schema.getNullable().getRight();
            if (!Objects.equals(left, right)) {
                details.add(String.format("Nullable changed: `%s` -> `%s`", left, right));
            }
        }

        if (schema.isChangeDefault()) {
            String oldDef = schema.getOldSchema() != null && schema.getOldSchema().getDefault() != null
                ? String.valueOf(schema.getOldSchema().getDefault()) : "none";
            String newDef = schema.getNewSchema() != null && schema.getNewSchema().getDefault() != null
                ? String.valueOf(schema.getNewSchema().getDefault()) : "none";
            if (!oldDef.equals(newDef)) {
                details.add(String.format("Default changed: `%s` -> `%s`", oldDef, newDef));
            }
        }

        if (schema.isChangeDeprecated()) {
            details.add("Deprecated status changed");
        }

        if (schema.getReadOnly() != null) {
            Boolean oldRo = schema.getOldSchema() != null ? schema.getOldSchema().getReadOnly() : null;
            Boolean newRo = schema.getNewSchema() != null ? schema.getNewSchema().getReadOnly() : null;
            if (!Objects.equals(oldRo, newRo)) {
                details.add(String.format("ReadOnly changed: `%s` -> `%s`", oldRo, newRo));
            }
        }

        if (schema.getWriteOnly() != null) {
            Boolean oldWo = schema.getOldSchema() != null ? schema.getOldSchema().getWriteOnly() : null;
            Boolean newWo = schema.getNewSchema() != null ? schema.getNewSchema().getWriteOnly() : null;
            if (!Objects.equals(oldWo, newWo)) {
                details.add(String.format("WriteOnly changed: `%s` -> `%s`", oldWo, newWo));
            }
        }

        if (schema.getEnumeration() != null) {
            var e = schema.getEnumeration();
            if (e.getIncreased() != null && !e.getIncreased().isEmpty()) {
                details.add("Added enum values: " + e.getIncreased());
            }
            if (e.getMissing() != null && !e.getMissing().isEmpty()) {
                details.add("Removed enum values: " + e.getMissing());
            }
        }

        if (schema.getMaxLength() != null) {
            var ml = schema.getMaxLength();
            if (!Objects.equals(ml.getOldValue(), ml.getNewValue())) {
                details.add(String.format("MaxLength changed: `%s` -> `%s`", ml.getOldValue(), ml.getNewValue()));
            }
        }

        if (schema.getNumericRange() != null) {
            var oldS = schema.getOldSchema();
            var newS = schema.getNewSchema();
            if (oldS != null && newS != null &&
                (!Objects.equals(oldS.getMinimum(), newS.getMinimum()) ||
                 !Objects.equals(oldS.getMaximum(), newS.getMaximum()))) {
                details.add(String.format("Numeric range changed: min `%s`->`%s`, max `%s`->`%s`",
                    oldS.getMinimum(), newS.getMinimum(), oldS.getMaximum(), newS.getMaximum()));
            }
        }

        if (schema.getPattern() != null) {
            var p = schema.getPattern();
            if (!Objects.equals(p.getOldPattern(), p.getNewPattern())) {
                details.add(String.format("Pattern changed: `%s` -> `%s`", p.getOldPattern(), p.getNewPattern()));
            }
        }

        if (schema.isDiscriminatorPropertyChanged()) {
            details.add("Discriminator property changed");
        }

        return details;
    }

    private boolean hasRequiredChanges(ChangedSchema schema) {
        if (schema == null || schema.getRequired() == null) return false;
        var req = schema.getRequired();
        return (req.getIncreased() != null && !req.getIncreased().isEmpty()) ||
               (req.getMissing() != null && !req.getMissing().isEmpty());
    }

    private String typeDescription(ChangedSchema schema) {
        if (schema == null) return "";
        String type = schema.getType();
        if (type == null && schema.getOldSchema() != null) {
            type = String.valueOf(schema.getOldSchema().getType());
        }
        if (type == null || type.equals("null")) return "";
        if (schema.isChangedType() && schema.getNewSchema() != null) {
            return type + " -> " + schema.getNewSchema().getType();
        }
        return type;
    }

    private String propertyNames(Map<String, Schema<?>> props) {
        if (props == null || props.isEmpty()) return "";
        return "`" + String.join("`, `", props.keySet()) + "`";
    }
}

