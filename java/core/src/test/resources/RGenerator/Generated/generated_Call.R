# Call
## R6 Class: Calls
#' The Calls class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Call entity
#' @title Calls Class
#' @docType class
#' @description The Calls class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Call entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Calls <- R6Class(
  "Calls",
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
    #' Gets a list Call objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param callSetDbIds A list of IDs which uniquely identify `CallSets` within the given database server
    #' @param variantDbIds A list of IDs which uniquely identify `Variant` within the given database server
    #' @param variantSetDbIds A list of IDs which uniquely identify `VariantSets` within the given database server
    #' @param expandHomozygotes Should homozygotes be expanded (true) or collapsed into a single occurrence (false)
    #' @param sepPhased The string used as a separator for phased allele calls.
    #' @param sepUnphased The string used as a separator for unphased allele calls.
    #' @param unknownString The string used as a representation for missing data.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Call objects.
    #'
    getAll = function(
        callSetDbIds = NULL,
        variantDbIds = NULL,
        variantSetDbIds = NULL,
        expandHomozygotes = NULL,
        sepPhased = NULL,
        sepUnphased = NULL,
        unknownString = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(callSetDbIds)) {
        queryParams$callSetDbId <- callSetDbIds
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbId <- variantDbIds
      }
      if (!is.null(variantSetDbIds)) {
        queryParams$variantSetDbId <- variantSetDbIds
      }
      if (!is.null(expandHomozygotes)) {
        queryParams$expandHomozygote <- expandHomozygotes
      }
      if (!is.null(sepPhased)) {
        queryParams$sepPhased <- sepPhased
      }
      if (!is.null(sepUnphased)) {
        queryParams$sepUnphased <- sepUnphased
      }
      if (!is.null(unknownString)) {
        queryParams$unknownString <- unknownString
      }
      private$.client$perform_get_request("/calls", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Call objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param callSetDbIds A list of IDs which uniquely identify `CallSets` within the given database server
    #' @param variantDbIds A list of IDs which uniquely identify `Variant` within the given database server
    #' @param variantSetDbIds A list of IDs which uniquely identify `VariantSets` within the given database server
    #' @param expandHomozygotes Should homozygotes be expanded (true) or collapsed into a single occurrence (false)
    #' @param sepPhased The string used as a separator for phased allele calls.
    #' @param sepUnphased The string used as a separator for unphased allele calls.
    #' @param unknownString The string used as a representation for missing data.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        callSetDbIds = NULL,
        variantDbIds = NULL,
        variantSetDbIds = NULL,
        expandHomozygotes = NULL,
        sepPhased = NULL,
        sepUnphased = NULL,
        unknownString = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(callSetDbIds)) {
        queryParams$callSetDbIds <- to_list(callSetDbIds)
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbIds <- to_list(variantDbIds)
      }
      if (!is.null(variantSetDbIds)) {
        queryParams$variantSetDbIds <- to_list(variantSetDbIds)
      }
      if (!is.null(expandHomozygotes)) {
        queryParams$expandHomozygotes <- to_list(expandHomozygotes)
      }
      if (!is.null(sepPhased)) {
        queryParams$sepPhased <- to_list(sepPhased)
      }
      if (!is.null(sepUnphased)) {
        queryParams$sepUnphased <- to_list(sepUnphased)
      }
      if (!is.null(unknownString)) {
        queryParams$unknownString <- to_list(unknownString)
      }
      private$.client$perform_post_request("/search/calls", queryParams, page, pageSize)
    },
    #' Get the result of a search for Call objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/calls", id, sep = '/'))
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
