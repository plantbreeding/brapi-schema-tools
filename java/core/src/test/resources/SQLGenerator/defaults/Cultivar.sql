-- Cultivar

/* 
A **Cultivar** is a distinct and uniform line selected for specific desirable characteristics. A cultivar can be a fixed line or a hybrid line.

### Phenome Implementation

> TODO Breeding Services please check

A Cultivar can be migrated from PRISM or a new Cultivar created in Phenome.

At a certain generation (e.g. F5 or F7), a germplasm can be promoted to Cultivar via manual action or some rules can be set depending on the breeding method. For example, the output of a double haploid method is always a Cultivar.

A new Cultivar can be also generated from an existing Cultivar.

At a certain generation (e.g. F5 or F7), a Germplasm is promoted to Cultivar
* Hybrid cross will generate a Cultivar
* CMS - maintaining A line,
* DH1 seed will be cultivar

A new Cultivar can be generated from an existing cultivar (Reselect case).

A parent of hybrid, the parents are advanced farther and used to create a hybrid (this can be a new version of hybrid).

### PRISM Implementation

The PRISM database does not have a concept of a Cultivar, the closest concept is a material.

In Field Crops Cultivars are Materials 
> TODO Breeding Services please check

###  Examples

| Source                  | cultivarDbId                         | cultivarName           | cultivarPUI    |
|-------------------------|--------------------------------------|------------------------|----------------|
| Field Crops - Phenome   | 1926D58A-D28D-11EF-9E83-2EF48249D8EC | null                   | QCULWH4417189  |
| Field Crops - PRISM     | PMWH100055711                        | WW18B0994              | PMWH100055711  |
| Vegetable Crops - PRISM | PMCU180341010                        | S-3                    | PMCU180341010  |
 */
CREATE TABLE IF NOT EXISTS sta_dash.dadi_br_sandbox.silver_phenome_cultivars (
  cultivarDbId STRING NOT NULL COMMENT 'The ID which uniquely identifies a cultivar within the given database server.',
  cultivarName STRING NOT NULL COMMENT 'Name of the cultivar. It can be the preferred name and does not have to be unique.',
  cultivarPUI STRING NOT NULL PRIMARY KEY COMMENT 'The Permanent Unique Identifier which represents a cultivar',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'Additional arbitrary info',
  attributeValuePUIs ARRAY<STRING> COMMENT 'attributeValues',
  commonCropName STRING NOT NULL COMMENT 'Common name for the crop   MCPD (v2.1) (CROPNAME) 10. Common name of the crop. Example: "malting barley", "mas".',
  cropPUI STRING NOT NULL COMMENT 'A permanent unique identifier associated with this Crop. For example, a URI or DOI',
  cultivarAudit 
    STRUCT<
      dateCreated STRING NOT NULL COMMENT 'The date the cultivar was created',
      dateModified STRING NOT NULL COMMENT 'The date the cultivar record was modified',
      personCreatedDbId STRING NOT NULL COMMENT 'Unique ID for a person',
      personModifiedDbId STRING NOT NULL COMMENT 'Unique ID for a person'
    > NOT NULL COMMENT 'The audit details of the cultivar record.',
  cultivarStage STRING NOT NULL COMMENT 'The stage of the cultivar.',
  cultivarType STRING NOT NULL COMMENT 'Type of cultivar which can be one of the following:   Fixed line   Hybrid   CMS   DH',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  germplasmPUIs ARRAY<STRING> COMMENT 'The germplasm that represent this cultivar',
  synonyms
    ARRAY<
      STRUCT<
        synonym STRING NOT NULL COMMENT 'Alternative name or ID used to reference this variety',
        type STRING NOT NULL COMMENT 'A descriptive classification for this synonym. Can one of the following:   Alternative Name   Short Name   Old Name   Production Code   Pool Name   Genebank Material Name   ERP Code'
      >
    > NOT NULL COMMENT 'List of alternative names or IDs used to reference this cultivar',
  varietyPUI STRING NOT NULL COMMENT 'The Permanent Unique Identifier which represents a variety'
) 
USING delta
CLUSTER BY (commonCropName,cultivarStage,cultivarType)
TBLPROPERTIES ('delta.enableChangeDataFeed' = true)
COMMENT 'A **Cultivar** is a distinct and uniform line selected for specific desirable characteristics. A cultivar can be a fixed line or a hybrid line.  ### Phenome Implementation  > TODO Breeding Services please check  A Cultivar can be migrated from PRISM or a new Cultivar created in Phenome.  At a certain generation (e.g. F5 or F7), a germplasm can be promoted to Cultivar via manual action or some rules can be set depending on the breeding method. For example, the output of a double haploid method is always a Cultivar.  A new Cultivar can be also generated from an existing Cultivar.  At a certain generation (e.g. F5 or F7), a Germplasm is promoted to Cultivar * Hybrid cross will generate a Cultivar * CMS - maintaining A line, * DH1 seed will be cultivar  A new Cultivar can be generated from an existing cultivar (Reselect case).  A parent of hybrid, the parents are advanced farther and used to create a hybrid (this can be a new version of hybrid).  ### PRISM Implementation  The PRISM database does not have a concept of a Cultivar, the closest concept is a material.  In Field Crops Cultivars are Materials  > TODO Breeding Services please check  ###  Examples  | Source                  | cultivarDbId                         | cultivarName           | cultivarPUI    | |-------------------------|--------------------------------------|------------------------|----------------| | Field Crops - Phenome   | 1926D58A-D28D-11EF-9E83-2EF48249D8EC | null                   | QCULWH4417189  | | Field Crops - PRISM     | PMWH100055711                        | WW18B0994              | PMWH100055711  | | Vegetable Crops - PRISM | PMCU180341010                        | S-3                    | PMCU180341010  |';


-- Generated by Schema Tools Generator Version: '0.60.0'
