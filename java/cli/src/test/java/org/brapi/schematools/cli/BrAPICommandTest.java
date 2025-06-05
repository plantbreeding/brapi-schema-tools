package org.brapi.schematools.cli;

import org.junit.jupiter.api.Test;

public class BrAPICommandTest {

    @Test
    void brapiCommandTest(){
        BrAPICommand.main(new String[]{
            "generate",
            "C:\\Users\\ps664\\Documents\\BrAPI\\API\\Specification\\BrAPI-Schema",
            "-f",
            "C:\\Users\\ps664\\Documents\\BrAPI\\API\\generator\\out",
            "-l",
            "OPEN_API",
            "-c",
            "C:\\Users\\ps664\\Documents\\BrAPI\\API\\Specification\\OpenAPI-Components",
            "-o",
            "C:\\Users\\ps664\\Documents\\BrAPI\\brapi-schema-tools\\java\\cli\\src\\main\\resources\\openapi-options.yaml"});

    }
}
