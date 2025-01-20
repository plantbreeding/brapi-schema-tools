package org.brapi.schematools.core.xlsx.options;

import lombok.Getter;
import lombok.Setter;

/**
 * Subclass of ValuePropertyOption for Columns
 */
@Getter
@Setter
public class ColumnOption extends ValuePropertyOption {
    /**
     * The label for the column
     */
    private String label ;
}
