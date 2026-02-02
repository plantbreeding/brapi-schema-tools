# Location
## R6 Class: Locations
#' The Locations class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Location entity
#' @title Locations Class
#' @docType class
#' @description The Locations class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Location entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Locations <- R6Class(
  "Locations",
  public = list(
    #' @description
    #' Creates a new instance of this [R6][R6::R6Class] class.
    #' It is not recommended that this object is created separately from the BrAPIClient
    #' @param client THe BrAPIClient to be used for server communication
    #'
    initialize = function(client) {
          private$.client <- client
    },
    #' @description
    #' Gets a Location object by DbId from the BrAPI server
    #' @param id The DbIid of the Location to be returned
    #' @return returns a Location object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/locations", id, sep = '/'))
    },
    #' @description
    #' Gets a list Location objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param locationDbIds The location ids to search for
    #' @param locationNames A human readable names to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param abbreviations A list of shortened human readable names for a set of Locations
    #' @param altitudeMin The minimum altitude to search for
    #' @param altitudeMax The maximum altitude to search for
    #' @param countryCodes [ISO_3166-1_alpha-3](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3) spec
    #' @param countryNames The full name of the country to search for
    #' @param coordinates 
    #' @param instituteAddresses The street address of the institute to search for
    #' @param instituteNames The name of the institute to search for
    #' @param locationTypes The type of location this represents (ex. Breeding Location, Storage Location, etc)
    #' @param parentLocationDbIds The unique identifier for a Location &lt;br/&gt; The Parent Location defines the encompassing location that this location belongs to.
    #' @param parentLocationNames A human readable name for a location &lt;br/&gt; The Parent Location defines the encompassing location that this location belongs to.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Location objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        locationDbIds = NULL,
        locationNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        abbreviations = NULL,
        altitudeMin = NULL,
        altitudeMax = NULL,
        countryCodes = NULL,
        countryNames = NULL,
        coordinates = NULL,
        instituteAddresses = NULL,
        instituteNames = NULL,
        locationTypes = NULL,
        parentLocationDbIds = NULL,
        parentLocationNames = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropName <- commonCropNames
      }
      if (!is.null(locationDbIds)) {
        queryParams$locationDbId <- locationDbIds
      }
      if (!is.null(locationNames)) {
        queryParams$locationName <- locationNames
      }
      if (!is.null(programDbIds)) {
        queryParams$programDbId <- programDbIds
      }
      if (!is.null(programNames)) {
        queryParams$programName <- programNames
      }
      if (!is.null(abbreviations)) {
        queryParams$abbreviation <- abbreviations
      }
      if (!is.null(altitudeMin)) {
        queryParams$altitudeMin <- altitudeMin
      }
      if (!is.null(altitudeMax)) {
        queryParams$altitudeMax <- altitudeMax
      }
      if (!is.null(countryCodes)) {
        queryParams$countryCode <- countryCodes
      }
      if (!is.null(countryNames)) {
        queryParams$countryName <- countryNames
      }
      if (!is.null(coordinates)) {
        queryParams$coordinate <- coordinates
      }
      if (!is.null(instituteAddresses)) {
        queryParams$instituteAddress <- instituteAddresses
      }
      if (!is.null(instituteNames)) {
        queryParams$instituteName <- instituteNames
      }
      if (!is.null(locationTypes)) {
        queryParams$locationType <- locationTypes
      }
      if (!is.null(parentLocationDbIds)) {
        queryParams$parentLocationDbId <- parentLocationDbIds
      }
      if (!is.null(parentLocationNames)) {
        queryParams$parentLocationName <- parentLocationNames
      }
      private$.client$perform_get_request("/locations", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Location objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param locationDbIds The location ids to search for
    #' @param locationNames A human readable names to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param abbreviations A list of shortened human readable names for a set of Locations
    #' @param altitudeMin The minimum altitude to search for
    #' @param altitudeMax The maximum altitude to search for
    #' @param countryCodes [ISO_3166-1_alpha-3](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3) spec
    #' @param countryNames The full name of the country to search for
    #' @param coordinates 
    #' @param instituteAddresses The street address of the institute to search for
    #' @param instituteNames The name of the institute to search for
    #' @param locationTypes The type of location this represents (ex. Breeding Location, Storage Location, etc)
    #' @param parentLocationDbIds The unique identifier for a Location &lt;br/&gt; The Parent Location defines the encompassing location that this location belongs to.
    #' @param parentLocationNames A human readable name for a location &lt;br/&gt; The Parent Location defines the encompassing location that this location belongs to.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        locationDbIds = NULL,
        locationNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        abbreviations = NULL,
        altitudeMin = NULL,
        altitudeMax = NULL,
        countryCodes = NULL,
        countryNames = NULL,
        coordinates = NULL,
        instituteAddresses = NULL,
        instituteNames = NULL,
        locationTypes = NULL,
        parentLocationDbIds = NULL,
        parentLocationNames = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropNames <- to_list(commonCropNames)
      }
      if (!is.null(locationDbIds)) {
        queryParams$locationDbIds <- to_list(locationDbIds)
      }
      if (!is.null(locationNames)) {
        queryParams$locationNames <- to_list(locationNames)
      }
      if (!is.null(programDbIds)) {
        queryParams$programDbIds <- to_list(programDbIds)
      }
      if (!is.null(programNames)) {
        queryParams$programNames <- to_list(programNames)
      }
      if (!is.null(abbreviations)) {
        queryParams$abbreviations <- to_list(abbreviations)
      }
      if (!is.null(altitudeMin)) {
        queryParams$altitudeMin <- to_list(altitudeMin)
      }
      if (!is.null(altitudeMax)) {
        queryParams$altitudeMax <- to_list(altitudeMax)
      }
      if (!is.null(countryCodes)) {
        queryParams$countryCodes <- to_list(countryCodes)
      }
      if (!is.null(countryNames)) {
        queryParams$countryNames <- to_list(countryNames)
      }
      if (!is.null(coordinates)) {
        queryParams$coordinates <- to_list(coordinates)
      }
      if (!is.null(instituteAddresses)) {
        queryParams$instituteAddresses <- to_list(instituteAddresses)
      }
      if (!is.null(instituteNames)) {
        queryParams$instituteNames <- to_list(instituteNames)
      }
      if (!is.null(locationTypes)) {
        queryParams$locationTypes <- to_list(locationTypes)
      }
      if (!is.null(parentLocationDbIds)) {
        queryParams$parentLocationDbIds <- to_list(parentLocationDbIds)
      }
      if (!is.null(parentLocationNames)) {
        queryParams$parentLocationNames <- to_list(parentLocationNames)
      }
      private$.client$perform_post_request("/search/locations", queryParams, page, pageSize)
    },
    #' Get the result of a search for Location objects on the BrAPI server. If the server
    #' returned search result ID for later retrieval, use this function to get the results.
    #' @param searchResultId The search result ID returned from a previous search call
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or by ID to the results.
    #'
    searchResult = function(searchResultId, page = 0, pageSize = 1000) {
      if (is.null(searchResultId)) {
       stop("searchResultId argument must be provided")
      }

      private$.client$perform_get_request(paste("/search/locations", id, sep = '/'))
    },
    #' @description
    #' Creates a Location object on the BrAPI server.
    #' @param newValue The new Location object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Location object on the BrAPI server.
    #' @param value The updated Location object to be sent to the server
    #'
    update = function(value) {
        stop("Update not yet implemented")
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'