-- GermplasmAttribute

/* 
The Trait-Method-Scale definition for a variable, specifically variables related to Germplasm. Similar to an ObservationVariable, but related to a Germplasm instead of an ObservationUnit
 */
CREATE TABLE brapi_GermplasmAttributes (
  -- Primary properties
  attributeDbId STRING NOT NULL PRIMARY KEY COMMENT 'The ID which uniquely identifies this attribute within the given database server',
  attributeName STRING NOT NULL PRIMARY KEY COMMENT 'A human readable name for this attribute',
  attributePUI STRING NOT NULL PRIMARY KEY COMMENT 'The Permanent Unique Identifier of an Attribute, usually in the form of a URI',
  -- Link properties
  attributeValueDbIds ARRAY<STRING> COMMENT 'attributeValues',
  methodDbId STRING COMMENT 'Method unique identifier',
  methodPUI STRING COMMENT 'The Permanent Unique Identifier of a Method, usually in the form of a URI',
  methodName STRING NOT NULL COMMENT 'Human readable name for the method <br/>MIAPPE V1.1 (DM-88) Method  Name of the method of observation',
  scaleDbId STRING NOT NULL COMMENT 'Unique identifier of the scale. If left blank, the upload system will automatically generate a scale ID.',
  scalePUI STRING COMMENT 'The Permanent Unique Identifier of a Scale, usually in the form of a URI',
  scaleName STRING NOT NULL COMMENT 'Name of the scale <br/>MIAPPE V1.1 (DM-92) Scale Name of the scale associated with the variable',
  traitDbId STRING COMMENT 'The ID which uniquely identifies a trait',
  traitPUI STRING COMMENT 'The Permanent Unique Identifier of a Trait, usually in the form of a URI',
  traitName STRING NOT NULL COMMENT 'The human readable name of a trait <br/>MIAPPE V1.1 (DM-86) Trait - Name of the (plant or environmental) trait under observation',
  -- Clustering properties
  commonCropName STRING COMMENT 'Crop name (examples: "Maize", "Wheat")',
  -- Properties
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  attributeCategory STRING NOT NULL COMMENT 'General category for the attribute. very similar to Trait class.',
  attributeDescription STRING NOT NULL COMMENT 'A human readable description of this attribute',
  contextOfUse ARRAY<STRING> COMMENT 'Indication of how trait is routinely used. (examples: ["Trial evaluation", "Nursery evaluation"])',
  defaultValue STRING COMMENT 'Variable default value. (examples: "red", "2.3", etc.)',
  documentationURL STRING COMMENT 'A URL to the human readable documentation of an object',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  growthStage STRING COMMENT 'Growth stage at which measurement is made (examples: "flowering")',
  institution STRING COMMENT 'Name of institution submitting the variable',
  language STRING COMMENT '2 letter ISO 639-1 code for the language of submission of the variable.',
  ontologyReference 
    STRUCT<
      -- Primary properties
      ontologyReferenceDbId STRING COMMENT 'The ID which uniquely identifies a ontology reference',
      -- Link properties
      ontologyDbId STRING COMMENT 'Ontology database unique identifier',
      ontologyName STRING COMMENT 'Ontology name',
      -- Properties
      documentationLinks
        ARRAY<
          STRUCT<
            URL STRING COMMENT 'The URL or URI to the documentation',
            type STRING COMMENT 'The type of documentation, which can be OBO Foundry, an RDF term or a webpage.'
          >
        > COMMENT 'links to various ontology documentation',
      version STRING COMMENT 'Ontology version (no specific format)'
    > NOT NULL COMMENT 'MIAPPE V1.1  (DM-85) Variable accession number - Accession number of the variable in the Crop Ontology  (DM-87) Trait accession number - Accession number of the trait in a suitable controlled vocabulary (Crop Ontology, Trait Ontology).  (DM-89) Method accession number - Accession number of the method in a suitable controlled vocabulary (Crop Ontology, Trait Ontology).  (DM-93) Scale accession number - Accession number of the scale in a suitable controlled vocabulary (Crop Ontology).',
  scientist STRING COMMENT 'Name of scientist submitting the variable.',
  status STRING COMMENT 'Variable status. (examples: "recommended", "obsolete", "legacy", etc.)',
  submissionTimestamp STRING COMMENT 'Timestamp when the Variable was added (ISO 8601)',
  synonyms ARRAY<STRING> COMMENT 'Other variable names'
) 
COMMENT 'The Trait-Method-Scale definition for a variable, specifically variables related to Germplasm. Similar to an ObservationVariable, but related to a Germplasm instead of an ObservationUnit';


/* 
Controlled Vocabulary for attributeCategories of GermplasmAttribute
 */
CREATE TABLE brapi_AttributeCategories (
  attributeCategory STRING NOT NULL COMMENT 'General category for the attribute. very similar to Trait class.'
) 
COMMENT 'Controlled Vocabulary table for property attributeCategory on GermplasmAttribute';


-- Generated by Schema Tools Generator Version: '0.62.0'
