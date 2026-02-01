# AlleleMatrix
## R6 Class: AlleleMatrix
#' The AlleleMatrix class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the AlleleMatrix entity
#' @title AlleleMatrix Class
#' @docType class
#' @description The AlleleMatrix class handles calling the BraAPI server and is a wrapper class around httr2 functionality for
#' the AlleleMatrix entity
#' @family generated
#' @keywords generated
#' @import R6
#' @importFrom glue glue
#' @export
AlleleMatrix <- R6Class(
  "AlleleMatrix",
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
    #' Searches for AlleleMatrix objects on the BrAPI server. The server
    #' may return the paged results or a search result ID for later retrieval
    #' @param pagination Pagination for the matrix
    #' @param preview Default Value = false &lt;br/&gt; If &#39;preview&#39; is set to true, then the server should only return the lists of &#39;callSetDbIds&#39;,  &#39;variantDbIds&#39;, and &#39;variantSetDbIds&#39;.
    #' @param dataMatrixNames `dataMatrixNames` is a list of names (ie &#39;Genotype&#39;, &#39;Read Depth&#39; etc).
    #' @param dataMatrixAbbreviations `dataMatrixAbbreviations` is a comma seperated list of abbreviations (ie &#39;GT&#39;, &#39;RD&#39; etc).
    #' @param positionRanges The postion range to search &lt;br/&gt; Uses the format &quot;&lt;chrom&gt;:&lt;start&gt;-&lt;end&gt;&quot; where &lt;chrom&gt; is the chromosome name, &lt;start&gt; is  the starting position of the range, and &lt;end&gt; is the ending position of the range
    #' @param germplasmNames A list of human readable `Germplasm` names
    #' @param germplasmPUIs A list of permanent unique identifiers associated with `Germplasm`
    #' @param germplasmDbIds A list of IDs which uniquely identify `Germplasm` within the given database server
    #' @param sampleDbIds A list of IDs which uniquely identify `Samples` within the given database server
    #' @param callSetDbIds A list of IDs which uniquely identify `CallSets` within the given database server
    #' @param variantDbIds A list of IDs which uniquely identify `Variants` within the given database server
    #' @param variantSetDbIds A list of IDs which uniquely identify `VariantSets` within the given database server
    #' @param expandHomozygotes Should homozygotes be expanded (true) or collapsed into a single occurrence (false)
    #' @param sepPhased The string used as a separator for phased allele calls.
    #' @param sepUnphased The string used as a separator for unphased allele calls.
    #' @param unknownString The string used as a representation for missing data.
    #' using the searchResult function
    #' @param page The page number of results to return, starting from 0
    #' @param pageSize The maximum number of results to return per page
    #' @return returns the search result or an ID to the results.
    #'
    search = function(
        pagination = NULL,
        preview = NULL,
        dataMatrixNames = NULL,
        dataMatrixAbbreviations = NULL,
        positionRanges = NULL,
        germplasmNames = NULL,
        germplasmPUIs = NULL,
        germplasmDbIds = NULL,
        sampleDbIds = NULL,
        callSetDbIds = NULL,
        variantDbIds = NULL,
        variantSetDbIds = NULL,
        expandHomozygotes = NULL,
        sepPhased = NULL,
        sepUnphased = NULL,
        unknownString = NULL,
        page = 0,
        pageSize = 1000) {
      queryParams <- list()
      
      if (!is.null(pagination)) {
        queryParams$pagination <- to_list(pagination)
      }
      if (!is.null(preview)) {
        queryParams$preview <- to_list(preview)
      }
      if (!is.null(dataMatrixNames)) {
        queryParams$dataMatrixNames <- to_list(dataMatrixNames)
      }
      if (!is.null(dataMatrixAbbreviations)) {
        queryParams$dataMatrixAbbreviations <- to_list(dataMatrixAbbreviations)
      }
      if (!is.null(positionRanges)) {
        queryParams$positionRanges <- to_list(positionRanges)
      }
      if (!is.null(germplasmNames)) {
        queryParams$germplasmNames <- to_list(germplasmNames)
      }
      if (!is.null(germplasmPUIs)) {
        queryParams$germplasmPUIs <- to_list(germplasmPUIs)
      }
      if (!is.null(germplasmDbIds)) {
        queryParams$germplasmDbIds <- to_list(germplasmDbIds)
      }
      if (!is.null(sampleDbIds)) {
        queryParams$sampleDbIds <- to_list(sampleDbIds)
      }
      if (!is.null(callSetDbIds)) {
        queryParams$callSetDbIds <- to_list(callSetDbIds)
      }
      if (!is.null(variantDbIds)) {
        queryParams$variantDbIds <- to_list(variantDbIds)
      }
      if (!is.null(variantSetDbIds)) {
        queryParams$variantSetDbIds <- to_list(variantSetDbIds)
      }
      if (!is.null(expandHomozygotes)) {
        queryParams$expandHomozygotes <- to_list(expandHomozygotes)
      }
      if (!is.null(sepPhased)) {
        queryParams$sepPhased <- to_list(sepPhased)
      }
      if (!is.null(sepUnphased)) {
        queryParams$sepUnphased <- to_list(sepUnphased)
      }
      if (!is.null(unknownString)) {
        queryParams$unknownString <- to_list(unknownString)
      }
      private$.client$perform_post_request("/search/allelematrix", queryParams, page, pageSize)
    },
    #' Get the result of a search for AlleleMatrix objects on the BrAPI server. If the server
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

      private$.client$perform_get_request(paste("/search/allelematrix", id, sep = '/'))
    }
  ),
  private = list(
    .client = NULL
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
