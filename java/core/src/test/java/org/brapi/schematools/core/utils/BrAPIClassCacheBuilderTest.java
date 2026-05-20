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
import java.util.Set;

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
        BrAPIClassCacheBuilder.BrAPIClassCache cache = BrAPIClassCacheBuilder.builder(brAPIClasses).build();

        assertEquals(165, cache.size());
        commonTests(cache) ;

        assertTrue(cache.containsBrAPIClass("GermplasmRequest")) ;
        assertNotNull(cache.getBrAPIClass("GermplasmRequest")) ;

        assertTrue(cache.containsBrAPIClass("Attribute")) ;
        assertNotNull(cache.getBrAPIClass("Attribute")) ;

        List<BrAPIClass> usedByDataType = cache.usedBy("DataType");

        assertNotNull(usedByDataType) ;
        assertEquals(4, usedByDataType.size());

        List<BrAPIClass> dependsOnGermplasm = cache.dependsOn("Germplasm");
        assertNotNull(dependsOnGermplasm) ;
        int dependsOnGermplasmCount = dependsOnGermplasm.size();
        assertEquals(17, dependsOnGermplasmCount);

        List<BrAPIClass> usedByGermplasm = cache.usedBy("Germplasm");
        assertNotNull(usedByGermplasm) ;
        int usedByGermplasmCount = usedByGermplasm.size();
        assertEquals(17, usedByGermplasmCount);

        assertTrue(brAPIClasses.containsAll(usedByGermplasm));

        List<BrAPIClass> allDependencies = cache.getAllNonPrimaryDependencies();
        assertNotNull(allDependencies) ;
        assertEquals(22, allDependencies.size());

        assertNotNull(cache.getBrAPIClassNames()) ;
        assertEquals(165, cache.getBrAPIClassNames().size()) ;

        assertNotNull(cache.getBrAPIClasses()) ;
        assertEquals(165, cache.getBrAPIClasses().size()) ;

        assertNotNull(cache.getBrAPIClassesAsSet()) ;
        assertEquals(165, cache.getBrAPIClassesAsSet().size()) ;

        assertNotNull(cache.getBrAPIClassesAsMap()) ;
        assertEquals(165, cache.getBrAPIClassesAsMap().size()) ;

        assertEquals(141, brAPIClasses.size()) ;

        List<BrAPIClass> primaryClasses = cache.getPrimaryClasses();
        assertNotNull(primaryClasses) ;
        assertEquals(141, primaryClasses.size()) ;

        Set<BrAPIClass> commonDependenciesGermplasm = cache.getCommonDependencies("Germplasm");
        assertNotNull(commonDependenciesGermplasm) ;
        int commonDependencyGermplasmCount = commonDependenciesGermplasm.size();
        assertEquals(1, commonDependencyGermplasmCount);

        Set<BrAPIClass> exclusiveDependenciesGermplasm = cache.getExclusiveDependencies("Germplasm");
        assertNotNull(exclusiveDependenciesGermplasm) ;
        int exclusiveDependenciesGermplasmCount = exclusiveDependenciesGermplasm.size();
        assertEquals(0, exclusiveDependenciesGermplasmCount);

        Set<BrAPIClass> primaryDependenciesGermplasm = cache.getPrimaryDependencies("Germplasm");
        assertNotNull(primaryDependenciesGermplasm) ;
        int primaryDependenciesGermplasmCount = primaryDependenciesGermplasm.size();
        assertEquals(16, primaryDependenciesGermplasmCount);

        assertEquals(dependsOnGermplasmCount, commonDependencyGermplasmCount +  exclusiveDependenciesGermplasmCount + primaryDependenciesGermplasmCount) ;
    }

    @Test
    void createCacheWithPredicate() {
        BrAPIClassCacheBuilder.BrAPIClassCache cache = BrAPIClassCacheBuilder.builder(brAPIClasses).cachePredicate(this::isCaching).build();

        assertEquals(165, cache.size());
        commonTests(cache) ;

        assertTrue(cache.containsBrAPIClass("GermplasmRequest")) ;
        assertNotNull(cache.getBrAPIClass("GermplasmRequest")) ;

        assertTrue(cache.containsBrAPIClass("Attribute")) ;
        assertNotNull(cache.getBrAPIClass("Attribute")) ;

        List<BrAPIClass> usedByDataType = cache.usedBy("DataType");

        assertNotNull(usedByDataType) ;
        assertEquals(4, usedByDataType.size());

        List<BrAPIClass> dependsOnGermplasm = cache.dependsOn("Germplasm");
        assertNotNull(dependsOnGermplasm) ;
        int dependsOnGermplasmCount = dependsOnGermplasm.size();
        assertEquals(17, dependsOnGermplasmCount);

        List<BrAPIClass> usedByGermplasm = cache.usedBy("Germplasm");
        assertNotNull(usedByGermplasm) ;
        int usedByGermplasmCount = usedByGermplasm.size();
        assertEquals(17, usedByGermplasmCount);

        assertTrue(brAPIClasses.containsAll(usedByGermplasm));

        List<BrAPIClass> allDependencies = cache.getAllNonPrimaryDependencies();
        assertNotNull(allDependencies) ;
        assertEquals(76, allDependencies.size());

        assertNotNull(cache.getBrAPIClassNames()) ;
        assertEquals(165, cache.getBrAPIClassNames().size()) ;

        assertNotNull(cache.getBrAPIClasses()) ;
        assertEquals(165, cache.getBrAPIClasses().size()) ;

        assertNotNull(cache.getBrAPIClassesAsSet()) ;
        assertEquals(165, cache.getBrAPIClassesAsSet().size()) ;

        assertNotNull(cache.getBrAPIClassesAsMap()) ;
        assertEquals(165, cache.getBrAPIClassesAsMap().size()) ;

        assertEquals(141, brAPIClasses.size()) ;

        List<BrAPIClass> primaryClasses = cache.getPrimaryClasses();
        assertNotNull(primaryClasses) ;
        assertEquals(36, primaryClasses.size()) ;

        Set<BrAPIClass> commonDependenciesGermplasm = cache.getCommonDependencies("Germplasm");
        assertNotNull(commonDependenciesGermplasm) ;
        int commonDependencyGermplasmCount = commonDependenciesGermplasm.size();
        assertEquals(4, commonDependencyGermplasmCount);

        Set<BrAPIClass> exclusiveDependenciesGermplasm = cache.getExclusiveDependencies("Germplasm");
        assertNotNull(exclusiveDependenciesGermplasm) ;
        int exclusiveDependenciesGermplasmCount = exclusiveDependenciesGermplasm.size();
        assertEquals(5, exclusiveDependenciesGermplasmCount);

        Set<BrAPIClass> primaryDependenciesGermplasm = cache.getPrimaryDependencies("Germplasm");
        assertNotNull(primaryDependenciesGermplasm) ;
        int primaryDependenciesGermplasmCount = primaryDependenciesGermplasm.size();
        assertEquals(8, primaryDependenciesGermplasmCount);

        assertEquals(dependsOnGermplasmCount, commonDependencyGermplasmCount +  exclusiveDependenciesGermplasmCount + primaryDependenciesGermplasmCount) ;
    }

    @Test
    void createMap() {
        Map<String, BrAPIClass> map = BrAPIClassCacheBuilder.builder(brAPIClasses).build().getBrAPIClassesAsMap();

        assertEquals(165, map.size());

        assertTrue(map.containsKey("Trial")) ;
        assertTrue(map.containsKey("Attribute")) ;
        assertTrue(map.containsKey("GermplasmAttribute")) ;
        assertTrue(map.containsKey("GermplasmAttributeValue")) ;
        assertTrue(map.containsKey("GermplasmRequest")) ;
    }

    @Test
    void createMapWithPredicate() {
        Map<String, BrAPIClass> map = BrAPIClassCacheBuilder.builder(brAPIClasses).cachePredicate(this::isCaching).build().getBrAPIClassesAsMap();

        assertEquals(165, map.size());

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