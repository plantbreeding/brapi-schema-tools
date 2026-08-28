package org.brapi.schematools.core.sql;

import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures generated FK constraints only reference columns that CREATE TABLE actually emitted,
 * and that multi-FK relationships to the same target keep unique constraint names.
 */
class SQLGeneratorForeignKeyConsistencyTest {

    private static final Pattern ALTER_FK_PATTERN = Pattern.compile(
        "ALTER\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?(\\S+)\\s+ADD\\s+CONSTRAINT\\s+(\\S+)\\s+FOREIGN\\s+KEY\\s*\\(([^)]+)\\)",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\S+)\\s*\\(",
        Pattern.CASE_INSENSITIVE);

    private static final String[] DATABRICKS_METADATA_FILES = {
        "prism-silver-sql-metadata.yaml",
        "phenome-silver-sql-metadata.yaml",
        "prism-sterling-sql-metadata.yaml",
        "phenome-sterling-sql-metadata.yaml"
    };

    @Test
    void foreignKeysOnlyReferenceEmittedColumnsForAllDatabricksTargets() throws Exception {
        for (String metadataFileName : DATABRICKS_METADATA_FILES) {
            assertDatabricksTarget(metadataFileName);
        }
    }

    @Test
    void defaultBrAPIGenerationKeepsUniqueSeedLotTransactionConstraintNamesAndValidColumns() throws Exception {
        Path outputDir = Path.of("build/test-output/SQLGenerator/defaults-fk-check");
        Files.createDirectories(outputDir);

        SQLGeneratorOptions options = SQLGeneratorOptions.load().setOverwrite(true);
        SQLGeneratorMetadata metadata = SQLGeneratorMetadata.load();

        Response<List<Path>> response = new SQLGenerator(options, outputDir)
            .generate(resourcePath("BrAPI-Schema"), metadata);
        assertFalse(response.hasErrors(), () -> response.getMessagesCombined(","));

        Path constraintsPath = outputDir.resolve("add_constraints.sql");
        assertTrue(Files.exists(constraintsPath));

        Map<String, Path> tableFilesByName = indexGeneratedTableFiles(outputDir);
        String constraintsSql = Files.readString(constraintsPath, StandardCharsets.UTF_8);

        assertNoDuplicateConstraintNames(constraintsSql, "defaults");
        assertEveryForeignKeyColumnExists(constraintsSql, tableFilesByName, "defaults");

        assertTrue(constraintsSql.contains("SeedLotTransactions_fromSeedLot_SeedLots_fk"));
        assertTrue(constraintsSql.contains("SeedLotTransactions_toSeedLot_SeedLots_fk"));
        assertFalse(constraintsSql.contains("SeedLotTransactions_SeedLots_fk"));
    }

    private static void assertDatabricksTarget(String metadataFileName) throws Exception {
        Path schemaDir = resourcePath("SQLGenerator/deprecated-fk-schema/schema");
        Path optionsFile = resourcePath("SQLGenerator/deprecated-fk-schema/databricks-sql-options.yaml");
        Path metadataFile = resourcePath("SQLGenerator/deprecated-fk-schema/" + metadataFileName);

        String targetName = metadataFileName.replace("-sql-metadata.yaml", "");
        Path outputDir = Path.of("build/test-output/SQLGenerator/deprecated-fk/" + targetName);
        Files.createDirectories(outputDir);

        SQLGeneratorOptions options = SQLGeneratorOptions.load(optionsFile).setOverwrite(true);
        SQLGeneratorMetadata metadata = SQLGeneratorMetadata.load(metadataFile);

        Response<List<Path>> response = new SQLGenerator(options, outputDir).generate(schemaDir, metadata);
        assertFalse(response.hasErrors(), () -> targetName + ": " + response.getMessagesCombined(","));

        Path constraintsPath = outputDir.resolve("add_constraints.sql");
        assertTrue(Files.exists(constraintsPath), "add_constraints.sql must be generated for " + targetName);

        Map<String, Path> tableFilesByName = indexGeneratedTableFiles(outputDir);
        String constraintsSql = Files.readString(constraintsPath, StandardCharsets.UTF_8);

        assertNoDuplicateConstraintNames(constraintsSql, targetName);
        assertEveryForeignKeyColumnExists(constraintsSql, tableFilesByName, targetName);

        Path localeSql = findTableFile(tableFilesByName, "locales");
        assertTrue(localeSql != null, "Locale.sql/locales table must be generated for " + targetName);
        String localeDdl = Files.readString(localeSql, StandardCharsets.UTF_8);
        assertFalse(containsTopLevelColumn(localeDdl, "programPUI"),
            targetName + ": deprecated Locale.program must not emit programPUI column; was:\n" + localeDdl);
        String constraintsLower = constraintsSql.toLowerCase(Locale.ROOT);
        assertFalse(constraintsLower.contains("program_programs_fk") || constraintsLower.contains("locales_program_"),
            targetName + ": deprecated Locale.program must not leave a program FK; constraints were:\n" + constraintsSql);
        assertFalse(constraintsSql.contains("programPUI"),
            targetName + ": no FK may reference omitted programPUI; constraints were:\n" + constraintsSql);

        assertTrue(
            constraintsSql.contains("from_inventory_lot_inventory_lots_fk"),
            targetName + ": fromInventoryLot FK must remain with unique name; was:\n" + constraintsSql);
        assertTrue(
            constraintsSql.contains("to_inventory_lot_inventory_lots_fk"),
            targetName + ": toInventoryLot FK must remain with unique name; was:\n" + constraintsSql);
    }

    private static void assertNoDuplicateConstraintNames(String constraintsSql, String targetName) {
        Matcher matcher = ALTER_FK_PATTERN.matcher(constraintsSql);
        Map<String, Long> counts = new HashMap<>();
        while (matcher.find()) {
            counts.merge(matcher.group(2), 1L, Long::sum);
        }
        List<String> duplicates = counts.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .sorted()
            .toList();
        assertTrue(duplicates.isEmpty(),
            targetName + " has duplicate FK constraint names: " + duplicates);
    }

    private static void assertEveryForeignKeyColumnExists(
            String constraintsSql,
            Map<String, Path> tableFilesByName,
            String targetName) throws Exception {

        Matcher matcher = ALTER_FK_PATTERN.matcher(constraintsSql);
        List<String> missing = new ArrayList<>();

        while (matcher.find()) {
            String tableName = matcher.group(1);
            String constraintName = matcher.group(2);
            String columnList = matcher.group(3);
            Path tableFile = findTableFile(tableFilesByName, tableName);
            if (tableFile == null) {
                missing.add(constraintName + " -> missing table file for " + tableName);
                continue;
            }
            String ddl = Files.readString(tableFile, StandardCharsets.UTF_8);
            for (String rawColumn : columnList.split(",")) {
                String columnName = rawColumn.trim();
                if (columnName.isEmpty()) {
                    continue;
                }
                if (!containsTopLevelColumn(ddl, columnName)) {
                    missing.add(constraintName + " column '" + columnName + "' not in " + tableFile.getFileName());
                }
            }
        }

        assertTrue(missing.isEmpty(),
            targetName + " FK columns missing from CREATE TABLE:\n" + String.join("\n", missing));
    }

    private static Map<String, Path> indexGeneratedTableFiles(Path outputDir) throws Exception {
        Map<String, Path> indexed = new LinkedHashMap<>();
        try (var stream = Files.list(outputDir)) {
            for (Path path : stream.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".sql")).toList()) {
                String fileName = path.getFileName().toString();
                if (fileName.equals("add_constraints.sql") || fileName.equals("drop_tables.sql")) {
                    continue;
                }
                String ddl = Files.readString(path, StandardCharsets.UTF_8);
                Matcher matcher = CREATE_TABLE_PATTERN.matcher(ddl);
                if (matcher.find()) {
                    indexed.put(normaliseTableKey(matcher.group(1)), path);
                }
                indexed.put(normaliseTableKey(fileName.substring(0, fileName.length() - 4)), path);
            }
        }
        return indexed;
    }

    private static Path findTableFile(Map<String, Path> tableFilesByName, String tableName) {
        String key = normaliseTableKey(tableName);
        Path direct = tableFilesByName.get(key);
        if (direct != null) {
            return direct;
        }
        String simple = key.contains(".") ? key.substring(key.lastIndexOf('.') + 1) : key;
        return tableFilesByName.entrySet().stream()
            .filter(entry -> entry.getKey().equals(simple) || entry.getKey().endsWith("." + simple))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    private static String normaliseTableKey(String tableName) {
        return tableName.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean containsTopLevelColumn(String ddl, String columnName) {
        Pattern columnLine = Pattern.compile(
            "(?m)^\\s*" + Pattern.quote(columnName) + "\\s+(STRING|INT|DOUBLE|BOOLEAN|ARRAY|MAP|STRUCT)\\b");
        return columnLine.matcher(ddl).find();
    }

    private static Path resourcePath(String classpath) throws Exception {
        return Path.of(ClassLoader.getSystemResource(classpath).toURI());
    }
}
