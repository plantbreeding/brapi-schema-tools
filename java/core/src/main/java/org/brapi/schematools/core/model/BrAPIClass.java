package org.brapi.schematools.core.model;

public interface BrAPIClass extends BrAPIType {
    String getDescription();
    String getModule();
    BrAPIMetadata getMetadata();
}
