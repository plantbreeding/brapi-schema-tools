package org.brapi.schematools.core.openapi.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;

/**
 * Provides metadata for the OpenAPI generation
 */
@Getter
@Setter
public class OpenAPIGeneratorMetadata implements Metadata {
    String version ;
    SingleGetMetadata singleGet = new SingleGetMetadata() ;
    ListGetMetadata listGet = new ListGetMetadata() ;
    PostMetadata post = new PostMetadata() ;
    PutMetadata put = new PutMetadata() ;
    DeleteMetadata delete = new DeleteMetadata() ;
    SearchMetadata search = new SearchMetadata() ;
}
