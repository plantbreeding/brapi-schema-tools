-- Observation

/* 
A value assigned for a specific ObservationVariable when observing a specific ObservationUnit.
 */
CREATE TABLE brapi_Observations (
  -- Primary properties
  observationDbId STRING NOT NULL PRIMARY KEY COMMENT 'The ID which uniquely identifies an observation',
  -- Link properties
  germplasmDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
  germplasmPUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
  germplasmName STRING NOT NULL COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.',
  observationUnitDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
  observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
  observationUnitName STRING COMMENT 'A human readable name for an observation unit',
  observationVariableDbId STRING NOT NULL COMMENT 'Variable unique identifier  MIAPPE V1.1 (DM-83) Variable ID - Code used to identify the variable in the data file. We recommend using a variable definition from the Crop Ontology where possible. Otherwise, the Crop Ontology naming convention is recommended: <trait abbreviation>_<method abbreviation>_<scale abbreviation>). A variable ID must be unique within a given investigation.',
  observationVariablePUI STRING COMMENT 'The Permanent Unique Identifier of a Observation Variable, usually in the form of a URI',
  observationVariableName STRING NOT NULL COMMENT 'Variable name (usually a short name)  MIAPPE V1.1 (DM-84) Variable name - Name of the variable.',
  seasonDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a season. For backward compatibility it can be a string like ''2012'', ''1957-2004''',
  seasonName STRING COMMENT 'Name of the season. ex. ''Spring'', ''Q2'', ''Season A'', etc.',
  studyDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a study within the given database server  MIAPPE V1.1 (DM-11) Study unique ID - Unique identifier comprising the name or identifier for the institution/database hosting the submission of the study data, and the identifier of the study in that institution.',
  studyPUI STRING COMMENT 'A permanent unique identifier associated with this study data. For example, a URI or DOI',
  studyName STRING NOT NULL COMMENT 'The human readable name for a study  MIAPPE V1.1 (DM-12) Study title - Human-readable text summarising the study',
  -- Clustering properties
  observationTimeStamp STRING COMMENT 'The date and time when this observation was made',
  -- Properties
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  collector STRING COMMENT 'The name or identifier of the entity which collected the observation',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  geoCoordinates 
    STRUCT<
      geometry1
        STRUCT<
          coordinates ARRAY<DOUBLE> COMMENT 'A single position',
          type STRING COMMENT 'The literal string "Point"'
        >,
      geometry2
        STRUCT<
          coordinates ARRAY<DOUBLE> COMMENT 'An array of linear rings',
          type STRING COMMENT 'The literal string "Polygon"'
        > COMMENT 'A geometry as defined by GeoJSON (RFC 7946). In this context, only Point or Polygon geometry are allowed.',
      type STRING COMMENT 'The literal string "Feature"'
    > NOT NULL COMMENT 'One geometry as defined by GeoJSON (RFC 7946). All coordinates are decimal values on the WGS84 geographic coordinate reference system.  Copied from RFC 7946 Section 3.1.1  A position is an array of numbers. There MUST be two or more elements. The first two elements are longitude and latitude, or easting and northing, precisely in that order and using decimal numbers. Altitude or elevation MAY be included as an optional third element.',
  uploadedBy STRING COMMENT 'The name or id of the user who uploaded the observation to the database system',
  value STRING COMMENT 'The value of the data collected as an observation'
) 
COMMENT 'A value assigned for a specific ObservationVariable when observing a specific ObservationUnit.';


-- Generated by Schema Tools Generator Version: '0.60.0'
