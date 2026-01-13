package org.brapi.schematools.core.utils;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;

/**
 * Provides utility methods for GraphQL
 */
public class GraphQLUtils {

    /**
     * Unwraps GraphQLList and GraphQLNonNull types to find the lowest inner class
     * @param type the type to be unwrapped
     * @return the unwrapped type
     */
    public static GraphQLType unwrapType(GraphQLType type) {
        if (type instanceof GraphQLList graphQLList) {
            return unwrapType(graphQLList.getWrappedType()) ;
        } else if (type instanceof GraphQLNonNull graphQLNonNull) {
            return unwrapType(graphQLNonNull.getWrappedType()) ;
        }

        return type;
    }
}
