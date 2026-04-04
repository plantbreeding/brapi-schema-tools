-- InventoryLot

/* 
An **Inventory Lot** is a collection of starting material (seeds, bulbs, root-stock, etc) for a particular germplasm. 
The amount of starting material available for each germplasm 
can be increased by e.g. seed production and decreased by planting or 
trading with another breeding program.

In BrAPI version 2.1, an InventoryLot is called a **Seed Lot**, 
which is a specific type of inventory only for seed. 

### Phenome Implementation

Inventory Lot in Phenome can be found in the Inventory table.

> TODO Breeding Services please check

### PRISM Implementation

Inventory Lot in PRISM can be found in the inventory table.

> TODO Breeding Services please check
 */
CREATE TABLE IF NOT EXISTS sta_dash.dadi_br_sandbox.silver_phenome_inventory_lots (
  inventoryLotDbId STRING NOT NULL COMMENT 'Unique DbId for the Seed Lot',
  inventoryLotName STRING NOT NULL COMMENT 'A human readable name for this Seed Lot',
  inventoryLotPUI STRING NOT NULL PRIMARY KEY COMMENT 'A permanent unique identifier associated with this Seed Lot. For example, a URI or DOI',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  amount DOUBLE COMMENT 'The current balance of the amount of material in a InventoryLot. Could be a count (seeds, bulbs, etc) or a weight (kg of seed).',
  attributeValuePUIs ARRAY<STRING> COMMENT 'attributeValues',
  contentMixture
    ARRAY<
      STRUCT<
        crossPUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier which represents a cultivar',
        germplasmPUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier which represents a germplasm  MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.  MIAPPE V1.1 (DM-51) Material source DOI - Digital Object Identifier (DOI) of the material source  MCPD (v2.1) (PUID) 0. Any persistent, unique identifier assigned to the accession so it can be unambiguously referenced at the global level and the information associated with it harvested through automated means. Report one PUID for each accession. The Secretariat of the International Treaty on Plant Genetic Resources for Food and Agriculture (PGRFA) is facilitating the assignment of a persistent unique identifier (PUID), in the form of a DOI, to PGRFA at the accession level. Genebanks not applying a true PUID to their accessions should use, and request recipients to use, the concatenation of INSTCODE, ACCENUMB, and GENUS as a globally unique identifier similar in most respects to the PUID whenever they exchange information on accessions with third parties.',
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
  fromInventoryLotTransactions
    ARRAY<
      STRUCT<
        additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
        amount DOUBLE COMMENT 'The number of units being transferred between InventoryLots. Could be a count (seeds, bulbs, etc) or a weight (kg of seed).',
        externalReferences
          ARRAY<
            STRUCT<
              referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
              referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
            >
          > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
        fromInventoryLotPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Seed Lot. For example, a URI or DOI',
        toInventoryLotPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Seed Lot. For example, a URI or DOI',
        transactionDbId STRING NOT NULL COMMENT 'Unique DbId for the Seed Lot Transaction',
        transactionDescription STRING COMMENT 'A general description of this Seed Lot Transaction',
        transactionTimestamp STRING COMMENT 'The time stamp for when the transaction occurred',
        units STRING COMMENT 'A description of the things being transferred between InventoryLots in a transaction (seeds, bulbs, kg, etc)'
      >
    > COMMENT 'fromInventoryLotTransactions',
  inventoryLotDescription STRING COMMENT 'A general description of this Seed Lot',
  inventoryLotStatus STRING COMMENT 'The status of a inventory lot. Examples can be Active and Inactive',
  lastUpdated STRING COMMENT 'The timestamp for the last update to this Seed Lot (including transactions)',
  locationPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this location. For example, a URI or DOI.',
  observationUnitPUIs ARRAY<STRING> COMMENT 'observationUnits',
  programPUI STRING COMMENT 'A permanent identifier for a program. Could be DOI or other URI formatted identifier.',
  sourceCollection STRING COMMENT 'The description of the source where this material was originally collected (wild, nursery, etc)',
  storageLocation STRING COMMENT 'Description the storage location',
  toInventoryLotTransactions
    ARRAY<
      STRUCT<
        additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
        amount DOUBLE COMMENT 'The number of units being transferred between InventoryLots. Could be a count (seeds, bulbs, etc) or a weight (kg of seed).',
        externalReferences
          ARRAY<
            STRUCT<
              referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
              referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
            >
          > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
        fromInventoryLotPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Seed Lot. For example, a URI or DOI',
        toInventoryLotPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Seed Lot. For example, a URI or DOI',
        transactionDbId STRING NOT NULL COMMENT 'Unique DbId for the Seed Lot Transaction',
        transactionDescription STRING COMMENT 'A general description of this Seed Lot Transaction',
        transactionTimestamp STRING COMMENT 'The time stamp for when the transaction occurred',
        units STRING COMMENT 'A description of the things being transferred between InventoryLots in a transaction (seeds, bulbs, kg, etc)'
      >
    > COMMENT 'toInventoryLotTransactions',
  units STRING COMMENT 'A description of the things being counted in a InventoryLot (seeds, bulbs, kg, tree, etc)'
) 
USING delta
CLUSTER BY (inventoryLotStatus,sourceCollection)
TBLPROPERTIES ('delta.enableChangeDataFeed' = true)
COMMENT 'An **Inventory Lot** is a collection of starting material (seeds, bulbs, root-stock, etc) for a particular germplasm.  The amount of starting material available for each germplasm  can be increased by e.g. seed production and decreased by planting or  trading with another breeding program.  In BrAPI version 2.1, an InventoryLot is called a **Seed Lot**,  which is a specific type of inventory only for seed.   ### Phenome Implementation  Inventory Lot in Phenome can be found in the Inventory table.  > TODO Breeding Services please check  ### PRISM Implementation  Inventory Lot in PRISM can be found in the inventory table.  > TODO Breeding Services please check';


-- Generated by Schema Tools Generator Version: '0.60.0'
