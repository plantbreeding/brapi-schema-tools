# Germplasm
## R6 Class: Germplasm
#' The Germplasm class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Germplasm entity
#' @title Germplasm Class
#' @docType class
#' @description The Germplasm class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Germplasm entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
Germplasm <- R6Class(
  "Germplasm",
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
    #' Gets a Germplasm object by DbId from the BrAPI server
    #' @param id The DbIid of the Germplasm to be returned
    #' @return returns a Germplasm object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/germplasm", id, sep = '/'))
    },
    #' @description
    #' Gets a list Germplasm objects from the BrAPI server
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
    #' @param germplasmPUIs List of Permanent Unique Identifiers to identify germplasm
    #' @param accessionNumbers A collection of unique identifiers for materials or germplasm within a genebank  MCPD (v2.
    #' @param collections A specific panel/collection/population name this germplasm belongs to.
    #' @param familyCodes A familyCode representing the family this germplasm belongs to.
    #' @param instituteCodes The code for the institute that maintains the material.
    #' @param binomialNames List of the full binomial name (scientific name) to identify a germplasm
    #' @param genus List of Genus names to identify germplasm
    #' @param species List of Species names to identify germplasm
    #' @param synonyms List of alternative names or IDs used to reference this germplasm
    #' @param parentDbIds Search for Germplasm with these parents
    #' @param progenyDbIds Search for Germplasm with these children
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Germplasm objects.
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
        germplasmPUIs = NULL,
        accessionNumbers = NULL,
        collections = NULL,
        familyCodes = NULL,
        instituteCodes = NULL,
        binomialNames = NULL,
        genus = NULL,
        species = NULL,
        synonyms = NULL,
        parentDbIds = NULL,
        progenyDbIds = NULL,
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
      if (!is.null(germplasmPUIs)) {
        queryParams$germplasmPUI <- germplasmPUIs
      }
      if (!is.null(accessionNumbers)) {
        queryParams$accessionNumber <- accessionNumbers
      }
      if (!is.null(collections)) {
        queryParams$collection <- collections
      }
      if (!is.null(familyCodes)) {
        queryParams$familyCode <- familyCodes
      }
      if (!is.null(instituteCodes)) {
        queryParams$instituteCode <- instituteCodes
      }
      if (!is.null(binomialNames)) {
        queryParams$binomialName <- binomialNames
      }
      if (!is.null(genus)) {
        queryParams$genu <- genus
      }
      if (!is.null(species)) {
        queryParams$specy <- species
      }
      if (!is.null(synonyms)) {
        queryParams$synonym <- synonyms
      }
      if (!is.null(parentDbIds)) {
        queryParams$parentDbId <- parentDbIds
      }
      if (!is.null(progenyDbIds)) {
        queryParams$progenyDbId <- progenyDbIds
      }
      private$.client$perform_get_request("/germplasm", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Germplasm objects on the BrAPI server. The server
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
    #' @param germplasmPUIs List of Permanent Unique Identifiers to identify germplasm
    #' @param accessionNumbers A collection of unique identifiers for materials or germplasm within a genebank  MCPD (v2.
    #' @param collections A specific panel/collection/population name this germplasm belongs to.
    #' @param familyCodes A familyCode representing the family this germplasm belongs to.
    #' @param instituteCodes The code for the institute that maintains the material.
    #' @param binomialNames List of the full binomial name (scientific name) to identify a germplasm
    #' @param genus List of Genus names to identify germplasm
    #' @param species List of Species names to identify germplasm
    #' @param synonyms List of alternative names or IDs used to reference this germplasm
    #' @param parentDbIds Search for Germplasm with these parents
    #' @param progenyDbIds Search for Germplasm with these children
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
        germplasmPUIs = NULL,
        accessionNumbers = NULL,
        collections = NULL,
        familyCodes = NULL,
        instituteCodes = NULL,
        binomialNames = NULL,
        genus = NULL,
        species = NULL,
        synonyms = NULL,
        parentDbIds = NULL,
        progenyDbIds = NULL,
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
      if (!is.null(germplasmPUIs)) {
        queryParams$germplasmPUIs <- to_list(germplasmPUIs)
      }
      if (!is.null(accessionNumbers)) {
        queryParams$accessionNumbers <- to_list(accessionNumbers)
      }
      if (!is.null(collections)) {
        queryParams$collections <- to_list(collections)
      }
      if (!is.null(familyCodes)) {
        queryParams$familyCodes <- to_list(familyCodes)
      }
      if (!is.null(instituteCodes)) {
        queryParams$instituteCodes <- to_list(instituteCodes)
      }
      if (!is.null(binomialNames)) {
        queryParams$binomialNames <- to_list(binomialNames)
      }
      if (!is.null(genus)) {
        queryParams$genus <- to_list(genus)
      }
      if (!is.null(species)) {
        queryParams$species <- to_list(species)
      }
      if (!is.null(synonyms)) {
        queryParams$synonyms <- to_list(synonyms)
      }
      if (!is.null(parentDbIds)) {
        queryParams$parentDbIds <- to_list(parentDbIds)
      }
      if (!is.null(progenyDbIds)) {
        queryParams$progenyDbIds <- to_list(progenyDbIds)
      }
      private$.client$perform_post_request("/search/germplasm", queryParams, page, pageSize)
    },
    #' Get the result of a search for Germplasm objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/germplasm", id, sep = '/'))
    },
    #' @description
    #' Creates a Germplasm object on the BrAPI server.
    #' @param newValue The new Germplasm object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Germplasm object on the BrAPI server.
    #' @param value The updated Germplasm object to be sent to the server
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
