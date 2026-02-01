# VariantSet
## R6 Class: VariantSets
#' The VariantSets class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the VariantSet entity
#' @title VariantSets Class
#' @docType class
#' @description The VariantSets class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the VariantSet entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
VariantSets <- R6Class(
  "VariantSets",
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
    #' Gets a VariantSet object by DbId from the BrAPI server
    #' @param id The DbIid of the VariantSet to be returned
    #' @return returns a VariantSet object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/variantsets", id, sep = '/'))
    },
    #' @description
    #' Gets a list VariantSet objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param callSetDbIds The unique identifier representing a CallSet
    #' @param variantDbIds The unique identifier representing a Variant
    #' @param variantSetDbIds The unique identifier representing a VariantSet
    #' @param referenceDbIds The unique identifier representing a genotype Reference
    #' @param referenceSetDbIds The unique identifier representing a genotype ReferenceSet
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of VariantSet objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        callSetDbIds = NULL,
        variantDbIds = NULL,
        variantSetDbIds = NULL,
        referenceDbIds = NULL,
        referenceSetDbIds = NULL,
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
      if (!is.null(callSetDbIds)) {
        queryParams$callSetDbId <- callSetDbIds
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbId <- variantDbIds
      }
      if (!is.null(variantSetDbIds)) {
        queryParams$variantSetDbId <- variantSetDbIds
      }
      if (!is.null(referenceDbIds)) {
        queryParams$referenceDbId <- referenceDbIds
      }
      if (!is.null(referenceSetDbIds)) {
        queryParams$referenceSetDbId <- referenceSetDbIds
      }
      private$.client$perform_get_request("/variantsets", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for VariantSet objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param callSetDbIds The unique identifier representing a CallSet
    #' @param variantDbIds The unique identifier representing a Variant
    #' @param variantSetDbIds The unique identifier representing a VariantSet
    #' @param referenceDbIds The unique identifier representing a genotype Reference
    #' @param referenceSetDbIds The unique identifier representing a genotype ReferenceSet
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        callSetDbIds = NULL,
        variantDbIds = NULL,
        variantSetDbIds = NULL,
        referenceDbIds = NULL,
        referenceSetDbIds = NULL,
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
      if (!is.null(callSetDbIds)) {
        queryParams$callSetDbIds <- to_list(callSetDbIds)
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbIds <- to_list(variantDbIds)
      }
      if (!is.null(variantSetDbIds)) {
        queryParams$variantSetDbIds <- to_list(variantSetDbIds)
      }
      if (!is.null(referenceDbIds)) {
        queryParams$referenceDbIds <- to_list(referenceDbIds)
      }
      if (!is.null(referenceSetDbIds)) {
        queryParams$referenceSetDbIds <- to_list(referenceSetDbIds)
      }
      private$.client$perform_post_request("/search/variantsets", queryParams, page, pageSize)
    },
    #' Get the result of a search for VariantSet objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/variantsets", id, sep = '/'))
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
