package org.brapi.schematools.core.utils;

import graphql.Scalars;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphQLUtilsTest {

    @Test
    void unwrapType_plainType_returnsSameType() {
        GraphQLType type = Scalars.GraphQLString;
        assertEquals(type, GraphQLUtils.unwrapType(type));
    }

    @Test
    void unwrapType_listWrappedType_returnsInnerType() {
        GraphQLType inner = Scalars.GraphQLString;
        GraphQLType list = GraphQLList.list(inner);
        assertEquals(inner, GraphQLUtils.unwrapType(list));
    }

    @Test
    void unwrapType_nonNullWrappedType_returnsInnerType() {
        GraphQLType inner = Scalars.GraphQLInt;
        GraphQLType nonNull = GraphQLNonNull.nonNull(inner);
        assertEquals(inner, GraphQLUtils.unwrapType(nonNull));
    }

    @Test
    void unwrapType_nonNullListWrappedType_returnsInnerType() {
        GraphQLType inner = Scalars.GraphQLBoolean;
        GraphQLType listOfInner = GraphQLList.list(inner);
        GraphQLType nonNullList = GraphQLNonNull.nonNull(listOfInner);
        assertEquals(inner, GraphQLUtils.unwrapType(nonNullList));
    }

    @Test
    void unwrapType_listOfNonNullWrappedType_returnsInnerType() {
        GraphQLType inner = Scalars.GraphQLFloat;
        GraphQLType nonNullInner = GraphQLNonNull.nonNull(inner);
        GraphQLType listOfNonNull = GraphQLList.list(nonNullInner);
        assertEquals(inner, GraphQLUtils.unwrapType(listOfNonNull));
    }

    @Test
    void unwrapType_nonNullListOfNonNullWrappedType_returnsInnerType() {
        GraphQLType inner = Scalars.GraphQLString;
        GraphQLType nonNullInner = GraphQLNonNull.nonNull(inner);
        GraphQLType listOfNonNull = GraphQLList.list(nonNullInner);
        GraphQLType nonNullListOfNonNull = GraphQLNonNull.nonNull(listOfNonNull);
        assertEquals(inner, GraphQLUtils.unwrapType(nonNullListOfNonNull));
    }
}

