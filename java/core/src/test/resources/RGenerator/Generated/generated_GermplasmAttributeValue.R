# GermplasmAttributeValue
## R6 Class: GermplasmAttributeValues
#' The GermplasmAttributeValues class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the GermplasmAttributeValue entity
#' @title GermplasmAttributeValues Class
#' @docType class
#' @description The GermplasmAttributeValues class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the GermplasmAttributeValue entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
GermplasmAttributeValues <- R6Class(
  "GermplasmAttributeValues",
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
    #' Gets a GermplasmAttributeValue object by DbId from the BrAPI server
    #' @param id The DbIid of the GermplasmAttributeValue to be returned
    #' @return returns a GermplasmAttributeValue object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/attributevalues", id, sep = '/'))
    },
    #' @description
    #' Gets a list GermplasmAttributeValue objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param attributeValueDbIds List of Germplasm Attribute Value IDs to search for
    #' @param attributeDbIds List of Germplasm Attribute IDs to search for
    #' @param attributeNames List of human readable Germplasm Attribute names to search for
    #' @param ontologyDbIds List of ontology IDs to search for
    #' @param methodDbIds List of methods to filter search results
    #' @param scaleDbIds List of scales to filter search results
    #' @param traitDbIds List of trait unique ID to filter search results
    #' @param traitClasses List of trait classes to filter search results
    #' @param dataTypes List of scale data types to filter search results
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of GermplasmAttributeValue objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        attributeValueDbIds = NULL,
        attributeDbIds = NULL,
        attributeNames = NULL,
        ontologyDbIds = NULL,
        methodDbIds = NULL,
        scaleDbIds = NULL,
        traitDbIds = NULL,
        traitClasses = NULL,
        dataTypes = NULL,
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
      if (!is.null(attributeValueDbIds)) {
        queryParams$attributeValueDbId <- attributeValueDbIds
      }
      if (!is.null(attributeDbIds)) {
        queryParams$attributeDbId <- attributeDbIds
      }
      if (!is.null(attributeNames)) {
        queryParams$attributeName <- attributeNames
      }
      if (!is.null(ontologyDbIds)) {
        queryParams$ontologyDbId <- ontologyDbIds
      }
      if (!is.null(methodDbIds)) {
        queryParams$methodDbId <- methodDbIds
      }
      if (!is.null(scaleDbIds)) {
        queryParams$scaleDbId <- scaleDbIds
      }
      if (!is.null(traitDbIds)) {
        queryParams$traitDbId <- traitDbIds
      }
      if (!is.null(traitClasses)) {
        queryParams$traitClass <- traitClasses
      }
      if (!is.null(dataTypes)) {
        queryParams$dataType <- dataTypes
      }
      private$.client$perform_get_request("/attributevalues", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for GermplasmAttributeValue objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param attributeValueDbIds List of Germplasm Attribute Value IDs to search for
    #' @param attributeDbIds List of Germplasm Attribute IDs to search for
    #' @param attributeNames List of human readable Germplasm Attribute names to search for
    #' @param ontologyDbIds List of ontology IDs to search for
    #' @param methodDbIds List of methods to filter search results
    #' @param scaleDbIds List of scales to filter search results
    #' @param traitDbIds List of trait unique ID to filter search results
    #' @param traitClasses List of trait classes to filter search results
    #' @param dataTypes List of scale data types to filter search results
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
        attributeValueDbIds = NULL,
        attributeDbIds = NULL,
        attributeNames = NULL,
        ontologyDbIds = NULL,
        methodDbIds = NULL,
        scaleDbIds = NULL,
        traitDbIds = NULL,
        traitClasses = NULL,
        dataTypes = NULL,
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
      if (!is.null(attributeValueDbIds)) {
        queryParams$attributeValueDbIds <- to_list(attributeValueDbIds)
      }
      if (!is.null(attributeDbIds)) {
        queryParams$attributeDbIds <- to_list(attributeDbIds)
      }
      if (!is.null(attributeNames)) {
        queryParams$attributeNames <- to_list(attributeNames)
      }
      if (!is.null(ontologyDbIds)) {
        queryParams$ontologyDbIds <- to_list(ontologyDbIds)
      }
      if (!is.null(methodDbIds)) {
        queryParams$methodDbIds <- to_list(methodDbIds)
      }
      if (!is.null(scaleDbIds)) {
        queryParams$scaleDbIds <- to_list(scaleDbIds)
      }
      if (!is.null(traitDbIds)) {
        queryParams$traitDbIds <- to_list(traitDbIds)
      }
      if (!is.null(traitClasses)) {
        queryParams$traitClasses <- to_list(traitClasses)
      }
      if (!is.null(dataTypes)) {
        queryParams$dataTypes <- to_list(dataTypes)
      }
      private$.client$perform_post_request("/search/attributevalues", queryParams, page, pageSize)
    },
    #' Get the result of a search for GermplasmAttributeValue objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/attributevalues", id, sep = '/'))
    },
    #' @description
    #' Creates a GermplasmAttributeValue object on the BrAPI server.
    #' @param newValue The new GermplasmAttributeValue object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a GermplasmAttributeValue object on the BrAPI server.
    #' @param value The updated GermplasmAttributeValue object to be sent to the server
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
