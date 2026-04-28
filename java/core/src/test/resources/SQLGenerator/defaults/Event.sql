-- Event

/* 
An event is discrete occurrence at a particular time in the experiment. Events may be the realization of Treatments or parts of Treatments, or may be confounding to Treatments. 
<br> ICASA Management Events allow for the following types -> planting, fertilizer, irrigation, tillage, organic_material, harvest, bed_prep, inorg_mulch, inorg_mul_rem, chemicals, mowing, observation, weeding, puddling, flood_level, other
 */
CREATE TABLE brapi_Events (
  -- Primary properties
  eventDbId STRING NOT NULL PRIMARY KEY COMMENT 'Internal database identifier',
  -- Link properties
  studyDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a study within the given database server  MIAPPE V1.1 (DM-11) Study unique ID - Unique identifier comprising the name or identifier for the institution/database hosting the submission of the study data, and the identifier of the study in that institution.',
  studyPUI STRING COMMENT 'A permanent unique identifier associated with this study data. For example, a URI or DOI',
  studyName STRING NOT NULL COMMENT 'The human readable name for a study  MIAPPE V1.1 (DM-12) Study title - Human-readable text summarising the study',
  -- Properties
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  eventDateRange
    ARRAY<
      STRUCT<
        -- Link properties
        eventDbId STRING COMMENT 'Internal database identifier',
        -- Properties
        discreteDates ARRAY<STRING> COMMENT 'A list of dates when the event occurred <br/>MIAPPE V1.1 (DM-68) Event date - Date and time of the event.',
        endDate STRING COMMENT 'The end of a continuous or regularly repetitive event <br/>MIAPPE V1.1 (DM-68) Event date - Date and time of the event.',
        startDate STRING COMMENT 'The beginning of a continuous or regularly repetitive event <br/>MIAPPE V1.1 (DM-68) Event date - Date and time of the event.'
      >
    > COMMENT 'An object describing when a particular Event has taken place. An Event can occur at one or more discrete time points (`discreteDates`) or an event can happen continuously over a longer period of time (`startDate`, `endDate`)',
  eventDescription STRING COMMENT 'A detailed, human-readable description of this event <br/>MIAPPE V1.1 (DM-67) Event description - Description of the event, including details such as amount applied and possibly duration of the event. ',
  eventParameters
    ARRAY<
      STRUCT<
        -- Link properties
        eventDbId STRING COMMENT 'Internal database identifier',
        -- Properties
        code STRING COMMENT 'The shortened code name of an event parameter <br>ICASA "Code_Display"',
        description STRING COMMENT 'A human readable description of this event parameter. This description is usually associated with the ''name'' and ''code'' of an event parameter.',
        name STRING COMMENT 'The full name of an event parameter <br>ICASA "Variable_Name"',
        units STRING COMMENT 'The units or data type of the ''value''.  <br>If the ''value'' comes from a standardized vocabulary or an encoded list of values, then ''unit'' should be ''code''.  <br>If the ''value'' IS NOT a number, then ''unit'' should specify a data type eg. ''text'', ''boolean'', ''date'', etc.  <br>If the value IS a number, then ''unit'' should specify the units used eg. ''ml'', ''cm'', etc <br>ICASA "Unit_or_type"',
        value STRING COMMENT 'The single value of this event parameter. This single value is accurate for all the dates in the date range. If ''value'' is populated then ''valuesByDate'' should NOT be populated.',
        valueDescription STRING COMMENT 'If the event parameter ''unit'' field is ''code'', then use ''valueDescription'' to add a human readable description to the value.',
        valuesByDate ARRAY<STRING> COMMENT 'An array of values corresponding to each timestamp in the ''discreteDates'' array of this event. The ''valuesByDate'' array should exactly match the size of the ''discreteDates'' array. If ''valuesByDate'' is populated then ''value'' should NOT be populated.'
      >
    > COMMENT 'A list of objects describing additional event parameters. Each of the following accepts a human-readable value or URI',
  eventType STRING COMMENT 'An identifier for this event type, in the form of an ontology class reference <br/>ICASA Management events allow for the following types: planting, fertilizer, irrigation, tillage, organic_material, harvest, bed_prep, inorg_mulch, inorg_mul_rem, chemicals, mowing, observation, weeding, puddling, flood_level, other <br/>MIAPPE V1.1 (DM-66) Event accession number - Accession number of the event type in a suitable controlled vocabulary (Crop Ontology).',
  eventTypeDbId STRING COMMENT 'An identifier for this event type, in the form of an ontology class reference <br/>ICASA Management events allow for the following types: planting, fertilizer, irrigation, tillage, organic_material, harvest, bed_prep, inorg_mulch, inorg_mul_rem, chemicals, mowing, observation, weeding, puddling, flood_level, other <br/>MIAPPE V1.1 (DM-66) Event accession number - Accession number of the event type in a suitable controlled vocabulary (Crop Ontology).',
  -- For property 'observationUnits' Link table 'ObservationUnitByEvent' will be created separately
) 
COMMENT 'An event is discrete occurrence at a particular time in the experiment. Events may be the realization of Treatments or parts of Treatments, or may be confounding to Treatments.  <br> ICASA Management Events allow for the following types -> planting, fertilizer, irrigation, tillage, organic_material, harvest, bed_prep, inorg_mulch, inorg_mul_rem, chemicals, mowing, observation, weeding, puddling, flood_level, other';


/* 
Creates a lookup table for property 'observationUnits' for 'Event' to 'ObservationUnit'
 */
CREATE TABLE brapi_ObservationUnitByEvent (
  observationUnitDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
  observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
  observationUnitName STRING COMMENT 'A human readable name for an observation unit',
  eventDbId STRING NOT NULL PRIMARY KEY COMMENT 'Internal database identifier'
) 
COMMENT 'Link table for Event to ObservationUnit on property observationUnits';


-- Generated by Schema Tools Generator Version: '0.62.0'
