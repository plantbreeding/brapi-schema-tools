package org.brapi.schematools.core.brapischema;

import org.brapi.schematools.core.model.BrAPIObjectType;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static graphql.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BrAPISchemaReaderTest {

    @Test
    void readDirectories() {

        try {
            Map<String, BrAPIObjectType> schemas =
                new BrAPISchemaReader().readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI())).stream().collect(Collectors.toMap(BrAPIObjectType::getName, Function.identity()));

            assertNotNull(schemas);
            assertEquals(46, schemas.size());

            BrAPIObjectType trialSchema = schemas.get("Trial");
            assertNotNull(trialSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNotNull(trialSchema.getMetadata());
            assertTrue(trialSchema.getMetadata().isPrimaryModel());
            assertFalse(trialSchema.isRequest());

            BrAPIObjectType sampleSchema = schemas.get("Sample");
            assertNotNull(sampleSchema);
            assertEquals("Sample", sampleSchema.getName());
            assertEquals("BrAPI-Genotyping", sampleSchema.getModule());
            assertNotNull(sampleSchema.getMetadata());
            assertTrue(sampleSchema.getMetadata().isPrimaryModel());
            assertFalse(sampleSchema.isRequest());

            BrAPIObjectType germplasmSchema = schemas.get("Germplasm");
            assertNotNull(germplasmSchema);
            assertEquals("Germplasm", germplasmSchema.getName());
            assertEquals("BrAPI-Germplasm", germplasmSchema.getModule());
            assertNotNull(germplasmSchema.getMetadata());
            assertTrue(germplasmSchema.getMetadata().isPrimaryModel());
            assertFalse(germplasmSchema.isRequest());

            BrAPIObjectType traitSchema = schemas.get("Trait");
            assertNotNull(traitSchema);
            assertEquals("Trait", traitSchema.getName());
            assertEquals("BrAPI-Phenotyping", traitSchema.getModule());
            assertNotNull(traitSchema.getMetadata());
            assertTrue(traitSchema.getMetadata().isPrimaryModel());
            assertFalse(traitSchema.isRequest());

            BrAPIObjectType listType = schemas.get("ListType");
            assertNotNull(listType);
            assertEquals("ListType", listType.getName());
            assertNull(listType.getModule());
            assertNull(listType.getMetadata());
            assertFalse(listType.isRequest());

            BrAPIObjectType listRequest = schemas.get("ListRequest");
            assertNotNull(listRequest);
            assertEquals("ListRequest", listRequest.getName());
            assertNull(listRequest.getModule());
            assertNull(listRequest.getMetadata());
            assertTrue(listRequest.isRequest());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readSchemaPath() {
        try {
            BrAPIObjectType trialSchema =
                new BrAPISchemaReader().readSchema(Path.of(ClassLoader.getSystemResource("BrAPI-Schema/BrAPI-Core/Trial.json").toURI()), "BrAPI-Core");

            assertNotNull(trialSchema);

            assertNotNull(trialSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNotNull(trialSchema.getMetadata());
            assertTrue(trialSchema.getMetadata().isPrimaryModel());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readSchemaString() {
        try {
            Path path = Paths.get(Objects.requireNonNull(this.getClass().getResource("/BrAPI-Schema/BrAPI-Core/Trial.json")).toURI());

            BrAPIObjectType trialSchema =
                new BrAPISchemaReader().readSchema(path, String.join("\n", Files.readAllLines(path, Charset.defaultCharset())), "BrAPI-Core");

            assertNotNull(trialSchema);

            assertNotNull(trialSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNotNull(trialSchema.getMetadata());
            assertTrue(trialSchema.getMetadata().isPrimaryModel());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}