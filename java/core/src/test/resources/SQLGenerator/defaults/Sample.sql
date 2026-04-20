-- Sample

/* 
The identifiers and metadata associated with a physical piece of biological material collected from the field for external analysis. A Sample can take many forms (leaf clipping, seed, DNA, etc) and might be used for a variety of analysis procedures (spectra, genotyping, etc).
 */
CREATE TABLE brapi_Samples (
  -- Primary properties
  sampleDbId STRING PRIMARY KEY COMMENT 'The ID which uniquely identifies a `Sample` <br> MIAPPE V1.1 (DM-76) Sample ID - Unique identifier for the sample.',
  sampleName STRING NOT NULL PRIMARY KEY COMMENT 'The human readable name of the `Sample`',
  samplePUI STRING PRIMARY KEY COMMENT 'A permanent unique identifier for the `Sample` (DOI, URL, UUID, etc) <br> MIAPPE V1.1 (DM-81) External ID - An identifier for the sample in a persistent repository, comprising the name of the repository and the accession number of the observation unit therein. Submission to the EBI Biosamples repository is recommended. URI are recommended when possible. ',
  -- Link properties
  callSetDbIds ARRAY<STRING> COMMENT '',
  germplasmDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
  germplasmPUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
  germplasmName STRING NOT NULL COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.',
  observationUnitDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies an observation unit  MIAPPE V1.1 (DM-70) Observation unit ID - Identifier used to identify the observation unit in data files containing the values observed or measured on that unit. Must be locally unique. ',
  observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.',
  observationUnitName STRING COMMENT 'A human readable name for an observation unit',
  plateDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a `Plate`',
  plateName STRING NOT NULL COMMENT 'A human readable name for a `Plate`',
  programDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies the program',
  programName STRING NOT NULL COMMENT 'Human readable name of the program',
  studyDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a study within the given database server  MIAPPE V1.1 (DM-11) Study unique ID - Unique identifier comprising the name or identifier for the institution/database hosting the submission of the study data, and the identifier of the study in that institution.',
  studyPUI STRING COMMENT 'A permanent unique identifier associated with this study data. For example, a URI or DOI',
  studyName STRING NOT NULL COMMENT 'The human readable name for a study  MIAPPE V1.1 (DM-12) Study title - Human-readable text summarising the study',
  trialDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a trial  MIAPPE V1.1 (DM-2) Investigation unique ID - Identifier comprising the unique name of the institution/database hosting the submission of the investigation data, and the accession number of the investigation in that institution.',
  trialPUI STRING COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.',
  trialName STRING NOT NULL COMMENT 'The human readable name of a trial  MIAPPE V1.1 (DM-3) Investigation title - Human-readable string summarising the investigation.',
  -- Properties
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  column INT COMMENT 'The Column identifier for this `Sample` location in the `Plate`',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  row STRING COMMENT 'The Row identifier for this `Sample` location in the `Plate`',
  sampleBarcode STRING COMMENT 'A unique identifier physically attached to the `Sample`',
  sampleDescription STRING COMMENT 'Description of a `Sample` <br>MIAPPE V1.1 (DM-79) Sample description - Any information not captured by the other sample fields, including quantification, sample treatments and processing.',
  sampleGroupId STRING COMMENT 'The ID which uniquely identifies a group of `Samples`',
  sampleTimestamp STRING COMMENT 'The date and time a `Sample` was collected from the field <br> MIAPPE V1.1 (DM-80) Collection date - The date and time when the sample was collected / harvested',
  sampleType STRING COMMENT 'The type of `Sample` taken. ex. ''DNA'', ''RNA'', ''Tissue'', etc',
  takenBy STRING COMMENT 'The name or identifier of the entity which took the `Sample` from the field',
  tissueType STRING COMMENT 'The type of tissue sampled. ex. ''Leaf'', ''Root'', etc. <br> MIAPPE V1.1 (DM-78) Plant anatomical entity - A description of  the plant part (e.g. leaf) or the plant product (e.g. resin) from which the sample was taken, in the form of an accession number to a suitable controlled vocabulary (Plant Ontology).',
  well STRING COMMENT 'The Well identifier for this `Sample` location in the `Plate`. Usually a concatenation of Row and Column, or just a number if the `Samples` are not part of an ordered `Plate`.'
) 
COMMENT 'The identifiers and metadata associated with a physical piece of biological material collected from the field for external analysis. A Sample can take many forms (leaf clipping, seed, DNA, etc) and might be used for a variety of analysis procedures (spectra, genotyping, etc).';


-- Generated by Schema Tools Generator Version: '0.62.0'
