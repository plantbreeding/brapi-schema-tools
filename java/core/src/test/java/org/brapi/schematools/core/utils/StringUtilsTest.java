package org.brapi.schematools.core.utils;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class StringUtilsTest {

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
}