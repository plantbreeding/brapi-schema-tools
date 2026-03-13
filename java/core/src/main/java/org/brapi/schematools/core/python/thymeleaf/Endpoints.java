package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Endpoints {
    String crud ;
    String search ;
    String table;

    boolean get ;
    boolean list ;
    boolean create ;
    boolean createMany;
    boolean update ;
    boolean delete;
}
