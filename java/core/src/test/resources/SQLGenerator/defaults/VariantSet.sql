-- VariantSet

/* 
A VariantSet is a collection of variants and variant calls intended to be analyzed together.
 */
CREATE TABLE brapi_VariantSets (
  -- Primary properties
  variantSetDbId STRING NOT NULL PRIMARY KEY COMMENT 'The unique identifier for a VariantSet',
  variantSetName STRING PRIMARY KEY COMMENT 'The human readable name for a VariantSet',
  -- Link properties
  referenceSetDbId STRING NOT NULL COMMENT 'The unique identifier for a ReferenceSet',
  referenceSetName STRING NOT NULL COMMENT 'The human readable name of a ReferenceSet',
  studyDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a study within the given database server  MIAPPE V1.1 (DM-11) Study unique ID - Unique identifier comprising the name or identifier for the institution/database hosting the submission of the study data, and the identifier of the study in that institution.',
  studyPUI STRING COMMENT 'A permanent unique identifier associated with this study data. For example, a URI or DOI',
  studyName STRING NOT NULL COMMENT 'The human readable name for a study  MIAPPE V1.1 (DM-12) Study title - Human-readable text summarising the study',
  -- Properties
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  analysis
    ARRAY<
      STRUCT<
        -- Primary properties
        analysisDbId STRING COMMENT 'Unique identifier for this analysis description',
        analysisName STRING COMMENT 'A human readable name for this analysis',
        -- Link properties
        variantSetDbId STRING COMMENT 'The unique identifier for a VariantSet',
        variantSetName STRING COMMENT 'The human readable name for a VariantSet',
        -- Properties
        created STRING COMMENT 'The time at which this record was created, in ISO 8601 format.',
        description STRING COMMENT 'A human readable description of the analysis',
        software ARRAY<STRING> COMMENT 'The software run to generate this analysis.',
        type STRING COMMENT 'The type of analysis.',
        updated STRING COMMENT 'The time at which this record was last updated, in ISO 8601 format.'
      >
    > COMMENT 'Set of Analysis descriptors for this VariantSet',
  availableFormats
    ARRAY<
      STRUCT<
        -- Link properties
        variantSetDbId STRING COMMENT 'The unique identifier for a VariantSet',
        variantSetName STRING COMMENT 'The human readable name for a VariantSet',
        -- Properties
        dataFormat STRING COMMENT 'dataFormat defines the structure of the data within a file (ie DartSeq, VCF, Hapmap, tabular, etc)',
        expandHomozygotes BOOLEAN COMMENT 'Should homozygotes be expanded (true) or collapsed into a single occurrence (false)',
        fileFormat STRING COMMENT 'fileFormat defines the MIME type of the file (ie text/csv, application/excel, application/zip). This should also be reflected in the Accept and ContentType HTTP headers for every relevant request and response.',
        fileURL STRING COMMENT 'A URL which indicates the location of the file version of this VariantSet. Could be a static file URL or an API endpoint which generates the file.',
        sepPhased STRING COMMENT 'The string used as a separator for phased allele calls.',
        sepUnphased STRING COMMENT 'The string used as a separator for unphased allele calls.',
        unknownString STRING COMMENT 'The string used as a representation for missing data.'
      >
    > COMMENT 'When the data for a VariantSet is retrieved, it can be retrieved in a variety of data formats and file formats.  <br/>''dataFormat'' defines the structure of the data within a file (ie DartSeq, VCF, Hapmap, tabular, etc) <br/>''fileFormat'' defines the MIME type of the file (ie text/csv, application/excel, application/zip). This should also be reflected in the Accept and ContentType HTTP headers for every relevant request and response.',
  callSetCount INT COMMENT 'The number of CallSets included in this VariantSet',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  metadataFields
    ARRAY<
      STRUCT<
        -- Link properties
        variantSetDbId STRING COMMENT 'The unique identifier for a VariantSet',
        variantSetName STRING COMMENT 'The human readable name for a VariantSet',
        -- Properties
        dataType STRING COMMENT 'The type of field represented in this Genotype Field. This is intended to help parse the data out of JSON.',
        fieldAbbreviation STRING COMMENT 'The abbreviated code of the field represented in this Genotype Field. These codes should match the VCF standard when possible. Examples include: "GQ", "RD", and "HQ"',
        fieldName STRING COMMENT 'The name of the field represented in this Genotype Field. Examples include: "Genotype Quality", "Read Depth", and "Haplotype Quality"'
      >
    > COMMENT 'The ''metadataField'' indicates which types of genotyping data and metadata are available in the VariantSet.  <br> When possible, these field names and abbreviations should follow the VCF standard ',
  variantCount INT COMMENT 'The number of Variants included in this VariantSet'
) 
COMMENT 'A VariantSet is a collection of variants and variant calls intended to be analyzed together.';


-- Generated by Schema Tools Generator Version: '0.62.0'
