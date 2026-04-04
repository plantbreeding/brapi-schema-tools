-- Germplasm

/* 
The conceptual identifiers and metadata describing a genetically unique organism that is noteworthy in some way. Depending on context, a Germplasm might be synonymous with Accession, Line, or Genotype. Note that Germplasm is conceptual data, not necessarily associated to a real physical object, so Seed/Inventory Lots and Observation Units become physical instantiations of a particular Germplasm. Note a Germplasm is unique and noteworthy, so a Cross may or may not create a new Germplasm, since not every Cross is unique or noteworthy.
 */
CREATE TABLE IF NOT EXISTS sta_dash.dadi_br_sandbox.silver_phenome_germplasm (
  germplasmDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
  germplasmName STRING NOT NULL COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.',
  germplasmPUI STRING NOT NULL PRIMARY KEY COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
  accessionNumber STRING COMMENT 'The unique identifier for a material or germplasm within a genebank  MCPD (v2.1) (ACCENUMB) 2. This is the unique identifier for accessions within a genebank, and is assigned when a sample is entered into the genebank collection (e.g. "PI 113869").',
  acquisitionDate STRING COMMENT 'The date a material or germplasm was acquired by the genebank   MCPD (v2.1) (ACQDATE) 12. Date on which the accession entered the collection [YYYYMMDD] where YYYY is the year, MM is the month and DD is the day. Missing data (MM or DD) should be indicated with hyphens or "00" [double zero].',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  biologicalStatusOfAccessionCode STRING COMMENT 'MCPD (v2.1) (SAMPSTAT) 19. The coding scheme proposed can be used at 3 different levels of detail: either by using the general codes such as 100, 200, 300, 400, or by using the more specific codes such as 110, 120, etc.   100) Wild  110) Natural  120) Semi-natural/wild  130) Semi-natural/sown  200) Weedy  300) Traditional cultivar/landrace  400) Breeding/research material  410) Breeders line  411) Synthetic population  412) Hybrid  413) Founder stock/base population  414) Inbred line (parent of hybrid cultivar)  415) Segregating population  416) Clonal selection  420) Genetic stock  421) Mutant (e.g. induced/insertion mutants, tilling populations)  422) Cytogenetic stocks (e.g. chromosome addition/substitution, aneuploids,  amphiploids)  423) Other genetic stocks (e.g. mapping populations)  500) Advanced or improved cultivar (conventional breeding methods)  600) GMO (by genetic engineering)  999) Other (Elaborate in REMARKS field)',
  biologicalStatusOfAccessionDescription STRING COMMENT 'Supplemental text description for \'biologicalStatusOfAccessionCode\'',
  breedingMethodDbId STRING NOT NULL COMMENT 'the unique identifier for this breeding method',
  collection STRING COMMENT 'A specific panel/collection/population name this germplasm belongs to.',
  commonCropName STRING NOT NULL COMMENT 'Common name for the crop   MCPD (v2.1) (CROPNAME) 10. Common name of the crop. Example: "malting barley", "mas".',
  countryOfOriginCode STRING COMMENT '3-letter ISO 3166-1 code of the country in which the sample was originally collected   MCPD (v2.1) (ORIGCTY) 13. 3-letter ISO 3166-1 code of the country in which the sample was originally collected (e.g. landrace, crop wild relative, farmers variety), bred or selected (breeding lines, GMOs, segregating populations, hybrids, modern cultivars, etc.). Note- Descriptors 14 to 16 below should be completed accordingly only if it was "collected".',
  cropPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Crop. For example, a URI or DOI',
  cultivarPUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier which represents a cultivar',
  defaultDisplayName STRING COMMENT 'Human readable name used for display purposes',
  documentationURL STRING COMMENT 'A URL to the human readable documentation of an object',
  donors
    ARRAY<
      STRUCT<
        donorAccessionNumber STRING COMMENT 'The accession number assigned by the donor  MCPD (v2.1) (DONORNUMB) 23. Identifier assigned to an accession by the donor. Follows ACCENUMB standard.',
        donorInstituteCode STRING COMMENT 'The institute code for the donor institute <br/>MCPD (v2.1) (DONORCODE) 22. FAO WIEWS code of the donor institute. Follows INSTCODE standard.'
      >
    > COMMENT 'List of donor institutes',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  generationCode STRING COMMENT 'The generation code is a simple code that denotes the generation since the last generative cross. There are several types of code generally used, although it is up to the implementing systems to define these.',
  genus STRING COMMENT 'Genus name for taxon. Initial uppercase letter required.  MCPD (v2.1) (GENUS) 5. Genus name for taxon. Initial uppercase letter required.  MIAPPE V1.1 (DM-43) Genus - Genus name for the organism under study, according to standard scientific nomenclature.',
  germplasmOrigin
    ARRAY<
      STRUCT<
        coordinateUncertainty STRING COMMENT 'Uncertainty associated with the coordinates in meters. Leave the value empty if the uncertainty is unknown.',
        coordinates
          ARRAY<
            STRUCT<
              geometry1
                STRUCT<
                  coordinates ARRAY<DOUBLE> NOT NULL COMMENT 'A single position',
                  type STRING NOT NULL COMMENT 'The literal string "Point"'
                >,
              geometry2
                STRUCT<
                  coordinates ARRAY<DOUBLE> NOT NULL COMMENT 'An array of linear rings',
                  type STRING NOT NULL COMMENT 'The literal string "Polygon"'
                > NOT NULL COMMENT 'A geometry as defined by GeoJSON (RFC 7946). In this context, only Point or Polygon geometry are allowed.',
              type STRING NOT NULL COMMENT 'The literal string "Feature"'
            >
          > COMMENT 'One geometry as defined by GeoJSON (RFC 7946). All coordinates are decimal values on the WGS84 geographic coordinate reference system.  Copied from RFC 7946 Section 3.1.1  A position is an array of numbers. There MUST be two or more elements. The first two elements are longitude and latitude, or easting and northing, precisely in that order and using decimal numbers. Altitude or elevation MAY be included as an optional third element.'
      >
    > COMMENT 'Information for material (orchard, natural sites, ...). Geographic identification of the plants from which seeds or cutting have been taken to produce that germplasm.',
  germplasmPreprocessing STRING COMMENT 'Description of any process or treatment applied uniformly to the germplasm, prior to the study itself. Can be provided as free text or as an accession number from a suitable controlled vocabulary.',
  instituteCode STRING COMMENT 'The code for the institute that maintains the material.   MCPD (v2.1) (INSTCODE) 1. FAO WIEWS code of the institute where the accession is maintained. The codes consist of the 3-letter ISO 3166 country code of the country where the institute is located plus a number (e.g. PER001). The current set of institute codes is available from http://www.fao.org/wiews. For those institutes not yet having an FAO Code, or for those with "obsolete" codes, see "Common formatting rules (v)".',
  instituteName STRING COMMENT 'The name of the institute that maintains the material',
  pedigree STRING COMMENT 'The cross name and optional selection history.  MCPD (v2.1) (ANCEST) 20. Information about either pedigree or other description of ancestral information (e.g. parent variety in case of mutant or selection). For example a pedigree \'Hanna/7*Atlas//Turk/8*Atlas\' or a description \'mutation found in Hanna\', \'selection from Irene\' or \'cross involving amongst others Hanna and Irene\'.',
  samplePUIs ARRAY<STRING> COMMENT 'samples',
  seedSource STRING COMMENT 'An identifier for the source of the biological material <br/>MIAPPE V1.1 (DM-50) Material source ID (Holding institute/stock centre, accession) - An identifier for the source of the biological material, in the form of a key-value pair comprising the name/identifier of the repository from which the material was sourced plus the accession number of the repository for that material. Where an accession number has not been assigned, but the material has been derived from the crossing of known accessions, the material can be defined as follows: "mother_accession X father_accession", or, if father is unknown, as "mother_accession X UNKNOWN". For in situ material, the region of provenance may be used when an accession is not available.',
  seedSourceDescription STRING COMMENT 'Description of the material source  MIAPPE V1.1 (DM-56) Material source description - Description of the material source',
  species STRING COMMENT 'Specific epithet portion of the scientific name in lowercase letters.  MCPD (v2.1) (SPECIES) 6. Specific epithet portion of the scientific name in lowercase letters. Only the following abbreviation is allowed: "sp."   MIAPPE V1.1 (DM-44) Species - Species name (formally: specific epithet) for the organism under study, according to standard scientific nomenclature.',
  speciesAuthority STRING COMMENT 'The authority organization responsible for tracking and maintaining the species name   MCPD (v2.1) (SPAUTHOR) 7. Provide the authority for the species name.',
  storageTypes
    ARRAY<
      STRUCT<
        code STRING COMMENT 'The 2 digit code representing the type of storage this germplasm is kept in at a genebank.   MCPD (v2.1) (STORAGE) 26. If germplasm is maintained under different types of storage, multiple choices are allowed, separated by a semicolon (e.g. 20;30). (Refer to FAO/IPGRI Genebank Standards 1994 for details on storage type.)   10) Seed collection  11) Short term  12) Medium term  13) Long term  20) Field collection  30) In vitro collection  40) Cryo-preserved collection  50) DNA collection  99) Other (elaborate in REMARKS field)',
        description STRING COMMENT 'A supplemental text description of the storage type'
      >
    > COMMENT 'The type of storage this germplasm is kept in at a genebank.',
  subtaxa STRING COMMENT 'Subtaxon can be used to store any additional taxonomic identifier.  MCPD (v2.1) (SUBTAXA) 8. Subtaxon can be used to store any additional taxonomic identifier. The following abbreviations are allowed: "subsp." (for subspecies); "convar." (for convariety); "var." (for variety); "f." (for form); "Group" (for "cultivar group").  MIAPPE V1.1 (DM-44) Infraspecific name - Name of any subtaxa level, including variety, crossing name, etc. It can be used to store any additional taxonomic identifier. Either free text description or key-value pair list format (the key is the name of the rank and the value is the value of  the rank). Ranks can be among the following terms: subspecies, cultivar, variety, subvariety, convariety, group, subgroup, hybrid, line, form, subform. For MCPD compliance, the following abbreviations are allowed: subsp. (subspecies); convar. (convariety); var. (variety); f. (form); Group (cultivar group).',
  subtaxaAuthority STRING COMMENT 'The authority organization responsible for tracking and maintaining the subtaxon information  MCPD (v2.1) (SUBTAUTHOR) 9. Provide the subtaxon authority at the most detailed taxonomic level.',
  synonyms
    ARRAY<
      STRUCT<
        synonym STRING NOT NULL COMMENT 'Alternative name or ID used to reference this variety',
        type STRING NOT NULL COMMENT 'A descriptive classification for this synonym. Can one of the following:   Alternative Name   Short Name   Old Name   Production Code   Pool Name   Genebank Material Name   ERP Code'
      >
    > COMMENT 'List of alternative names or IDs used to reference this germplasm  MCPD (v2.1) (OTHERNUMB) 24. Any other identifiers known to exist in other collections for this accession. Use the following format: INSTCODE:ACCENUMB;INSTCODE:identifier;INSTCODE and identifier are separated by a colon without space. Pairs of INSTCODE and identifier are separated by a semicolon without space. When the institute is not known, the identifier should be preceded by a colon.',
  taxonIds
    ARRAY<
      STRUCT<
        sourceName STRING NOT NULL COMMENT 'The human readable name of the taxonomy provider',
        taxonId STRING NOT NULL COMMENT 'The identifier (name, ID, URI) of a particular taxonomy within the source provider'
      >
    > COMMENT 'The list of IDs for this SPECIES from different sources. If present, NCBI Taxon should be always listed as "ncbiTaxon" preferably with a purl. The rank of this ID should be species.  MIAPPE V1.1 (DM-42) Organism - An identifier for the organism at the species level. Use of the NCBI taxon ID is recommended.'
) 
USING delta
CLUSTER BY (commonCropName,genus,species)
TBLPROPERTIES ('delta.enableChangeDataFeed' = true)
COMMENT 'The conceptual identifiers and metadata describing a genetically unique organism that is noteworthy in some way. Depending on context, a Germplasm might be synonymous with Accession, Line, or Genotype. Note that Germplasm is conceptual data, not necessarily associated to a real physical object, so Seed/Inventory Lots and Observation Units become physical instantiations of a particular Germplasm. Note a Germplasm is unique and noteworthy, so a Cross may or may not create a new Germplasm, since not every Cross is unique or noteworthy.';


-- Generated by Schema Tools Generator Version: '0.60.0'
