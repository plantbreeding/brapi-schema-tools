# Reference
## R6 Class: References
#' The References class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Reference entity
#' @title References Class
#' @docType class
#' @description The References class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Reference entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
References <- R6Class(
  "References",
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
    #' Gets a Reference object by DbId from the BrAPI server
    #' @param id The DbIid of the Reference to be returned
    #' @return returns a Reference object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/references", id, sep = '/'))
    },
    #' @description
    #' Gets a list Reference objects from the BrAPI server
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
    #' @param accessions If specified, return the references for which the `accession` matches this string (case-sensitive, exact match).
    #' @param md5checksums If specified, return the references for which the `md5checksum` matches this string (case-sensitive, exact match).
    #' @param referenceDbIds A list of IDs which uniquely identify `References` within the given database server
    #' @param referenceSetDbIds A list of IDs which uniquely identify `ReferenceSets` within the given database server
    #' @param isDerived A sequence X is said to be derived from source sequence Y, if X and Y are of the same length and the per-base sequence divergence at A/C/G/T bases is sufficiently small.
    #' @param minLength The minimum length of this `References` sequence.
    #' @param maxLength The minimum length of this `References` sequence.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Reference objects.
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
        accessions = NULL,
        md5checksums = NULL,
        referenceDbIds = NULL,
        referenceSetDbIds = NULL,
        isDerived = NULL,
        minLength = NULL,
        maxLength = NULL,
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
      if (!is.null(accessions)) {
        queryParams$accession <- accessions
      }
      if (!is.null(md5checksums)) {
        queryParams$md5checksum <- md5checksums
      }
      if (!is.null(referenceDbIds)) {
        queryParams$referenceDbId <- referenceDbIds
      }
      if (!is.null(referenceSetDbIds)) {
        queryParams$referenceSetDbId <- referenceSetDbIds
      }
      if (!is.null(isDerived)) {
        queryParams$isDerived <- isDerived
      }
      if (!is.null(minLength)) {
        queryParams$minLength <- minLength
      }
      if (!is.null(maxLength)) {
        queryParams$maxLength <- maxLength
      }
      private$.client$perform_get_request("/references", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Reference objects on the BrAPI server. The server
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
    #' @param accessions If specified, return the references for which the `accession` matches this string (case-sensitive, exact match).
    #' @param md5checksums If specified, return the references for which the `md5checksum` matches this string (case-sensitive, exact match).
    #' @param referenceDbIds A list of IDs which uniquely identify `References` within the given database server
    #' @param referenceSetDbIds A list of IDs which uniquely identify `ReferenceSets` within the given database server
    #' @param isDerived A sequence X is said to be derived from source sequence Y, if X and Y are of the same length and the per-base sequence divergence at A/C/G/T bases is sufficiently small.
    #' @param minLength The minimum length of this `References` sequence.
    #' @param maxLength The minimum length of this `References` sequence.
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
        accessions = NULL,
        md5checksums = NULL,
        referenceDbIds = NULL,
        referenceSetDbIds = NULL,
        isDerived = NULL,
        minLength = NULL,
        maxLength = NULL,
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
      if (!is.null(accessions)) {
        queryParams$accessions <- to_list(accessions)
      }
      if (!is.null(md5checksums)) {
        queryParams$md5checksums <- to_list(md5checksums)
      }
      if (!is.null(referenceDbIds)) {
        queryParams$referenceDbIds <- to_list(referenceDbIds)
      }
      if (!is.null(referenceSetDbIds)) {
        queryParams$referenceSetDbIds <- to_list(referenceSetDbIds)
      }
      if (!is.null(isDerived)) {
        queryParams$isDerived <- to_list(isDerived)
      }
      if (!is.null(minLength)) {
        queryParams$minLength <- to_list(minLength)
      }
      if (!is.null(maxLength)) {
        queryParams$maxLength <- to_list(maxLength)
      }
      private$.client$perform_post_request("/search/references", queryParams, page, pageSize)
    },
    #' Get the result of a search for Reference objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/references", id, sep = '/'))
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
