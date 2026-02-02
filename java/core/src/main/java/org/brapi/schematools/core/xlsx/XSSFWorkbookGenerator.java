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
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.xlsx.options.ColumnOption;
import org.brapi.schematools.core.xlsx.options.ValuePropertyOption;
import org.brapi.schematools.core.xlsx.options.XSSFWorkbookGeneratorOptions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.brapi.schematools.core.response.Response.fail;

/**
 * Generates Excel (xlsx) file(s) for type and their field descriptions from a BrAPI JSON Schema.
 */
@Slf4j
@AllArgsConstructor
public class XSSFWorkbookGenerator {
    private final BrAPISchemaReader schemaReader;
    private final XSSFWorkbookGeneratorOptions options;
    private final Path outputPath ;

    /**
     * Creates a XSSFWorkbookGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link XSSFWorkbookGeneratorOptions}.
     * @param outputPath the path of the output file or directory
     */
    public XSSFWorkbookGenerator(Path outputPath) {
        this(XSSFWorkbookGeneratorOptions.load(), outputPath) ;
    }

    /**
     * Creates a XSSFWorkbookGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link XSSFWorkbookGeneratorOptions}.
     * @param options The options to be used in the generation.
     * @param outputPath the path of the output file or directory
     */
    public XSSFWorkbookGenerator(XSSFWorkbookGeneratorOptions options, Path outputPath) {
        this(new BrAPISchemaReader(), options, outputPath) ;
    }

    /**
     * Generates Excel (xlsx) file(s) for type and their field descriptions
     * from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI JSON schema and the additional subdirectories called 'Requests'
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

        private final Map<String, BrAPIClass> brAPIClasses ;

        public Generator(List<BrAPIClass> brAPISchemas) {

            brAPIClasses = BrAPIClassCacheBuilder.createMap(this::isGenerating, brAPISchemas) ;
        }

        public Response<List<Path>> generate() {
            try {
                return generateDataClasses(new ArrayList<>(brAPIClasses.values())) ;
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass.getMetadata() == null || !(brAPIClass.getMetadata().isRequest() || brAPIClass.getMetadata().isParameters());
        }

        private Response<List<Path>> generateDataClasses(List<BrAPIClass> brAPIClasses) {

            // TODO option to split by domain
            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet("Data Classes");

            createHeaderRow(workbook, sheet, options.getDataClassProperties()) ;
            createRows(sheet, 1, null, options.getDataClassProperties(), brAPIClasses);

            formatSheet(sheet, brAPIClasses.size()) ;

            sheet = workbook.createSheet("Data Classes Fields");

            createHeaderRow(workbook, sheet, options.getDataClassFieldProperties(), options.getDataClassFieldHeaders()) ;
            createRows(sheet, 1, this::getHeaders, options.getDataClassFieldProperties(), brAPIClasses, this::findFields);

            formatSheet(sheet, brAPIClasses.size()) ;

            return saveWorkbook(workbook, outputPath).mapResult(Collections::singletonList) ;
        }

        private List<String> getHeaders(BrAPIClass brAPIClass) {
            return Arrays.asList(brAPIClass.getName(), brAPIClass.getModule()) ;
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

        private void createHeaderRow(Workbook workbook, Sheet sheet, List<ColumnOption> columns) {
            createHeaderRow(workbook, sheet, columns, null);
        }

        private void createHeaderRow(Workbook workbook, Sheet sheet, List<ColumnOption> columns, List<String> headers) {
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = createHeaderStyle(workbook);

            final AtomicInteger columnIndex = new AtomicInteger(0) ;

            if (headers != null) {
                headers.forEach(header -> {
                    sheet.setColumnWidth(columnIndex.get(), 6000);
                    Cell headerCell = headerRow.createCell(columnIndex.getAndIncrement());
                    headerCell.setCellValue(header);
                    headerCell.setCellStyle(headerStyle);
                });
            }

            for (ColumnOption column : columns) {
                sheet.setColumnWidth(columnIndex.get(), 6000);
                Cell headerCell = headerRow.createCell(columnIndex.getAndIncrement());
                headerCell.setCellValue(column.getLabel() != null ? column.getLabel() : StringUtils.toLabel(column.getName()));
                headerCell.setCellStyle(headerStyle);
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

        private <T> int createRows(Sheet sheet, int startIndex, List<String> headers, List<ColumnOption> columns, List<T> values) {
            int rowIndex = startIndex ;

            for (Object value : values) {
                createRow(sheet, columns, rowIndex, headers, value);

                ++rowIndex ;
            }

            return rowIndex ;
        }

        private <T, V> void createRows(Sheet sheet, int startIndex, Function<T, List<String>> headerFunction, List<ColumnOption> columns, List<T> values, Function<T, List<V>> valuesFunction) {
            int rowIndex = startIndex ;

            for (T value : values) {
                rowIndex = createRows(sheet, rowIndex, headerFunction.apply(value), columns, valuesFunction.apply(value));
            }
        }

        private <T> void createRow(Sheet sheet, List<ColumnOption> columns, int rowIndex, List<String> headers, T bean)  {
            Row row = sheet.createRow(rowIndex);

            final AtomicInteger columnIndex = new AtomicInteger(0) ;

            if (headers != null) {
                headers.forEach(header -> {
                    Cell cell = row.createCell(columnIndex.getAndIncrement());
                    cell.setCellValue(header);
                });
            }

            for (ColumnOption column : columns) {
                try {
                    updateCellValue(row.createCell(columnIndex.getAndIncrement()), column, column.getDefaultValue(), rowIndex, PropertyUtils.getProperty(bean, column.getName())) ;
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    log.warn("Error parsing bean with property '{}', at index '{}' due to '{}'", column.getName(), rowIndex, e.getMessage());
                }
            }
        }

        private <T> void updateCellValue(Cell cell, ValuePropertyOption column, Object defaultValue, int rowIndex, Object value) {
            try {
                if (value instanceof Boolean booleanValue) {
                    cell.setCellValue(booleanValue);
                } else if (value instanceof Integer integerValue) {
                    cell.setCellValue(integerValue);
                } else if (value instanceof Double doubleValue) {
                    cell.setCellValue(doubleValue);
                } else if (value instanceof List listValue) {
                    if (column.getIndex() != null) {
                        updateCellValue(cell, column, defaultValue, rowIndex, listValue.get(column.getIndex()));
                    } else {
                        cell.setCellValue(listValue.stream().collect(Collectors.joining(", ")).toString());
                    }
                } else if (value instanceof Map mapValue) {
                    if (column.getKey() != null) {
                        updateCellValue(cell, column, defaultValue, rowIndex, mapValue.get(column.getKey()));
                    } else {
                        cell.setCellValue(mapValue.entrySet().stream().collect(Collectors.joining(", ")).toString());
                    }
                } else if (value != null) {
                    if (column.getChildProperty() != null) {
                        updateCellValue(cell, column.getChildProperty(), defaultValue, rowIndex, PropertyUtils.getProperty(value, column.getChildProperty().getName())) ;
                    } else {
                        cell.setCellValue(value.toString());
                    }
                } else if (column.getDefaultValue() != null) {
                    updateCellValue(cell, column, null, rowIndex, column.getDefaultValue());
                } else if (defaultValue != null) {
                    updateCellValue(cell, column, null, rowIndex, defaultValue);
                }
            } catch (Exception e) {
                log.warn("Error parsing bean with property '{}', at row index '{}' due to '{}'", column.getName(), rowIndex, e.getMessage());
            }
        }
    }
}
