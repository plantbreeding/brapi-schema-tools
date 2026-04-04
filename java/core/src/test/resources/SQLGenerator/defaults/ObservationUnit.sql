-- ObservationUnit

/* 
A representation of the physical entity being observed during a phenotype data collection process. Typically, this is a Plot or a Plant, but it could include things like Fields, Blocks, or Samples.
 */
CREATE TABLE brapi_ObservationUnits (
  observationUnitDbId STRING NOT NULL PRIMARY KEY COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
  observationUnitName STRING PRIMARY KEY COMMENT 'A human readable name for an observation unit',
  observationUnitPUI STRING PRIMARY KEY COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  crossDbId STRING NOT NULL COMMENT 'the unique identifier for a cross',
  crossName STRING COMMENT 'the human readable name for a cross',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  germplasmDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
  germplasmPUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
  germplasmName STRING NOT NULL COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.',
  locationDbId STRING NOT NULL COMMENT 'The unique identifier for a Location',
  locationName STRING NOT NULL COMMENT 'A human readable name for a Location <br/> MIAPPE V1.1 (DM-18) Experimental site name - The name of the natural site, experimental field, greenhouse, phenotyping facility, etc. where the experiment took place.',
  observationUnitPosition
    ARRAY<
      STRUCT<
        entryType STRING COMMENT 'The type of entry for this observation unit. ex. "CHECK", "TEST", "FILLER"',
        geoCoordinates
          ARRAY<
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
            >
          > COMMENT 'One geometry as defined by GeoJSON (RFC 7946). All coordinates are decimal values on the WGS84 geographic coordinate reference system.  Copied from RFC 7946 Section 3.1.1  A position is an array of numbers. There MUST be two or more elements. The first two elements are longitude and latitude, or easting and northing, precisely in that order and using decimal numbers. Altitude or elevation MAY be included as an optional third element.',
        observationLevel 
          STRUCT<
            levelCode STRING COMMENT 'An ID code or number to represent a real thing that may or may not be an an observation unit. <br/>For example, if the \'levelName\' is \'plot\', then the \'levelCode\' would be the plot number or plot barcode. If this plot is also considered an ObservationUnit, then the appropriate observationUnitDbId should also be recorded. <br/>If the \'levelName\' is \'field\', then the \'levelCode\' might be something like \'3\' or \'F3\' to indicate the third field at a research station. ',
            levelName STRING COMMENT 'A name for this level   **Standard Level Names: study, field, entry, rep, block, sub-block, plot, sub-plot, plant, pot, sample**   For more information on Observation Levels, please review the <a target="_blank" href="https://wiki.brapi.org/index.php/Observation_Levels">Observation Levels documentation</a>. ',
            levelOrder INT COMMENT '`levelOrder` defines where that level exists in the hierarchy of levels. `levelOrder`\'s lower numbers  are at the top of the hierarchy (ie field -> 1) and higher numbers are at the bottom of the hierarchy (ie plant -> 9).   For more information on Observation Levels, please review the <a target="_blank" href="https://wiki.brapi.org/index.php/Observation_Levels">Observation Levels documentation</a>. '
          > COMMENT 'The exact level and level code of an observation unit.   For more information on Observation Levels, please review the <a target="_blank" href="https://wiki.brapi.org/index.php/Observation_Levels">Observation Levels documentation</a>.   MIAPPE V1.1 DM-71 Observation unit type "Type of observation unit in textual form, usually one of the following: study, block, sub-block, plot, sub-plot, pot, plant. Use of other observation unit types is possible but not recommended.  The observation unit type can not be used to indicate sub-plant levels. However, observations can still be made on the sub-plant level, as long as the details are indicated in the associated observed variable (see observed variables).  Alternatively, it is possible to use samples for more detailed tracing of sub-plant units, attaching the observations to them instead." ',
        observationLevelRelationships
          ARRAY<
            STRUCT<
              levelCode STRING COMMENT 'An ID code or number to represent a real thing that may or may not be an an observation unit. <br/>For example, if the \'levelName\' is \'plot\', then the \'levelCode\' would be the plot number or plot barcode. If this plot is also considered an ObservationUnit, then the appropriate observationUnitDbId should also be recorded. <br/>If the \'levelName\' is \'field\', then the \'levelCode\' might be something like \'3\' or \'F3\' to indicate the third field at a research station. ',
              levelName STRING COMMENT 'A name for this level   **Standard Level Names: study, field, entry, rep, block, sub-block, plot, sub-plot, plant, pot, sample**   For more information on Observation Levels, please review the <a target="_blank" href="https://wiki.brapi.org/index.php/Observation_Levels">Observation Levels documentation</a>. ',
              levelOrder INT COMMENT '`levelOrder` defines where that level exists in the hierarchy of levels. `levelOrder`\'s lower numbers  are at the top of the hierarchy (ie field -> 1) and higher numbers are at the bottom of the hierarchy (ie plant -> 9).   For more information on Observation Levels, please review the <a target="_blank" href="https://wiki.brapi.org/index.php/Observation_Levels">Observation Levels documentation</a>. ',
              observationUnitDbId STRING COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
              observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
              observationUnitName STRING COMMENT 'A human readable name for an observation unit'
            >
          > COMMENT 'Observation levels indicate the granularity level at which the measurements are taken. `levelName`  defines the level, `levelOrder` defines where that level exists in the hierarchy of levels.  `levelOrder`s lower numbers are at the top of the hierarchy (ie field > 0) and higher numbers are  at the bottom of the hierarchy (ie plant > 6). `levelCode` is an ID code for this level tag. Identify  this observation unit by each level of the hierarchy where it exists.   For more information on Observation Levels, please review the <a target="_blank" href="https://wiki.brapi.org/index.php/Observation_Levels">Observation Levels documentation</a>.   **Standard Level Names: study, field, entry, rep, block, sub-block, plot, sub-plot, plant, pot, sample** ',
        observationUnitDbId STRING COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
        observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
        observationUnitName STRING COMMENT 'A human readable name for an observation unit',
        positionCoordinateX STRING COMMENT 'The X position coordinate for an observation unit. Different systems may use different coordinate systems.',
        positionCoordinateXType STRING COMMENT 'The type of positional coordinate used for the X coordinate of the position.',
        positionCoordinateY STRING COMMENT 'The Y position coordinate for an observation unit. Different systems may use different coordinate systems.',
        positionCoordinateYType STRING COMMENT 'The type of positional coordinate used for the Y coordinate of the position.'
      >
    > COMMENT 'All positional and layout information related to this Observation Unit   MIAPPE V1.1 (DM-73) Spatial distribution - Type and value of a spatial coordinate (georeference or relative)  or level of observation (plot 45, subblock 7, block 2) provided as a key-value pair of the form type:value.  Levels of observation must be consistent with those listed in the Study section.',
  programDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies the program',
  programName STRING NOT NULL COMMENT 'Human readable name of the program',
  seedLotDbId STRING NOT NULL COMMENT 'Unique DbId for the Seed Lot',
  seedLotName STRING NOT NULL COMMENT 'A human readable name for this Seed Lot',
  studyDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a study within the given database server  MIAPPE V1.1 (DM-11) Study unique ID - Unique identifier comprising the name or identifier for the institution/database hosting the submission of the study data, and the identifier of the study in that institution.',
  studyPUI STRING COMMENT 'A permanent unique identifier associated with this study data. For example, a URI or DOI',
  studyName STRING NOT NULL COMMENT 'The human readable name for a study  MIAPPE V1.1 (DM-12) Study title - Human-readable text summarising the study',
  treatments
    ARRAY<
      STRUCT<
        factor STRING COMMENT 'The type of treatment/factor. ex. \'fertilizer\', \'inoculation\', \'irrigation\', etc  MIAPPE V1.1 (DM-61) Experimental Factor type - Name/Acronym of the experimental factor.',
        modality STRING COMMENT 'The treatment/factor description. ex. \'low fertilizer\', \'yellow rust inoculation\', \'high water\', etc  MIAPPE V1.1 (DM-62) Experimental Factor description - Free text description of the experimental factor. This includes all relevant treatments planned and protocol planned for all the plants targeted by a given experimental factor. ',
        observationUnitDbId STRING COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
        observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
        observationUnitName STRING COMMENT 'A human readable name for an observation unit'
      >
    > COMMENT 'List of treatments applied to an observation unit.  MIAPPE V1.1 (DM-74) Observation Unit factor value - List of values for each factor applied to the observation unit.',
  trialDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a trial  MIAPPE V1.1 (DM-2) Investigation unique ID - Identifier comprising the unique name of the institution/database hosting the submission of the investigation data, and the accession number of the investigation in that institution.',
  trialPUI STRING COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.',
  trialName STRING NOT NULL COMMENT 'The human readable name of a trial  MIAPPE V1.1 (DM-3) Investigation title - Human-readable string summarising the investigation.'
) 
COMMENT 'A representation of the physical entity being observed during a phenotype data collection process. Typically, this is a Plot or a Plant, but it could include things like Fields, Blocks, or Samples.';


-- Generated by Schema Tools Generator Version: '0.60.0'
