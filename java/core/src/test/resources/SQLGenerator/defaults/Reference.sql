-- Reference

/* 
A `Reference` is a canonical assembled contig, intended to act as a reference coordinate space for other genomic annotations. A single `Reference` might represent the human chromosome 1, for instance. `References` are designed to be immutable.
 */
CREATE TABLE brapi_References (
  -- Primary properties
  referenceDbId STRING NOT NULL PRIMARY KEY COMMENT 'The unique identifier for a `Reference`',
  referenceName STRING NOT NULL PRIMARY KEY COMMENT 'The human readable name of a `Reference` within a `ReferenceSet`.',
  -- Link properties
  referenceSetDbId STRING NOT NULL COMMENT 'The unique identifier for a ReferenceSet',
  referenceSetName STRING NOT NULL COMMENT 'The human readable name of a ReferenceSet',
  variantDbIds ARRAY<STRING> COMMENT 'variants',
  -- Clustering properties
  commonCropName STRING COMMENT 'Common name for the crop',
  -- Properties
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  bases 
    STRUCT<
      nextPageToken STRING COMMENT 'The continuation token, which is used to page through large result sets. Provide this value in a subsequent request to return the next page of results. This field will be empty if there are not any additional results.',
      offset INT COMMENT 'The offset position (0-based) of the given sequence from the start of this `Reference`. This value will differ for each page in a request.',
      sequence STRING COMMENT 'A sub-string of the bases that make up this reference. Bases are represented as IUPAC-IUB codes, this string matches the regular expression `[ACGTMRWSYKVHDBN]*`.'
    > NOT NULL COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  externalReferences
    ARRAY<
      STRUCT<
        referenceID STRING COMMENT '**Deprecated in v2.1** Please use `referenceId`. Github issue number #460 <br>The external reference ID. Could be a simple string or a URI.',
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > NOT NULL COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  isDerived BOOLEAN COMMENT 'A sequence X is said to be derived from source sequence Y, if X and Y are of the same length and the per-base sequence divergence at A/C/G/T bases is sufficiently small. Two sequences derived from the same official sequence share the same coordinates and annotations, and can be replaced with the official sequence for certain use cases.',
  length INT COMMENT 'The length of this `Reference` sequence.',
  md5checksum STRING COMMENT 'The MD5 checksum uniquely representing this `Reference` as a lower-case hexadecimal string, calculated as the MD5 of the upper-case sequence excluding all whitespace characters (this is equivalent to SQ:M5 in SAM).',
  sourceAccessions ARRAY<STRING> COMMENT 'All known corresponding accession IDs in INSDC (GenBank/ENA/DDBJ) which must include a version number, e.g. `GCF_000001405.26`.',
  sourceDivergence DOUBLE COMMENT 'The `sourceDivergence` is the fraction of non-indel bases that do not match the `Reference` this message was derived from.',
  sourceGermplasm
    ARRAY<
      STRUCT<
        -- Link properties
        referenceDbId STRING COMMENT 'The unique identifier for a `Reference`',
        referenceName STRING COMMENT 'The human readable name of a `Reference` within a `ReferenceSet`.',
        -- Properties
        germplasmDbId STRING COMMENT 'The ID which uniquely identifies a germplasm within the given database server  <br>MIAPPE V1.1 (DM-41) Biological material ID - Code used to identify the biological material in the data file. Should be unique within the Investigation. Can correspond to experimental plant ID, inventory lot ID, etc. This material identification is different from a BiosampleID which corresponds to Observation Unit or Samples sections below.',
        germplasmName STRING COMMENT 'Name of the germplasm. It can be the preferred name and does not have to be unique.  MCPD (v2.1) (ACCENAME) 11. Either a registered or other designation given to the material received, other than the donors accession number (23) or collecting number (3). First letter uppercase. Multiple names are separated by a semicolon without space.'
      >
    > COMMENT 'All known corresponding Germplasm',
  sourceURI STRING COMMENT 'The URI from which the sequence was obtained. Specifies a FASTA format file/string with one name, sequence pair. In most cases, clients should call the `getReferenceBases()` method to obtain sequence bases for a `Reference` instead of attempting to retrieve this URI.',
  species 
    STRUCT<
      term STRING COMMENT 'Ontology term - the label of the ontology term the termId is pointing to.',
      termURI STRING COMMENT 'Ontology term identifier - the CURIE for an ontology term. It differs from the standard GA4GH schema''s :ref:`id ` in that it is a CURIE pointing to an information resource outside of the scope of the schema or its resource implementation.'
    > NOT NULL COMMENT 'An ontology term describing an attribute.'
) 
COMMENT 'A `Reference` is a canonical assembled contig, intended to act as a reference coordinate space for other genomic annotations. A single `Reference` might represent the human chromosome 1, for instance. `References` are designed to be immutable.';


-- Generated by Schema Tools Generator Version: '0.68.0'
