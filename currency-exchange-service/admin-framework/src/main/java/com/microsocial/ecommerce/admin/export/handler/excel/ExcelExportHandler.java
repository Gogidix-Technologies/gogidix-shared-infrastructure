package com.gogidix.shared.ecommerce.admin.export.handler.excel;

import com.microsocial.ecommerce.admin.export.ExportException;
import com.microsocial.ecommerce.admin.export.ExportFormat;
import com.microsocial.ecommerce.admin.export.ExportHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Excel (XLSX) export handler implementation.
 */
@Component
public class ExcelExportHandler<T> implements ExportHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportHandler.class);
    private static final String DEFAULT_SHEET_NAME = "Export";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int DEFAULT_COLUMN_WIDTH = 20;

    @Override
    public void export(List<T> data, OutputStream outputStream, Map<String, Object> options) throws ExportException {
        if (data == null || data.isEmpty()) {
            log.warn("No data provided for Excel export");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // Get export options or use defaults
            String sheetName = getOptionAsString(options, "sheetName", DEFAULT_SHEET_NAME);
            boolean includeHeader = getOptionAsBoolean(options, "includeHeader", true);
            String dateFormat = getOptionAsString(options, "dateFormat", DEFAULT_DATE_FORMAT);
            
            // Create sheet
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Get all fields from the first data item
            T firstItem = data.get(0);
            List<Field> fields = getAllFields(firstItem.getClass());
            List<Method> getters = getGetters(firstItem.getClass(), fields);
            
            // Create header row if needed
            int rowIndex = 0;
            if (includeHeader) {
                Row headerRow = sheet.createRow(rowIndex++);
                createHeaderRow(headerRow, fields, workbook);
            }
            
            // Create data rows
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);
                createDataRow(row, item, fields, getters, sdf, workbook);
            }
            
            // Auto-size columns
            IntStream.range(0, fields.size())
                    .forEach(sheet::autoSizeColumn);
            
            // Set default column width
            for (int i = 0; i < fields.size(); i++) {
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(width, DEFAULT_COLUMN_WIDTH * 256)); // Convert to 1/256th of a character
            }
            
            // Write to output stream
            workbook.write(outputStream);
            log.info("Exported {} items to Excel format", data.size());
            
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new ExportException("Error generating Excel file", e);
        }
    }
    
    @Override
    public ExportFormat getFormat() {
        return ExportFormat.EXCEL_XLSX;
    }
    
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        
        // Get all fields including inherited ones
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(0, Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }
    
    private List<Method> getGetters(Class<?> clazz, List<Field> fields) {
        return fields.stream()
                .map(field -> {
                    String getterName = "get" + field.getName().substring(0, 1).toUpperCase() + 
                                       field.getName().substring(1);
                    try {
                        return clazz.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        // Try with 'is' prefix for boolean fields
                        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            try {
                                return clazz.getMethod("is" + field.getName().substring(0, 1).toUpperCase() + 
                                                    field.getName().substring(1));
                            } catch (NoSuchMethodException ex) {
                                return null;
                            }
                        }
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
    
    private void createHeaderRow(Row headerRow, List<Field> fields, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int i = 0; i < fields.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields.get(i).getName());
            cell.setCellStyle(headerStyle);
        }
    }
    
    private void createDataRow(Row row, T item, List<Field> fields, List<Method> getters, 
                              SimpleDateFormat dateFormat, Workbook workbook) 
            throws IllegalAccessException, InvocationTargetException {
            
        CellStyle dateCellStyle = createDateCellStyle(workbook, dateFormat.toPattern());
        
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Cell cell = row.createCell(i);
            
            try {
                Object value = getters.get(i).invoke(item);
                setCellValue(cell, value, dateCellStyle, dateFormat);
            } catch (Exception e) {
                log.warn("Error setting value for field: " + field.getName(), e);
                cell.setCellValue("");
            }
        }
    }
    
    private void setCellValue(Cell cell, Object value, CellStyle dateCellStyle, SimpleDateFormat dateFormat) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue(dateFormat.format((Date) value));
            cell.setCellStyle(dateCellStyle);
        } else if (value instanceof Calendar) {
            cell.setCellValue(dateFormat.format(((Calendar) value).getTime()));
            cell.setCellStyle(dateCellStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDateCellStyle(Workbook workbook, String dateFormat) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat(dateFormat));
        return style;
    }
    
    @SuppressWarnings("unchecked")
    private <V> V getOption(Map<String, Object> options, String key, V defaultValue) {
        return options != null && options.containsKey(key) 
                ? (V) options.get(key) 
                : defaultValue;
    }
    
    private String getOptionAsString(Map<String, Object> options, String key, String defaultValue) {
        return getOption(options, key, defaultValue);
    }
    
    private boolean getOptionAsBoolean(Map<String, Object> options, String key, boolean defaultValue) {
        return getOption(options, key, defaultValue);
    }
}
