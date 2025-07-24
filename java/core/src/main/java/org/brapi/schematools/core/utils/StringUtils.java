package org.brapi.schematools.core.utils;

import graphql.com.google.common.collect.ImmutableList;
import graphql.com.google.common.collect.ImmutableSet;
import org.atteo.evo.inflector.English;
import org.brapi.schematools.core.response.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for working with Strings
 */
public class StringUtils {

    /**
     * Create a capitalised version of a string value, where the first character is converted to upper case
     * @param value the string value to be capitalised
     * @return a capitalised version of a string value, where the first character is converted to upper case
     */
    public static String capitalise(String value) {
        return value != null ? value.substring(0, 1).toUpperCase() + value.substring(1) : null ;
    }
    private static final Set<String> unpluralisables = ImmutableSet.of("germplasm");

    private static final List<Replacer> singularisations = ImmutableList.of(
        replace("(.*)people$").with("$1person"),
        replace("(.*)People$").with("$1Person"),
        replace("oxen$").with("ox"),
        replace("children$").with("child"),
        replace("feet$").with("foot"),
        replace("teeth$").with("tooth"),
        replace("geese$").with("goose"),
        replace("(.*)ives?$").with("$1ife"),
        replace("(.*)ves?$").with("$1f"),
        replace("(.*)men$").with("$1man"),
        replace("(.+[aeiou])ys$").with("$1y"),
        replace("(.+[^aeiou])ies$").with("$1y"),
        replace("(.+)zes$").with("$1"),
        replace("([m|l])ice$").with("$1ouse"),
        replace("(.+)matrices$").with("$1matrix"),
        replace("(.+)Matrices$").with("$1Matrix"),
        replace("indices$").with("index"),
        replace("(.+[^aeiou])ices$").with("$1ice"),
        replace("(.*)ices$").with("$1ex"),
        replace("(octop|vir)i$").with("$1us"),
        replace("bases$").with("base"),
        replace("(.+(s|x|sh|ch))es$").with("$1"),
        replace("(.+)s$").with("$1")
    );

    private static final List<Replacer> pluralisations = ImmutableList.of(
        replace("(.*)matrix$").with("$1matrices"),
        replace("(.*)Matrix$").with("$1Matrices"),
        replace("(.*)person$").with("$1people"),
        replace("(.*)Person$").with("$1People")
    );

    /**
     * Converts the noun from its plural form to its singular form
     *
     * @param value a noun to be converted
     * @return singular form of a plural noun
     */
    public static String toSingular(String value) {
        if (value == null) {
            return null;
        }

        if (unpluralisables.contains(value.toLowerCase())) {
            return value;
        }

        for (final Replacer singularization : singularisations) {
            if (singularization.matches(value)) {
                return singularization.replace();
            }
        }

        return value ;
    }

    /**
     * Converts the noun from its singular form to its plural form
     *
     * @param value a noun to be converted
     * @return plural form of a singular noun
     */
    public static String toPlural(String value) {
        if (value == null) {
            return null;
        }

        if (unpluralisables.contains(value.toLowerCase())) {
            return value;
        }

        for (final Replacer pluralisation : pluralisations) {
            if (pluralisation.matches(value)) {
                return pluralisation.replace();
            }
        }

        return English.plural(value);
    }

    /**
     * Creates a valid name for use in GraphQL
     *
     * @param string a string which might contain invalid characters for a valid GraphQL name
     * @return a valid GraphQL name based on the input string
     */
    public static String makeValidName(String string) {

        if (string == null) {
            return "null";
        }

        if (string.isBlank()) {
            return "blank";
        }

        if (string.matches("^\\d.*$")) {
            string = "N" + string;
        }

        return string.
            replace("-", "_").
            replace("/", "_").
            replace(".", "_");
    }

    /**
     * Makes the first letter in the string upper case
     *
     * @param value the string to be converted
     * @return the converted string
     */

    public static String toSentenceCase(String value) {
        return value != null ? value.substring(0, 1).toUpperCase() + value.substring(1) : null ;
    }

    /**
     * Makes the first letter in the string lower case
     *
     * @param value the string to be converted
     * @return the converted string
     */
    public static String toParameterCase(String value) {
        return value != null ? value.substring(0, 1).toLowerCase() + value.substring(1) : null ;
    }

    /**
     * Makes the whole string lower case
     *
     * @param value the string to be converted
     * @return the converted string
     */
    public static String toLowerCase(String value) {
        return value != null ? value.toLowerCase() : null ;
    }

    /**
     * Determines if the string starts with an lower case character
     *
     * @param value the string to be tested
     * @return {@code true} if the string starts with an lower case character, {@code false} otherwise
     */
    public static boolean startsWithLowerCase(String value) {
        return value != null && value.matches("^[a-z].*$");
    }

    /**
     * Determines if the string starts with an upper case character
     *
     * @param value the string to be tested
     * @return {@code true} if the string starts with an upper case character, {@code false} otherwise
     */
    public static boolean startsWithUpperCase(String value) {
        return value != null && value.matches("^[A-Z].*$");
    }

    /**
     * Create a label for a property
     * @param propertyName the property name in lower camel case
     * @return a label for the property in sentence case
     */
    public static String toLabel(String propertyName) {
        return capitalise(propertyName).replaceAll("([A-Z])", " $1").trim() ;
    }

    /**
     * Reads a string from a file path
     * @param path the path of the file
     * @return a string read from a file path
     */
    public static Response<String> readStringFromPath(Path path) {
        try {
            Stream<String> lines = Files.lines(path);
            String data = lines.collect(Collectors.joining("\n"));
            lines.close();

            return Response.success(data);
        } catch (IOException exception) {
            return Response.fail(Response.ErrorType.VALIDATION, exception.getMessage());
        }
    }

    /**
     * Check if a string is not null and not blank
     * @param string the string to be tested
     * @return <code>true</code> if a string is not null and not blank
     */
    public static boolean isNotBlank(String string) {
        return string != null && !string.isBlank() ;
    }

    static class Replacer {
        final Pattern pattern;
        final String replacement;
        Matcher m;

        static class Builder {
            private final Pattern pattern;

            Builder(Pattern pattern) {
                this.pattern = pattern;
            }

            Replacer with(String replacement) {
                return new Replacer(pattern, replacement);
            }
        }

        private Replacer(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        boolean matches(String word) {
            m = pattern.matcher(word);
            return m.matches();
        }

        String replace() {
            return m.replaceFirst(replacement);
        }
    }

    static Replacer.Builder replace(String pattern) {
        return new Replacer.Builder(Pattern.compile(pattern));
    }
}
