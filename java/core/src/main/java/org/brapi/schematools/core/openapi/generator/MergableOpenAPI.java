package org.brapi.schematools.core.openapi.generator;

import io.swagger.v3.oas.models.OpenAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergableOpenAPI extends OpenAPI {

    public MergableOpenAPI() {
        super();
    }

    public MergableOpenAPI(OpenAPI existingOpenAPI){
        super();
        this.merge(existingOpenAPI);
    }

    public MergableOpenAPI merge(OpenAPI incoming) {

        if (this.getComponents() != null && incoming.getComponents() != null) {
            mergeMaps(this.getComponents().getCallbacks(), incoming.getComponents().getCallbacks());
            mergeMaps(this.getComponents().getExamples(), incoming.getComponents().getExamples());
            mergeMaps(this.getComponents().getExtensions(), incoming.getComponents().getExtensions());
            mergeMaps(this.getComponents().getHeaders(), incoming.getComponents().getHeaders());
            mergeMaps(this.getComponents().getLinks(), incoming.getComponents().getLinks());
            mergeMaps(this.getComponents().getParameters(), incoming.getComponents().getParameters());
            mergeMaps(this.getComponents().getPathItems(), incoming.getComponents().getPathItems());
            mergeMaps(this.getComponents().getRequestBodies(), incoming.getComponents().getRequestBodies());
            mergeMaps(this.getComponents().getResponses(), incoming.getComponents().getResponses());
            mergeMaps(this.getComponents().getSchemas(), incoming.getComponents().getSchemas());
            mergeMaps(this.getComponents().getSecuritySchemes(), incoming.getComponents().getSecuritySchemes());
        }

        mergeMaps(this.getExtensions(), incoming.getExtensions());
        if(incoming.getExternalDocs() != null)
            this.setExternalDocs(incoming.getExternalDocs());
        if(incoming.getInfo() != null)
            this.setInfo(incoming.getInfo());
        if(incoming.getJsonSchemaDialect() != null)
            this.setJsonSchemaDialect(incoming.getJsonSchemaDialect());
        if(incoming.getOpenapi() != null)
            this.setOpenapi(incoming.getOpenapi());
        mergeMaps(this.getPaths(), incoming.getPaths());
        mergeLists(this.getSecurity(), incoming.getSecurity());
        mergeLists(this.getServers(), incoming.getServers());
        if(incoming.getSpecVersion() != null)
            this.setSpecVersion(incoming.getSpecVersion());
        mergeLists(this.getTags(), incoming.getTags());
        mergeMaps(this.getWebhooks(), incoming.getWebhooks());

        return this;
    }

    private <K, V> void mergeMaps(Map<K, V> base, Map<K, V> incoming){
        if (incoming == null || incoming.isEmpty()){
            return;
        }

        if (base == null){
            base = new HashMap<>();
        }

        base.putAll(incoming);
    }

    private <L> void mergeLists(List<L> base, List<L> incoming){
        if (incoming == null || incoming.isEmpty()){
            return;
        }

        if(base == null){
            base = new ArrayList<>();
        }
        base.addAll(incoming);
    }

}
