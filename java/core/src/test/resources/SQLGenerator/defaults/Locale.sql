-- Locale

/* 
A **Locale** is a location where one or more studies
are set up within a **single** season.

A Locale is a way of grouping of different studies and is **not** crop-specific.

### PRISM Implementation

A Locale concept in PRISM is the Yield Trial Field Book **name** or Nursery Field Book **name** in a **specific** season.

### Phenome Implementation

A Locale in Phenome can be migrated from PRISM or new Locale created only in Phenome.
 */
CREATE TABLE IF NOT EXISTS sta_dash.dadi_br_sandbox.silver_phenome_locales (
  localeDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a locale for the given database server',
  localePUI STRING PRIMARY KEY COMMENT 'A permanent unique identifier associated with this locale. For example, a URI or DOI',
  active BOOLEAN NOT NULL COMMENT 'Is this locale currently active',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  attributeValues
    ARRAY<
      STRUCT<
        attributeValueDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies this attribute value within the given database server',
        attributeValuePUI STRING COMMENT 'The Permanent Unique Identifier of an Attribute Value, usually in the form of a URI',
        additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
        attributePUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier of an Attribute, usually in the form of a URI',
        determinedDate STRING COMMENT 'The date the value of this attribute was determined for a given germplasm',
        externalReferences
          ARRAY<
            STRUCT<
              referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
              referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
            >
          > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
        localePUI STRING PRIMARY KEY COMMENT 'A permanent unique identifier associated with this locale. For example, a URI or DOI',
        value STRING COMMENT 'The value of this attribute for a given entity'
      >
    > COMMENT 'attributeValues',
  commonCropName STRING NOT NULL COMMENT 'Common name for the crop associated with this locale',
  contacts
    ARRAY<
      STRUCT<
        contactDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies this contact  MIAPPE V1.1 (DM-33) Person ID - An identifier for the data submitter. If that submitter is an individual, ORCID identifiers are recommended.',
        email STRING COMMENT 'The contacts email address  MIAPPE V1.1 (DM-32) Person email - The electronic mail address of the person.',
        instituteName STRING COMMENT 'The name of the institution which this contact is part of  MIAPPE V1.1 (DM-35) Person affiliation - The institution the person belongs to',
        name STRING COMMENT 'The full name of this contact person  MIAPPE V1.1 (DM-31) Person name - The name of the person (either full name or as used in scientific publications)',
        orcid STRING COMMENT 'The Open Researcher and Contributor ID for this contact person (orcid.org)  MIAPPE V1.1 (DM-33) Person ID - An identifier for the data submitter. If that submitter is an individual, ORCID identifiers are recommended.',
        type STRING COMMENT 'The type of person this contact represents (ex: Coordinator, Scientist, PI, etc.)  MIAPPE V1.1 (DM-34) Person role - Type of contribution of the person to the investigation'
      >
    > COMMENT 'List of contact entities associated with this locale',
  cropPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Crop. For example, a URI or DOI',
  endDate STRING COMMENT 'The date the Locale ends',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  lastUpdate 
    STRUCT<
      timestamp STRING NOT NULL COMMENT 'The timestamp of the update.',
      version STRING NOT NULL COMMENT 'The version of the update.'
    > COMMENT 'The date and time when this Locale was last modified',
  localeDescription STRING COMMENT 'The description of this locale',
  localeType STRING COMMENT 'The type of locale ex. "Yield Trial", etc',
  locationPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this location. For example, a URI or DOI.',
  programPUI STRING COMMENT 'A permanent identifier for a program. Could be DOI or other URI formatted identifier.',
  seasonPUI STRING COMMENT 'A permanent unique identifier associated with this season. For example, a URI or DOI.',
  startDate STRING COMMENT 'The date this locale started  MIAPPE V1.1 (DM-14) Start date of locale - Date and, if relevant, time when the experiment started',
  studyPUIs ARRAY<STRING> COMMENT ''
) 
USING delta
CLUSTER BY (commonCropName,localeType,active)
TBLPROPERTIES ('delta.enableChangeDataFeed' = true)
COMMENT 'A **Locale** is a location where one or more studies are set up within a **single** season.  A Locale is a way of grouping of different studies and is **not** crop-specific.  ### PRISM Implementation  A Locale concept in PRISM is the Yield Trial Field Book **name** or Nursery Field Book **name** in a **specific** season.  ### Phenome Implementation  A Locale in Phenome can be migrated from PRISM or new Locale created only in Phenome.';


-- Generated by Schema Tools Generator Version: '0.60.0'
