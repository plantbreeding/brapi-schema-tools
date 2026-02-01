# Trial
## R6 Class: Trials
#' The Trials class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Trial entity
#' @title Trials Class
#' @docType class
#' @description The Trials class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Trial entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Trials <- R6Class(
  "Trials",
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
    #' Gets a Trial object by DbId from the BrAPI server
    #' @param id The DbIid of the Trial to be returned
    #' @return returns a Trial object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/trials", id, sep = '/'))
    },
    #' @description
    #' Gets a list Trial objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param locationDbIds The location ids to search for
    #' @param locationNames A human readable names to search for
    #' @param observationVariableDbIds The DbIds of Variables to search for
    #' @param observationVariableNames The names of Variables to search for
    #' @param observationVariablePUIs The Permanent Unique Identifier of an Observation Variable, usually in the form of a URI
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param active A flag to indicate if a Trial is currently active and ongoing
    #' @param contactDbIds List of contact entities associated with this trial
    #' @param searchDateRangeStart The start of the overlapping search date range.
    #' @param searchDateRangeEnd The end of the overlapping search date range.
    #' @param trialPUIs A permanent identifier for a trial.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Trial objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        locationDbIds = NULL,
        locationNames = NULL,
        observationVariableDbIds = NULL,
        observationVariableNames = NULL,
        observationVariablePUIs = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        active = NULL,
        contactDbIds = NULL,
        searchDateRangeStart = NULL,
        searchDateRangeEnd = NULL,
        trialPUIs = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropName <- commonCropNames
      }
      if (!is.null(locationDbIds)) {
        queryParams$locationDbId <- locationDbIds
      }
      if (!is.null(locationNames)) {
        queryParams$locationName <- locationNames
      }
      if (!is.null(observationVariableDbIds)) {
        queryParams$observationVariableDbId <- observationVariableDbIds
      }
      if (!is.null(observationVariableNames)) {
        queryParams$observationVariableName <- observationVariableNames
      }
      if (!is.null(observationVariablePUIs)) {
        queryParams$observationVariablePUI <- observationVariablePUIs
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
      if (!is.null(active)) {
        queryParams$actife <- active
      }
      if (!is.null(contactDbIds)) {
        queryParams$contactDbId <- contactDbIds
      }
      if (!is.null(searchDateRangeStart)) {
        queryParams$searchDateRangeStart <- searchDateRangeStart
      }
      if (!is.null(searchDateRangeEnd)) {
        queryParams$searchDateRangeEnd <- searchDateRangeEnd
      }
      if (!is.null(trialPUIs)) {
        queryParams$trialPUI <- trialPUIs
      }
      private$.client$perform_get_request("/trials", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Trial objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param locationDbIds The location ids to search for
    #' @param locationNames A human readable names to search for
    #' @param observationVariableDbIds The DbIds of Variables to search for
    #' @param observationVariableNames The names of Variables to search for
    #' @param observationVariablePUIs The Permanent Unique Identifier of an Observation Variable, usually in the form of a URI
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param active A flag to indicate if a Trial is currently active and ongoing
    #' @param contactDbIds List of contact entities associated with this trial
    #' @param searchDateRangeStart The start of the overlapping search date range.
    #' @param searchDateRangeEnd The end of the overlapping search date range.
    #' @param trialPUIs A permanent identifier for a trial.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        locationDbIds = NULL,
        locationNames = NULL,
        observationVariableDbIds = NULL,
        observationVariableNames = NULL,
        observationVariablePUIs = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        active = NULL,
        contactDbIds = NULL,
        searchDateRangeStart = NULL,
        searchDateRangeEnd = NULL,
        trialPUIs = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(commonCropNames)) {
        queryParams$commonCropNames <- to_list(commonCropNames)
      }
      if (!is.null(locationDbIds)) {
        queryParams$locationDbIds <- to_list(locationDbIds)
      }
      if (!is.null(locationNames)) {
        queryParams$locationNames <- to_list(locationNames)
      }
      if (!is.null(observationVariableDbIds)) {
        queryParams$observationVariableDbIds <- to_list(observationVariableDbIds)
      }
      if (!is.null(observationVariableNames)) {
        queryParams$observationVariableNames <- to_list(observationVariableNames)
      }
      if (!is.null(observationVariablePUIs)) {
        queryParams$observationVariablePUIs <- to_list(observationVariablePUIs)
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
      if (!is.null(active)) {
        queryParams$active <- to_list(active)
      }
      if (!is.null(contactDbIds)) {
        queryParams$contactDbIds <- to_list(contactDbIds)
      }
      if (!is.null(searchDateRangeStart)) {
        queryParams$searchDateRangeStart <- to_list(searchDateRangeStart)
      }
      if (!is.null(searchDateRangeEnd)) {
        queryParams$searchDateRangeEnd <- to_list(searchDateRangeEnd)
      }
      if (!is.null(trialPUIs)) {
        queryParams$trialPUIs <- to_list(trialPUIs)
      }
      private$.client$perform_post_request("/search/trials", queryParams, page, pageSize)
    },
    #' Get the result of a search for Trial objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/trials", id, sep = '/'))
    },
    #' @description
    #' Creates a Trial object on the BrAPI server.
    #' @param newValue The new Trial object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Trial object on the BrAPI server.
    #' @param value The updated Trial object to be sent to the server
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
