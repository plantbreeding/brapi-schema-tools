package org.brapi.schematools.core.brapischema;

import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BrAPISchemaReaderTest {

  @Test
  void read()  {

    try {
      Map<String, BrAPIObjectType> schemas =
              new BrAPISchemaReader().read(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI())).stream().collect(Collectors.toMap(BrAPIObjectType::getName, Function.identity()));

      assertNotNull(schemas) ;
      assertEquals(37, schemas.size()) ;

      BrAPIObjectType trialSchema = schemas.get("Trial") ;
      assertNotNull(trialSchema) ;
      assertEquals("Trial", trialSchema.getName()) ;
      assertEquals("BrAPI-Core", trialSchema.getModule()) ;

      BrAPIObjectType sampleSchema = schemas.get("Sample") ;
      assertNotNull(sampleSchema) ;
      assertEquals("Sample", sampleSchema.getName()) ;
      assertEquals("BrAPI-Genotyping", sampleSchema.getModule()) ;

      BrAPIObjectType germplasmSchema = schemas.get("Germplasm") ;
      assertNotNull(germplasmSchema) ;
      assertEquals("Germplasm", germplasmSchema.getName()) ;
      assertEquals("BrAPI-Germplasm", germplasmSchema.getModule()) ;

      BrAPIObjectType traitSchema = schemas.get("Trait") ;
      assertNotNull(traitSchema) ;
      assertEquals("Trait", traitSchema.getName()) ;
      assertEquals("BrAPI-Phenotyping", traitSchema.getModule()) ;

    } catch (Exception e) {
      fail(e.getMessage()) ;
    }
  }
}