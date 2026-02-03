package org.brapi.schematools.core.utils;

import org.brapi.schematools.core.model.BrAPIArrayType;
import org.brapi.schematools.core.model.BrAPIMetadata;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BrAPITypeUtilsTest {

    @Test
    void isNonPrimaryModel() {
        BrAPIObjectType c1 = BrAPIObjectType.builder().build();
        assertTrue(BrAPITypeUtils.isNonPrimaryModel(c1));

        BrAPIObjectType c2 = BrAPIObjectType.builder().metadata(BrAPIMetadata.builder().primaryModel(false).build()).build();
        assertTrue(BrAPITypeUtils.isNonPrimaryModel(c2));

        BrAPIObjectType c3 = BrAPIObjectType.builder().metadata(BrAPIMetadata.builder().primaryModel(true).build()).build();
        assertFalse(BrAPITypeUtils.isNonPrimaryModel(c3));
    }

    @Test
    void isPrimaryModel() {
        BrAPIObjectType c1 = BrAPIObjectType.builder().build();
        assertFalse(BrAPITypeUtils.isPrimaryModel(c1));

        BrAPIObjectType c2 = BrAPIObjectType.builder().metadata(BrAPIMetadata.builder().primaryModel(false).build()).build();
        assertFalse(BrAPITypeUtils.isPrimaryModel(c2));

        BrAPIObjectType c3 = BrAPIObjectType.builder().metadata(BrAPIMetadata.builder().primaryModel(true).build()).build();
        assertTrue(BrAPITypeUtils.isPrimaryModel(c3));
    }

    @Test
    void unwrapType() {
        BrAPIType objectType = BrAPIObjectType.builder().build();
        assertSame(objectType, BrAPITypeUtils.unwrapType(objectType));

        BrAPIType arrayType = BrAPIArrayType.builder().items(objectType).build();
        assertSame(objectType, BrAPITypeUtils.unwrapType(arrayType));

        BrAPIType innerArrayType = BrAPIArrayType.builder().items(arrayType).build();
        assertSame(objectType, BrAPITypeUtils.unwrapType(innerArrayType));
    }

    @Test
    void validateBrAPIMetadata() {
        BrAPIObjectType type = BrAPIObjectType.builder().build();
        Response<BrAPIMetadata> response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertFalse(response.hasErrors());

        type = BrAPIObjectType.builder().name("TestClass").metadata(BrAPIMetadata.builder().primaryModel(true).build()).build();
        response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertFalse(response.hasErrors());

        type = BrAPIObjectType.builder().name("TestClass").metadata(BrAPIMetadata.builder().request(true).build()).build();
        response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertFalse(response.hasErrors());

        type = BrAPIObjectType.builder().name("TestClass").metadata(BrAPIMetadata.builder().parameters(true).build()).build();
        response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertFalse(response.hasErrors());

        type = BrAPIObjectType.builder().name("TestClass").metadata(BrAPIMetadata.builder().primaryModel(true).request(true).build()).build();
        response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertTrue(response.hasErrors());
        assertTrue(response.getMessagesCombined(",").contains("mutually exclusive"));

        type = BrAPIObjectType.builder().name("TestClass").metadata(BrAPIMetadata.builder().primaryModel(true).parameters(true).build()).build();
        response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertTrue(response.hasErrors());
        assertTrue(response.getMessagesCombined(",").contains("mutually exclusive"));

        type = BrAPIObjectType.builder().name("TestClass").metadata(BrAPIMetadata.builder().request(true).parameters(true).build()).build();
        response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertTrue(response.hasErrors());
        assertTrue(response.getMessagesCombined(",").contains("mutually exclusive"));

        type = BrAPIObjectType.builder().name("TestClass").metadata(BrAPIMetadata.builder().primaryModel(true).request(true).parameters(true).build()).build();
        response = BrAPITypeUtils.validateBrAPIMetadata(type);
        assertTrue(response.hasErrors());
        assertTrue(response.getMessagesCombined(",").contains("mutually exclusive"));
    }

    @Test
    void mergeMetadata() {
        BrAPIMetadata m1 = BrAPIMetadata.builder()
            .primaryModel(true)
            .request(false)
            .parameters(false)
            .interfaceClass(false)
            .controlledVocabularyProperties(Arrays.asList("a", "b"))
            .subQueryProperties(Collections.singletonList("x"))
            .updatableProperties(Collections.singletonList("u"))
            .writableProperties(Collections.singletonList("w"))
            .build();
        BrAPIMetadata m2 = BrAPIMetadata.builder()
            .primaryModel(false)
            .request(true)
            .parameters(true)
            .interfaceClass(true)
            .controlledVocabularyProperties(Arrays.asList("b", "c"))
            .subQueryProperties(Collections.singletonList("y"))
            .updatableProperties(Collections.singletonList("v"))
            .writableProperties(Collections.singletonList("z"))
            .build();

        BrAPIMetadata merged = BrAPITypeUtils.mergeMetadata(m1, m2);
        assertTrue(merged.isPrimaryModel());
        assertFalse(merged.isRequest());
        assertFalse(merged.isParameters());
        assertFalse(merged.isInterfaceClass());
        assertEquals(Arrays.asList("a", "b", "c"), merged.getControlledVocabularyProperties());
        assertEquals(Arrays.asList("x", "y"), merged.getSubQueryProperties());
        assertEquals(Arrays.asList("u", "v"), merged.getUpdatableProperties());
        assertEquals(Arrays.asList("w", "z"), merged.getWritableProperties());

        // primary model has precedence over request and parameters
        m1 = BrAPIMetadata.builder()
            .primaryModel(true)
            .request(false)
            .build();
        m2 = BrAPIMetadata.builder()
            .primaryModel(false)
            .request(true)
            .build();

        merged = BrAPITypeUtils.mergeMetadata(m1, m2);
        assertTrue(merged.isPrimaryModel());
        assertFalse(merged.isRequest());
        assertFalse(merged.isParameters());

        m1 = BrAPIMetadata.builder()
            .primaryModel(true)
            .parameters(false)
            .build();
        m2 = BrAPIMetadata.builder()
            .primaryModel(false)
            .parameters(true)
            .build();

        merged = BrAPITypeUtils.mergeMetadata(m1, m2);
        assertTrue(merged.isPrimaryModel());
        assertFalse(merged.isRequest());
        assertFalse(merged.isParameters());

        // request has precedence over parameters

        m1 = BrAPIMetadata.builder()
            .request(true)
            .parameters(false)
            .build();
        m2 = BrAPIMetadata.builder()
            .primaryModel(false)
            .parameters(true)
            .build();

        merged = BrAPITypeUtils.mergeMetadata(m1, m2);
        assertFalse(merged.isPrimaryModel());
        assertTrue(merged.isRequest());
        assertFalse(merged.isParameters());

        // primary model has precedence over interface class

        m1 = BrAPIMetadata.builder()
            .primaryModel(true)
            .interfaceClass(false)
            .build();
        m2 = BrAPIMetadata.builder()
            .primaryModel(false)
            .interfaceClass(true)
            .build();

        merged = BrAPITypeUtils.mergeMetadata(m1, m2);
        assertTrue(merged.isPrimaryModel());
        assertFalse(merged.isInterfaceClass());
    }

    @Test
    void mergePropertyNames() {
        List<String> l1 = Arrays.asList("a", "b", "c");
        List<String> l2 = Arrays.asList("b", "c", "d");
        List<String> merged = BrAPITypeUtils.mergePropertyNames(l1, l2);
        assertEquals(Arrays.asList("a", "b", "c", "d"), merged);

        assertEquals(l1, BrAPITypeUtils.mergePropertyNames(l1, null));
        assertEquals(l2, BrAPITypeUtils.mergePropertyNames(null, l2));
        assertNull(BrAPITypeUtils.mergePropertyNames(null, null));
    }
}
