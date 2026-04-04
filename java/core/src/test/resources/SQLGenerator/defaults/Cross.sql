-- Cross

/* 
The identifiers and metadata represent that specific organisms have mated to produce offspring with particular traits or genes. The offspring of a Cross might be developed into a Germplasm if the desired traits are present.
 */
CREATE TABLE brapi_Crosses (
  crossDbId STRING NOT NULL PRIMARY KEY COMMENT 'the unique identifier for a cross',
  crossName STRING PRIMARY KEY COMMENT 'the human readable name for a cross',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  crossAttributes
    ARRAY<
      STRUCT<
        crossAttributeName STRING COMMENT 'the human readable name of a cross attribute',
        crossDbId STRING COMMENT 'the unique identifier for a cross',
        crossName STRING COMMENT 'the human readable name for a cross',
        crossAttributeValue STRING COMMENT 'the value of a cross attribute'
      >
    > COMMENT 'Set of custom attributes associated with a cross',
  crossType STRING NOT NULL COMMENT 'the type of cross',
  crossingProjectDbId STRING NOT NULL COMMENT 'The unique identifier for a crossing project',
  crossingProjectName STRING NOT NULL COMMENT 'The human readable name for a crossing project',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  parent1 
    STRUCT<
      germplasmDbId STRING COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
      germplasmPUI STRING COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
      germplasmName STRING COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.',
      observationUnitDbId STRING COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
      observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
      observationUnitName STRING COMMENT 'A human readable name for an observation unit',
      parentType STRING COMMENT 'The type of parent ex. \'MALE\', \'FEMALE\', \'SELF\', \'POPULATION\', etc.'
    > NOT NULL COMMENT 'the first parent used in the cross',
  parent2 
    STRUCT<
      germplasmDbId STRING COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
      germplasmPUI STRING COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
      germplasmName STRING COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.',
      observationUnitDbId STRING COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
      observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
      observationUnitName STRING COMMENT 'A human readable name for an observation unit',
      parentType STRING COMMENT 'The type of parent ex. \'MALE\', \'FEMALE\', \'SELF\', \'POPULATION\', etc.'
    > NOT NULL COMMENT 'the second parent used in the cross',
  plannedCrossDbId STRING NOT NULL COMMENT 'the unique identifier for a planned cross',
  plannedCrossName STRING COMMENT 'the human readable name for a planned cross',
  pollinationEvents
    ARRAY<
      STRUCT<
        crossDbId STRING COMMENT 'the unique identifier for a cross',
        crossName STRING COMMENT 'the human readable name for a cross',
        pollinationNumber STRING COMMENT 'The unique identifier for this pollination event',
        pollinationSuccessful BOOLEAN COMMENT 'True if the pollination was successful',
        pollinationTimeStamp STRING COMMENT 'The timestamp when the pollination took place'
      >
    > COMMENT 'The list of pollination events that occurred for this cross.'
) 
COMMENT 'The identifiers and metadata represent that specific organisms have mated to produce offspring with particular traits or genes. The offspring of a Cross might be developed into a Germplasm if the desired traits are present.';


-- Generated by Schema Tools Generator Version: '0.60.0'
