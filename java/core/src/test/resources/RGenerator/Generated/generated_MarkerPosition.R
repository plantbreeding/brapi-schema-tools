# MarkerPosition
## R6 Class: MarkerPositions
#' The MarkerPositions class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the MarkerPosition entity
#' @title MarkerPositions Class
#' @docType class
#' @description The MarkerPositions class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the MarkerPosition entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
MarkerPositions <- R6Class(
  "MarkerPositions",
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
    #' Gets a list MarkerPosition objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param mapDbIds A list of IDs which uniquely identify `GenomeMaps` within the given database server
    #' @param linkageGroupNames A list of Uniquely Identifiable linkage group names
    #' @param variantDbIds A list of IDs which uniquely identify `Variants` within the given database server
    #' @param minPosition The minimum position of markers in a given map
    #' @param maxPosition The maximum position of markers in a given map
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of MarkerPosition objects.
    #'
    getAll = function(
        mapDbIds = NULL,
        linkageGroupNames = NULL,
        variantDbIds = NULL,
        minPosition = NULL,
        maxPosition = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(mapDbIds)) {
        queryParams$mapDbId <- mapDbIds
      }
      if (!is.null(linkageGroupNames)) {
        queryParams$linkageGroupName <- linkageGroupNames
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbId <- variantDbIds
      }
      if (!is.null(minPosition)) {
        queryParams$minPosition <- minPosition
      }
      if (!is.null(maxPosition)) {
        queryParams$maxPosition <- maxPosition
      }
      private$.client$perform_get_request("/markerpositions", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for MarkerPosition objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param mapDbIds A list of IDs which uniquely identify `GenomeMaps` within the given database server
    #' @param linkageGroupNames A list of Uniquely Identifiable linkage group names
    #' @param variantDbIds A list of IDs which uniquely identify `Variants` within the given database server
    #' @param minPosition The minimum position of markers in a given map
    #' @param maxPosition The maximum position of markers in a given map
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        mapDbIds = NULL,
        linkageGroupNames = NULL,
        variantDbIds = NULL,
        minPosition = NULL,
        maxPosition = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(mapDbIds)) {
        queryParams$mapDbIds <- to_list(mapDbIds)
      }
      if (!is.null(linkageGroupNames)) {
        queryParams$linkageGroupNames <- to_list(linkageGroupNames)
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbIds <- to_list(variantDbIds)
      }
      if (!is.null(minPosition)) {
        queryParams$minPosition <- to_list(minPosition)
      }
      if (!is.null(maxPosition)) {
        queryParams$maxPosition <- to_list(maxPosition)
      }
      private$.client$perform_post_request("/search/markerpositions", queryParams, page, pageSize)
    },
    #' Get the result of a search for MarkerPosition objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/markerpositions", id, sep = '/'))
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
