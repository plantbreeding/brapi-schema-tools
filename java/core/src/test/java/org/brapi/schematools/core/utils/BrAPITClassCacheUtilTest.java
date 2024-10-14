package org.brapi.schematools.core.utils;

import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BrAPITClassCacheUtilTest {

    List<BrAPIClass> brAPIClasses ;

    @BeforeEach
    void setUp() throws URISyntaxException {

        brAPIClasses = new BrAPISchemaReader()
            .readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()))
            .onFailDoWithResponse(response -> fail(response.getMessagesCombined(",")))
            .getResult();
    }

    @Test
    void createMap() {
        BrAPITClassCacheUtil subject = new BrAPITClassCacheUtil(this::isCaching) ;
        Map<String, BrAPIClass> map = subject.createMap(brAPIClasses);

        assertEquals(102, map.size());

        assertTrue(map.containsKey("Trial")) ;
        assertTrue(map.containsKey("Attribute")) ;
        assertTrue(map.containsKey("GermplasmAttribute")) ;
        assertTrue(map.containsKey("GermplasmAttributeValue")) ;
    }

    private boolean isCaching(BrAPIClass brAPIClass) {
        return brAPIClass.getMetadata() == null || !(brAPIClass.getMetadata().isRequest() || brAPIClass.getMetadata().isParameters());
    }
}