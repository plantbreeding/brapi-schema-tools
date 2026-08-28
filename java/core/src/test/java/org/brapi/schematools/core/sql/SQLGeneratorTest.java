package org.brapi.schematools.core.sql;

import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.brapi.schematools.core.test.TestUtils.assertMultilineEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class SQLGeneratorTest {

    @Test
    void generateWithDefaults() {
        generate(SQLGeneratorOptions.load(), SQLGeneratorMetadata.load(), 0, "build/test-output/SQLGenerator/defaults") ;
    }

    @Test
    void generateWithOverwrite() {
        generate(SQLGeneratorOptions.load().setOverwrite(true), SQLGeneratorMetadata.load(), 37, "build/test-output/SQLGenerator/defaults") ;
    }

    @Test
    void foreignKeyConstraintNamesAreUniquePerRelationshipProperty() throws Exception {
        generate(SQLGeneratorOptions.load().setOverwrite(true), SQLGeneratorMetadata.load(), 37, "build/test-output/SQLGenerator/defaults");

        Path constraintsPath = Path.of("build/test-output/SQLGenerator/defaults/add_constraints.sql");
        assertTrue(Files.exists(constraintsPath), "add_constraints.sql should be generated");

        String constraintsSql = Files.readString(constraintsPath);
        assertTrue(constraintsSql.contains("SeedLotTransactions_fromSeedLot_SeedLots_fk"),
            "fromSeedLot FK must include relationship property in constraint name");
        assertTrue(constraintsSql.contains("SeedLotTransactions_toSeedLot_SeedLots_fk"),
            "toSeedLot FK must include relationship property in constraint name");
        assertFalse(constraintsSql.contains("SeedLotTransactions_SeedLots_fk"),
            "ambiguous shared SeedLotTransactions_SeedLots_fk name must not be emitted");

        java.util.regex.Pattern constraintNamePattern = java.util.regex.Pattern.compile(
            "ADD CONSTRAINT\\s+(\\S+)\\s+FOREIGN KEY", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.Map<String, Long> nameCounts = constraintNamePattern.matcher(constraintsSql).results()
            .map(match -> match.group(1))
            .collect(java.util.stream.Collectors.groupingBy(name -> name, java.util.stream.Collectors.counting()));
        java.util.List<String> duplicatedNames = nameCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(java.util.Map.Entry::getKey)
            .sorted()
            .toList();
        assertTrue(duplicatedNames.isEmpty(),
            "FK constraint names must be unique; duplicates=" + duplicatedNames);
    }

    @Test
    void controlledVocabularyTablesAreFlattenedToRowShape() throws Exception {
        Path outputDir = Path.of("build/test-output/SQLGenerator/cv-flatten");
        Files.createDirectories(outputDir);

        SQLGeneratorOptions options = SQLGeneratorOptions.load().setOverwrite(true);
        SQLGeneratorMetadata metadata = SQLGeneratorMetadata.load();

        Response<List<Path>> response = new SQLGenerator(options, outputDir)
            .generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), metadata);
        assertFalse(response.hasErrors(), () -> response.getMessagesCombined(","));

        Path studySql = outputDir.resolve("Study.sql");
        assertTrue(Files.exists(studySql));
        String studyDdl = Files.readString(studySql);

        assertTrue(studyDdl.contains("CREATE TABLE brapi_ObservationLevels"),
            "ObservationLevels CV table should still be generated");
        assertTrue(studyDdl.contains("levelName STRING"),
            "ObservationLevels should expose flattened levelName column");
        assertTrue(studyDdl.contains("levelOrder INT") || studyDdl.contains("levelOrder INTEGER"),
            "ObservationLevels should expose flattened levelOrder column");
        assertFalse(studyDdl.contains("CREATE TABLE brapi_ObservationLevels (\n  observationLevels\n    ARRAY<"),
            "ObservationLevels must not keep a single ARRAY<STRUCT> column");
        assertFalse(studyDdl.matches("(?s).*CREATE TABLE brapi_ObservationLevels \\(\\s*observationLevels\\s+ARRAY<.*"),
            "ObservationLevels must not wrap vocabulary fields in ARRAY");
    }

    @Test
    void separateLinkAndControlledVocabularyTablesWriteOwnFiles() throws Exception {
        Path outputDir = Path.of("build/test-output/SQLGenerator/separate-tables");
        Files.createDirectories(outputDir);

        SQLGeneratorOptions options = SQLGeneratorOptions.load()
            .setOverwrite(true)
            .setSeparateLinkTables(true)
            .setSeparateControlledVocabularyTables(true);
        SQLGeneratorMetadata metadata = SQLGeneratorMetadata.load();

        Response<List<Path>> response = new SQLGenerator(options, outputDir)
            .generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), metadata);
        assertFalse(response.hasErrors(), () -> response.getMessagesCombined(","));

        Path studySql = outputDir.resolve("Study.sql");
        assertTrue(Files.exists(studySql));
        String studyDdl = Files.readString(studySql);
        assertFalse(studyDdl.contains("CREATE TABLE brapi_ObservationLevels"),
            "Study.sql must not embed ObservationLevels when separateControlledVocabularyTables=true");
        assertFalse(studyDdl.contains("CREATE TABLE brapi_ObservationVariableByStudy")
                && studyDdl.contains("Link table for Study to ObservationVariable"),
            "Study.sql must not embed link tables when separateLinkTables=true");

        Path observationLevelsSql = outputDir.resolve("ObservationLevels.sql");
        assertTrue(Files.exists(observationLevelsSql), "ObservationLevels.sql should be written separately");
        String observationLevelsDdl = Files.readString(observationLevelsSql);
        assertTrue(observationLevelsDdl.contains("CREATE TABLE brapi_ObservationLevels"));
        assertTrue(observationLevelsDdl.contains("levelName STRING"));
        assertFalse(observationLevelsDdl.contains("ARRAY<"));

        assertTrue(response.getResult().stream().anyMatch(path -> path.getFileName().toString().equals("ObservationLevels.sql")));
        assertTrue(response.getResult().size() > 37,
            "separate table files should increase generated path count above primary-only defaults");
    }

    void generate(SQLGeneratorOptions options, SQLGeneratorMetadata metadata, int expectedSize, String classpath) {
        Response<List<Path>> response = null;
        try {
            SQLGenerator generator = new SQLGenerator(options, Paths.get(classpath));

            response = generator.generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), metadata) ;

            assertNotNull(response);

            response.getAllErrors().forEach(this::printError);
            assertFalse(response.hasErrors());

            assertNotNull(response.getResult());
            assertEquals(expectedSize, response.getResult().size());

            response.getResult().forEach(path -> {
                assertTrue(Files.exists(path), "Generated file does not exist: " + path);
                assertTrue(Files.isRegularFile(path), "Generated path is not a file: " + path);

                try {
                    assertDDLEquals(Path.of(ClassLoader.getSystemResource("SQLGenerator/defaults").toURI()).resolve(path.getFileName().toString()), path) ;
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }

    private void assertDDLEquals(Path expectedPath, Path actualPath) {
        try {
            String expected = StringUtils.readStringFromPath(expectedPath).getResultOrThrow() ;
            String actual = StringUtils.readStringFromPath(actualPath).getResultOrThrow() ;

            assertMultilineEqual(expected, actual);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
    }

}