# Observation
## R6 Class: Observations
#' The Observations class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Observation entity
#' @title Observations Class
#' @docType class
#' @description The Observations class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Observation entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Observations <- R6Class(
  "Observations",
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
    #' Gets a Observation object by DbId from the BrAPI server
    #' @param id The DbIid of the Observation to be returned
    #' @return returns a Observation object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/observations", id, sep = '/'))
    },
    #' @description
    #' Gets a list Observation objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
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
    #' @param observationDbIds The unique id of an Observation
    #' @param observationUnitDbIds The unique id of an Observation Unit
    #' @param observationLevels Searches for values in ObservationUnit-&gt;observationUnitPosition-&gt;observationLevel
    #' @param observationLevelRelationships Searches for values in ObservationUnit-&gt;observationUnitPosition-&gt;observationLevelRelationships
    #' @param observationTimeStampRangeEnd Timestamp range end
    #' @param observationTimeStampRangeStart Timestamp range start
    #' @param seasonDbIds The year or Phenotyping campaign of a multi-annual study (trees, grape, ...)
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Observation objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        germplasmDbIds = NULL,
        germplasmNames = NULL,
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
        observationDbIds = NULL,
        observationUnitDbIds = NULL,
        observationLevels = NULL,
        observationLevelRelationships = NULL,
        observationTimeStampRangeEnd = NULL,
        observationTimeStampRangeStart = NULL,
        seasonDbIds = NULL,
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
      if (!is.null(observationDbIds)) {
        queryParams$observationDbId <- observationDbIds
      }
      if (!is.null(observationUnitDbIds)) {
        queryParams$observationUnitDbId <- observationUnitDbIds
      }
      if (!is.null(observationLevels)) {
        queryParams$observationLevel <- observationLevels
      }
      if (!is.null(observationLevelRelationships)) {
        queryParams$observationLevelRelationship <- observationLevelRelationships
      }
      if (!is.null(observationTimeStampRangeEnd)) {
        queryParams$observationTimeStampRangeEnd <- observationTimeStampRangeEnd
      }
      if (!is.null(observationTimeStampRangeStart)) {
        queryParams$observationTimeStampRangeStart <- observationTimeStampRangeStart
      }
      if (!is.null(seasonDbIds)) {
        queryParams$seasonDbId <- seasonDbIds
      }
      private$.client$perform_get_request("/observations", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Observation objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
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
    #' @param observationDbIds The unique id of an Observation
    #' @param observationUnitDbIds The unique id of an Observation Unit
    #' @param observationLevels Searches for values in ObservationUnit-&gt;observationUnitPosition-&gt;observationLevel
    #' @param observationLevelRelationships Searches for values in ObservationUnit-&gt;observationUnitPosition-&gt;observationLevelRelationships
    #' @param observationTimeStampRangeEnd Timestamp range end
    #' @param observationTimeStampRangeStart Timestamp range start
    #' @param seasonDbIds The year or Phenotyping campaign of a multi-annual study (trees, grape, ...)
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
        observationVariableDbIds = NULL,
        observationVariableNames = NULL,
        observationVariablePUIs = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        observationDbIds = NULL,
        observationUnitDbIds = NULL,
        observationLevels = NULL,
        observationLevelRelationships = NULL,
        observationTimeStampRangeEnd = NULL,
        observationTimeStampRangeStart = NULL,
        seasonDbIds = NULL,
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
      if (!is.null(observationDbIds)) {
        queryParams$observationDbIds <- to_list(observationDbIds)
      }
      if (!is.null(observationUnitDbIds)) {
        queryParams$observationUnitDbIds <- to_list(observationUnitDbIds)
      }
      if (!is.null(observationLevels)) {
        queryParams$observationLevels <- to_list(observationLevels)
      }
      if (!is.null(observationLevelRelationships)) {
        queryParams$observationLevelRelationships <- to_list(observationLevelRelationships)
      }
      if (!is.null(observationTimeStampRangeEnd)) {
        queryParams$observationTimeStampRangeEnd <- to_list(observationTimeStampRangeEnd)
      }
      if (!is.null(observationTimeStampRangeStart)) {
        queryParams$observationTimeStampRangeStart <- to_list(observationTimeStampRangeStart)
      }
      if (!is.null(seasonDbIds)) {
        queryParams$seasonDbIds <- to_list(seasonDbIds)
      }
      private$.client$perform_post_request("/search/observations", queryParams, page, pageSize)
    },
    #' Get the result of a search for Observation objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/observations", id, sep = '/'))
    },
    #' @description
    #' Creates a Observation object on the BrAPI server.
    #' @param newValue The new Observation object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Observation object on the BrAPI server.
    #' @param value The updated Observation object to be sent to the server
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