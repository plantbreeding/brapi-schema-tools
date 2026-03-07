package org.brapi.schematools.core.utils;

import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIReferenceType;
import org.brapi.schematools.core.model.BrAPIType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BrAPIClassCacheBuilderTest {

    List<BrAPIClass> brAPIClasses ;

    @BeforeEach
    void setUp() throws URISyntaxException {

        brAPIClasses = new BrAPISchemaReader()
            .readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()))
            .onFailDoWithResponse(response -> fail(response.getMessagesCombined(",")))
            .getResult();
    }

    @Test
    void createCache() {
        BrAPIClassCacheBuilder.BrAPIClassCache cache = BrAPIClassCacheBuilder.createCache(brAPIClasses);

        assertEquals(146, cache.size());
        commonTests(cache) ;

        assertTrue(cache.containsBrAPIClass("GermplasmRequest")) ;
        assertNotNull(cache.getBrAPIClass("GermplasmRequest")) ;

        assertTrue(cache.containsBrAPIClass("Attribute")) ;
        assertNotNull(cache.getBrAPIClass("Attribute")) ;

        List<BrAPIClass> usedByDataType = cache.usedBy("DataType");

        assertNotNull(usedByDataType) ;
        assertEquals(4, usedByDataType.size());

        List<BrAPIClass> usedByGermplasm = cache.dependsOn("Germplasm");

        assertNotNull(usedByGermplasm) ;
        assertEquals(16, usedByGermplasm.size());

        assertEquals(52, cache.getAllDependencies().size());

        assertEquals(146, cache.getBrAPIClassNames().size()) ;

        assertEquals(146, cache.getBrAPIClasses().size()) ;

        assertEquals(146, cache.getBrAPIClassesAsSet().size()) ;

        assertEquals(4, cache.getCommonDependencies("Germplasm").size());
        assertEquals(5, cache.getExclusiveDependencies("Germplasm").size());
    }

    @Test
    void createCacheWithPredicate() {
        BrAPIClassCacheBuilder.BrAPIClassCache cache = BrAPIClassCacheBuilder.createCache(this::isCaching, brAPIClasses);

        assertEquals(81, cache.size());

        commonTests(cache) ;

        List<BrAPIClass> dependsOnDataType = cache.dependsOn("DataType");

        assertNotNull(dependsOnDataType) ;
        assertEquals(0, dependsOnDataType.size());

        List<BrAPIClass> usedByGermplasm = cache.dependsOn("Germplasm");

        assertNotNull(usedByGermplasm) ;
        assertEquals(16, usedByGermplasm.size());

        assertEquals(39, cache.getAllDependencies().size());

        assertEquals(81, cache.getBrAPIClassNames().size()) ;

        assertEquals(81, cache.getBrAPIClasses().size()) ;

        assertEquals(81, cache.getBrAPIClassesAsSet().size()) ;

        assertEquals(5, cache.getCommonDependencies("Germplasm").size());
        assertEquals(6, cache.getExclusiveDependencies("Germplasm").size());
    }

    @Test
    void createMap() {
        Map<String, BrAPIClass> map = BrAPIClassCacheBuilder.createMap(brAPIClasses);

        assertEquals(146, map.size());

        assertTrue(map.containsKey("Trial")) ;
        assertTrue(map.containsKey("Attribute")) ;
        assertTrue(map.containsKey("GermplasmAttribute")) ;
        assertTrue(map.containsKey("GermplasmAttributeValue")) ;
        assertTrue(map.containsKey("GermplasmRequest")) ;
    }

    @Test
    void createMapWithPredicate() {
        Map<String, BrAPIClass> map = BrAPIClassCacheBuilder.createMap(this::isCaching, brAPIClasses);

        assertEquals(81, map.size());

        assertTrue(map.containsKey("Trial")) ;
        assertTrue(map.containsKey("GermplasmAttribute")) ;
        assertTrue(map.containsKey("GermplasmAttributeValue")) ;
    }

    private boolean isCaching(BrAPIClass brAPIClass) {
        return brAPIClass.getMetadata() != null && brAPIClass.getMetadata().isPrimaryModel() ;
    }

    private void commonTests(BrAPIClassCacheBuilder.BrAPIClassCache cache) {

        assertTrue(cache.containsBrAPIClass("Trial")) ;
        assertTrue(cache.containsBrAPIClass("GermplasmAttribute")) ;
        assertTrue(cache.containsBrAPIClass("GermplasmAttributeValue")) ;

        assertNotNull(cache.getBrAPIClass("Trial")); ;
        assertNotNull(cache.getBrAPIClass("GermplasmAttribute")) ;
        assertNotNull(cache.getBrAPIClass("GermplasmAttributeValue")) ;

        BrAPIType germplasm = cache.dereferenceType(BrAPIReferenceType.builder().name("Trial").build());

        assertEquals(BrAPIObjectType.class, germplasm.getClass());
    }

}