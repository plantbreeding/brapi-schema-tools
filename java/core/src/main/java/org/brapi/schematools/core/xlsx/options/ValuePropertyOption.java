package org.brapi.schematools.core.xlsx.options;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.validiation.Validation;

import java.util.Arrays;
import java.util.Map;

/**
 * A value property in a worksheet.
 */
@Getter
@Setter
public class ValuePropertyOption implements Options {
    /**
     * The name of the property
     */
    private String name ;
    /**
     * The label for the column
     */
    private Integer index ;
    /**
     * If the value of the property is a {@link Map} with key {@link String} use the value with this key
     */
    private String key ;
    /**
     * If the value used if the property is null
     */
    private Object defaultValue ;
    /**
     * If the value is an object allows to recursively reference a child property
     */
    private ValuePropertyOption childProperty ;

    @Override
    public Validation validate() {
        return Validation
            .valid()
            .assertNotNull(name, "'name' option on %s is null", this.getClass().getSimpleName())
            .assertMutuallyExclusive(this, "index", "key")
            .assertClass(defaultValue, Arrays.asList(Integer.class, String.class, Boolean.class))
            .mergeOnCondition(childProperty != null, childProperty, "Child property:") ;
    }
}
