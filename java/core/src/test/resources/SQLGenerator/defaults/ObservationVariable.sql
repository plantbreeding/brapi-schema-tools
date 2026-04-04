-- ObservationVariable

/* 
An **Observation Variable** is a parameter of the observation unit that is assessed in the context of a study. An ObservationVariable is defined at **crop**-level.

Several Observation Variables can be related to the same phenotypic characteristic
(e.g. height) but will be considered as different observation variables
if they are measured at a different scale.
For example, the Observation Variable 'height in centimeters' differs from the 
Observation Variable 'height in inches'.

An Observation Variable is typically used to capture phenotypic measurements such as 
height, yield, weight, etc. However, it can also capture genotypic or other characteristics. 

### Phenome Implementation

Observation Variables are Variables with the Variable type 'Variate'
that have been assigned to the Germplasm entity.

### PRISM Implementation

> TODO Breeding Services please check

In Field Crops and Vegetable Observation Variables are held different workbooks depending
on the type of Study and are columns in the corresponding field book table.
 */
CREATE TABLE IF NOT EXISTS sta_dash.dadi_br_sandbox.silver_phenome_observation_variables (
  observationVariableDbId STRING NOT NULL COMMENT 'Variable unique identifier  MIAPPE V1.1 (DM-83) Variable ID - Code used to identify the variable in the data file. We recommend using a variable definition from the Crop Ontology where possible. Otherwise, the Crop Ontology naming convention is recommended: <trait abbreviation>_<method abbreviation>_<scale abbreviation>). A variable ID must be unique within a given investigation.',
  observationVariableName STRING NOT NULL COMMENT 'Variable name (usually a short name)  MIAPPE V1.1 (DM-84) Variable name - Name of the variable.',
  observationVariablePUI STRING PRIMARY KEY COMMENT 'The Permanent Unique Identifier of a Observation Variable, usually in the form of a URI',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  commonCropName STRING COMMENT 'Crop name (examples: "Maize", "Wheat")',
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
  methodPUI STRING COMMENT 'The Permanent Unique Identifier of a Method, usually in the form of a URI',
  ontologyReference 
    STRUCT<
      ontologyReferenceDbId STRING COMMENT 'The ID which uniquely identifies a ontology reference',
      documentationLinks
        ARRAY<
          STRUCT<
            URL STRING COMMENT 'The URL or URI to the documentation',
            type STRING COMMENT 'The type of documentation, which can be OBO Foundry, an RDF term or a webpage.'
          >
        > COMMENT 'links to various ontology documentation',
      ontologyDbId STRING NOT NULL COMMENT 'Ontology database unique identifier',
      version STRING COMMENT 'Ontology version (no specific format)'
    > NOT NULL COMMENT 'MIAPPE V1.1  (DM-85) Variable accession number - Accession number of the variable in the Crop Ontology  (DM-87) Trait accession number - Accession number of the trait in a suitable controlled vocabulary (Crop Ontology, Trait Ontology).  (DM-89) Method accession number - Accession number of the method in a suitable controlled vocabulary (Crop Ontology, Trait Ontology).  (DM-93) Scale accession number - Accession number of the scale in a suitable controlled vocabulary (Crop Ontology).',
  scalePUI STRING COMMENT 'The Permanent Unique Identifier of a Scale, usually in the form of a URI',
  scientist STRING COMMENT 'Name of scientist submitting the variable.',
  status STRING COMMENT 'Variable status. (examples: "recommended", "obsolete", "legacy", etc.)',
  submissionTimestamp STRING COMMENT 'Timestamp when the Variable was added (ISO 8601)',
  synonyms ARRAY<STRING> COMMENT 'Other variable names',
  traitPUI STRING COMMENT 'The Permanent Unique Identifier of a Trait, usually in the form of a URI',
  variableCategory STRING COMMENT 'General category for the variable. very similar to Trait class.'
) 
USING delta
CLUSTER BY (commonCropName,status,variableCategory)
TBLPROPERTIES ('delta.enableChangeDataFeed' = true)
COMMENT 'An **Observation Variable** is a parameter of the observation unit that is assessed in the context of a study. An ObservationVariable is defined at **crop**-level.  Several Observation Variables can be related to the same phenotypic characteristic (e.g. height) but will be considered as different observation variables if they are measured at a different scale. For example, the Observation Variable \\'height in centimeters\\' differs from the  Observation Variable \\'height in inches\\'.  An Observation Variable is typically used to capture phenotypic measurements such as  height, yield, weight, etc. However, it can also capture genotypic or other characteristics.   ### Phenome Implementation  Observation Variables are Variables with the Variable type \\'Variate\\' that have been assigned to the Germplasm entity.  ### PRISM Implementation  > TODO Breeding Services please check  In Field Crops and Vegetable Observation Variables are held different workbooks depending on the type of Study and are columns in the corresponding field book table.';


-- Generated by Schema Tools Generator Version: '0.60.0'
