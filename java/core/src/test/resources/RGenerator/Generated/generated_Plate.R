# Plate
## R6 Class: Plates
#' The Plates class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Plate entity
#' @title Plates Class
#' @docType class
#' @description The Plates class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Plate entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Plates <- R6Class(
  "Plates",
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
    #' Gets a Plate object by DbId from the BrAPI server
    #' @param id The DbIid of the Plate to be returned
    #' @return returns a Plate object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/plates", id, sep = '/'))
    },
    #' @description
    #' Gets a list Plate objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param observationUnitDbIds The ID which uniquely identifies an observation unit
    #' @param plateDbIds The ID which uniquely identifies a plate of samples
    #' @param plateNames The human readable name of a plate of samples
    #' @param plateBarcodes A unique identifier physically attached to the plate
    #' @param sampleDbIds The ID which uniquely identifies a sample
    #' @param sampleNames The human readable name of the sample
    #' @param sampleGroupDbIds The unique identifier for a group of related Samples
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Plate objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        observationUnitDbIds = NULL,
        plateDbIds = NULL,
        plateNames = NULL,
        plateBarcodes = NULL,
        sampleDbIds = NULL,
        sampleNames = NULL,
        sampleGroupDbIds = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropName <- commonCropNames
      }
      if (!is.null(germplasmDbIds)) {
        queryParams$germplasmDbId <- germplasmDbIds
      }
      if (!is.null(germplasmNames)) {
        queryParams$germplasmName <- germplasmNames
      }
      if (!is.null(programDbIds)) {
        queryParams$programDbId <- programDbIds
      }
      if (!is.null(programNames)) {
        queryParams$programName <- programNames
      }
      if (!is.null(studyDbIds)) {
        queryParams$studyDbId <- studyDbIds
      }
      if (!is.null(studyNames)) {
        queryParams$studyName <- studyNames
      }
      if (!is.null(trialDbIds)) {
        queryParams$trialDbId <- trialDbIds
      }
      if (!is.null(trialNames)) {
        queryParams$trialName <- trialNames
      }
      if (!is.null(observationUnitDbIds)) {
        queryParams$observationUnitDbId <- observationUnitDbIds
      }
      if (!is.null(plateDbIds)) {
        queryParams$plateDbId <- plateDbIds
      }
      if (!is.null(plateNames)) {
        queryParams$plateName <- plateNames
      }
      if (!is.null(plateBarcodes)) {
        queryParams$plateBarcode <- plateBarcodes
      }
      if (!is.null(sampleDbIds)) {
        queryParams$sampleDbId <- sampleDbIds
      }
      if (!is.null(sampleNames)) {
        queryParams$sampleName <- sampleNames
      }
      if (!is.null(sampleGroupDbIds)) {
        queryParams$sampleGroupDbId <- sampleGroupDbIds
      }
      private$.client$perform_get_request("/plates", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Plate objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param observationUnitDbIds The ID which uniquely identifies an observation unit
    #' @param plateDbIds The ID which uniquely identifies a plate of samples
    #' @param plateNames The human readable name of a plate of samples
    #' @param plateBarcodes A unique identifier physically attached to the plate
    #' @param sampleDbIds The ID which uniquely identifies a sample
    #' @param sampleNames The human readable name of the sample
    #' @param sampleGroupDbIds The unique identifier for a group of related Samples
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        observationUnitDbIds = NULL,
        plateDbIds = NULL,
        plateNames = NULL,
        plateBarcodes = NULL,
        sampleDbIds = NULL,
        sampleNames = NULL,
        sampleGroupDbIds = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropNames <- to_list(commonCropNames)
      }
      if (!is.null(germplasmDbIds)) {
        queryParams$germplasmDbIds <- to_list(germplasmDbIds)
      }
      if (!is.null(germplasmNames)) {
        queryParams$germplasmNames <- to_list(germplasmNames)
      }
      if (!is.null(programDbIds)) {
        queryParams$programDbIds <- to_list(programDbIds)
      }
      if (!is.null(programNames)) {
        queryParams$programNames <- to_list(programNames)
      }
      if (!is.null(studyDbIds)) {
        queryParams$studyDbIds <- to_list(studyDbIds)
      }
      if (!is.null(studyNames)) {
        queryParams$studyNames <- to_list(studyNames)
      }
      if (!is.null(trialDbIds)) {
        queryParams$trialDbIds <- to_list(trialDbIds)
      }
      if (!is.null(trialNames)) {
        queryParams$trialNames <- to_list(trialNames)
      }
      if (!is.null(observationUnitDbIds)) {
        queryParams$observationUnitDbIds <- to_list(observationUnitDbIds)
      }
      if (!is.null(plateDbIds)) {
        queryParams$plateDbIds <- to_list(plateDbIds)
      }
      if (!is.null(plateNames)) {
        queryParams$plateNames <- to_list(plateNames)
      }
      if (!is.null(plateBarcodes)) {
        queryParams$plateBarcodes <- to_list(plateBarcodes)
      }
      if (!is.null(sampleDbIds)) {
        queryParams$sampleDbIds <- to_list(sampleDbIds)
      }
      if (!is.null(sampleNames)) {
        queryParams$sampleNames <- to_list(sampleNames)
      }
      if (!is.null(sampleGroupDbIds)) {
        queryParams$sampleGroupDbIds <- to_list(sampleGroupDbIds)
      }
      private$.client$perform_post_request("/search/plates", queryParams, page, pageSize)
    },
    #' Get the result of a search for Plate objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/plates", id, sep = '/'))
    },
    #' @description
    #' Creates a Plate object on the BrAPI server.
    #' @param newValue The new Plate object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'