# Image
## R6 Class: Images
#' The Images class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Image entity
#' @title Images Class
#' @docType class
#' @description The Images class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Image entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Images <- R6Class(
  "Images",
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
    #' Gets a Image object by DbId from the BrAPI server
    #' @param id The DbIid of the Image to be returned
    #' @return returns a Image object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/images", id, sep = '/'))
    },
    #' @description
    #' Gets a list Image objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param descriptiveOntologyTerms A list of terms to formally describe the image to search for.
    #' @param imageFileNames Image file names to search for.
    #' @param imageFileSizeMax A maximum image file size to search for.
    #' @param imageFileSizeMin A minimum image file size to search for.
    #' @param imageHeightMax A maximum image height to search for.
    #' @param imageHeightMin A minimum image height to search for.
    #' @param imageLocation 
    #' @param imageNames Human readable names to search for.
    #' @param imageTimeStampRangeEnd The latest timestamp to search for.
    #' @param imageTimeStampRangeStart The earliest timestamp to search for.
    #' @param imageWidthMax A maximum image width to search for.
    #' @param imageWidthMin A minimum image width to search for.
    #' @param mimeTypes A set of image file types to search for.
    #' @param observationDbIds A list of observation Ids this image is associated with to search for
    #' @param imageDbIds A list of image Ids to search for
    #' @param observationUnitDbIds A set of observation unit identifiers to search for.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Image objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        descriptiveOntologyTerms = NULL,
        imageFileNames = NULL,
        imageFileSizeMax = NULL,
        imageFileSizeMin = NULL,
        imageHeightMax = NULL,
        imageHeightMin = NULL,
        imageLocation = NULL,
        imageNames = NULL,
        imageTimeStampRangeEnd = NULL,
        imageTimeStampRangeStart = NULL,
        imageWidthMax = NULL,
        imageWidthMin = NULL,
        mimeTypes = NULL,
        observationDbIds = NULL,
        imageDbIds = NULL,
        observationUnitDbIds = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropName <- commonCropNames
      }
      if (!is.null(programDbIds)) {
        queryParams$programDbId <- programDbIds
      }
      if (!is.null(programNames)) {
        queryParams$programName <- programNames
      }
      if (!is.null(descriptiveOntologyTerms)) {
        queryParams$descriptiveOntologyTerm <- descriptiveOntologyTerms
      }
      if (!is.null(imageFileNames)) {
        queryParams$imageFileName <- imageFileNames
      }
      if (!is.null(imageFileSizeMax)) {
        queryParams$imageFileSizeMax <- imageFileSizeMax
      }
      if (!is.null(imageFileSizeMin)) {
        queryParams$imageFileSizeMin <- imageFileSizeMin
      }
      if (!is.null(imageHeightMax)) {
        queryParams$imageHeightMax <- imageHeightMax
      }
      if (!is.null(imageHeightMin)) {
        queryParams$imageHeightMin <- imageHeightMin
      }
      if (!is.null(imageLocation)) {
        queryParams$imageLocation <- imageLocation
      }
      if (!is.null(imageNames)) {
        queryParams$imageName <- imageNames
      }
      if (!is.null(imageTimeStampRangeEnd)) {
        queryParams$imageTimeStampRangeEnd <- imageTimeStampRangeEnd
      }
      if (!is.null(imageTimeStampRangeStart)) {
        queryParams$imageTimeStampRangeStart <- imageTimeStampRangeStart
      }
      if (!is.null(imageWidthMax)) {
        queryParams$imageWidthMax <- imageWidthMax
      }
      if (!is.null(imageWidthMin)) {
        queryParams$imageWidthMin <- imageWidthMin
      }
      if (!is.null(mimeTypes)) {
        queryParams$mimeType <- mimeTypes
      }
      if (!is.null(observationDbIds)) {
        queryParams$observationDbId <- observationDbIds
      }
      if (!is.null(imageDbIds)) {
        queryParams$imageDbId <- imageDbIds
      }
      if (!is.null(observationUnitDbIds)) {
        queryParams$observationUnitDbId <- observationUnitDbIds
      }
      private$.client$perform_get_request("/images", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Image objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param descriptiveOntologyTerms A list of terms to formally describe the image to search for.
    #' @param imageFileNames Image file names to search for.
    #' @param imageFileSizeMax A maximum image file size to search for.
    #' @param imageFileSizeMin A minimum image file size to search for.
    #' @param imageHeightMax A maximum image height to search for.
    #' @param imageHeightMin A minimum image height to search for.
    #' @param imageLocation 
    #' @param imageNames Human readable names to search for.
    #' @param imageTimeStampRangeEnd The latest timestamp to search for.
    #' @param imageTimeStampRangeStart The earliest timestamp to search for.
    #' @param imageWidthMax A maximum image width to search for.
    #' @param imageWidthMin A minimum image width to search for.
    #' @param mimeTypes A set of image file types to search for.
    #' @param observationDbIds A list of observation Ids this image is associated with to search for
    #' @param imageDbIds A list of image Ids to search for
    #' @param observationUnitDbIds A set of observation unit identifiers to search for.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        descriptiveOntologyTerms = NULL,
        imageFileNames = NULL,
        imageFileSizeMax = NULL,
        imageFileSizeMin = NULL,
        imageHeightMax = NULL,
        imageHeightMin = NULL,
        imageLocation = NULL,
        imageNames = NULL,
        imageTimeStampRangeEnd = NULL,
        imageTimeStampRangeStart = NULL,
        imageWidthMax = NULL,
        imageWidthMin = NULL,
        mimeTypes = NULL,
        observationDbIds = NULL,
        imageDbIds = NULL,
        observationUnitDbIds = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropNames <- to_list(commonCropNames)
      }
      if (!is.null(programDbIds)) {
        queryParams$programDbIds <- to_list(programDbIds)
      }
      if (!is.null(programNames)) {
        queryParams$programNames <- to_list(programNames)
      }
      if (!is.null(descriptiveOntologyTerms)) {
        queryParams$descriptiveOntologyTerms <- to_list(descriptiveOntologyTerms)
      }
      if (!is.null(imageFileNames)) {
        queryParams$imageFileNames <- to_list(imageFileNames)
      }
      if (!is.null(imageFileSizeMax)) {
        queryParams$imageFileSizeMax <- to_list(imageFileSizeMax)
      }
      if (!is.null(imageFileSizeMin)) {
        queryParams$imageFileSizeMin <- to_list(imageFileSizeMin)
      }
      if (!is.null(imageHeightMax)) {
        queryParams$imageHeightMax <- to_list(imageHeightMax)
      }
      if (!is.null(imageHeightMin)) {
        queryParams$imageHeightMin <- to_list(imageHeightMin)
      }
      if (!is.null(imageLocation)) {
        queryParams$imageLocation <- to_list(imageLocation)
      }
      if (!is.null(imageNames)) {
        queryParams$imageNames <- to_list(imageNames)
      }
      if (!is.null(imageTimeStampRangeEnd)) {
        queryParams$imageTimeStampRangeEnd <- to_list(imageTimeStampRangeEnd)
      }
      if (!is.null(imageTimeStampRangeStart)) {
        queryParams$imageTimeStampRangeStart <- to_list(imageTimeStampRangeStart)
      }
      if (!is.null(imageWidthMax)) {
        queryParams$imageWidthMax <- to_list(imageWidthMax)
      }
      if (!is.null(imageWidthMin)) {
        queryParams$imageWidthMin <- to_list(imageWidthMin)
      }
      if (!is.null(mimeTypes)) {
        queryParams$mimeTypes <- to_list(mimeTypes)
      }
      if (!is.null(observationDbIds)) {
        queryParams$observationDbIds <- to_list(observationDbIds)
      }
      if (!is.null(imageDbIds)) {
        queryParams$imageDbIds <- to_list(imageDbIds)
      }
      if (!is.null(observationUnitDbIds)) {
        queryParams$observationUnitDbIds <- to_list(observationUnitDbIds)
      }
      private$.client$perform_post_request("/search/images", queryParams, page, pageSize)
    },
    #' Get the result of a search for Image objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/images", id, sep = '/'))
    },
    #' @description
    #' Creates a Image object on the BrAPI server.
    #' @param newValue The new Image object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Image object on the BrAPI server.
    #' @param value The updated Image object to be sent to the server
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
