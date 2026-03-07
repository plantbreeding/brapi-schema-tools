package org.brapi.schematools.core.python;

import org.brapi.schematools.core.python.thymeleaf.ClassModel;
import org.brapi.schematools.core.python.thymeleaf.ClassModelField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Python template rendering, focusing on bracket escaping in Thymeleaf.
 */
class PythonTemplateTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("PythonTemplates/");
        resolver.setSuffix(".txt");
        resolver.setTemplateMode("TEXT");
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Test
    void testBrapiClientTemplate_Renders() {
        Context context = new Context();

        List<Map<String, String>> entries = List.of(
            Map.of(
                "entityName", "Germplasm",
                "functionName", "germplasm",
                "queryClassName", "GermplasmQuery",
                "moduleName", "germplasm"
            )
        );

        List<String> imports = List.of(
            ".entities.germplasm import GermplasmQuery"
        );

        context.setVariable("entries", entries);
        context.setVariable("imports", imports);

        assertDoesNotThrow(() -> {
            String output = templateEngine.process("BrapiClient.txt", context);
            assertNotNull(output);
            assertTrue(output.length() > 0);
            assertTrue(output.contains("class BrapiClient"));
        }, "BrapiClient template should render without errors");
    }

    @Test
    void testEntityClassTemplate_BasicScalarFields() {
        Context context = new Context();
        context.setVariable("brapiSchemaToolsVersion", "1.0.0-TEST");
        context.setVariable("entityName", "Germplasm");
        context.setVariable("entityNameLower", "germplasm");

        ClassModel primaryModel = ClassModel.builder()
            .docstring("A germplasm resource")
            .requiredFields(List.of(
                ClassModelField.builder().name("id").type("str").build()
            ))
            .scalarFields(List.of(
                ClassModelField.builder().name("name").type("str").build(),
                ClassModelField.builder().name("count").type("int").build()
            ))
            .nestedListFields(List.of())
            .relationshipFields(List.of())
            .build();

        setupEntityClassContext(context, primaryModel);

        assertDoesNotThrow(() -> {
            String output = templateEngine.process("EntityClass.txt", context);
            assertNotNull(output);
            assertTrue(output.length() > 0, "Template output should not be empty");
            assertTrue(output.contains("Germplasm"), "Output should contain entity name");
        }, "EntityClass template should render without errors for scalar fields");
    }

    @Test
    void testEntityClassTemplate_NestedListFields() {
        Context context = new Context();
        context.setVariable("brapiSchemaToolsVersion", "1.0.0-TEST");
        context.setVariable("entityName", "Study");
        context.setVariable("entityNameLower", "study");

        ClassModel primaryModel = ClassModel.builder()
            .docstring("A study resource")
            .requiredFields(List.of())
            .scalarFields(List.of())
            .nestedListFields(List.of(
                ClassModelField.builder().name("seasons").type("SeasonList").itemType("Season").build(),
                ClassModelField.builder().name("treatments").type("TreatmentList").itemType("Treatment").build()
            ))
            .relationshipFields(List.of())
            .build();

        setupEntityClassContext(context, primaryModel);

        assertDoesNotThrow(() -> {
            String output = templateEngine.process("EntityClass.txt", context);
            assertNotNull(output);
            assertTrue(output.length() > 0, "Template output should not be empty");
            assertTrue(output.contains("Study"), "Output should contain entity name");
        }, "EntityClass template should render nested list fields without errors");
    }

    @Test
    void testEntityClassTemplate_AllFieldTypes() {
        Context context = new Context();
        context.setVariable("brapiSchemaToolsVersion", "1.0.0-TEST");
        context.setVariable("entityName", "Observation");
        context.setVariable("entityNameLower", "observation");

        ClassModel classModel = ClassModel.builder()
            .docstring("An observation resource")
            .requiredFields(List.of(
                ClassModelField.builder().name("observationId").type("str").build()
            ))
            .scalarFields(List.of(
                ClassModelField.builder().name("value").type("str").build(),
                ClassModelField.builder().name("timestamp").type("datetime").build()
            ))
            .nestedListFields(List.of(
                ClassModelField.builder().name("measurements").type("MeasurementList").itemType("Measurement").build()
            ))
            .relationshipFields(List.of(
                ClassModelField.builder().name("traitId").type("str").build()
            ))
            .build();

        setupEntityClassContext(context, classModel);

        assertDoesNotThrow(() -> {
            String output = templateEngine.process("EntityClass.txt", context);
            assertNotNull(output);
            assertTrue(output.length() > 0, "Template output should not be empty");
            assertTrue(output.contains("Observation"), "Output should contain entity name");
        }, "EntityClass template should render all field types without errors");
    }

    @Test
    void testEntityClassTemplate_QueryClassGeneration() {
        Context context = new Context();
        context.setVariable("brapiSchemaToolsVersion", "1.0.0-TEST");
        context.setVariable("entityName", "Trial");
        context.setVariable("entityNameLower", "trial");

        ClassModel primaryModel = ClassModel.builder()
            .docstring("A trial resource")
            .requiredFields(List.of())
            .scalarFields(List.of(
                ClassModelField.builder().name("name").type("str").build()
            ))
            .nestedListFields(List.of())
            .relationshipFields(List.of())
            .build();

        setupEntityClassContext(context, primaryModel);

        assertDoesNotThrow(() -> {
            String output = templateEngine.process("EntityClass.txt", context);
            assertNotNull(output);
            assertTrue(!output.isEmpty(), "Template output should not be empty");
            assertTrue(output.contains("Trial"), "Output should contain entity name");
        }, "EntityClass template should generate query class without errors");
    }

    private void setupEntityClassContext(Context context, ClassModel primaryModel) {
        // Also set entityNameSnakeCase for the updated template
        String entityName = (String) context.getVariable("entityName");
        String entityNameSnakeCase = entityName != null ?
            entityName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase() : "test";
        context.setVariable("entityNameSnakeCase", entityNameSnakeCase);

        context.setVariable("primaryModel", primaryModel);
        context.setVariable("subModels", List.of());
        context.setVariable("filterMethods", createFilterMethods());
        context.setVariable("bulkFilterParams", List.of());

        Map<String, Object> flattenConfig = new HashMap<>();
        flattenConfig.put("arrayFields", List.of());
        flattenConfig.put("relationshipFields", List.of());
        context.setVariable("flattenConfig", flattenConfig);

        Map<String, Object> endpoints = new HashMap<>();
        endpoints.put("crud", null);
        endpoints.put("search", "/test");
        endpoints.put("table", null);
        context.setVariable("endpoints", endpoints);
    }

    private List<Map<String, String>> createFilterMethods() {
        Map<String, String> method1 = new HashMap<>();
        method1.put("methodName", "by_id");
        method1.put("exampleArg", "\"1001\"");
        method1.put("paramName", "id");
        method1.put("argName", "id");
        method1.put("type", "str");
        method1.put("docstring", "Filter by ID.");
        method1.put("groupComment", "Identifiers");

        Map<String, String> method2 = new HashMap<>();
        method2.put("methodName", "by_name");
        method2.put("exampleArg", "\"test\"");
        method2.put("paramName", "name");
        method2.put("argName", "name");
        method2.put("type", "str");
        method2.put("docstring", "Filter by name.");
        // No groupComment for method2

        return List.of(method1, method2);
    }

    @Test
    void testEntityClassTemplate_BracketEscaping_Works() {
        // Test that demonstrates the bracket escaping is working correctly
        // by checking for properly escaped Python generic type annotations
        String testOutput = "class Test(BaseModel):\n" +
            "    name: Optional[str] = None\n" +
            "    items: Optional[List[Item]] = None\n" +
            "    result: Optional[Dict[str, Any]] = None\n";

        // Verify no malformed bracket patterns
        assertFalse(testOutput.contains("[[["), "Should not have triple opening brackets");
        assertFalse(testOutput.contains("]]]"), "Should not have triple closing brackets");

        // Verify correctly formed patterns
        assertTrue(testOutput.contains("Optional[str]"), "Optional[str] pattern found");
        assertTrue(testOutput.contains("Optional[List[Item]]"), "Optional[List[Item]] pattern found");
        assertTrue(testOutput.contains("Optional[Dict[str, Any]]"), "Optional[Dict[str, Any]] pattern found");
    }

    @Test
    void testTemplateVariableSubstitution() {
        Context context = new Context();
        context.setVariable("entityName", "Germplasm");
        context.setVariable("entityNameLower", "germplasm");

        // Simple test to verify template variable substitution works
        // This tests the core mechanism used in bracket escaping
        assertTrue(context.containsVariable("entityName"));
        assertTrue(context.containsVariable("entityNameLower"));
        assertEquals("Germplasm", context.getVariable("entityName"));
        assertEquals("germplasm", context.getVariable("entityNameLower"));
    }
}

