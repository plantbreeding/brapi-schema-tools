# GermplasmAttribute
## R6 Class: GermplasmAttributes
#' The GermplasmAttributes class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the GermplasmAttribute entity
#' @title GermplasmAttributes Class
#' @docType class
#' @description The GermplasmAttributes class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the GermplasmAttribute entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
GermplasmAttributes <- R6Class(
  "GermplasmAttributes",
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
    #' Gets a GermplasmAttribute object by DbId from the BrAPI server
    #' @param id The DbIid of the GermplasmAttribute to be returned
    #' @return returns a GermplasmAttribute object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/attributes", id, sep = '/'))
    },
    #' @description
    #' Gets a list GermplasmAttribute objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param studyDbId **Deprecated in v2.
    #' @param ontologyDbIds List of ontology IDs to search for
    #' @param methodDbIds List of methods to filter search results
    #' @param methodNames Human readable name for the method &lt;br/&gt;MIAPPE V1.
    #' @param methodPUIs The Permanent Unique Identifier of a Method, usually in the form of a URI
    #' @param scaleDbIds The unique identifier for a Scale
    #' @param scaleNames Name of the scale &lt;br/&gt;MIAPPE V1.
    #' @param scalePUIs The Permanent Unique Identifier of a Scale, usually in the form of a URI
    #' @param dataTypes List of scale data types to filter search results
    #' @param traitClasses List of trait classes to filter search results
    #' @param traitDbIds The unique identifier for a Trait
    #' @param traitNames The human readable name of a trait &lt;br/&gt;MIAPPE V1.
    #' @param traitPUIs The Permanent Unique Identifier of a Trait, usually in the form of a URI
    #' @param traitAttributes A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the attribute is the observed feature (or characteristic) of the entity e.
    #' @param traitAttributePUIs The Permanent Unique Identifier of a Trait Attribute, usually in the form of a URI &lt;br/&gt;A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the attribute is the observed feature (or characteristic) of the entity e.
    #' @param traitEntities A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the entity is the part of the plant that the trait refers to e.
    #' @param traitEntityPUIs The Permanent Unique Identifier of a Trait Entity, usually in the form of a URI &lt;br/&gt;A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the entity is the part of the plant that the trait refers to e.
    #' @param attributeDbIds List of Germplasm Attribute IDs to search for
    #' @param attributeNames List of human readable Germplasm Attribute names to search for
    #' @param attributePUIs The Permanent Unique Identifier of an Attribute, usually in the form of a URI
    #' @param attributeCategories General category for the attribute.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of GermplasmAttribute objects.
    #'
    getAll = function(
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        studyDbId = NULL,
        ontologyDbIds = NULL,
        methodDbIds = NULL,
        methodNames = NULL,
        methodPUIs = NULL,
        scaleDbIds = NULL,
        scaleNames = NULL,
        scalePUIs = NULL,
        dataTypes = NULL,
        traitClasses = NULL,
        traitDbIds = NULL,
        traitNames = NULL,
        traitPUIs = NULL,
        traitAttributes = NULL,
        traitAttributePUIs = NULL,
        traitEntities = NULL,
        traitEntityPUIs = NULL,
        attributeDbIds = NULL,
        attributeNames = NULL,
        attributePUIs = NULL,
        attributeCategories = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(germplasmDbIds)) {
        queryParams$germplasmDbId <- germplasmDbIds
      }
      if (!is.null(germplasmNames)) {
        queryParams$germplasmName <- germplasmNames
      }
      if (!is.null(commonCropNames)) {
        queryParams$commonCropName <- commonCropNames
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
      if (!is.null(studyDbId)) {
        queryParams$studyDbId <- studyDbId
      }
      if (!is.null(ontologyDbIds)) {
        queryParams$ontologyDbId <- ontologyDbIds
      }
      if (!is.null(methodDbIds)) {
        queryParams$methodDbId <- methodDbIds
      }
      if (!is.null(methodNames)) {
        queryParams$methodName <- methodNames
      }
      if (!is.null(methodPUIs)) {
        queryParams$methodPUI <- methodPUIs
      }
      if (!is.null(scaleDbIds)) {
        queryParams$scaleDbId <- scaleDbIds
      }
      if (!is.null(scaleNames)) {
        queryParams$scaleName <- scaleNames
      }
      if (!is.null(scalePUIs)) {
        queryParams$scalePUI <- scalePUIs
      }
      if (!is.null(dataTypes)) {
        queryParams$dataType <- dataTypes
      }
      if (!is.null(traitClasses)) {
        queryParams$traitClass <- traitClasses
      }
      if (!is.null(traitDbIds)) {
        queryParams$traitDbId <- traitDbIds
      }
      if (!is.null(traitNames)) {
        queryParams$traitName <- traitNames
      }
      if (!is.null(traitPUIs)) {
        queryParams$traitPUI <- traitPUIs
      }
      if (!is.null(traitAttributes)) {
        queryParams$traitAttribute <- traitAttributes
      }
      if (!is.null(traitAttributePUIs)) {
        queryParams$traitAttributePUI <- traitAttributePUIs
      }
      if (!is.null(traitEntities)) {
        queryParams$traitEntity <- traitEntities
      }
      if (!is.null(traitEntityPUIs)) {
        queryParams$traitEntityPUI <- traitEntityPUIs
      }
      if (!is.null(attributeDbIds)) {
        queryParams$attributeDbId <- attributeDbIds
      }
      if (!is.null(attributeNames)) {
        queryParams$attributeName <- attributeNames
      }
      if (!is.null(attributePUIs)) {
        queryParams$attributePUI <- attributePUIs
      }
      if (!is.null(attributeCategories)) {
        queryParams$attributeCategory <- attributeCategories
      }
      private$.client$perform_get_request("/attributes", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for GermplasmAttribute objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param germplasmDbIds List of IDs which uniquely identify germplasm to search for
    #' @param germplasmNames List of human readable names to identify germplasm to search for
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param studyDbIds List of study identifiers to search for
    #' @param studyNames List of study names to filter search results
    #' @param trialDbIds The ID which uniquely identifies a trial to search for
    #' @param trialNames The human readable name of a trial to search for
    #' @param studyDbId **Deprecated in v2.
    #' @param ontologyDbIds List of ontology IDs to search for
    #' @param methodDbIds List of methods to filter search results
    #' @param methodNames Human readable name for the method &lt;br/&gt;MIAPPE V1.
    #' @param methodPUIs The Permanent Unique Identifier of a Method, usually in the form of a URI
    #' @param scaleDbIds The unique identifier for a Scale
    #' @param scaleNames Name of the scale &lt;br/&gt;MIAPPE V1.
    #' @param scalePUIs The Permanent Unique Identifier of a Scale, usually in the form of a URI
    #' @param dataTypes List of scale data types to filter search results
    #' @param traitClasses List of trait classes to filter search results
    #' @param traitDbIds The unique identifier for a Trait
    #' @param traitNames The human readable name of a trait &lt;br/&gt;MIAPPE V1.
    #' @param traitPUIs The Permanent Unique Identifier of a Trait, usually in the form of a URI
    #' @param traitAttributes A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the attribute is the observed feature (or characteristic) of the entity e.
    #' @param traitAttributePUIs The Permanent Unique Identifier of a Trait Attribute, usually in the form of a URI &lt;br/&gt;A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the attribute is the observed feature (or characteristic) of the entity e.
    #' @param traitEntities A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the entity is the part of the plant that the trait refers to e.
    #' @param traitEntityPUIs The Permanent Unique Identifier of a Trait Entity, usually in the form of a URI &lt;br/&gt;A trait can be decomposed as &quot;Trait&quot; = &quot;Entity&quot; + &quot;Attribute&quot;, the entity is the part of the plant that the trait refers to e.
    #' @param attributeDbIds List of Germplasm Attribute IDs to search for
    #' @param attributeNames List of human readable Germplasm Attribute names to search for
    #' @param attributePUIs The Permanent Unique Identifier of an Attribute, usually in the form of a URI
    #' @param attributeCategories General category for the attribute.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        germplasmDbIds = NULL,
        germplasmNames = NULL,
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        studyDbIds = NULL,
        studyNames = NULL,
        trialDbIds = NULL,
        trialNames = NULL,
        studyDbId = NULL,
        ontologyDbIds = NULL,
        methodDbIds = NULL,
        methodNames = NULL,
        methodPUIs = NULL,
        scaleDbIds = NULL,
        scaleNames = NULL,
        scalePUIs = NULL,
        dataTypes = NULL,
        traitClasses = NULL,
        traitDbIds = NULL,
        traitNames = NULL,
        traitPUIs = NULL,
        traitAttributes = NULL,
        traitAttributePUIs = NULL,
        traitEntities = NULL,
        traitEntityPUIs = NULL,
        attributeDbIds = NULL,
        attributeNames = NULL,
        attributePUIs = NULL,
        attributeCategories = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(germplasmDbIds)) {
        queryParams$germplasmDbIds <- to_list(germplasmDbIds)
      }
      if (!is.null(germplasmNames)) {
        queryParams$germplasmNames <- to_list(germplasmNames)
      }
      if (!is.null(commonCropNames)) {
        queryParams$commonCropNames <- to_list(commonCropNames)
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
      if (!is.null(studyDbId)) {
        queryParams$studyDbId <- to_list(studyDbId)
      }
      if (!is.null(ontologyDbIds)) {
        queryParams$ontologyDbIds <- to_list(ontologyDbIds)
      }
      if (!is.null(methodDbIds)) {
        queryParams$methodDbIds <- to_list(methodDbIds)
      }
      if (!is.null(methodNames)) {
        queryParams$methodNames <- to_list(methodNames)
      }
      if (!is.null(methodPUIs)) {
        queryParams$methodPUIs <- to_list(methodPUIs)
      }
      if (!is.null(scaleDbIds)) {
        queryParams$scaleDbIds <- to_list(scaleDbIds)
      }
      if (!is.null(scaleNames)) {
        queryParams$scaleNames <- to_list(scaleNames)
      }
      if (!is.null(scalePUIs)) {
        queryParams$scalePUIs <- to_list(scalePUIs)
      }
      if (!is.null(dataTypes)) {
        queryParams$dataTypes <- to_list(dataTypes)
      }
      if (!is.null(traitClasses)) {
        queryParams$traitClasses <- to_list(traitClasses)
      }
      if (!is.null(traitDbIds)) {
        queryParams$traitDbIds <- to_list(traitDbIds)
      }
      if (!is.null(traitNames)) {
        queryParams$traitNames <- to_list(traitNames)
      }
      if (!is.null(traitPUIs)) {
        queryParams$traitPUIs <- to_list(traitPUIs)
      }
      if (!is.null(traitAttributes)) {
        queryParams$traitAttributes <- to_list(traitAttributes)
      }
      if (!is.null(traitAttributePUIs)) {
        queryParams$traitAttributePUIs <- to_list(traitAttributePUIs)
      }
      if (!is.null(traitEntities)) {
        queryParams$traitEntities <- to_list(traitEntities)
      }
      if (!is.null(traitEntityPUIs)) {
        queryParams$traitEntityPUIs <- to_list(traitEntityPUIs)
      }
      if (!is.null(attributeDbIds)) {
        queryParams$attributeDbIds <- to_list(attributeDbIds)
      }
      if (!is.null(attributeNames)) {
        queryParams$attributeNames <- to_list(attributeNames)
      }
      if (!is.null(attributePUIs)) {
        queryParams$attributePUIs <- to_list(attributePUIs)
      }
      if (!is.null(attributeCategories)) {
        queryParams$attributeCategories <- to_list(attributeCategories)
      }
      private$.client$perform_post_request("/search/attributes", queryParams, page, pageSize)
    },
    #' Get the result of a search for GermplasmAttribute objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/attributes", id, sep = '/'))
    },
    #' @description
    #' Creates a GermplasmAttribute object on the BrAPI server.
    #' @param newValue The new GermplasmAttribute object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a GermplasmAttribute object on the BrAPI server.
    #' @param value The updated GermplasmAttribute object to be sent to the server
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
