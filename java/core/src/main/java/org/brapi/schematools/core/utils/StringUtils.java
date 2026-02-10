package org.brapi.schematools.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.com.google.common.collect.ImmutableSet;
import org.brapi.schematools.core.response.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * Nouns that are the same in singular and plural form (both singular AND plural)
     */
    private static final Set<String> unchangeables = ImmutableSet.of("germplasm", "species", "series", "sheep", "deer", "fish", "bison", "moose", "swine", "offspring", "salmon", "trout", "hovercraft", "spacecraft");

    /**
     * Nouns that are always plural (no singular form)
     */
    private static final Set<String> pluralOnly = ImmutableSet.of();

    /**
     * Custom plural mappings for irregular plurals
     */
    private static final Map<String, String> customPlurals = Map.ofEntries(
        Map.entry("matrix", "matrices"),
        Map.entry("person", "people"),
        Map.entry("child", "children"),
        Map.entry("mouse", "mice"),
        Map.entry("goose", "geese"),
        Map.entry("foot", "feet"),
        Map.entry("tooth", "teeth"),
        Map.entry("man", "men"),
        Map.entry("woman", "women"),
        Map.entry("genus", "genera"),
        Map.entry("phylum", "phyla"),
        Map.entry("taxon", "taxa")
    );

    /**
     * Custom singular mappings (reverse of customPlurals)
     */
    private static final Map<String, String> customSingulars = Map.ofEntries(
        Map.entry("matrices", "matrix"),
        Map.entry("people", "person"),
        Map.entry("children", "child"),
        Map.entry("mice", "mouse"),
        Map.entry("geese", "goose"),
        Map.entry("feet", "foot"),
        Map.entry("teeth", "tooth"),
        Map.entry("men", "man"),
        Map.entry("women", "woman"),
        Map.entry("genera", "genus"),
        Map.entry("phyla", "phylum"),
        Map.entry("taxa", "taxon")
    );

    /**
     * Create a capitalised version of a string value, where the first character is converted to upper case
     * @param value the string value to be capitalised
     * @return a capitalised version of a string value, where the first character is converted to upper case
     */
    public static String capitalise(String value) {
        return value != null ? !value.isEmpty() ? value.substring(0, 1).toUpperCase() + value.substring(1) : "" : null ;
    }

    /**
     * Tests if a word is plural using rule-based pattern matching.
     *
     * @param word the word to test
     * @return {@code true} if the word is plural, {@code false} if singular or unknown
     */
    public static boolean isPlural(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        String lowerWord = word.toLowerCase();

        // Words that are both singular and plural
        if (unchangeables.contains(lowerWord)) {
            return true;
        }

        // Words that are only plural
        if (pluralOnly.contains(lowerWord)) {
            return true;
        }

        // Check if this is a compound camelCase word (e.g., DataMatrices, dataMatrices)
        // Split on uppercase letter transitions
        Pattern camelCasePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        String[] parts = camelCasePattern.split(word);

        if (parts.length > 1) {
            // It's a compound word - check only the last part
            String lastPart = parts[parts.length - 1];
            return isPlural(lastPart);
        }

        // Single word - use rule-based checking
        return looksLikePlural(lowerWord);
    }

    /**
     * Check if a word looks like a plural based on common patterns
     *
     * @param lowerWord the lowercase word to check
     * @return true if it looks like a plural
     */
    private static boolean looksLikePlural(String lowerWord) {
        // Check custom plurals
        if (customPlurals.containsValue(lowerWord)) {
            return true;
        }

        // Words ending in 'us' are typically singular (genus, focus, etc.)
        if (lowerWord.endsWith("us")) {
            return false;
        }

        // Common plural endings
        return lowerWord.endsWith("s") || lowerWord.endsWith("es") ||
               lowerWord.endsWith("ies") || lowerWord.endsWith("ves") ||
               lowerWord.endsWith("i") || lowerWord.endsWith("a");
    }

    /**
     * Tests if a word is singular using rule-based pattern matching.
     *
     * @param word the word to test
     * @return {@code true} if the word is singular, {@code false} if plural or unknown
     */
    public static boolean isSingular(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        String lowerWord = word.toLowerCase();

        // Words that are both singular and plural
        if (unchangeables.contains(lowerWord)) {
            return true;
        }

        // Words that are only plural (never singular)
        if (pluralOnly.contains(lowerWord)) {
            return false;
        }

        // Check if this is a compound camelCase word (e.g., DataMatrix, dataMatrix)
        // Split on uppercase letter transitions
        Pattern camelCasePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        String[] parts = camelCasePattern.split(word);

        if (parts.length > 1) {
            // It's a compound word - check only the last part
            String lastPart = parts[parts.length - 1];
            return isSingular(lastPart);
        }

        // Single word - use rule-based checking
        return !looksLikePlural(lowerWord);
    }

    /**
     * Converts a plural noun to singular using rule-based approach.
     *
     * @param word the word to convert
     * @return the singular form of the word
     */
    public static String toSingular(String word) {
        if (word == null) {
            return null;
        }

        String lowerWord = word.toLowerCase();

        // Words that don't change
        if (unchangeables.contains(lowerWord) || pluralOnly.contains(lowerWord)) {
            return word;
        }

        // If the word is already a known singular, return it unchanged
        if (customSingulars.containsValue(lowerWord)) {
            return word;
        }

        // Check if this is a compound camelCase word (e.g., DataMatrices, dataMatrices)
        // Split on uppercase letter transitions
        Pattern camelCasePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        String[] parts = camelCasePattern.split(word);

        if (parts.length > 1) {
            // It's a compound word - singularize only the last part
            String lastPart = parts[parts.length - 1];
            String singularizedLastPart = singularizeSingleWord(lastPart);
            parts[parts.length - 1] = singularizedLastPart;
            return String.join("", parts);
        } else {
            // It's a single word - singularize normally
            return singularizeSingleWord(word);
        }
    }

    /**
     * Helper method to singularize a single word (not compound) using rule-based approach.
     *
     * @param word the single word to singularize
     * @return the singular form of the word
     */
    private static String singularizeSingleWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        // Check if word starts with uppercase to preserve case
        boolean startsWithUpperCase = Character.isUpperCase(word.charAt(0));

        // Get the lowercase version for lookup
        String lowerWord = word.toLowerCase();

        // Check custom singular mappings first
        String singularForm = customSingulars.get(lowerWord);

        if (singularForm == null) {
            // Apply reverse pluralization rules
            singularForm = applySingularizationRules(lowerWord);
        }

        // Preserve original case
        if (startsWithUpperCase) {
            return capitalise(singularForm);
        }
        return singularForm;
    }

    /**
     * Apply reverse pluralization rules to singularize a word
     *
     * @param word the lowercase plural word
     * @return the singular form
     */
    private static String applySingularizationRules(String word) {
        // Words ending in 'ies' -> change to 'y' (but only if preceded by a consonant)
        // This handles: studies -> study, but not objectives (which should just lose 's')
        if (word.endsWith("ies") && word.length() > 3) {
            char beforeIes = word.charAt(word.length() - 4);
            // Check if it's a consonant (not a vowel)
            if (!isVowel(beforeIes)) {
                return word.substring(0, word.length() - 3) + "y";
            }
        }

        // Words ending in 'ves' -> change to 'f' or 'fe'
        // But only if there's a consonant before 'ves' (wolves, knives)
        // Not for words like objectives (vowel before ves)
        if (word.endsWith("ves") && word.length() > 3) {
            char beforeVes = word.charAt(word.length() - 4);
            // Check if it's a consonant (not a vowel)
            if (!isVowel(beforeVes)) {
                return word.substring(0, word.length() - 3) + "f";
            }
        }

        // Words ending in 'ses', 'shes', 'ches', 'xes', 'zes' -> remove 'es'
        if ((word.endsWith("ses") && !word.endsWith("sses")) ||
            word.endsWith("shes") || word.endsWith("ches") ||
            word.endsWith("xes") || word.endsWith("zes")) {
            return word.substring(0, word.length() - 2);
        }

        // Words ending in 'sses' -> remove 'es'
        if (word.endsWith("sses")) {
            return word.substring(0, word.length() - 2);
        }

        // Words ending in 'i' (plural of 'us')
        if (word.endsWith("i") && word.length() > 2) {
            return word.substring(0, word.length() - 1) + "us";
        }

        // Words ending in 's' -> remove 's' (but not if it ends in 'us' which is typically singular)
        if (word.endsWith("s") && !word.endsWith("us") && !word.endsWith("ss") && word.length() > 1) {
            return word.substring(0, word.length() - 1);
        }

        // Default: return as is
        return word;
    }


    /**
     * Creates a valid name for use in GraphQL
     * @param word the word to convert
     * @return the plural form of the word
     */
    public static String toPlural(String word) {
        if (word == null) {
            return null;
        }

        String lowerWord = word.toLowerCase();

        // Words that don't change
        if (unchangeables.contains(lowerWord) || pluralOnly.contains(lowerWord)) {
            return word;
        }

        // If the word is already a known plural, return it unchanged
        if (customPlurals.containsValue(lowerWord)) {
            return word;
        }

        // Check if this is a compound camelCase word (e.g., DataMatrix, dataMatrix)
        // Split on uppercase letter transitions
        Pattern camelCasePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        String[] parts = camelCasePattern.split(word);

        if (parts.length > 1) {
            // It's a compound word - pluralize only the last part
            int lastIndex = parts.length - 1;
            String lastPart = parts[lastIndex];
            String pluralizedLastPart = pluralizeSingleWord(lastPart);

            // Rebuild the word with the pluralized last part
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < lastIndex; i++) {
                result.append(parts[i]);
            }
            result.append(pluralizedLastPart);

            return result.toString();
        } else {
            // It's a single word - pluralize normally
            return pluralizeSingleWord(word);
        }
    }

    /**
     * Helper method to pluralize a single word (not compound) using custom rules.
     *
     * @param word the single word to pluralize
     * @return the plural form of the word
     */
    private static String pluralizeSingleWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        // Check if word starts with uppercase to preserve case
        boolean startsWithUpperCase = Character.isUpperCase(word.charAt(0));

        // Get the lowercase version for lookup
        String lowerWord = word.toLowerCase();

        // Check custom plural mappings first
        String pluralForm = customPlurals.get(lowerWord);

        if (pluralForm == null) {
            // Apply pluralization rules
            pluralForm = applyPluralizationRules(lowerWord);
        }

        // Preserve original case
        if (startsWithUpperCase) {
            pluralForm = capitalise(pluralForm);
        }

        return pluralForm;
    }

    /**
     * Apply standard English pluralization rules
     *
     * @param word the lowercase word to pluralize
     * @return the pluralized form
     */
    private static String applyPluralizationRules(String word) {
        // Irregular plurals that end in 'y'
        if (word.endsWith("y") && word.length() > 1 && !isVowel(word.charAt(word.length() - 2))) {
            return word.substring(0, word.length() - 1) + "ies";
        }

        // Words ending in s, ss, sh, ch, x, z
        if (word.endsWith("s") || word.endsWith("ss") || word.endsWith("sh") ||
            word.endsWith("ch") || word.endsWith("x") || word.endsWith("z")) {
            return word + "es";
        }

        // Words ending in 'o' preceded by a consonant
        if (word.endsWith("o") && word.length() > 1 && !isVowel(word.charAt(word.length() - 2))) {
            return word + "es";
        }

        // Words ending in 'f' or 'fe'
        if (word.endsWith("f")) {
            return word.substring(0, word.length() - 1) + "ves";
        }
        if (word.endsWith("fe")) {
            return word.substring(0, word.length() - 2) + "ves";
        }

        // Words ending in 'us'
        if (word.endsWith("us")) {
            return word.substring(0, word.length() - 2) + "i";
        }

        // Words ending in 'is'
        if (word.endsWith("is")) {
            return word.substring(0, word.length() - 2) + "es";
        }


        // Default: just add 's'
        return word + "s";
    }

    /**
     * Check if a character is a vowel
     *
     * @param c the character to check
     * @return true if the character is a vowel
     */
    private static boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) >= 0;
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
     * Inserts an underscore before each upper case letter (except the first) and makes the whole string lower case
     *
     * @param value the string to be converted
     * @return the converted string
     */
    public static String toSnakeCase(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String result = value.replaceAll("([A-Z])", "_$1");
        if (result.startsWith("_")) {
            result = result.substring(1);
        }
        return result.toLowerCase();
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
     * Reads a string from a Classpath
     *
     * @param classpath the classpath
     * @return a string read from a classpath
     */
    public static Response<String> readStringFromClasspath(String classpath) {
        try (InputStream in = Version.class.getClassLoader().getResourceAsStream(classpath)) {
            if (in != null) {
                return Response.success(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Could not find resource on classpath '%s'", classpath));
            }
        } catch (IOException exception) {
            return Response.fail(Response.ErrorType.VALIDATION, exception.getMessage());
        }
    }

    /**
     * Reads a string from a file path
     * @param path the path of the file
     * @return a string read from a file path
     */
    public static Response<String> readStringFromPath(Path path) {
        try {
            Stream<String> lines = Files.lines(path);
            String data = lines.collect(Collectors.joining(System.lineSeparator()));
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

    /**
     * Compare two multiline strings to if they are the same.
     * If the strings are JSON use the {@link #isJSONEqual(String, String)} method.
     * @param expected the expected string
     * @param actual the actual string
     * @return <code>true</code> if actual string is equal to the expected string
     */
    public static boolean isMultilineEqual(String expected, String actual) {
        BufferedReader expectedReader = new BufferedReader(new StringReader(expected));
        BufferedReader actualReader = new BufferedReader(new StringReader(actual));

        boolean equals = true;

        try {
            String expectedLine = expectedReader.readLine() ;
            String actualLine = actualReader.readLine() ;

            while (equals && expectedLine != null && actualLine != null) {
                equals = expectedLine.equals(actualLine);

                expectedLine = expectedReader.readLine() ;
                actualLine = actualReader.readLine() ;
            }

            equals = equals && expectedLine == null && actualLine == null ;
        }
        catch (Exception e) {
            equals = false ;
        }

        return equals;
    }

    /**
     * Compare two JSON strings to if they are the same.
     * If the strings are not JSON use the {@link #isJSONEqual(String, String)} method.
     * @param expected the expected JSON string
     * @param actual the actual JSON string
     * @return <code>true</code> if actual string is equal to the expected string, <code>false</code> otherwise
     * @throws JsonProcessingException if the strings cannot be converted to JSON nodes.
     */
    public static boolean isJSONEqual(String expected, String actual) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readTree(expected).equals(mapper.readTree(actual)) ;
    }

    /**
     * Pretty print an object to a JSON String, with a default indentation of 4 spaces.
     * @param value the object to be Pretty printed
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(Object value) throws JsonProcessingException {
        return prettyPrint(value, 4) ;
    }

    /**
     * Pretty print an object to a JSON String.
     * @param value the object to be Pretty printed
     * @param indent the number of spaces to indent
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(Object value, int indent) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        DefaultPrettyPrinter.Indenter indenter =
            new DefaultIndenter(" ".repeat(indent), DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);

        return mapper.writer(printer).writeValueAsString(value);
    }

    /**
     * Places text in a string in the format {key} with a value.
     * The
     * @param format the format string that contains the keys to substitute.
     * @param parameters A map of key value pairs
     * @return the formatted string
     */
    public static String format(String format, Map<String, Object> parameters) {
        if (format == null) {
            return null ;
        }

        StringBuilder newFormat = new StringBuilder(format);
        List<Object> valueList = new ArrayList<>();

        Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(format);

        while (matcher.find()) {
            String key = matcher.group(1);

            String paramName = "${" + key + "}";
            int index = newFormat.indexOf(paramName);
            if (index != -1) {
                newFormat.replace(index, index + paramName.length(), "%s");
                valueList.add(parameters.get(key));
            }
        }

        return String.format(newFormat.toString(), valueList.toArray());
    }

    public static String escapeQuotes(String inputString) {
        return inputString.replaceAll("\"", "\\\"").replaceAll("'", "\\\\'") ;
    }

    public static String escapeSpecialCharacters(String inputString) {
        StringBuilder escapedString = new StringBuilder();
        for (char c : inputString.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                escapedString.append("\\");
            }
            escapedString.append(c);
        }
        return escapedString.toString();
    }

    public static String removeCarriageReturns(String inputString) {
        return inputString.replaceAll("[\\n\\r]", " ") ;
    }

    public static String extractFirstLine(String description) {
        if (description == null || description.isEmpty()) {
            return description;
        }
        String cleaned = description.replace("\r", "").replace("\n", " ");
        int bracketDepth = 0;
        int parenDepth = 0;
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c == '[') bracketDepth++;
            if (c == ']') bracketDepth = Math.max(0, bracketDepth - 1);
            if (c == '(') parenDepth++;
            if (c == ')') parenDepth = Math.max(0, parenDepth - 1);
            if (c == '.' && bracketDepth == 0 && parenDepth == 0) {
                return cleaned.substring(0, i + 1).trim();
            }
        }
        return cleaned.trim();
    }
}
