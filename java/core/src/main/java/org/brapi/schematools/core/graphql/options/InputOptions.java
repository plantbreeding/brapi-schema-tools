package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.StringUtils;

@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InputOptions {
    private String name;
    private String nameFormat;
    private String typeNameFormat;

    public void validate() {
        assert name != null : "'name' option on Input Options is null";
        assert typeNameFormat != null : "'typeNameFormat' option on Input Options is null";
    }

    /**
     * Gets the name of the input for a specific primary model
     * @param name the name of the primary model
     * @return the name of the input for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull String name) {
        return nameFormat != null ? String.format(nameFormat, name) : name ;
    }

    /**
     * Gets the name of input for a specific primary model
     * @param type the primary model
     * @return the name of the input for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull BrAPIType type) {
        return getNameFor(type.getName());
    }

    /**
     * Gets the type name for a specific primary model
     * @param name the name of the primary model
     * @return the  type name for a specific primary model
     */
    @JsonIgnore
    public final String getTypeNameFor(@NonNull String name) {
        return String.format(typeNameFormat, name) ;
    }

    /**
     * Gets the type name for a specific primary model
     * @param type the primary model
     * @return the  type name for a specific primary model
     */
    @JsonIgnore
    public final String getTypeNameFor(@NonNull BrAPIType type) {
        return getTypeNameFor(type.getName());
    }

    /**
     * Gets the type name for a query
     * @param queryName the name of the query
     * @return the name of the type name for a query
     */
    @JsonIgnore
    public final String getTypeNameForQuery(@NonNull String queryName) {
        return String.format(typeNameFormat, StringUtils.toSentenceCase(queryName)) ;
    }
}
