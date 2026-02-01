# PedigreeNode
## R6 Class: PedigreeNodes
#' The PedigreeNodes class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the PedigreeNode entity
#' @title PedigreeNodes Class
#' @docType class
#' @description The PedigreeNodes class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the PedigreeNode entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
PedigreeNodes <- R6Class(
  "PedigreeNodes",
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
    #' Gets a list PedigreeNode objects from the BrAPI server
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
    #' @param includeParents If this parameter is true, include the array of parents in the response
    #' @param includeSiblings If this parameter is true, include the array of siblings in the response
    #' @param includeProgeny If this parameter is true, include the array of progeny in the response
    #' @param includeFullTree If this parameter is true, recursively include ALL of the nodes available in this pedigree tree
    #' @param pedigreeDepth Recursively include this number of levels up the tree in the response (parents, grand-parents, great-grand-parents, etc)
    #' @param progenyDepth Recursively include this number of levels down the tree in the response (children, grand-children, great-grand-children, etc)
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of PedigreeNode objects.
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
        includeParents = NULL,
        includeSiblings = NULL,
        includeProgeny = NULL,
        includeFullTree = NULL,
        pedigreeDepth = NULL,
        progenyDepth = NULL,
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
      if (!is.null(includeParents)) {
        queryParams$includeParent <- includeParents
      }
      if (!is.null(includeSiblings)) {
        queryParams$includeSibling <- includeSiblings
      }
      if (!is.null(includeProgeny)) {
        queryParams$includeProgeny <- includeProgeny
      }
      if (!is.null(includeFullTree)) {
        queryParams$includeFullTree <- includeFullTree
      }
      if (!is.null(pedigreeDepth)) {
        queryParams$pedigreeDepth <- pedigreeDepth
      }
      if (!is.null(progenyDepth)) {
        queryParams$progenyDepth <- progenyDepth
      }
      private$.client$perform_get_request("/pedigree", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for PedigreeNode objects on the BrAPI server. The server
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
    #' @param includeParents If this parameter is true, include the array of parents in the response
    #' @param includeSiblings If this parameter is true, include the array of siblings in the response
    #' @param includeProgeny If this parameter is true, include the array of progeny in the response
    #' @param includeFullTree If this parameter is true, recursively include ALL of the nodes available in this pedigree tree
    #' @param pedigreeDepth Recursively include this number of levels up the tree in the response (parents, grand-parents, great-grand-parents, etc)
    #' @param progenyDepth Recursively include this number of levels down the tree in the response (children, grand-children, great-grand-children, etc)
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
        includeParents = NULL,
        includeSiblings = NULL,
        includeProgeny = NULL,
        includeFullTree = NULL,
        pedigreeDepth = NULL,
        progenyDepth = NULL,
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
      if (!is.null(includeParents)) {
        queryParams$includeParents <- to_list(includeParents)
      }
      if (!is.null(includeSiblings)) {
        queryParams$includeSiblings <- to_list(includeSiblings)
      }
      if (!is.null(includeProgeny)) {
        queryParams$includeProgeny <- to_list(includeProgeny)
      }
      if (!is.null(includeFullTree)) {
        queryParams$includeFullTree <- to_list(includeFullTree)
      }
      if (!is.null(pedigreeDepth)) {
        queryParams$pedigreeDepth <- to_list(pedigreeDepth)
      }
      if (!is.null(progenyDepth)) {
        queryParams$progenyDepth <- to_list(progenyDepth)
      }
      private$.client$perform_post_request("/search/pedigree", queryParams, page, pageSize)
    },
    #' Get the result of a search for PedigreeNode objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/pedigree", id, sep = '/'))
    },
    #' @description
    #' Creates a PedigreeNode object on the BrAPI server.
    #' @param newValue The new PedigreeNode object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
