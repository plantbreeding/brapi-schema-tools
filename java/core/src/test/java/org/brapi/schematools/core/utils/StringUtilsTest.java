package org.brapi.schematools.core.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class StringUtilsTest {

    @Test
    void capitalise() {
        assertEquals("Datamatrix", StringUtils.capitalise("datamatrix")) ;
        assertEquals("Person", StringUtils.capitalise("person")) ;
        assertEquals("Germplasm", StringUtils.capitalise("germplasm")) ;
        assertEquals("Studies", StringUtils.capitalise("studies")) ;
        assertEquals("DataMatrices", StringUtils.capitalise("dataMatrices")) ;
        assertEquals("People", StringUtils.capitalise("people")) ;
        assertEquals("Germplasm", StringUtils.capitalise("germplasm")) ;
        assertEquals("Study", StringUtils.capitalise("study")) ;
        assertEquals("", StringUtils.capitalise("")) ;
        assertNull(StringUtils.capitalise(null));
    }

    @Test
    void toSingular() {
        assertEquals("DataMatrix", StringUtils.toSingular("DataMatrices")) ;
        assertEquals("Person", StringUtils.toSingular("People")) ;
        assertEquals("Germplasm", StringUtils.toSingular("Germplasm")) ;
        assertEquals("Study", StringUtils.toSingular("Studies")) ;
        assertEquals("dataMatrix", StringUtils.toSingular("dataMatrices")) ;
        assertEquals("person", StringUtils.toSingular("people")) ;
        assertEquals("germplasm", StringUtils.toSingular("germplasm")) ;
        assertEquals("study", StringUtils.toSingular("studies")) ;
        assertEquals("trial", StringUtils.toSingular("trials")) ;
    }

    @Test
    void toPlural() {
        assertEquals("DataMatrices", StringUtils.toPlural("DataMatrix")) ;
        assertEquals("People", StringUtils.toPlural("Person")) ;
        assertEquals("Germplasm", StringUtils.toPlural("Germplasm")) ;
        assertEquals("Studies", StringUtils.toPlural("Study")) ;
        assertEquals("dataMatrices", StringUtils.toPlural("dataMatrix")) ;
        assertEquals("people", StringUtils.toPlural("person")) ;
        assertEquals("germplasm", StringUtils.toPlural("germplasm")) ;
        assertEquals("studies", StringUtils.toPlural("study")) ;
        assertEquals("trials", StringUtils.toPlural("trial")) ;
    }

    @Test
    void toSnakeCase() {
        assertEquals("data_matrix", StringUtils.toSnakeCase("DataMatrix")) ;
        assertEquals("person", StringUtils.toSnakeCase("Person")) ;
        assertEquals("germplasm", StringUtils.toSnakeCase("Germplasm")) ;
        assertEquals("study", StringUtils.toSnakeCase("Study")) ;
        assertEquals("data_matrix", StringUtils.toSnakeCase("dataMatrix")) ;
        assertEquals("person", StringUtils.toSnakeCase("person")) ;
        assertEquals("germplasm", StringUtils.toSnakeCase("germplasm")) ;
        assertEquals("study", StringUtils.toSnakeCase("study")) ;
        assertEquals("trial", StringUtils.toSnakeCase("trial")) ;
        assertEquals("breeding_method", StringUtils.toSnakeCase("BreedingMethod")) ;
        assertEquals("inventory_lot_attribute_value", StringUtils.toSnakeCase("InventoryLotAttributeValue")) ;
    }

    @Test
    void makeValidName() {
        assertEquals("null", StringUtils.makeValidName(null)) ;
        assertEquals("blank", StringUtils.makeValidName("")) ;
        assertEquals("N1", StringUtils.makeValidName("1")) ;
        assertEquals("this_is_a_test", StringUtils.makeValidName("this/is/a/test")) ;
        assertEquals("this_is_a_test", StringUtils.makeValidName("this.is.a.test")) ;
        assertEquals("this_is_a_test", StringUtils.makeValidName("this-is-a-test")) ;
    }

    @Test
    void toSentenceCase() {
        assertEquals("SentenceCase", StringUtils.toSentenceCase("SentenceCase")) ;
        assertEquals("ParameterCase", StringUtils.toSentenceCase("parameterCase")) ;
    }

    @Test
    void toParameterCase() {
        assertEquals("sentenceCase", StringUtils.toParameterCase("SentenceCase")) ;
        assertEquals("parameterCase", StringUtils.toParameterCase("parameterCase")) ;
    }

    @Test
    void startsWithLowerCase() {
        assertTrue(StringUtils.startsWithLowerCase("lowerCase")) ;
        assertFalse(StringUtils.startsWithLowerCase("UpperCase")) ;
    }

    @Test
    void startsWithUpperCase() {
        assertFalse(StringUtils.startsWithUpperCase("lowerCase")) ;
        assertTrue(StringUtils.startsWithUpperCase("UpperCase")) ;
    }

    @Test
    void toLabel() {
        assertEquals("Sentence Case", StringUtils.toLabel("SentenceCase")) ;
        assertEquals("Parameter Case", StringUtils.toLabel("parameterCase")) ;
    }

    @Test
    void isMultilineEqual() {
        assertTrue(StringUtils.isMultilineEqual("Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.", "Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.")) ;
        assertTrue(StringUtils.isMultilineEqual("Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.", "Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.")) ;

        assertFalse(StringUtils.isMultilineEqual("1Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.", "Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.")) ;


        assertFalse(StringUtils.isMultilineEqual("Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut 2nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.", "Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.")) ;

        assertFalse(StringUtils.isMultilineEqual("Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut 3velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.", "Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
            "\n" +
            "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
            "\n" +
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.")) ;

        assertFalse(StringUtils.isMultilineEqual("Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.",
            "Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
                "\n" +
                "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
                "\n" +
                "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.")) ;

        assertFalse(StringUtils.isMultilineEqual("Lorem ipsum dolor sit amet. Ab earum voluptatem et delectus perferendis eos doloribus accusamus eum velit consequatur ut Quis eaque ut quaerat error. Qui quidem repellendus eos aliquam eveniet sed distinctio debitis id odit quasi. Et velit cupiditate ea omnis fugit et doloribus nihil hic voluptatem libero sit blanditiis odit ut fugit voluptatem qui rerum perferendis. In consequatur magnam aut galisum doloremque sit dolores saepe aut impedit vero est obcaecati excepturi et aspernatur rerum.\n" +
                "\n" +
                "Ut nobis quae non voluptatibus fuga ab odit voluptatum. Ut incidunt eligendi est repellendus velit id dolorem enim et nemo enim est sequi libero aut voluptatem illum. Qui libero culpa ut quos dolor ut possimus dolor et tempora debitis. In aliquid tempore et maiores iusto ut doloribus eaque.\n" +
                "\n" +
                "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.",
            "Ut velit quae ut doloremque molestiae id provident dolor. Et nihil voluptate eum voluptatibus doloremque et eaque praesentium. In repellendus autem qui corporis quidem ut quia molestiae vel tempora galisum.")) ;
    }

    @Test
    void isJSONEqual() {
        try {
            String expected = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.json").toURI())).getResultOrThrow();
            String actual = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.json").toURI())).getResultOrThrow();

            assertTrue(StringUtils.isJSONEqual(expected, actual));

            actual = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.json").toURI())).getResultOrThrow();

            assertFalse(StringUtils.isJSONEqual(expected, actual));

        } catch (Exception e) {
            fail(e.getMessage()) ;
        }

    }

    @Test
    void testEscapeQuotes() {
        assertEquals("\\\" \\' SimpleText123 ", StringUtils.escapeQuotes("\\\" ' SimpleText123 "));
        assertEquals(
            "`levelOrder` defines where that level exists in the hierarchy of levels. `levelOrder`\\'s lower numbers \nare at the top of the hierarchy (ie field -> 1) and higher numbers are at the bottom of the hierarchy (ie plant -> 9). \n\nFor more information on Observation Levels, please review the <a target=\"_blank\" href=\"https://wiki.brapi.org/index.php/Observation_Levels\">Observation Levels documentation</a>. ",
            StringUtils.escapeQuotes("`levelOrder` defines where that level exists in the hierarchy of levels. `levelOrder`'s lower numbers \nare at the top of the hierarchy (ie field -> 1) and higher numbers are at the bottom of the hierarchy (ie plant -> 9). \n\nFor more information on Observation Levels, please review the <a target=\"_blank\" href=\"https://wiki.brapi.org/index.php/Observation_Levels\">Observation Levels documentation</a>. ")) ;
    }

    @Test
    void testEscapeSpecialCharacters() {
        assertEquals("\\#\\$\\%\\^\\&\\*\\(\\)\\ SimpleText123\\ ", StringUtils.escapeSpecialCharacters("#$%^&*() SimpleText123 "));
    }

    @Test
    void testRemoveCarriageReturns() {
        assertEquals("test1 test2", StringUtils.removeCarriageReturns("test1\ntest2"));
        assertEquals("test3  test4", StringUtils.removeCarriageReturns("test3\n\rtest4"));
        assertEquals("test5 test6", StringUtils.removeCarriageReturns("test5\ntest6"));
    }

    @Test
    void testExtractFirstLine() {
        // Basic case
        assertEquals("This is the first line.", StringUtils.extractFirstLine("This is the first line. This is the second line."));
        // No full stop
        assertEquals("No full stop in this string", StringUtils.extractFirstLine("No full stop in this string"));
        // Carriage returns and newlines
        assertEquals("First line only.", StringUtils.extractFirstLine("First line only.\nSecond line here."));
        assertEquals("First line only.", StringUtils.extractFirstLine("First line only.\rSecond line here."));
        assertEquals("First line only.", StringUtils.extractFirstLine("First line only.\r\nSecond line here."));
        // Leading/trailing whitespace
        assertEquals("First line.", StringUtils.extractFirstLine("  First line.  Second line."));
        // Empty string
        assertEquals("", StringUtils.extractFirstLine(""));
        // Null input
        assertNull(StringUtils.extractFirstLine(null));
        // Period inside square brackets
        assertEquals("This is a test [with a period. inside brackets].", StringUtils.extractFirstLine("This is a test [with a period. inside brackets]. This is after."));
        // Period inside parentheses
        assertEquals("Sentence (with a period. inside parens).", StringUtils.extractFirstLine("Sentence (with a period. inside parens). Next sentence."));
        // Periods inside both types of brackets
        assertEquals("Start [a.b (c.d)].", StringUtils.extractFirstLine("Start [a.b (c.d)]. End."));
        // Multiple nested brackets
        assertEquals("A [b (c.d [e.f] g.h) i.j] k.", StringUtils.extractFirstLine("A [b (c.d [e.f] g.h) i.j] k. End."));
        // Periods inside and outside brackets
        assertEquals("First (ignore. this) sentence.", StringUtils.extractFirstLine("First (ignore. this) sentence. Second sentence."));
    }

}