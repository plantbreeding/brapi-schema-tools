-- Trial

/* 
A Trial represents a collection of Study objects, and the metadata associated with that collection. A Trial could represent a multi-location experiment, and could contain information related to publications and data licensing.
 */
CREATE TABLE IF NOT EXISTS sta_dash.dadi_br_sandbox.silver_phenome_trials (
  trialDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a trial  MIAPPE V1.1 (DM-2) Investigation unique ID - Identifier comprising the unique name of the institution/database hosting the submission of the investigation data, and the accession number of the investigation in that institution.',
  trialName STRING NOT NULL COMMENT 'The human readable name of a trial  MIAPPE V1.1 (DM-3) Investigation title - Human-readable string summarising the investigation.',
  trialPUI STRING PRIMARY KEY COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.',
  active BOOLEAN COMMENT 'A flag to indicate if a Trial is currently active and ongoing',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  commonCropName STRING COMMENT 'Common name for the crop associated with this trial',
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
    > COMMENT 'List of contact entities associated with this trial',
  cropPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Crop. For example, a URI or DOI',
  datasetAuthorships
    ARRAY<
      STRUCT<
        datasetPUI STRING COMMENT 'The DOI or other permanent unique identifier for this published dataset',
        license STRING COMMENT 'MIAPPE V1.1 (DM-7) License - License for the reuse of the data associated with this investigation. The Creative Commons licenses cover most use cases and are recommended.',
        publicReleaseDate STRING COMMENT 'MIAPPE V1.1 (DM-6) Public release date - Date of first public release of the dataset presently being described.',
        submissionDate STRING COMMENT 'MIAPPE V1.1 (DM-5) Submission date - Date of submission of the dataset presently being described to a host repository.',
        trialPUI STRING PRIMARY KEY COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.'
      >
    > COMMENT 'License and citation information for the data in this trial',
  documentationURL STRING COMMENT 'A URL to the human readable documentation of an object',
  endDate STRING COMMENT 'The date this trial ends',
  experimentalDesign 
    STRUCT<
      experimentalDesignPUI STRING COMMENT 'A Permanent Unique Identifier (PUI) for the Type of experimental design, which can be in the form of an Ontology reference for example. ',
      PUI STRING COMMENT 'A Permanent Unique Identifier (PUI) for the Type of experimental design, which can be in the form of an Ontology reference for example. See MIAPPE V1.1 (DM-23) Type of experimental design. **Deprecated in v2.2** Please use `experimentalDesignPUI`. Github issue number #539',
      description STRING COMMENT 'Short description of the experimental design, possibly including statistical design. In specific cases, e.g. legacy datasets or data computed from several studies, the experimental design can be "unknown"/"NA", "aggregated/reduced data", or simply \'none\'. See MIAPPE V1.1 (DM-22) Description of the experimental design',
      experimentFactors
        ARRAY<
          STRUCT<
            factor STRING NOT NULL COMMENT 'The name of the factor, eg. \'fertilizer\', \'inoculation\', \'irrigation\', etc  MIAPPE V1.1 (DM-61) Experimental Factor type - Name/Acronym of the experimental factor.',
            factorPUI STRING COMMENT 'The PUI of the factor which may link to an ontology.',
            modalities
              ARRAY<
                STRUCT<
                  modality STRING NOT NULL COMMENT 'The factor modality/level description. ex. \'low fertilizer\', \'yellow rust inoculation\', \'high water\', etc  MIAPPE V1.1 (DM-62) Experimental Factor description - Free text description of the experimental factor. This includes all relevant treatments planned and protocol planned for all the plants targeted by a given experimental factor. ',
                  modalityPUI STRING COMMENT 'The PUI of the modality/level which may link to an ontology.'
                >
              > NOT NULL COMMENT 'An array of possible factor modalities/levels for this factor'
          >
        > COMMENT 'The factors used in the experimental design.',
      experimentTreatments
        ARRAY<
          STRUCT<
            observationTreatments
              ARRAY<
                STRUCT<
                  factor STRING NOT NULL COMMENT 'The type of treatment/factor. ex. \'fertilizer\', \'inoculation\', \'irrigation\', etc  MIAPPE V1.1 (DM-61) Experimental Factor type - Name/Acronym of the experimental factor.',
                  factorPUI STRING COMMENT 'The PUI of the factor which may link to an ontology.',
                  modality STRING NOT NULL COMMENT 'The treatment/factor description. ex. \'low fertilizer\', \'yellow rust inoculation\', \'high water\', etc  MIAPPE V1.1 (DM-62) Experimental Factor description - Free text description of the experimental factor. This includes all relevant treatments planned and protocol planned for all the plants targeted by a given experimental factor. ',
                  modalityPUI STRING COMMENT 'The PUI of the modality which may link to an ontology.',
                  observationUnitPUI STRING COMMENT 'A Permanent Unique Identifier for an observation unit  MIAPPE V1.1 (DM-72) External ID - Identifier for the observation unit in a persistent repository, comprises the name of the repository and the identifier of the observation unit therein. The EBI Biosamples repository can be used. URI are recommended when possible.'
                >
              > NOT NULL COMMENT 'An array of possible factor modalities/levels combinations for this Experiment Treatment',
            treatmentCode STRING COMMENT 'A code or number that relates a unique combination of observation treatments in the context of the Experimental Design.'
          >
        > COMMENT 'The unique combination of factors and their modalities/levels that are used in this Experimental Design.',
      firstRepRand STRING COMMENT 'A description of how the first replication is treated in the Experimental Design',
      layoutPattern STRING COMMENT 'How the trial is layout in the field',
      numberOfReps DOUBLE COMMENT 'The number of replications.',
      randomizationType STRING COMMENT 'The type of randomization use to create the experimental design.',
      rowsPerPlot DOUBLE COMMENT 'The number of rows in a plot',
      setSize DOUBLE COMMENT 'The levels of all combination of factors involved. For example if the experiment involves single factor with 10 modalities/levels, set size will be 10. If the experiment involves 2 factors with one factor having 5 levels and another factor having 2 levels, the set size will be 10.'
    > NOT NULL COMMENT 'The experimental and statistical design full description plus a category PUI taken from crop research ontology or agronomy ontology',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  programPUI STRING COMMENT 'A permanent identifier for a program. Could be DOI or other URI formatted identifier.',
  publications
    ARRAY<
      STRUCT<
        publicationPUI STRING COMMENT 'The permanent unique identifier of the publication.',
        publicationReference STRING COMMENT 'The publication reference.',
        trialPUI STRING PRIMARY KEY COMMENT 'A permanent identifier for a trial. Could be DOI or other URI formatted identifier.'
      >
    > COMMENT 'MIAPPE V1.1 (DM-9) Associated publication - An identifier for a literature publication where the investigation is described. Use of DOIs is recommended.',
  stage STRING NOT NULL COMMENT 'The current stage of this trial. Example include Advance, Elite, EPT, EST, Prelim, PST.',
  startDate STRING COMMENT 'The date this trial started',
  studyPUIs ARRAY<STRING> COMMENT '',
  trialDescription STRING COMMENT 'The human readable description of a trial  MIAPPE V1.1 (DM-4) Investigation description - Human-readable text describing the investigation in more detail.',
  trialType STRING NOT NULL COMMENT 'The type of trial being performed. ex. "Yield Trial", etc'
) 
USING delta
CLUSTER BY (commonCropName,stage,trialType)
TBLPROPERTIES ('delta.enableChangeDataFeed' = true)
COMMENT 'A Trial represents a collection of Study objects, and the metadata associated with that collection. A Trial could represent a multi-location experiment, and could contain information related to publications and data licensing.';


-- Generated by Schema Tools Generator Version: '0.60.0'
