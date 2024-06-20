package org.brapi.schematools.core.graphql.options;

import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InputOptions {
    String name;
    String nameFormat;
    String typeNameFormat;
}
