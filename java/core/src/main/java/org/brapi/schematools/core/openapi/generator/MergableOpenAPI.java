package org.brapi.schematools.core.openapi.generator;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

        if (incoming.getComponents() != null) {
            if (this.getComponents() == null) this.setComponents(new Components());
            mergeMaps(this.getComponents().getCallbacks(), incoming.getComponents().getCallbacks(), m -> this.getComponents().setCallbacks(m));
            mergeMaps(this.getComponents().getExamples(), incoming.getComponents().getExamples(), m -> this.getComponents().setExamples(m));
            mergeMaps(this.getComponents().getExtensions(), incoming.getComponents().getExtensions(), m -> this.getComponents().setExtensions(m));
            mergeMaps(this.getComponents().getHeaders(), incoming.getComponents().getHeaders(), m -> this.getComponents().setHeaders(m));
            mergeMaps(this.getComponents().getLinks(), incoming.getComponents().getLinks(), m -> this.getComponents().setLinks(m));
            mergeMaps(this.getComponents().getParameters(), incoming.getComponents().getParameters(), m -> this.getComponents().setParameters(m));
            mergeMaps(this.getComponents().getPathItems(), incoming.getComponents().getPathItems(), m -> this.getComponents().setPathItems(m));
            mergeMaps(this.getComponents().getRequestBodies(), incoming.getComponents().getRequestBodies(), m -> this.getComponents().setRequestBodies(m));
            mergeMaps(this.getComponents().getResponses(), incoming.getComponents().getResponses(), m -> this.getComponents().setResponses(m));
            mergeMaps(this.getComponents().getSchemas(), incoming.getComponents().getSchemas(), m -> this.getComponents().setSchemas(m));
            mergeMaps(this.getComponents().getSecuritySchemes(), incoming.getComponents().getSecuritySchemes(), m -> this.getComponents().setSecuritySchemes(m));
        }

        mergeMaps(this.getExtensions(), incoming.getExtensions(), this::setExtensions);
        if(incoming.getExternalDocs() != null)
            this.setExternalDocs(incoming.getExternalDocs());
        if(incoming.getInfo() != null)
            this.setInfo(incoming.getInfo());
        if(incoming.getJsonSchemaDialect() != null)
            this.setJsonSchemaDialect(incoming.getJsonSchemaDialect());
        if(incoming.getOpenapi() != null)
            this.setOpenapi(incoming.getOpenapi());
        if (incoming.getPaths() != null) {
            if (this.getPaths() == null) this.setPaths(new Paths());
            this.getPaths().putAll(incoming.getPaths());
        }
        mergeLists(this.getSecurity(), incoming.getSecurity(), this::setSecurity);
        mergeLists(this.getServers(), incoming.getServers(), this::setServers);
        if(incoming.getSpecVersion() != null)
            this.setSpecVersion(incoming.getSpecVersion());
        mergeLists(this.getTags(), incoming.getTags(), this::setTags);
        mergeMaps(this.getWebhooks(), incoming.getWebhooks(), this::setWebhooks);

        return this;
    }

    private <K, V> void mergeMaps(Map<K, V> base, Map<K, V> incoming, java.util.function.Consumer<Map<K, V>> setter) {
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        if (base == null) {
            base = new LinkedHashMap<>();
            setter.accept(base);
        }
        base.putAll(incoming);
    }

    private <L> void mergeLists(List<L> base, List<L> incoming, java.util.function.Consumer<List<L>> setter) {
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        if (base == null) {
            base = new ArrayList<>();
            setter.accept(base);
        }
        base.addAll(incoming);
    }

}
