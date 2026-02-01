# Program
## R6 Class: Programs
#' The Programs class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Program entity
#' @title Programs Class
#' @docType class
#' @description The Programs class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Program entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Programs <- R6Class(
  "Programs",
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
    #' Gets a Program object by DbId from the BrAPI server
    #' @param id The DbIid of the Program to be returned
    #' @return returns a Program object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/programs", id, sep = '/'))
    },
    #' @description
    #' Gets a list Program objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param abbreviations A list of shortened human readable names for a set of Programs
    #' @param leadPersonDbIds The person DbIds of the program leader to search for
    #' @param leadPersonNames The names of the program leader to search for
    #' @param objectives A program objective to search for
    #' @param programTypes The type of program entity this object represents &lt;br/&gt; &#39;STANDARD&#39; represents a standard, permanent breeding program &lt;br/&gt; &#39;PROJECT&#39; represents a short term project, usually with a set time limit based on funding
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Program objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        abbreviations = NULL,
        leadPersonDbIds = NULL,
        leadPersonNames = NULL,
        objectives = NULL,
        programTypes = NULL,
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
      if (!is.null(abbreviations)) {
        queryParams$abbreviation <- abbreviations
      }
      if (!is.null(leadPersonDbIds)) {
        queryParams$leadPersonDbId <- leadPersonDbIds
      }
      if (!is.null(leadPersonNames)) {
        queryParams$leadPersonName <- leadPersonNames
      }
      if (!is.null(objectives)) {
        queryParams$objectife <- objectives
      }
      if (!is.null(programTypes)) {
        queryParams$programType <- programTypes
      }
      private$.client$perform_get_request("/programs", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Program objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param abbreviations A list of shortened human readable names for a set of Programs
    #' @param leadPersonDbIds The person DbIds of the program leader to search for
    #' @param leadPersonNames The names of the program leader to search for
    #' @param objectives A program objective to search for
    #' @param programTypes The type of program entity this object represents &lt;br/&gt; &#39;STANDARD&#39; represents a standard, permanent breeding program &lt;br/&gt; &#39;PROJECT&#39; represents a short term project, usually with a set time limit based on funding
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        abbreviations = NULL,
        leadPersonDbIds = NULL,
        leadPersonNames = NULL,
        objectives = NULL,
        programTypes = NULL,
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
      if (!is.null(abbreviations)) {
        queryParams$abbreviations <- to_list(abbreviations)
      }
      if (!is.null(leadPersonDbIds)) {
        queryParams$leadPersonDbIds <- to_list(leadPersonDbIds)
      }
      if (!is.null(leadPersonNames)) {
        queryParams$leadPersonNames <- to_list(leadPersonNames)
      }
      if (!is.null(objectives)) {
        queryParams$objectives <- to_list(objectives)
      }
      if (!is.null(programTypes)) {
        queryParams$programTypes <- to_list(programTypes)
      }
      private$.client$perform_post_request("/search/programs", queryParams, page, pageSize)
    },
    #' Get the result of a search for Program objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/programs", id, sep = '/'))
    },
    #' @description
    #' Creates a Program object on the BrAPI server.
    #' @param newValue The new Program object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Program object on the BrAPI server.
    #' @param value The updated Program object to be sent to the server
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
