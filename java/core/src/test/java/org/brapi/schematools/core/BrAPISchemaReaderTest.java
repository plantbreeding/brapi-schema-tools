package org.brapi.schematools.core;

import org.brapi.schematools.core.model.BrAPISchema;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BrAPISchemaReaderTest {

  @Test
  void read()  {

    try {
      Map<String, BrAPISchema> schemas =
              new BrAPISchemaReader().read(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI())).stream().collect(Collectors.toMap(BrAPISchema::getName, Function.identity()));

      assertNotNull(schemas) ;
      assertEquals(37, schemas.size()) ;

      BrAPISchema trialSchema = schemas.get("Trial") ;
      assertNotNull(trialSchema) ;
      assertEquals("Trial", trialSchema.getName()) ;
      assertEquals("BrAPI-Core", trialSchema.getModule()) ;

      BrAPISchema sampleSchema = schemas.get("Sample") ;
      assertNotNull(sampleSchema) ;
      assertEquals("Sample", sampleSchema.getName()) ;
      assertEquals("BrAPI-Genotyping", sampleSchema.getModule()) ;

      BrAPISchema germplasmSchema = schemas.get("Germplasm") ;
      assertNotNull(germplasmSchema) ;
      assertEquals("Germplasm", germplasmSchema.getName()) ;
      assertEquals("BrAPI-Germplasm", germplasmSchema.getModule()) ;

      BrAPISchema traitSchema = schemas.get("Trait") ;
      assertNotNull(traitSchema) ;
      assertEquals("Trait", traitSchema.getName()) ;
      assertEquals("BrAPI-Phenotyping", traitSchema.getModule()) ;

    } catch (Exception e) {
      fail(e.getMessage()) ;
    }
  }
}