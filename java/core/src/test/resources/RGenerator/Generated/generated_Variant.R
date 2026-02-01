# Variant
## R6 Class: Variants
#' The Variants class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Variant entity
#' @title Variants Class
#' @docType class
#' @description The Variants class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Variant entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Variants <- R6Class(
  "Variants",
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
    #' Gets a Variant object by DbId from the BrAPI server
    #' @param id The DbIid of the Variant to be returned
    #' @return returns a Variant object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/variants", id, sep = '/'))
    },
    #' @description
    #' Gets a list Variant objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param callSetDbIds **Deprecated in v2.
    #' @param end The end of the window (0-based, exclusive) for which overlapping variants should be returned.
    #' @param referenceDbId **Deprecated in v2.
    #' @param referenceDbIds The unique identifier representing a genotype `Reference`
    #' @param referenceSetDbIds The unique identifier representing a genotype `ReferenceSet`
    #' @param start The beginning of the window (0-based, inclusive) for which overlapping variants should be returned.
    #' @param variantDbIds A list of IDs which uniquely identify `Variants`
    #' @param variantSetDbIds A list of IDs which uniquely identify `VariantSets`
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Variant objects.
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
        end = NULL,
        referenceDbId = NULL,
        referenceDbIds = NULL,
        referenceSetDbIds = NULL,
        start = NULL,
        variantDbIds = NULL,
        variantSetDbIds = NULL,
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
      if (!is.null(end)) {
        queryParams$end <- end
      }
      if (!is.null(referenceDbId)) {
        queryParams$referenceDbId <- referenceDbId
      }
      if (!is.null(referenceDbIds)) {
        queryParams$referenceDbId <- referenceDbIds
      }
      if (!is.null(referenceSetDbIds)) {
        queryParams$referenceSetDbId <- referenceSetDbIds
      }
      if (!is.null(start)) {
        queryParams$start <- start
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbId <- variantDbIds
      }
      if (!is.null(variantSetDbIds)) {
        queryParams$variantSetDbId <- variantSetDbIds
      }
      private$.client$perform_get_request("/variants", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Variant objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param callSetDbIds **Deprecated in v2.
    #' @param end The end of the window (0-based, exclusive) for which overlapping variants should be returned.
    #' @param referenceDbId **Deprecated in v2.
    #' @param referenceDbIds The unique identifier representing a genotype `Reference`
    #' @param referenceSetDbIds The unique identifier representing a genotype `ReferenceSet`
    #' @param start The beginning of the window (0-based, inclusive) for which overlapping variants should be returned.
    #' @param variantDbIds A list of IDs which uniquely identify `Variants`
    #' @param variantSetDbIds A list of IDs which uniquely identify `VariantSets`
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
        end = NULL,
        referenceDbId = NULL,
        referenceDbIds = NULL,
        referenceSetDbIds = NULL,
        start = NULL,
        variantDbIds = NULL,
        variantSetDbIds = NULL,
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
      if (!is.null(end)) {
        queryParams$end <- to_list(end)
      }
      if (!is.null(referenceDbId)) {
        queryParams$referenceDbId <- to_list(referenceDbId)
      }
      if (!is.null(referenceDbIds)) {
        queryParams$referenceDbIds <- to_list(referenceDbIds)
      }
      if (!is.null(referenceSetDbIds)) {
        queryParams$referenceSetDbIds <- to_list(referenceSetDbIds)
      }
      if (!is.null(start)) {
        queryParams$start <- to_list(start)
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbIds <- to_list(variantDbIds)
      }
      if (!is.null(variantSetDbIds)) {
        queryParams$variantSetDbIds <- to_list(variantSetDbIds)
      }
      private$.client$perform_post_request("/search/variants", queryParams, page, pageSize)
    },
    #' Get the result of a search for Variant objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/variants", id, sep = '/'))
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
