package org.brapi.schematools.core.sql;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.isMultilineEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
class ANSICreateTableDDLGeneratorTest {

    private static List<BrAPIClass> brAPIClasses;

    @BeforeAll
    static void setup() throws URISyntaxException {
        BrAPISchemaReader schemaReader = new BrAPISchemaReader();
        brAPIClasses = schemaReader.readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI())).getResultOrThrow() ;
    }

    @Test
    void generateStudy() {
        generate(SQLGeneratorOptions.load(), SQLGeneratorMetadata.load(),"Study", "SQLGenerator/ANSI/Study.sql") ;
    }

    @Test
    void generateStudyWithUsing() {
        generate(SQLGeneratorOptions.load().setTableUsing("delta"), SQLGeneratorMetadata.load(),"Study", "SQLGenerator/ANSI/Study-using.sql") ;
    }

    @Test
    void generateStudyWithClustering() {
        generate(SQLGeneratorOptions.load().setClustering(true), SQLGeneratorMetadata.load(),"Study", "SQLGenerator/ANSI/Study-clustering.sql") ;
    }

    @Test
    void generateStudyWithTableProperties() {
        generate(SQLGeneratorOptions.load().setTableProperties(Map.of("delta.enableChangeDataFeed", true)), SQLGeneratorMetadata.load(), "Study", "SQLGenerator/ANSI/Study-table-properties.sql") ;
    }

    @Test
    void generateStudyWithUsingAndClustering() {
        generate(SQLGeneratorOptions.load().setTableUsing("delta").setClustering(true), SQLGeneratorMetadata.load(),"Study", "SQLGenerator/ANSI/Study-using-and-clustering.sql") ;
    }

    @Test
    void generateStudyWithClusteringAndTableProperties() {
        generate(SQLGeneratorOptions.load().setClustering(true).setTableProperties(Map.of("delta.enableChangeDataFeed", true)), SQLGeneratorMetadata.load(),"Study", "SQLGenerator/ANSI/Study-clustering-and-table-properties.sql") ;
    }

    @Test
    void generateStudyWithUsingAndTableProperties() {
        generate(SQLGeneratorOptions.load().setTableUsing("delta").setTableProperties(Map.of("delta.enableChangeDataFeed", true)), SQLGeneratorMetadata.load(),"Study", "SQLGenerator/ANSI/Study-using-and-table-properties.sql") ;
    }

    @Test
    void generateStudyWithUsingAndClusteringAndTableProperties() {
        generate(SQLGeneratorOptions.load().setTableUsing("delta").setClustering(true).setTableProperties(Map.of("delta.enableChangeDataFeed", true)), SQLGeneratorMetadata.load(),"Study", "SQLGenerator/ANSI/Study-using-and-clustering-and-table-properties.sql") ;
    }

    void generate(SQLGeneratorOptions options, SQLGeneratorMetadata metadata, String className, String classPath) {
        Response<String> response = null;
        try {
            response = new ANSICreateTableDDLGenerator(options, metadata, brAPIClasses)
                .generateDDLForObjectType(find(className));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }

        assertNotNull(response);

        response.getAllErrors().forEach(this::printError);
        assertFalse(response.hasErrors());

        assertNotNull(response.getResult());

        assertDDLEquals(classPath, response.getResult()) ;
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }

    private BrAPIObjectType find(String className) {
        return (BrAPIObjectType)brAPIClasses.stream().filter(brAPIClass -> brAPIClass instanceof BrAPIObjectType && brAPIClass.getName().equals(className)).findFirst().orElseThrow() ;
    }

    private void assertDDLEquals(String classPath, String ddl) {
        try {
            String expected = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource(classPath).toURI())).getResultOrThrow() ;
            String actual = format(ddl).getResultOrThrow() ;

            if (!isMultilineEqual(expected, actual)) {
                Path build = Paths.get("build/test-output", classPath);
                Files.createDirectories(build.getParent()) ;
                Files.writeString(build, actual);
            }

            assertMultilineEqual(expected, actual);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
    }

    private Response<String> format(String sqlStr) {
        try {
            return success(SqlFormatter.format(sqlStr)) ;
        } catch (Exception e) {
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    public static void assertMultilineEqual(String expected, String actual) {
        BufferedReader expectedReader = new BufferedReader(new StringReader(expected));
        BufferedReader actualReader = new BufferedReader(new StringReader(actual));

        boolean equals = true;

        try {
            String expectedLine = expectedReader.readLine() ;
            String actualLine = actualReader.readLine() ;

            int line = 1 ;

            while (equals && expectedLine != null && actualLine != null) {
                equals = expectedLine.equals(actualLine);
                assertEquals(expectedLine, actualLine, String.format("Expected '%s' but got '%s' at line %d", expectedLine, actualLine, line));

                expectedLine = expectedReader.readLine() ;
                actualLine = actualReader.readLine() ;

                ++line;
            }

            assertNull(expectedLine, String.format("Expected no more line in expected string at line %d", line));
            assertNull(actualLine, String.format("Expected no more line in expected string at line %d", line));
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
    }
}