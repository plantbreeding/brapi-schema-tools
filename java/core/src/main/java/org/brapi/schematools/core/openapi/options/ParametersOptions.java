package org.brapi.schematools.core.openapi.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.options.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides options for the generation of ID, Name and PUI parameter and their usage
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParametersOptions implements Options {
    private ParameterOptions id ;
    private ParameterOptions name ;
    private ParameterOptions pui ;

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(id, "'id' option on %s is null", this.getClass().getSimpleName()) ;
    }


    public List<BrAPIObjectProperty> getLinkParametersFor(BrAPIObjectType brAPIObjectType) {
        List<BrAPIObjectProperty> linkParameters = new ArrayList<>() ;

        if (id.isLink()) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(id.getParameterFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkParameters::add);
        }

        if (name != null && name.isLink()) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(name.getParameterFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkParameters::add);
        }

        if (pui != null && pui.isLink()) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(pui.getParameterFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkParameters::add);
        }

        return linkParameters ;
    }

    /**
     * Gets the id parameter for a type
     * @param type The BrAPI type
     * @return the id parameter for a type
     */
    public String getIdParameterFor(BrAPIType type) {
        return id.getParameterFor(type) ;
    }
}
