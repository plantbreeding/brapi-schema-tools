package org.brapi.schematools.core.brapischema;

import org.brapi.schematools.core.model.BrAPIObjectType;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BrAPISchemaReaderTest {

  @Test
  void readDirectories()  {

    try {
      Map<String, BrAPIObjectType> schemas =
              new BrAPISchemaReader().readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI())).stream().collect(Collectors.toMap(BrAPIObjectType::getName, Function.identity()));

      assertNotNull(schemas) ;
      assertEquals(37, schemas.size()) ;

      BrAPIObjectType trialSchema = schemas.get("Trial") ;
      assertNotNull(trialSchema) ;
      assertEquals("Trial", trialSchema.getName()) ;
      assertEquals("BrAPI-Core", trialSchema.getModule()) ;
      assertTrue(trialSchema.isPrimaryModel()) ;

      BrAPIObjectType sampleSchema = schemas.get("Sample") ;
      assertNotNull(sampleSchema) ;
      assertEquals("Sample", sampleSchema.getName()) ;
      assertEquals("BrAPI-Genotyping", sampleSchema.getModule()) ;
      assertFalse(sampleSchema.isPrimaryModel()); ;

      BrAPIObjectType germplasmSchema = schemas.get("Germplasm") ;
      assertNotNull(germplasmSchema) ;
      assertEquals("Germplasm", germplasmSchema.getName()) ;
      assertEquals("BrAPI-Germplasm", germplasmSchema.getModule()) ;
      assertFalse(germplasmSchema.isPrimaryModel()); ;

      BrAPIObjectType traitSchema = schemas.get("Trait") ;
      assertNotNull(traitSchema) ;
      assertEquals("Trait", traitSchema.getName()) ;
      assertEquals("BrAPI-Phenotyping", traitSchema.getModule()) ;
      assertFalse(traitSchema.isPrimaryModel()); ;

    } catch (Exception e) {
      fail(e.getMessage()) ;
    }
  }

  @Test
  void readSchemaPath()  {
    try {
      BrAPIObjectType trialSchema =
              new BrAPISchemaReader().readSchema(Path.of(ClassLoader.getSystemResource("BrAPI-Schema/BrAPI-Core/Trial.json").toURI()), "BrAPI-Core") ;

      assertNotNull(trialSchema) ;

      assertNotNull(trialSchema) ;
      assertEquals("Trial", trialSchema.getName()) ;
      assertEquals("BrAPI-Core", trialSchema.getModule()) ;
      assertTrue(trialSchema.isPrimaryModel()) ;
    } catch (Exception e) {
      fail(e.getMessage()) ;
    }
  }

  @Test
  void readSchemaString()  {
    try {
      BrAPIObjectType trialSchema =
              new BrAPISchemaReader().readSchema(String.join("\n", Files.readAllLines(
                      Paths.get(this.getClass().getResource("/BrAPI-Schema/BrAPI-Core/Trial.json").toURI()), Charset.defaultCharset())), "BrAPI-Core") ;

      assertNotNull(trialSchema) ;

      assertNotNull(trialSchema) ;
      assertEquals("Trial", trialSchema.getName()) ;
      assertEquals("BrAPI-Core", trialSchema.getModule()) ;
      assertTrue(trialSchema.isPrimaryModel()) ;
    } catch (Exception e) {
      fail(e.getMessage()) ;
    }
  }
}