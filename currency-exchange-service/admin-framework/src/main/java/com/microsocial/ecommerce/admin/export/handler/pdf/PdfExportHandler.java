package com.gogidix.shared.ecommerce.admin.export.handler.pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.microsocial.ecommerce.admin.export.ExportException;
import com.microsocial.ecommerce.admin.export.ExportFormat;
import com.microsocial.ecommerce.admin.export.ExportHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * PDF export handler implementation using iText 7.
 */
@Component
public class PdfExportHandler<T> implements ExportHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(PdfExportHandler.class);
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final float[] COLUMN_WIDTHS = {3, 5, 8, 4, 3, 5, 8};
    
    // Colors
    private static final Color HEADER_BG_COLOR = ColorConstants.LIGHT_GRAY;
    private static final Color HEADER_TEXT_COLOR = ColorConstants.BLACK;
    private static final Color ROW_ALTERNATE_BG_COLOR = new DeviceRgb(248, 248, 248);
    private static final Color BORDER_COLOR = new DeviceRgb(221, 221, 221);
    
    // Fonts
    private static final float HEADER_FONT_SIZE = 10f;
    private static final float BODY_FONT_SIZE = 9f;
    private static final float TITLE_FONT_SIZE = 14f;

    @Override
    public void export(List<T> data, OutputStream outputStream, Map<String, Object> options) throws ExportException {
        if (data == null || data.isEmpty()) {
            log.warn("No data provided for PDF export");
            return;
        }

        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Get export options
            String title = getOptionAsString(options, "title", "Export Report");
            boolean includeHeader = getOptionAsBoolean(options, "includeHeader", true);
            String dateFormat = getOptionAsString(options, "dateFormat", DEFAULT_DATE_FORMAT);
            boolean landscape = getOptionAsBoolean(options, "landscape", false);
            
            // Set page layout
            if (landscape) {
                pdf.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4.rotate());
            }
            
            // Add title
            addTitle(document, title);
            
            // Get all fields from the first data item
            T firstItem = data.get(0);
            List<Field> fields = getAllFields(firstItem.getClass());
            List<Method> getters = getGetters(firstItem.getClass(), fields);
            
            // Create table
            Table table = new Table(UnitValue.createPercentArray(fields.size()))
                    .useAllAvailableWidth()
                    .setFontSize(BODY_FONT_SIZE);
            
            // Add header row if needed
            if (includeHeader) {
                addTableHeader(table, fields);
            }
            
            // Add data rows
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            for (int i = 0; i < data.size(); i++) {
                T item = data.get(i);
                // Alternate row background color
                boolean isEvenRow = i % 2 == 1;
                addTableRow(table, item, fields, getters, sdf, isEvenRow);
            }
            
            // Add table to document
            document.add(table);
            
            // Add footer
            addFooter(document);
            
            log.info("Exported {} items to PDF format", data.size());
            
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new ExportException("Error generating PDF file", e);
        }
    }
    
    @Override
    public ExportFormat getFormat() {
        return ExportFormat.PDF;
    }
    
    private void addTitle(Document document, String title) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        Paragraph titlePara = new Paragraph(title)
                .setFont(font)
                .setFontSize(TITLE_FONT_SIZE)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(titlePara);
        
        // Add current date
        Paragraph datePara = new Paragraph("Generated on: " + new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date()))
                .setFontSize(BODY_FONT_SIZE - 1)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(15);
        document.add(datePara);
    }
    
    private void addTableHeader(Table table, List<Field> fields) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        
        for (Field field : fields) {
            Cell cell = new Cell()
                    .add(new Paragraph(field.getName()).setFont(font).setFontSize(HEADER_FONT_SIZE))
                    .setBackgroundColor(HEADER_BG_COLOR)
                    .setFontColor(HEADER_TEXT_COLOR)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
            
            table.addHeaderCell(cell);
        }
    }
    
    private void addTableRow(Table table, T item, List<Field> fields, List<Method> getters, 
                            SimpleDateFormat sdf, boolean alternateRow) 
            throws IllegalAccessException, InvocationTargetException, IOException {
            
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Object value = getters.get(i).invoke(item);
            String cellValue = formatValue(value, sdf);
            
            Cell cell = new Cell()
                    .add(new Paragraph(cellValue).setFont(font).setFontSize(BODY_FONT_SIZE))
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.5f))
                    .setPadding(5);
            
            if (alternateRow) {
                cell.setBackgroundColor(ROW_ALTERNATE_BG_COLOR);
            }
            
            // Right-align numeric values
            if (value instanceof Number) {
                cell.setTextAlignment(TextAlignment.RIGHT);
            }
            
            table.addCell(cell);
        }
    }
    
    private void addFooter(Document document) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        Paragraph footer = new Paragraph("Generated by MicroSocial E-commerce Admin Framework")
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);
    }
    
    private String formatValue(Object value, SimpleDateFormat sdf) {
        if (value == null) {
            return "";
        } else if (value instanceof Date) {
            return sdf.format((Date) value);
        } else if (value instanceof LocalDate) {
            return sdf.format(java.sql.Date.valueOf((LocalDate) value));
        } else if (value instanceof LocalDateTime) {
            return sdf.format(java.sql.Timestamp.valueOf((LocalDateTime) value));
        } else if (value instanceof Double || value instanceof Float) {
            return String.format("%.2f", ((Number) value).doubleValue());
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            return value.toString();
        }
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
    
    // Helper class for RGB colors
    private static class DeviceRgb extends com.itextpdf.kernel.colors.DeviceRgb {
        public DeviceRgb(int r, int g, int b) {
            super(r, g, b);
        }
    }
}
