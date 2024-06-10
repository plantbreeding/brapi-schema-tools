package org.brapi.schematools.core.utils;

import graphql.com.google.common.collect.ImmutableList;
import graphql.com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with Strings
 */
public class StringUtils {
    private static final Set<String> unpluralisables = ImmutableSet.of(
        "equipment", "information", "rice", "money", "species", "series",
        "fish", "sheep", "deer");

    private static final List<Replacer> singularisations = ImmutableList.of(
        replace("(.*)people$").with("$1person"),
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
        replace("matrices$").with("matrix"),
        replace("indices$").with("index"),
        replace("(.+[^aeiou])ices$").with("$1ice"),
        replace("(.*)ices$").with("$1ex"),
        replace("(octop|vir)i$").with("$1us"),
        replace("bases$").with("base"),
        replace("(.+(s|x|sh|ch))es$").with("$1"),
        replace("(.+)s$").with("$1")
    );

    private static final List<Replacer> pluralisations = ImmutableList.of(
        replace("(.*)person$").with("$1people"),
        replace("ox$").with("oxen"),
        replace("child$").with("children"),
        replace("foot$").with("feet"),
        replace("tooth$").with("teeth"),
        replace("goose$").with("geese"),
        replace("(.*)fe?$").with("$1ves"),
        replace("(.*)man$").with("$1men"),
        replace("(.+[aeiou]y)$").with("$1s"),
        replace("(.+[^aeiou])y$").with("$1ies"),
        replace("(.+z)$").with("$1zes"),
        replace("([m|l])ouse$").with("$1ice"),
        replace("(.+)(e|i)x$").with("$1ices"),
        replace("(octop|vir)us$").with("$1i"),
        replace("(.+(s|x|sh|ch))$").with("$1es"),
        replace("(.+)").with("$1s")
    );

    /**
     * Converts the noun from its plural form to its singular form
     *
     * @param value a noun to be converted
     * @return singular form of a plural noun
     */
    public static String toSingular(String value) {
        if (unpluralisables.contains(value.toLowerCase())) {
            return value;
        }

        for (final Replacer singularization : singularisations) {
            if (singularization.matches(value)) {
                return singularization.replace();
            }
        }

        return value;
    }

    /**
     * Converts the noun from its singular form to its plural form
     *
     * @param value a noun to be converted
     * @return plural form of a singular noun
     */
    public static String toPlural(String value) {
        if (unpluralisables.contains(value.toLowerCase())) {
            return value;
        }

        for (final Replacer pluralisation : pluralisations) {
            if (pluralisation.matches(value)) {
                return pluralisation.replace();
            }
        }

        return value;
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
            replace(".", "_"); // TODO other replacements
    }

    /**
     * Makes the first letter in the string upper case
     *
     * @param value the string to be converted
     * @return the converted string
     */

    public static String toSentenceCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    /**
     * Makes the first letter in the string lower case
     *
     * @param value the string to be converted
     * @return the converted string
     */
    public static String toParameterCase(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    /**
     * Determines if the string starts with an lower case character
     *
     * @param value the string to be tested
     * @return <code>true</code> if the string starts with an lower case character, <code>false</code> otherwise
     */
    public static boolean startsWithLowerCase(String value) {
        return value.matches("^[a-z].*$");
    }

    /**
     * Determines if the string starts with an upper case character
     *
     * @param value the string to be tested
     * @return <code>true</code> if the string starts with an upper case character, <code>false</code> otherwise
     */
    public static boolean startsWithUpperCase(String value) {
        return value.matches("^[A-Z].*$");
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
