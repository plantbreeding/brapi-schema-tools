package org.brapi.schematools.core.examples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.brapi.schematools.core.response.Response;
import org.dflib.DataFrame;
import org.dflib.json.Json;

import java.util.HashMap;
import java.util.Map;

public class ExamplesGenerator {

    private final ExamplesGeneratorOptions options ;
    private final ObjectMapper mapper ;

    public ExamplesGenerator(ExamplesGeneratorOptions options) {
        this(options, new ObjectMapper());
    }

    public ExamplesGenerator(ExamplesGeneratorOptions options, ObjectMapper mapper) {
        this.mapper = mapper;
        this.options = options;
    }

    public Response<Map<String, JsonNode>> generate(Map<String, DataFrame> dataFrames) {
        return new Generator(dataFrames).generate();
    }

    private class Generator {

        Map<String, DataFrame> dataFrames ;
        Map<String, Response<JsonNode>> jsonNodes ;

        private Generator(Map<String, DataFrame> dataFrames) {
            this.dataFrames = dataFrames;
        }

        private Response<JsonNode> parse(Map.Entry<String, DataFrame> entry) {
            return parse(entry.getValue()) ;
        }

        private Response<JsonNode> parse(DataFrame dataFrame) {



            try {
                return Response.success(mapper.readTree(Json.saver().saveToString(dataFrame))) ;
            } catch (JsonProcessingException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        public Response<Map<String, JsonNode>> generate() {

            Map<String, JsonNode> examples = new HashMap<>() ;
            Response<Map<String, JsonNode>> response = Response.empty();

            this.dataFrames.forEach((key, value) -> parse(value)
                .onSuccessDoWithResult(jsonNode -> examples.put(key, jsonNode))
                .onFailDoWithResponse(response::mergeErrors));

            if (response.hasErrors()) {
                return response ;
            } else {
                return Response.success(examples) ;
            }
        }
    }
}
