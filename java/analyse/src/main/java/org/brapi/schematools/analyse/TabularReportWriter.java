package org.brapi.schematools.analyse;

import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dflib.DataFrame;
import org.dflib.excel.Excel;
import org.dflib.excel.ExcelSaver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.function.UnaryOperator.identity;

/**
 * Writes a tabular report to file.
 */
public class TabularReportWriter {

    private final ExcelSaver saver;
    private boolean autoFilterColumns = false ;
    private boolean freezePane = false ;


    private TabularReportWriter() {
        saver = Excel.saver()
            .autoSizeColumns() ;
    }

    /**
     * Creates the default Writer
     * @return the default Writer
     */
    public static TabularReportWriter writer() {
        return new TabularReportWriter() ;
    }

    /**
     * Auto-sizes all columns
     * @return the writer for method chaining
     */
    public TabularReportWriter autoSizeColumns() {
        this.saver.autoSizeColumns() ;
        return this;
    }

    /**
     * Auto-filter all columns
     * @return the writer for method chaining
     */
    public TabularReportWriter autoFilterColumns() {
        this.autoFilterColumns = true;
        return this;
    }

    /**
     * Freeze pane all columns
     * @return the writer for method chaining
     */
    public TabularReportWriter freezePane() {
        this.freezePane = true;
        return this;
    }

    /**
     * Write the tabular reports to an Excel file
     * @param reports the tabular reports to be written to an Excel file
     * @param path the path of the Excel file.
     * @throws IOException if th Excel file can be written.
     */
    public void writeToExcel(List<DataFrame> reports, Path path) throws IOException {

        saver.save(reports.stream().collect(Collectors.toMap(DataFrame::getName, identity())), path);

        updateHeaders(path) ;
    }

    /**
     * Write the tabular reports to an Excel file
     * @param report the tabular report to be written to an Excel file
     * @param path the path of the Excel file.
     * @throws IOException if th Excel file can be written.
     */
    public void writeToExcel(DataFrame report, Path path) throws IOException {
        saver.save(Collections.singletonMap(report.getName(), report), path);

        updateHeaders(path) ;
    }

    private void updateHeaders(Path path) throws IOException {
        if (needsUpdateAfterSave()) {
            try (InputStream inputStream = Files.newInputStream(path)) {
                Workbook workbook = WorkbookFactory.create(inputStream);

                StreamSupport.stream(workbook.spliterator(), false).forEach(this::addHeaders);

                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    workbook.write(outputStream);
                }
            }
        }
    }

    private void addHeaders(Sheet sheet) {

        if (autoFilterColumns) {
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 8));
        }

        if (freezePane) {
            sheet.createFreezePane(0, 1);
        }
    }

    private boolean needsUpdateAfterSave () {
        return autoFilterColumns || freezePane ;
    }
}
