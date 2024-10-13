package org.brapi.schematools.core.xlsx;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.xlsx.options.XSSFWorkbookGeneratorOptions;

import java.beans.PropertyDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.brapi.schematools.core.response.Response.fail;

/**
 * Generates Excel (xlsx) file(s) for type and their field descriptions from a BrAPI Json Schema.
 */
@Slf4j
@AllArgsConstructor
public class XSSFWorkbookGenerator {
    private final BrAPISchemaReader schemaReader;
    private final XSSFWorkbookGeneratorOptions options;

    private Path outputPath ;
    private boolean overwrite ;

    /**
     * Creates a XSSFWorkbookGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link OntModelGeneratorOptions}.
     */
    public XSSFWorkbookGenerator(Path outputPath, boolean overwrite) {
        this(new BrAPISchemaReader(), XSSFWorkbookGeneratorOptions.load(), outputPath, overwrite) ;
    }

    /**
     * Creates a XSSFWorkbookGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link XSSFWorkbookGenerator}.
     * @param options The options to be used in the generation.
     */
    public XSSFWorkbookGenerator(XSSFWorkbookGeneratorOptions options, Path outputPath, boolean overwrite) {
        this(new BrAPISchemaReader(), options, outputPath, overwrite) ;
    }

    /**
     * Generates Excel (xlsx) file(s) for type and their field descriptions
     * from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @return the path of the Excel (xlsx) file(s) generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(Path schemaDirectory) {
        return schemaReader.readDirectories(schemaDirectory)
            .mapResultToResponse(brAPISchemas -> new XSSFWorkbookGenerator.Generator(brAPISchemas).generate()) ;
    }

    private class Generator {

        private final List<BrAPIClass> brAPISchemas ;

        public Generator(List<BrAPIClass> brAPISchemas) {
            this.brAPISchemas = brAPISchemas.stream().filter(this::isGenerating).
                sorted(Comparator.comparing(BrAPIClass::getName)).collect(Collectors.toList()) ;
        }

        public Response<List<Path>> generate() {
            try {
                Function<Response<List<Workbook>>, Response<?>> saveWorkbooks;
                return generateDataClasses(brAPISchemas) ;
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass.getMetadata() != null && !(brAPIClass.getMetadata().isRequest() || brAPIClass.getMetadata().isParameters());
        }

        private Response<List<Path>> generateDataClasses(List<BrAPIClass> brAPIClasses) {

            // TODO option to split by domain
            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet("Data Classes");

            createHeaderRow(workbook, sheet, options.getDataClassProperties()) ;
            createRows(sheet, 1, null, options.getDataClassProperties(), brAPIClasses);

            formatSheet(sheet, brAPIClasses.size()) ;

            sheet = workbook.createSheet("Data Classes Fields");

            createHeaderRow(workbook, sheet, options.getDataClassFieldProperties()) ;
            createRows(sheet, 1, BrAPIClass::getName, options.getDataClassFieldProperties(), brAPIClasses, this::findFields);

            formatSheet(sheet, brAPIClasses.size()) ;

            return saveWorkbook(workbook, outputPath).mapResult(Collections::singletonList) ;
        }

        private List<BrAPIObjectProperty> findFields(BrAPIClass brAPIClass) {
            List<BrAPIObjectProperty> properties = new ArrayList<>() ;

            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                return brAPIObjectType.getProperties();
            }

            return properties ;
        }

        private Response<Path> saveWorkbook(Workbook workbook, Path path) {
            try {
                FileOutputStream outputStream = new FileOutputStream(path.toFile());
                workbook.write(outputStream);
                workbook.close();
                return Response.success(path);
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private void formatSheet(Sheet sheet, int lastIndex) {
            CellRangeAddress ca =
                new CellRangeAddress(0, lastIndex,
                    sheet.getRow(0).getFirstCellNum(),
                    sheet.getRow(0).getLastCellNum() - 1);
            sheet.setAutoFilter(ca);
        }

        private void createHeaderRow(Workbook workbook, Sheet sheet, List<String> properties) {
            createHeaderRow(workbook, sheet, properties, null);
        }

        private void createHeaderRow(Workbook workbook, Sheet sheet, List<String> properties, String header) {
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = createHeaderStyle(workbook);

            int columnIndex = 0 ;

            if (header != null) {
                sheet.setColumnWidth(columnIndex, 6000);
                Cell headerCell = headerRow.createCell(columnIndex);
                headerCell.setCellValue(header);
                headerCell.setCellStyle(headerStyle);
                ++columnIndex ;
            }

            for (String property : properties) {
                sheet.setColumnWidth(columnIndex, 6000);
                Cell headerCell = headerRow.createCell(columnIndex);
                headerCell.setCellValue(StringUtils.toLabel(property));
                headerCell.setCellStyle(headerStyle);
                ++columnIndex ;
            }
        }

        private CellStyle createHeaderStyle(Workbook workbook) {
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFFont font = ((XSSFWorkbook) workbook).createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 12);
            font.setBold(true);
            headerStyle.setFont(font);

            return headerStyle ;
        }

        private <T> int createRows(Sheet sheet, int startIndex, String header, List<String> properties, List<T> values) {
            int rowIndex = startIndex ;

            for (Object value : values) {
                createRow(sheet, properties, rowIndex, header, value);

                ++rowIndex ;
            }

            return rowIndex ;
        }

        private <T, V> void createRows(Sheet sheet, int startIndex, Function<T, String> headerFunction, List<String> properties, List<T> values, Function<T, List<V>> valuesFunction) {
            int rowIndex = startIndex ;

            for (T value : values) {
                rowIndex = createRows(sheet, rowIndex, headerFunction.apply(value), properties, valuesFunction.apply(value));
            }
        }

        private <T> void createRow(Sheet sheet, List<String> properties, int rowIndex, String header, T bean)  {
            Row row = sheet.createRow(rowIndex);

            int columnIndex = 0 ;

            if (header != null) {
                Cell cell = row.createCell(columnIndex);
                cell.setCellValue(header);
                ++columnIndex ;
            }

            for (String property : properties) {
                Cell cell = row.createCell(columnIndex);
                Object value = null;
                try {
                    value = PropertyUtils.getProperty(bean, property);
                    if (value instanceof Boolean booleanValue) {
                        cell.setCellValue(booleanValue);
                    } else if (value instanceof Integer integerValue) {
                        cell.setCellValue(integerValue);
                    } else if (value instanceof Double doubleValue) {
                        cell.setCellValue(doubleValue);
                    } else if (value instanceof List listValue) {
                        cell.setCellValue(listValue.stream().collect(Collectors.joining(", ")).toString());
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    log.warn(String.format("Error parsing bean with property '%s', at index '%d' due to '%s'", property, rowIndex, e.getMessage())) ;
                }

                ++columnIndex ;
            }
        }
    }

}
