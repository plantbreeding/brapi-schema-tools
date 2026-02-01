# Person
## R6 Class: People
#' The People class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Person entity
#' @title People Class
#' @docType class
#' @description The People class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the Person entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
People <- R6Class(
  "People",
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
    #' Gets a Person object by DbId from the BrAPI server
    #' @param id The DbIid of the Person to be returned
    #' @return returns a Person object by ID.
    #'
    get = function(id) {
      if (is.null(id)) {
       stop("id argument must be provided")
      }

      private$.client$perform_get_request(paste("/people", id, sep = '/'))
    },
    #' @description
    #' Gets a list Person objects from the BrAPI server
    #' Note the filtering arguments are all optional and can be combined to filter the results
    #' The value of the filters an be a single value or a vector of values
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param emailAddresses email address for this person
    #' @param firstNames Persons first name
    #' @param lastNames Persons last name
    #' @param mailingAddresses physical address of this person
    #' @param middleNames Persons middle name
    #' @param personDbIds Unique ID for this person
    #' @param phoneNumbers phone number of this person
    #' @param userIDs A systems user ID associated with this person.
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns a paged and filtered list of Person objects.
    #'
    getAll = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        emailAddresses = NULL,
        firstNames = NULL,
        lastNames = NULL,
        mailingAddresses = NULL,
        middleNames = NULL,
        personDbIds = NULL,
        phoneNumbers = NULL,
        userIDs = NULL,
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
      if (!is.null(emailAddresses)) {
        queryParams$emailAddress <- emailAddresses
      }
      if (!is.null(firstNames)) {
        queryParams$firstName <- firstNames
      }
      if (!is.null(lastNames)) {
        queryParams$lastName <- lastNames
      }
      if (!is.null(mailingAddresses)) {
        queryParams$mailingAddress <- mailingAddresses
      }
      if (!is.null(middleNames)) {
        queryParams$middleName <- middleNames
      }
      if (!is.null(personDbIds)) {
        queryParams$personDbId <- personDbIds
      }
      if (!is.null(phoneNumbers)) {
        queryParams$phoneNumber <- phoneNumbers
      }
      if (!is.null(userIDs)) {
        queryParams$userID <- userIDs
      }
      private$.client$perform_get_request("/people", queryParams, page, pageSize)
    },
    #' @description
    #' Searches for Person objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param commonCropNames The BrAPI Common Crop Name is the simple, generalized, widely accepted name of the organism being researched.
    #' @param programDbIds A BrAPI Program represents the high level organization or group who is responsible for conducting trials and studies.
    #' @param programNames Use this parameter to only return results associated with the given program names.
    #' @param emailAddresses email address for this person
    #' @param firstNames Persons first name
    #' @param lastNames Persons last name
    #' @param mailingAddresses physical address of this person
    #' @param middleNames Persons middle name
    #' @param personDbIds Unique ID for this person
    #' @param phoneNumbers phone number of this person
    #' @param userIDs A systems user ID associated with this person.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        commonCropNames = NULL,
        programDbIds = NULL,
        programNames = NULL,
        emailAddresses = NULL,
        firstNames = NULL,
        lastNames = NULL,
        mailingAddresses = NULL,
        middleNames = NULL,
        personDbIds = NULL,
        phoneNumbers = NULL,
        userIDs = NULL,
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
      if (!is.null(emailAddresses)) {
        queryParams$emailAddresses <- to_list(emailAddresses)
      }
      if (!is.null(firstNames)) {
        queryParams$firstNames <- to_list(firstNames)
      }
      if (!is.null(lastNames)) {
        queryParams$lastNames <- to_list(lastNames)
      }
      if (!is.null(mailingAddresses)) {
        queryParams$mailingAddresses <- to_list(mailingAddresses)
      }
      if (!is.null(middleNames)) {
        queryParams$middleNames <- to_list(middleNames)
      }
      if (!is.null(personDbIds)) {
        queryParams$personDbIds <- to_list(personDbIds)
      }
      if (!is.null(phoneNumbers)) {
        queryParams$phoneNumbers <- to_list(phoneNumbers)
      }
      if (!is.null(userIDs)) {
        queryParams$userIDs <- to_list(userIDs)
      }
      private$.client$perform_post_request("/search/people", queryParams, page, pageSize)
    },
    #' Get the result of a search for Person objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/people", id, sep = '/'))
    },
    #' @description
    #' Creates a Person object on the BrAPI server.
    #' @param newValue The new Person object to be created
    #'
    create = function(newValue) {
        stop("Create not yet implemented")
    },
    #' @description
    #' Creates a Person object on the BrAPI server.
    #' @param value The updated Person object to be sent to the server
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
