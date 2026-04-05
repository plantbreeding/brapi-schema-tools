-- Location

/* 
A geographic Location on earth. This is usually used to describe the general area where an experiment took place. For example, a natural site, an experimental field, a greenhouse, a phenotyping facility, etc.
 */
CREATE TABLE brapi_Locations (
  -- Primary properties
  locationDbId STRING NOT NULL PRIMARY KEY COMMENT 'The unique identifier for a Location',
  locationName STRING NOT NULL PRIMARY KEY COMMENT 'A human readable name for a Location <br/> MIAPPE V1.1 (DM-18) Experimental site name - The name of the natural site, experimental field, greenhouse, phenotyping facility, etc. where the experiment took place.',
  -- Link properties
  parentLocationDbId STRING NOT NULL COMMENT 'The unique identifier for a Location',
  parentLocationName STRING NOT NULL COMMENT 'A human readable name for a Location <br/> MIAPPE V1.1 (DM-18) Experimental site name - The name of the natural site, experimental field, greenhouse, phenotyping facility, etc. where the experiment took place.',
  -- Clustering properties
  countryCode STRING COMMENT '[ISO_3166-1_alpha-3](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3) spec <br/> MIAPPE V1.1 (DM-17) Geographic location (country) - The country where the experiment took place, either as a full name or preferably as a 2-letter code.''',
  environmentType STRING COMMENT 'Describes the general type of environment of a Location. (ex. forest, field, nursery, etc)',
  locationType STRING COMMENT 'A short description of a type of Location (ex. Field Station, Breeding Location, Storage Location, etc)',
  -- Properties
  abbreviation STRING COMMENT 'A shortened version of the human readable name for a Location',
  additionalInfo MAP<STRING,STRING> NOT NULL COMMENT 'A free space containing any additional information related to a particular object. A data source may provide any JSON object, unrestricted by the BrAPI specification.',
  coordinateDescription STRING COMMENT 'Describes the precision and landmarks of the coordinate values used for a Location. (ex. the site, the nearest town, a 10 kilometers radius circle, +/- 20 meters, etc)',
  coordinateUncertainty STRING COMMENT 'Uncertainty associated with the coordinates in meters. Leave the value empty if the uncertainty is unknown.',
  coordinates 
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
    > NOT NULL COMMENT 'One geometry as defined by GeoJSON (RFC 7946). All coordinates are decimal values on the WGS84 geographic coordinate reference system.  Copied from RFC 7946 Section 3.1.1  A position is an array of numbers. There MUST be two or more elements. The first two elements are longitude and latitude, or easting and northing, precisely in that order and using decimal numbers. Altitude or elevation MAY be included as an optional third element.',
  countryName STRING COMMENT 'The full name of the country where a Location is located <br/> MIAPPE V1.1 (DM-17) Geographic location (country) - The country where the experiment took place, either as a full name or preferably as a 2-letter code.',
  documentationURL STRING COMMENT 'A URL to the human readable documentation of an object',
  exposure STRING COMMENT 'Describes the level of protection/exposure for things like sun light and wind at a particular Location',
  externalReferences
    ARRAY<
      STRUCT<
        referenceId STRING COMMENT 'The external reference ID. Could be a simple string or a URI.',
        referenceSource STRING COMMENT 'An identifier for the source system or database of this reference'
      >
    > COMMENT 'An array of external reference ids. These are references to this piece of data in an external system. Could be a simple string or a URI.',
  instituteAddress STRING COMMENT 'The street address of the institute at a particular Location <br/> MIAPPE V1.1 (DM-16) Contact institution - Name and address of the institution responsible for the study.',
  instituteName STRING COMMENT 'The full name of the institute at a particular Location <br/> MIAPPE V1.1 (DM-16) Contact institution - Name and address of the institution responsible for the study.',
  siteStatus STRING COMMENT 'Description of the accessibility of the location (ex. Public, Private)',
  slope STRING COMMENT 'Describes the approximate slope (height/distance) of a Location.',
  topography STRING COMMENT 'Describes the topography of the land at a Location. (ex. Plateau, Cirque, Hill, Valley, etc)'
) 
COMMENT 'A geographic Location on earth. This is usually used to describe the general area where an experiment took place. For example, a natural site, an experimental field, a greenhouse, a phenotyping facility, etc.';


-- Generated by Schema Tools Generator Version: '0.60.0'
