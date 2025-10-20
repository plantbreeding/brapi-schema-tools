package org.brapi.schematools.core.utils;

public class Version {
    public static String getVersion() {
        return StringUtils.readStringFromClasspath("version.txt").orElseResult("Unknown Version") ;
    }
}
