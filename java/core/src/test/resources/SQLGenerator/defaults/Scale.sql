-- Scale

/* 
A Scale describes the units and acceptable values for an ObservationVariable. 
<br>For example, an ObservationVariable might be defined with a Trait of "plant height", a Scale of "meters", and a Method of "tape measure". This variable would be distinct from a variable with the Scale "inches" or "pixels".
 */
CREATE TABLE brapi_Scales (
  scaleDbId STRING NOT NULL PRIMARY KEY COMMENT 'Unique identifier of the scale. If left blank, the upload system will automatically generate a scale ID.',
  scaleName STRING NOT NULL PRIMARY KEY COMMENT 'Name of the scale <br/>MIAPPE V1.1 (DM-92) Scale Name of the scale associated with the variable',
  scalePUI STRING PRIMARY KEY COMMENT 'The Permanent Unique Identifier of a Scale, usually in the form of a URI',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  dataType STRING COMMENT '<p>Class of the scale, entries can be</p> <p>"Code" -  This scale class is exceptionally used to express complex traits. Code is a nominal scale that combines the expressions of the different traits composing the complex trait. For example a severity trait might be expressed by a 2 digit and 2 character code. The first 2 digits are the percentage of the plant covered by a fungus and the 2 characters refer to the delay in development, e.g. "75VD" means "75 %" of the plant is infected and the plant is very delayed.</p> <p>"Date" - The date class is for events expressed in a time format, See ISO 8601</p> <p>"Duration" - The Duration class is for time elapsed between two events expressed in a time format, e.g. days, hours, months</p> <p>"Nominal" - Categorical scale that can take one of a limited and fixed number of categories. There is no intrinsic ordering to the categories</p> <p>"Numerical" - Numerical scales express the trait with real numbers. The numerical scale defines the unit e.g. centimeter, ton per hectare, branches</p> <p>"Ordinal" - Ordinal scales are scales composed of ordered categories</p> <p>"Text" - A free text is used to express the trait.</p>',
  decimalPlaces INT COMMENT 'For numerical, number of decimal places to be reported',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
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
      ontologyDbId STRING COMMENT 'Ontology database unique identifier',
      ontologyName STRING COMMENT 'Ontology name',
      version STRING COMMENT 'Ontology version (no specific format)'
    > NOT NULL COMMENT 'MIAPPE V1.1  (DM-85) Variable accession number - Accession number of the variable in the Crop Ontology  (DM-87) Trait accession number - Accession number of the trait in a suitable controlled vocabulary (Crop Ontology, Trait Ontology).  (DM-89) Method accession number - Accession number of the method in a suitable controlled vocabulary (Crop Ontology, Trait Ontology).  (DM-93) Scale accession number - Accession number of the scale in a suitable controlled vocabulary (Crop Ontology).',
  units STRING COMMENT 'This field can be used to describe the units used for this scale. This should be the abbreviated  form of the units, intended to be displayed with every value using this scale. Usually this only  applies when `dataType` is Numeric, but could also be included for other dataTypes when applicable.',
  validValues 
    STRUCT<
      categories
        ARRAY<
          STRUCT<
            label STRING COMMENT 'A text label for a category',
            value STRING COMMENT 'The actual value for a category'
          >
        > COMMENT 'List of possible values with optional labels',
      maximumValue STRING COMMENT 'Maximum value for numerical, date, and time scales. Typically used for data capture control and QC.',
      minimumValue STRING COMMENT 'Minimum value for numerical, date, and time scales. Typically used for data capture control and QC.'
    > COMMENT 'validValues: ValidValues'
) 
COMMENT 'A Scale describes the units and acceptable values for an ObservationVariable.  <br>For example, an ObservationVariable might be defined with a Trait of "plant height", a Scale of "meters", and a Method of "tape measure". This variable would be distinct from a variable with the Scale "inches" or "pixels".';


-- Generated by Schema Tools Generator Version: '0.60.0'
