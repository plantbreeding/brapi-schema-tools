-- Trial

/* 
A Trial represents a collection of Study objects, and the metadata associated with that collection. A Trial could represent a multi-location experiment, and could contain information related to publications and data licensing.
 */
CREATE TABLE brapi_Trials (
  -- Primary properties
  trialDbId STRING NOT NULL PRIMARY KEY COMMENT 'The ID which uniquely identifies a trial  MIAPPE V1.1 (DM-2) Investigation unique ID - Identifier comprising the unique name of the institution/database hosting the submission of the investigation data, and the accession number of the investigation in that institution.',
  trialName STRING NOT NULL PRIMARY KEY COMMENT 'The human readable name of a trial  MIAPPE V1.1 (DM-3) Investigation title - Human-readable string summarising the investigation.',
  trialPUI STRING PRIMARY KEY COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.',
  -- Link properties
  programDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies the program',
  programName STRING NOT NULL COMMENT 'Human readable name of the program',
  studyDbIds ARRAY<STRING> COMMENT '',
  -- Clustering properties
  commonCropName STRING COMMENT 'Common name for the crop associated with this trial',
  -- Properties
  active BOOLEAN COMMENT 'A flag to indicate if a Trial is currently active and ongoing',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  contacts
    ARRAY<
      STRUCT<
        -- Primary properties
        contactDbId STRING COMMENT 'The ID which uniquely identifies this contact  MIAPPE V1.1 (DM-33) Person ID - An identifier for the data submitter. If that submitter is an individual, ORCID identifiers are recommended.',
        email STRING COMMENT 'The contacts email address  MIAPPE V1.1 (DM-32) Person email - The electronic mail address of the person.',
        instituteName STRING COMMENT 'The name of the institution which this contact is part of  MIAPPE V1.1 (DM-35) Person affiliation - The institution the person belongs to',
        name STRING COMMENT 'The full name of this contact person  MIAPPE V1.1 (DM-31) Person name - The name of the person (either full name or as used in scientific publications)',
        orcid STRING COMMENT 'The Open Researcher and Contributor ID for this contact person (orcid.org)  MIAPPE V1.1 (DM-33) Person ID - An identifier for the data submitter. If that submitter is an individual, ORCID identifiers are recommended.',
        type STRING COMMENT 'The type of person this contact represents (ex: Coordinator, Scientist, PI, etc.)  MIAPPE V1.1 (DM-34) Person role - Type of contribution of the person to the investigation'
      >
    > COMMENT 'List of contact entities associated with this trial',
  datasetAuthorships
    ARRAY<
      STRUCT<
        -- Link properties
        trialDbId STRING COMMENT 'The ID which uniquely identifies a trial  MIAPPE V1.1 (DM-2) Investigation unique ID - Identifier comprising the unique name of the institution/database hosting the submission of the investigation data, and the accession number of the investigation in that institution.',
        trialPUI STRING COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.',
        trialName STRING COMMENT 'The human readable name of a trial  MIAPPE V1.1 (DM-3) Investigation title - Human-readable string summarising the investigation.',
        -- Properties
        datasetPUI STRING COMMENT 'The DOI or other permanent unique identifier for this published dataset',
        license STRING COMMENT 'MIAPPE V1.1 (DM-7) License - License for the reuse of the data associated with this investigation. The Creative Commons licenses cover most use cases and are recommended.',
        publicReleaseDate STRING COMMENT 'MIAPPE V1.1 (DM-6) Public release date - Date of first public release of the dataset presently being described.',
        submissionDate STRING COMMENT 'MIAPPE V1.1 (DM-5) Submission date - Date of submission of the dataset presently being described to a host repository.'
      >
    > COMMENT 'License and citation information for the data in this trial',
  documentationURL STRING COMMENT 'A URL to the human readable documentation of an object',
  endDate STRING COMMENT 'The date this trial ends',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  publications
    ARRAY<
      STRUCT<
        -- Primary properties
        publicationPUI STRING COMMENT 'The permanent unique identifier of the publication.',
        -- Link properties
        trialDbId STRING COMMENT 'The ID which uniquely identifies a trial  MIAPPE V1.1 (DM-2) Investigation unique ID - Identifier comprising the unique name of the institution/database hosting the submission of the investigation data, and the accession number of the investigation in that institution.',
        trialPUI STRING COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.',
        trialName STRING COMMENT 'The human readable name of a trial  MIAPPE V1.1 (DM-3) Investigation title - Human-readable string summarising the investigation.',
        -- Properties
        publicationReference STRING COMMENT 'The publication reference.'
      >
    > COMMENT 'MIAPPE V1.1 (DM-9) Associated publication - An identifier for a literature publication where the investigation is described. Use of DOIs is recommended.',
  startDate STRING COMMENT 'The date this trial started',
  trialDescription STRING COMMENT 'The human readable description of a trial  MIAPPE V1.1 (DM-4) Investigation description - Human-readable text describing the investigation in more detail.'
) 
COMMENT 'A Trial represents a collection of Study objects, and the metadata associated with that collection. A Trial could represent a multi-location experiment, and could contain information related to publications and data licensing.';


-- Generated by Schema Tools Generator Version: '0.62.0'
