# Study
## R6 Class: Studies
#' The Studies class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Study entity
#' @title Studies Class
#' @docType class
#' @description The Studies class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Study entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Studies <- R6Class(
  "Studies",
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
    #' Gets a Study object by DbId from the BrAPI server
    #' @param id The DbIid of the Study to be returned
    #' @return returns a Study object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/studies", id, sep = '/'))
    },
    #' @description
    #' Gets a list Study objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param locationDbIds The location ids to search for
    #' @param locationNames A human readable names to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param observationVariableDbIds The DbIds of Variables to search for
    #' @param observationVariableNames The names of Variables to search for
    #' @param observationVariablePUIs The Permanent Unique Identifier of an Observation Variable, usually in the form of a URI
    #' @param active A flag to indicate if a Study is currently active and ongoing
    #' @param seasonDbIds The ID which uniquely identifies a season
    #' @param studyTypes The type of study being performed.
    #' @param studyCodes A short human readable code for a study
    #' @param studyPUIs Permanent unique identifier associated with study data.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Study objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        locationDbIds = NULL,
        locationNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        observationVariableDbIds = NULL,
        observationVariableNames = NULL,
        observationVariablePUIs = NULL,
        active = NULL,
        seasonDbIds = NULL,
        studyTypes = NULL,
        studyCodes = NULL,
        studyPUIs = NULL,
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
      if (!is.null(locationDbIds)) {
        queryParams$locationDbId <- locationDbIds
      }
      if (!is.null(locationNames)) {
        queryParams$locationName <- locationNames
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
      if (!is.null(observationVariableDbIds)) {
        queryParams$observationVariableDbId <- observationVariableDbIds
      }
      if (!is.null(observationVariableNames)) {
        queryParams$observationVariableName <- observationVariableNames
      }
      if (!is.null(observationVariablePUIs)) {
        queryParams$observationVariablePUI <- observationVariablePUIs
      }
      if (!is.null(active)) {
        queryParams$actife <- active
      }
      if (!is.null(seasonDbIds)) {
        queryParams$seasonDbId <- seasonDbIds
      }
      if (!is.null(studyTypes)) {
        queryParams$studyType <- studyTypes
      }
      if (!is.null(studyCodes)) {
        queryParams$studyCode <- studyCodes
      }
      if (!is.null(studyPUIs)) {
        queryParams$studyPUI <- studyPUIs
      }
      private$.client$perform_get_request("/studies", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Study objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param locationDbIds The location ids to search for
    #' @param locationNames A human readable names to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param observationVariableDbIds The DbIds of Variables to search for
    #' @param observationVariableNames The names of Variables to search for
    #' @param observationVariablePUIs The Permanent Unique Identifier of an Observation Variable, usually in the form of a URI
    #' @param active A flag to indicate if a Study is currently active and ongoing
    #' @param seasonDbIds The ID which uniquely identifies a season
    #' @param studyTypes The type of study being performed.
    #' @param studyCodes A short human readable code for a study
    #' @param studyPUIs Permanent unique identifier associated with study data.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        locationDbIds = NULL,
        locationNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        observationVariableDbIds = NULL,
        observationVariableNames = NULL,
        observationVariablePUIs = NULL,
        active = NULL,
        seasonDbIds = NULL,
        studyTypes = NULL,
        studyCodes = NULL,
        studyPUIs = NULL,
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
      if (!is.null(locationDbIds)) {
        queryParams$locationDbIds <- to_list(locationDbIds)
      }
      if (!is.null(locationNames)) {
        queryParams$locationNames <- to_list(locationNames)
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
      if (!is.null(observationVariableDbIds)) {
        queryParams$observationVariableDbIds <- to_list(observationVariableDbIds)
      }
      if (!is.null(observationVariableNames)) {
        queryParams$observationVariableNames <- to_list(observationVariableNames)
      }
      if (!is.null(observationVariablePUIs)) {
        queryParams$observationVariablePUIs <- to_list(observationVariablePUIs)
      }
      if (!is.null(active)) {
        queryParams$active <- to_list(active)
      }
      if (!is.null(seasonDbIds)) {
        queryParams$seasonDbIds <- to_list(seasonDbIds)
      }
      if (!is.null(studyTypes)) {
        queryParams$studyTypes <- to_list(studyTypes)
      }
      if (!is.null(studyCodes)) {
        queryParams$studyCodes <- to_list(studyCodes)
      }
      if (!is.null(studyPUIs)) {
        queryParams$studyPUIs <- to_list(studyPUIs)
      }
      private$.client$perform_post_request("/search/studies", queryParams, page, pageSize)
    },
    #' Get the result of a search for Study objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/studies", id, sep = '/'))
    },
    #' @description
    #' Creates a Study object on the BrAPI server.
    #' @param newValue The new Study object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Study object on the BrAPI server.
    #' @param value The updated Study object to be sent to the server
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
