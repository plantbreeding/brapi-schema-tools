package org.brapi.schematools.core.openapi.options;

import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdOptions {
    String nameFormat;
}
