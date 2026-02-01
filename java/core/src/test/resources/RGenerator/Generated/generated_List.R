# List
## R6 Class: Lists
#' The Lists class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the List entity
#' @title Lists Class
#' @docType class
#' @description The Lists class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the List entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Lists <- R6Class(
  "Lists",
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
    #' Gets a List object by DbId from the BrAPI server
    #' @param id The DbIid of the List to be returned
    #' @return returns a List object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/lists", id, sep = '/'))
    },
    #' @description
    #' Gets a list List objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param dateCreatedRangeStart Define the beginning for an interval of time and only include Lists that are created within this interval.
    #' @param dateCreatedRangeEnd Define the end for an interval of time and only include Lists that are created within this interval.
    #' @param dateModifiedRangeStart Define the beginning for an interval of time and only include Lists that are modified within this interval.
    #' @param dateModifiedRangeEnd Define the end for an interval of time and only include Lists that are modified within this interval.
    #' @param listDbIds An array of primary database identifiers to identify a set of Lists
    #' @param listNames An array of human readable names to identify a set of Lists
    #' @param listOwnerNames An array of names for the people or entities who are responsible for a set of Lists
    #' @param listOwnerPersonDbIds An array of primary database identifiers to identify people or entities who are responsible for a set of Lists
    #' @param listSources An array of terms identifying lists from different sources (ie &#39;USER&#39;, &#39;SYSTEM&#39;, etc)
    #' @param listType 
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of List objects.
    #'
    getAll = function(
        dateCreatedRangeStart = NULL,
        dateCreatedRangeEnd = NULL,
        dateModifiedRangeStart = NULL,
        dateModifiedRangeEnd = NULL,
        listDbIds = NULL,
        listNames = NULL,
        listOwnerNames = NULL,
        listOwnerPersonDbIds = NULL,
        listSources = NULL,
        listType = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(dateCreatedRangeStart)) {
        queryParams$dateCreatedRangeStart <- dateCreatedRangeStart
      }
      if (!is.null(dateCreatedRangeEnd)) {
        queryParams$dateCreatedRangeEnd <- dateCreatedRangeEnd
      }
      if (!is.null(dateModifiedRangeStart)) {
        queryParams$dateModifiedRangeStart <- dateModifiedRangeStart
      }
      if (!is.null(dateModifiedRangeEnd)) {
        queryParams$dateModifiedRangeEnd <- dateModifiedRangeEnd
      }
      if (!is.null(listDbIds)) {
        queryParams$listDbId <- listDbIds
      }
      if (!is.null(listNames)) {
        queryParams$listName <- listNames
      }
      if (!is.null(listOwnerNames)) {
        queryParams$listOwnerName <- listOwnerNames
      }
      if (!is.null(listOwnerPersonDbIds)) {
        queryParams$listOwnerPersonDbId <- listOwnerPersonDbIds
      }
      if (!is.null(listSources)) {
        queryParams$listSource <- listSources
      }
      if (!is.null(listType)) {
        queryParams$listType <- listType
      }
      private$.client$perform_get_request("/lists", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for List objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param dateCreatedRangeStart Define the beginning for an interval of time and only include Lists that are created within this interval.
    #' @param dateCreatedRangeEnd Define the end for an interval of time and only include Lists that are created within this interval.
    #' @param dateModifiedRangeStart Define the beginning for an interval of time and only include Lists that are modified within this interval.
    #' @param dateModifiedRangeEnd Define the end for an interval of time and only include Lists that are modified within this interval.
    #' @param listDbIds An array of primary database identifiers to identify a set of Lists
    #' @param listNames An array of human readable names to identify a set of Lists
    #' @param listOwnerNames An array of names for the people or entities who are responsible for a set of Lists
    #' @param listOwnerPersonDbIds An array of primary database identifiers to identify people or entities who are responsible for a set of Lists
    #' @param listSources An array of terms identifying lists from different sources (ie &#39;USER&#39;, &#39;SYSTEM&#39;, etc)
    #' @param listType 
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        dateCreatedRangeStart = NULL,
        dateCreatedRangeEnd = NULL,
        dateModifiedRangeStart = NULL,
        dateModifiedRangeEnd = NULL,
        listDbIds = NULL,
        listNames = NULL,
        listOwnerNames = NULL,
        listOwnerPersonDbIds = NULL,
        listSources = NULL,
        listType = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(dateCreatedRangeStart)) {
        queryParams$dateCreatedRangeStart <- to_list(dateCreatedRangeStart)
      }
      if (!is.null(dateCreatedRangeEnd)) {
        queryParams$dateCreatedRangeEnd <- to_list(dateCreatedRangeEnd)
      }
      if (!is.null(dateModifiedRangeStart)) {
        queryParams$dateModifiedRangeStart <- to_list(dateModifiedRangeStart)
      }
      if (!is.null(dateModifiedRangeEnd)) {
        queryParams$dateModifiedRangeEnd <- to_list(dateModifiedRangeEnd)
      }
      if (!is.null(listDbIds)) {
        queryParams$listDbIds <- to_list(listDbIds)
      }
      if (!is.null(listNames)) {
        queryParams$listNames <- to_list(listNames)
      }
      if (!is.null(listOwnerNames)) {
        queryParams$listOwnerNames <- to_list(listOwnerNames)
      }
      if (!is.null(listOwnerPersonDbIds)) {
        queryParams$listOwnerPersonDbIds <- to_list(listOwnerPersonDbIds)
      }
      if (!is.null(listSources)) {
        queryParams$listSources <- to_list(listSources)
      }
      if (!is.null(listType)) {
        queryParams$listType <- to_list(listType)
      }
      private$.client$perform_post_request("/search/lists", queryParams, page, pageSize)
    },
    #' Get the result of a search for List objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/lists", id, sep = '/'))
    },
    #' @description
    #' Creates a List object on the BrAPI server.
    #' @param newValue The new List object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a List object on the BrAPI server.
    #' @param value The updated List object to be sent to the server
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
