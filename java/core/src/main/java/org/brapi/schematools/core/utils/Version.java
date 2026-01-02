package org.brapi.schematools.core.utils;

/**
 * A wrapper class for reading a version txt file.
 */
public class Version {
    public static String getVersion() {
        return StringUtils.readStringFromClasspath("version.txt").orElseResult("Unknown") ;
    }
}
