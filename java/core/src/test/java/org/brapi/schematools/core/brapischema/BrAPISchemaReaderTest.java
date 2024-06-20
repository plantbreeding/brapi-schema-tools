package org.brapi.schematools.core.brapischema;

import org.brapi.schematools.core.model.BrAPIClass;
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
            Map<String, BrAPIClass> schemas = new BrAPISchemaReader().
                readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI())).
                onFailDoWithResponse(response -> fail(response.getMessagesCombined(","))).
                getResult().stream().collect(Collectors.toMap(BrAPIClass::getName, Function.identity()));

            assertNotNull(schemas);
            assertEquals(94, schemas.size());

            BrAPIClass trialSchema = schemas.get("Trial");
            assertNotNull(trialSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNotNull(trialSchema.getMetadata());
            assertTrue(trialSchema.getMetadata().isPrimaryModel());

            BrAPIClass sampleSchema = schemas.get("Sample");
            assertNotNull(sampleSchema);
            assertEquals("Sample", sampleSchema.getName());
            assertEquals("BrAPI-Genotyping", sampleSchema.getModule());
            assertNotNull(sampleSchema.getMetadata());
            assertTrue(sampleSchema.getMetadata().isPrimaryModel());

            BrAPIClass germplasmSchema = schemas.get("Germplasm");
            assertNotNull(germplasmSchema);
            assertEquals("Germplasm", germplasmSchema.getName());
            assertEquals("BrAPI-Germplasm", germplasmSchema.getModule());
            assertNotNull(germplasmSchema.getMetadata());
            assertTrue(germplasmSchema.getMetadata().isPrimaryModel());

            BrAPIClass traitSchema = schemas.get("Trait");
            assertNotNull(traitSchema);
            assertEquals("Trait", traitSchema.getName());
            assertEquals("BrAPI-Phenotyping", traitSchema.getModule());
            assertNotNull(traitSchema.getMetadata());
            assertTrue(traitSchema.getMetadata().isPrimaryModel());

            BrAPIClass listType = schemas.get("ListType");
            assertNotNull(listType);
            assertEquals("ListType", listType.getName());
            assertEquals("BrAPI-Core", listType.getModule());
            assertNull(listType.getMetadata());

            BrAPIClass listRequest = schemas.get("ListRequest");
            assertNotNull(listRequest);
            assertEquals("ListRequest", listRequest.getName());
            assertNull(listRequest.getModule());
            assertNotNull(listRequest.getMetadata());
            assertTrue(listRequest.getMetadata().isRequest());
            assertFalse(listRequest.getMetadata().isPrimaryModel());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void readSchemaPath() {
        try {
            BrAPIClass trialSchema = new BrAPISchemaReader().
                readSchema(Path.of(ClassLoader.getSystemResource("BrAPI-Schema/BrAPI-Core/Trial.json").toURI()), "BrAPI-Core").
                onFailDoWithResponse(response -> fail(response.getMessagesCombined(","))).
                getResult();

            assertNotNull(trialSchema);

            assertNotNull(trialSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNotNull(trialSchema.getMetadata());
            assertTrue(trialSchema.getMetadata().isPrimaryModel());

            BrAPIClass listTypeSchema = new BrAPISchemaReader().
                readSchema(Path.of(ClassLoader.getSystemResource("BrAPI-Schema/BrAPI-Core/ListType.json").toURI()), "BrAPI-Core").
                onFailDoWithResponse(response -> fail(response.getMessagesCombined(","))).
                getResult();

            assertNotNull(listTypeSchema);

            assertNotNull(listTypeSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNull(listTypeSchema.getMetadata());

            BrAPIClass listRequest = new BrAPISchemaReader().
                readSchema(Path.of(ClassLoader.getSystemResource("BrAPI-Schema/Requests/ListRequest.json").toURI()), null).
                onFailDoWithResponse(response -> fail(response.getMessagesCombined(","))).
                getResult();

            assertNotNull(listRequest);
            assertEquals("ListRequest", listRequest.getName());
            assertNull(listRequest.getModule());
            assertNotNull(listRequest.getMetadata());
            assertTrue(listRequest.getMetadata().isRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void readSchemaString() {
        try {
            Path path = Paths.get(Objects.requireNonNull(this.getClass().getResource("/BrAPI-Schema/BrAPI-Core/Trial.json")).toURI());

            BrAPIClass trialSchema = new BrAPISchemaReader().
                readSchema(path, String.join("\n", Files.readAllLines(path, Charset.defaultCharset())), "BrAPI-Core").
                onFailDoWithResponse(response -> fail(response.getMessagesCombined(","))).
                getResult();


            assertNotNull(trialSchema);

            assertNotNull(trialSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNotNull(trialSchema.getMetadata());
            assertTrue(trialSchema.getMetadata().isPrimaryModel());

            path = Paths.get(Objects.requireNonNull(this.getClass().getResource("/BrAPI-Schema/BrAPI-Core/ListType.json")).toURI());

            BrAPIClass listTypeSchema = new BrAPISchemaReader().
                readSchema(path, String.join("\n", Files.readAllLines(path, Charset.defaultCharset())), "BrAPI-Core").
                onFailDoWithResponse(response -> fail(response.getMessagesCombined(","))).
                getResult();

            assertNotNull(listTypeSchema);

            assertNotNull(listTypeSchema);
            assertEquals("Trial", trialSchema.getName());
            assertEquals("BrAPI-Core", trialSchema.getModule());
            assertNull(listTypeSchema.getMetadata());

            path = Paths.get(Objects.requireNonNull(this.getClass().getResource("/BrAPI-Schema/Requests/ListRequest.json")).toURI());

            BrAPIClass listRequest = new BrAPISchemaReader().
                readSchema(null, String.join("\n", Files.readAllLines(path, Charset.defaultCharset())), null).
                onFailDoWithResponse(response -> fail(response.getMessagesCombined(","))).
                getResult();

            assertNotNull(listRequest);
            assertEquals("ListRequest", listRequest.getName());
            assertNull(listRequest.getModule());
            assertNotNull(listRequest.getMetadata());
            assertTrue(listRequest.getMetadata().isRequest());
            assertFalse(listRequest.getMetadata().isPrimaryModel());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}