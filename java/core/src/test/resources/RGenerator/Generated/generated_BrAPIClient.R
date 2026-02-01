# BrAPIClient
#' The BrAPIClient class handles the configuration of the BrAPIClient connection and
#' is a wrapper class around httr2 functionality
#' @title BrAPIClient Class
#' @docType class
#' @description The BrAPIClient class handles the configuration of the BrAPI connection and
#' is a wrapper class around httr2 functionality
#' @family generated
#' @keywords generated
#' @import R6
#' @export
BrAPIClient <- R6Class(
  "BrAPIClient",
  inherit = BaseBrAPIClient,
  active = list(
  
    #' @field alleleMatrix
    #' Get the AlleleMatrix R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    alleleMatrix = function() {
      if (is.null(private$.alleleMatrix)) {
        if (private$.verbosity > 0) {
          message("Creating AlleleMatrix R6 class object ")
        }
        private$.alleleMatrix <- (AlleleMatrix$new(self))
      }

      return(private$.alleleMatrix)
    },
    #' @field breedingMethods
    #' Get the BreedingMethods R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    breedingMethods = function() {
      if (is.null(private$.breedingMethods)) {
        if (private$.verbosity > 0) {
          message("Creating BreedingMethods R6 class object ")
        }
        private$.breedingMethods <- (BreedingMethods$new(self))
      }

      return(private$.breedingMethods)
    },
    #' @field calls
    #' Get the Calls R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    calls = function() {
      if (is.null(private$.calls)) {
        if (private$.verbosity > 0) {
          message("Creating Calls R6 class object ")
        }
        private$.calls <- (Calls$new(self))
      }

      return(private$.calls)
    },
    #' @field callSets
    #' Get the CallSets R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    callSets = function() {
      if (is.null(private$.callSets)) {
        if (private$.verbosity > 0) {
          message("Creating CallSets R6 class object ")
        }
        private$.callSets <- (CallSets$new(self))
      }

      return(private$.callSets)
    },
    #' @field crosses
    #' Get the Crosses R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    crosses = function() {
      if (is.null(private$.crosses)) {
        if (private$.verbosity > 0) {
          message("Creating Crosses R6 class object ")
        }
        private$.crosses <- (Crosses$new(self))
      }

      return(private$.crosses)
    },
    #' @field crossingProjects
    #' Get the CrossingProjects R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    crossingProjects = function() {
      if (is.null(private$.crossingProjects)) {
        if (private$.verbosity > 0) {
          message("Creating CrossingProjects R6 class object ")
        }
        private$.crossingProjects <- (CrossingProjects$new(self))
      }

      return(private$.crossingProjects)
    },
    #' @field events
    #' Get the Events R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    events = function() {
      if (is.null(private$.events)) {
        if (private$.verbosity > 0) {
          message("Creating Events R6 class object ")
        }
        private$.events <- (Events$new(self))
      }

      return(private$.events)
    },
    #' @field genomeMaps
    #' Get the GenomeMaps R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    genomeMaps = function() {
      if (is.null(private$.genomeMaps)) {
        if (private$.verbosity > 0) {
          message("Creating GenomeMaps R6 class object ")
        }
        private$.genomeMaps <- (GenomeMaps$new(self))
      }

      return(private$.genomeMaps)
    },
    #' @field germplasm
    #' Get the Germplasm R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    germplasm = function() {
      if (is.null(private$.germplasm)) {
        if (private$.verbosity > 0) {
          message("Creating Germplasm R6 class object ")
        }
        private$.germplasm <- (Germplasm$new(self))
      }

      return(private$.germplasm)
    },
    #' @field germplasmAttributes
    #' Get the GermplasmAttributes R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    germplasmAttributes = function() {
      if (is.null(private$.germplasmAttributes)) {
        if (private$.verbosity > 0) {
          message("Creating GermplasmAttributes R6 class object ")
        }
        private$.germplasmAttributes <- (GermplasmAttributes$new(self))
      }

      return(private$.germplasmAttributes)
    },
    #' @field germplasmAttributeValues
    #' Get the GermplasmAttributeValues R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    germplasmAttributeValues = function() {
      if (is.null(private$.germplasmAttributeValues)) {
        if (private$.verbosity > 0) {
          message("Creating GermplasmAttributeValues R6 class object ")
        }
        private$.germplasmAttributeValues <- (GermplasmAttributeValues$new(self))
      }

      return(private$.germplasmAttributeValues)
    },
    #' @field images
    #' Get the Images R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    images = function() {
      if (is.null(private$.images)) {
        if (private$.verbosity > 0) {
          message("Creating Images R6 class object ")
        }
        private$.images <- (Images$new(self))
      }

      return(private$.images)
    },
    #' @field lists
    #' Get the Lists R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    lists = function() {
      if (is.null(private$.lists)) {
        if (private$.verbosity > 0) {
          message("Creating Lists R6 class object ")
        }
        private$.lists <- (Lists$new(self))
      }

      return(private$.lists)
    },
    #' @field locations
    #' Get the Locations R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    locations = function() {
      if (is.null(private$.locations)) {
        if (private$.verbosity > 0) {
          message("Creating Locations R6 class object ")
        }
        private$.locations <- (Locations$new(self))
      }

      return(private$.locations)
    },
    #' @field markerPositions
    #' Get the MarkerPositions R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    markerPositions = function() {
      if (is.null(private$.markerPositions)) {
        if (private$.verbosity > 0) {
          message("Creating MarkerPositions R6 class object ")
        }
        private$.markerPositions <- (MarkerPositions$new(self))
      }

      return(private$.markerPositions)
    },
    #' @field methods
    #' Get the Methods R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    methods = function() {
      if (is.null(private$.methods)) {
        if (private$.verbosity > 0) {
          message("Creating Methods R6 class object ")
        }
        private$.methods <- (Methods$new(self))
      }

      return(private$.methods)
    },
    #' @field observations
    #' Get the Observations R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    observations = function() {
      if (is.null(private$.observations)) {
        if (private$.verbosity > 0) {
          message("Creating Observations R6 class object ")
        }
        private$.observations <- (Observations$new(self))
      }

      return(private$.observations)
    },
    #' @field observationUnits
    #' Get the ObservationUnits R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    observationUnits = function() {
      if (is.null(private$.observationUnits)) {
        if (private$.verbosity > 0) {
          message("Creating ObservationUnits R6 class object ")
        }
        private$.observationUnits <- (ObservationUnits$new(self))
      }

      return(private$.observationUnits)
    },
    #' @field observationVariables
    #' Get the ObservationVariables R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    observationVariables = function() {
      if (is.null(private$.observationVariables)) {
        if (private$.verbosity > 0) {
          message("Creating ObservationVariables R6 class object ")
        }
        private$.observationVariables <- (ObservationVariables$new(self))
      }

      return(private$.observationVariables)
    },
    #' @field ontologies
    #' Get the Ontologies R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    ontologies = function() {
      if (is.null(private$.ontologies)) {
        if (private$.verbosity > 0) {
          message("Creating Ontologies R6 class object ")
        }
        private$.ontologies <- (Ontologies$new(self))
      }

      return(private$.ontologies)
    },
    #' @field pedigreeNodes
    #' Get the PedigreeNodes R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    pedigreeNodes = function() {
      if (is.null(private$.pedigreeNodes)) {
        if (private$.verbosity > 0) {
          message("Creating PedigreeNodes R6 class object ")
        }
        private$.pedigreeNodes <- (PedigreeNodes$new(self))
      }

      return(private$.pedigreeNodes)
    },
    #' @field people
    #' Get the People R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    people = function() {
      if (is.null(private$.people)) {
        if (private$.verbosity > 0) {
          message("Creating People R6 class object ")
        }
        private$.people <- (People$new(self))
      }

      return(private$.people)
    },
    #' @field plannedCrosses
    #' Get the PlannedCrosses R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    plannedCrosses = function() {
      if (is.null(private$.plannedCrosses)) {
        if (private$.verbosity > 0) {
          message("Creating PlannedCrosses R6 class object ")
        }
        private$.plannedCrosses <- (PlannedCrosses$new(self))
      }

      return(private$.plannedCrosses)
    },
    #' @field plates
    #' Get the Plates R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    plates = function() {
      if (is.null(private$.plates)) {
        if (private$.verbosity > 0) {
          message("Creating Plates R6 class object ")
        }
        private$.plates <- (Plates$new(self))
      }

      return(private$.plates)
    },
    #' @field programs
    #' Get the Programs R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    programs = function() {
      if (is.null(private$.programs)) {
        if (private$.verbosity > 0) {
          message("Creating Programs R6 class object ")
        }
        private$.programs <- (Programs$new(self))
      }

      return(private$.programs)
    },
    #' @field references
    #' Get the References R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    references = function() {
      if (is.null(private$.references)) {
        if (private$.verbosity > 0) {
          message("Creating References R6 class object ")
        }
        private$.references <- (References$new(self))
      }

      return(private$.references)
    },
    #' @field referenceSets
    #' Get the ReferenceSets R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    referenceSets = function() {
      if (is.null(private$.referenceSets)) {
        if (private$.verbosity > 0) {
          message("Creating ReferenceSets R6 class object ")
        }
        private$.referenceSets <- (ReferenceSets$new(self))
      }

      return(private$.referenceSets)
    },
    #' @field samples
    #' Get the Samples R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    samples = function() {
      if (is.null(private$.samples)) {
        if (private$.verbosity > 0) {
          message("Creating Samples R6 class object ")
        }
        private$.samples <- (Samples$new(self))
      }

      return(private$.samples)
    },
    #' @field scales
    #' Get the Scales R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    scales = function() {
      if (is.null(private$.scales)) {
        if (private$.verbosity > 0) {
          message("Creating Scales R6 class object ")
        }
        private$.scales <- (Scales$new(self))
      }

      return(private$.scales)
    },
    #' @field seasons
    #' Get the Seasons R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    seasons = function() {
      if (is.null(private$.seasons)) {
        if (private$.verbosity > 0) {
          message("Creating Seasons R6 class object ")
        }
        private$.seasons <- (Seasons$new(self))
      }

      return(private$.seasons)
    },
    #' @field seedLots
    #' Get the SeedLots R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    seedLots = function() {
      if (is.null(private$.seedLots)) {
        if (private$.verbosity > 0) {
          message("Creating SeedLots R6 class object ")
        }
        private$.seedLots <- (SeedLots$new(self))
      }

      return(private$.seedLots)
    },
    #' @field studies
    #' Get the Studies R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    studies = function() {
      if (is.null(private$.studies)) {
        if (private$.verbosity > 0) {
          message("Creating Studies R6 class object ")
        }
        private$.studies <- (Studies$new(self))
      }

      return(private$.studies)
    },
    #' @field traits
    #' Get the Traits R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    traits = function() {
      if (is.null(private$.traits)) {
        if (private$.verbosity > 0) {
          message("Creating Traits R6 class object ")
        }
        private$.traits <- (Traits$new(self))
      }

      return(private$.traits)
    },
    #' @field trials
    #' Get the Trials R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    trials = function() {
      if (is.null(private$.trials)) {
        if (private$.verbosity > 0) {
          message("Creating Trials R6 class object ")
        }
        private$.trials <- (Trials$new(self))
      }

      return(private$.trials)
    },
    #' @field variants
    #' Get the Variants R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    variants = function() {
      if (is.null(private$.variants)) {
        if (private$.verbosity > 0) {
          message("Creating Variants R6 class object ")
        }
        private$.variants <- (Variants$new(self))
      }

      return(private$.variants)
    },
    #' @field variantSets
    #' Get the VariantSets R6 class object which can be used to call the BrAPI server for
    #' [${entityNames[iterStat.index]}]] entities
    variantSets = function() {
      if (is.null(private$.variantSets)) {
        if (private$.verbosity > 0) {
          message("Creating VariantSets R6 class object ")
        }
        private$.variantSets <- (VariantSets$new(self))
      }

      return(private$.variantSets)
    }
  ),
  private = list(
  
      .alleleMatrix = NULL,
      .breedingMethods = NULL,
      .calls = NULL,
      .callSets = NULL,
      .crosses = NULL,
      .crossingProjects = NULL,
      .events = NULL,
      .genomeMaps = NULL,
      .germplasm = NULL,
      .germplasmAttributes = NULL,
      .germplasmAttributeValues = NULL,
      .images = NULL,
      .lists = NULL,
      .locations = NULL,
      .markerPositions = NULL,
      .methods = NULL,
      .observations = NULL,
      .observationUnits = NULL,
      .observationVariables = NULL,
      .ontologies = NULL,
      .pedigreeNodes = NULL,
      .people = NULL,
      .plannedCrosses = NULL,
      .plates = NULL,
      .programs = NULL,
      .references = NULL,
      .referenceSets = NULL,
      .samples = NULL,
      .scales = NULL,
      .seasons = NULL,
      .seedLots = NULL,
      .studies = NULL,
      .traits = NULL,
      .trials = NULL,
      .variants = NULL,
      .variantSets = NULL
  
  )
)

# Generated by Schema Tools Generator Version: '0.48.0'
