-- SeedLot

/* 
A SeedLot, also known as an InventoryLot, is a collection of starting material (seeds, bulbs, root-stock, etc) for a particular Germplasm. The amount of material available for each Germplasm can be increased by seed production and decreased by planting or trading with another breeding Program.
 */
CREATE TABLE brapi_SeedLots (
  -- Primary properties
  seedLotDbId STRING NOT NULL PRIMARY KEY COMMENT 'Unique DbId for the Seed Lot',
  seedLotName STRING NOT NULL PRIMARY KEY COMMENT 'A human readable name for this Seed Lot',
  -- Link properties
  locationDbId STRING NOT NULL COMMENT 'The unique identifier for a Location',
  locationName STRING NOT NULL COMMENT 'A human readable name for a Location <br/> MIAPPE V1.1 (DM-18) Experimental site name - The name of the natural site, experimental field, greenhouse, phenotyping facility, etc. where the experiment took place.',
  programDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies the program',
  programName STRING NOT NULL COMMENT 'Human readable name of the program',
  -- Properties
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  amount DOUBLE COMMENT 'The current balance of the amount of material in a SeedLot. Could be a count (seeds, bulbs, etc) or a weight (kg of seed).',
  contentMixture
    ARRAY<
      STRUCT<
        -- Link properties
        crossDbId STRING COMMENT 'the unique identifier for a cross',
        crossName STRING COMMENT 'the human readable name for a cross',
        germplasmDbId STRING COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
        germplasmPUI STRING COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
        germplasmName STRING COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.',
        seedLotDbId STRING COMMENT 'Unique DbId for the Seed Lot',
        seedLotName STRING COMMENT 'A human readable name for this Seed Lot',
        -- Properties
        mixturePercentage INT COMMENT 'The percentage of the given germplasm in the seed lot mixture.'
      >
    > COMMENT 'The mixture of germplasm present in the seed lot. <br/> If this seed lot only contains a single germplasm, the response should contain the name  and DbId of that germplasm with a mixturePercentage value of 100 <br/> If the seed lot contains a mixture of different germplasm, the response should contain  the name and DbId every germplasm present. The mixturePercentage field should contain  the ratio of each germplasm in the total mixture. All of the mixturePercentage values  in this array should sum to equal 100.',
  createdDate STRING COMMENT 'The time stamp for when this seed lot was created',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  fromSeedLotTransactions
    ARRAY<
      STRUCT<
        -- Link properties
        fromSeedLotDbId STRING COMMENT 'Unique DbId for the Seed Lot',
        fromSeedLotName STRING COMMENT 'A human readable name for this Seed Lot',
        toSeedLotDbId STRING COMMENT 'Unique DbId for the Seed Lot',
        toSeedLotName STRING COMMENT 'A human readable name for this Seed Lot',
        -- Properties
        additionalInfo MAP<STRING,STRING> COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
        amount DOUBLE COMMENT 'The number of units being transferred between SeedLots. Could be a count (seeds, bulbs, etc) or a weight (kg of seed).',
        externalReferences
          ARRAY<
            STRUCT<
              referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
              referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
            >
          > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
        transactionDbId STRING COMMENT 'Unique DbId for the Seed Lot Transaction',
        transactionDescription STRING COMMENT 'A general description of this Seed Lot Transaction',
        transactionTimestamp STRING COMMENT 'The time stamp for when the transaction occurred',
        units STRING COMMENT 'A description of the things being transferred between SeedLots in a transaction (seeds, bulbs, kg, etc)'
      >
    > COMMENT 'fromSeedLotTransactions',
  lastUpdated STRING COMMENT 'The timestamp for the last update to this Seed Lot (including transactions)',
  seedLotDescription STRING COMMENT 'A general description of this Seed Lot',
  sourceCollection STRING COMMENT 'The description of the source where this material was originally collected (wild, nursery, etc)',
  storageLocation STRING COMMENT 'Description the storage location',
  toSeedLotTransactions
    ARRAY<
      STRUCT<
        -- Link properties
        fromSeedLotDbId STRING COMMENT 'Unique DbId for the Seed Lot',
        fromSeedLotName STRING COMMENT 'A human readable name for this Seed Lot',
        toSeedLotDbId STRING COMMENT 'Unique DbId for the Seed Lot',
        toSeedLotName STRING COMMENT 'A human readable name for this Seed Lot',
        -- Properties
        additionalInfo MAP<STRING,STRING> COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
        amount DOUBLE COMMENT 'The number of units being transferred between SeedLots. Could be a count (seeds, bulbs, etc) or a weight (kg of seed).',
        externalReferences
          ARRAY<
            STRUCT<
              referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
              referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
            >
          > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
        transactionDbId STRING COMMENT 'Unique DbId for the Seed Lot Transaction',
        transactionDescription STRING COMMENT 'A general description of this Seed Lot Transaction',
        transactionTimestamp STRING COMMENT 'The time stamp for when the transaction occurred',
        units STRING COMMENT 'A description of the things being transferred between SeedLots in a transaction (seeds, bulbs, kg, etc)'
      >
    > COMMENT 'toSeedLotTransactions',
  units STRING COMMENT 'A description of the things being counted in a SeedLot (seeds, bulbs, kg, tree, etc)'
) 
COMMENT 'A SeedLot, also known as an InventoryLot, is a collection of starting material (seeds, bulbs, root-stock, etc) for a particular Germplasm. The amount of material available for each Germplasm can be increased by seed production and decreased by planting or trading with another breeding Program.';


-- Generated by Schema Tools Generator Version: '0.60.0'
