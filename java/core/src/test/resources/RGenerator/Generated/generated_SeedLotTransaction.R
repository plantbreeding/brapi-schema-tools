# SeedLotTransaction
## R6 Class: SeedLotTransactions
#' The SeedLotTransactions class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the SeedLotTransaction entity
#' @title SeedLotTransactions Class
#' @docType class
#' @description The SeedLotTransactions class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the SeedLotTransaction entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
SeedLotTransactions <- R6Class(
  "SeedLotTransactions",
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
    #' Gets a SeedLotTransaction object by DbId from the BrAPI server
    #' @param id The DbIid of the SeedLotTransaction to be returned
    #' @return returns a SeedLotTransaction object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/seedlottransactions", id, sep = '/'))
    },
    #' @description
    #' Gets a list SeedLotTransaction objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param seedLotDbIds Unique id for a seed lot on this server
    #' @param crossDbIds Search for Cross with this unique id
    #' @param crossNames Search for Cross with this human readable name
    #' @param transactionDbIds Unique id for a Transaction that has occurred
    #' @param transactionDirection Filter results to only include transactions directed to the specific Seed Lot (TO), away from the specific Seed Lot (FROM), or both (BOTH).
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of SeedLotTransaction objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        seedLotDbIds = NULL,
        crossDbIds = NULL,
        crossNames = NULL,
        transactionDbIds = NULL,
        transactionDirection = NULL,
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
      if (!is.null(seedLotDbIds)) {
        queryParams$seedLotDbId <- seedLotDbIds
      }
      if (!is.null(crossDbIds)) {
        queryParams$crossDbId <- crossDbIds
      }
      if (!is.null(crossNames)) {
        queryParams$crossName <- crossNames
      }
      if (!is.null(transactionDbIds)) {
        queryParams$transactionDbId <- transactionDbIds
      }
      if (!is.null(transactionDirection)) {
        queryParams$transactionDirection <- transactionDirection
      }
      private$.client$perform_get_request("/seedlottransactions", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for SeedLotTransaction objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param seedLotDbIds Unique id for a seed lot on this server
    #' @param crossDbIds Search for Cross with this unique id
    #' @param crossNames Search for Cross with this human readable name
    #' @param transactionDbIds Unique id for a Transaction that has occurred
    #' @param transactionDirection Filter results to only include transactions directed to the specific Seed Lot (TO), away from the specific Seed Lot (FROM), or both (BOTH).
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
        seedLotDbIds = NULL,
        crossDbIds = NULL,
        crossNames = NULL,
        transactionDbIds = NULL,
        transactionDirection = NULL,
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
      if (!is.null(seedLotDbIds)) {
        queryParams$seedLotDbIds <- to_list(seedLotDbIds)
      }
      if (!is.null(crossDbIds)) {
        queryParams$crossDbIds <- to_list(crossDbIds)
      }
      if (!is.null(crossNames)) {
        queryParams$crossNames <- to_list(crossNames)
      }
      if (!is.null(transactionDbIds)) {
        queryParams$transactionDbIds <- to_list(transactionDbIds)
      }
      if (!is.null(transactionDirection)) {
        queryParams$transactionDirection <- to_list(transactionDirection)
      }
      private$.client$perform_post_request("/search/seedlottransactions", queryParams, page, pageSize)
    },
    #' Get the result of a search for SeedLotTransaction objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/seedlottransactions", id, sep = '/'))
    },
    #' @description
    #' Creates a SeedLotTransaction object on the BrAPI server.
    #' @param newValue The new SeedLotTransaction object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a SeedLotTransaction object on the BrAPI server.
    #' @param value The updated SeedLotTransaction object to be sent to the server
    #'
    update = function(value) {
        stop("Update not yet implemented")
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.77.0'