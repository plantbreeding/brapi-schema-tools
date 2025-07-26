package org.brapi.schematools.core.openapi.comparator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openapitools.openapidiff.core.exception.RendererException;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.output.Render;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * A modified render for JSON that allows pretty print.
 */
public class JsonRender implements Render {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonRender(boolean prettyPrint) {
        this.objectMapper.setSerializationInclusion(Include.NON_NULL);
        if (prettyPrint) {
            this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        this.objectMapper.findAndRegisterModules();
    }

    public void render(ChangedOpenApi diff, OutputStreamWriter outputStreamWriter) {
        try {
            this.objectMapper.writeValue(outputStreamWriter, diff);
            outputStreamWriter.close();
        } catch (JsonProcessingException var4) {
            throw new RendererException("Could not serialize diff as JSON", var4);
        } catch (IOException var5) {
            throw new RendererException(var5);
        }
    }
}
