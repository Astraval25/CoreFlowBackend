package com.astraval.coreflow.common.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.font.constants.StandardFonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Reusable PDF builder for generating documents like salary slips, invoices,
 * receipts, etc.
 * Usage:
 * PdfBuilder builder = PdfBuilder.create();
 * builder.addTitle("Salary Slip");
 * builder.addKeyValue("Employee", "Ravi Kumar");
 * Table table = builder.createTable(columns, widths);
 * builder.addTable(table);
 * byte[] pdf = builder.build();
 */
public class PdfBuilder {

    // Brand colors
    public static final DeviceRgb PRIMARY = new DeviceRgb(33, 37, 41);
    public static final DeviceRgb ACCENT = new DeviceRgb(13, 110, 253);
    public static final DeviceRgb HEADER_BG = new DeviceRgb(33, 37, 41);
    public static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    public static final DeviceRgb ROW_ALT = new DeviceRgb(248, 249, 250);
    public static final DeviceRgb BORDER_COLOR = new DeviceRgb(222, 226, 230);
    public static final DeviceRgb LIGHT_BG = new DeviceRgb(248, 249, 250);

    private final ByteArrayOutputStream baos;
    private final PdfDocument pdfDocument;
    private final Document document;
    private final PdfFont regular;
    private final PdfFont bold;

    private PdfBuilder() throws IOException {
        this.baos = new ByteArrayOutputStream();
        this.pdfDocument = new PdfDocument(new PdfWriter(baos));
        this.pdfDocument.setDefaultPageSize(PageSize.A4);
        this.document = new Document(pdfDocument, PageSize.A4);
        this.document.setMargins(36, 36, 36, 36);
        this.regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        this.bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    }

    public static PdfBuilder create() throws IOException {
        return new PdfBuilder();
    }

    // ── Title / headings ──

    public PdfBuilder addTitle(String title) {
        Paragraph p = new Paragraph(title)
                .setFont(bold)
                .setFontSize(18)
                .setFontColor(PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4);
        document.add(p);
        return this;
    }

    public PdfBuilder addSubTitle(String text) {
        Paragraph p = new Paragraph(text)
                .setFont(regular)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(12);
        document.add(p);
        return this;
    }

    public PdfBuilder addSectionHeader(String text) {
        Paragraph p = new Paragraph(text)
                .setFont(bold)
                .setFontSize(12)
                .setFontColor(ACCENT)
                .setMarginTop(14)
                .setMarginBottom(6)
                .setBorderBottom(new SolidBorder(ACCENT, 1));
        document.add(p);
        return this;
    }

    // ── Info grid (key-value pairs in a 2-column layout) ──

    public PdfBuilder addInfoGrid(String[][] pairs) {
        // int cols = 4; // label, value, label, value
        Table table = new Table(UnitValue.createPercentArray(new float[] { 25, 25, 25, 25 }))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        for (int i = 0; i < pairs.length; i += 2) {
            // Left pair
            table.addCell(labelCell(pairs[i][0]));
            table.addCell(valueCell(pairs[i][1]));

            // Right pair (if exists)
            if (i + 1 < pairs.length) {
                table.addCell(labelCell(pairs[i + 1][0]));
                table.addCell(valueCell(pairs[i + 1][1]));
            } else {
                table.addCell(emptyCell());
                table.addCell(emptyCell());
            }
        }

        document.add(table);
        return this;
    }

    public PdfBuilder addKeyValue(String label, String value) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                .useAllAvailableWidth()
                .setMarginBottom(2);
        table.addCell(labelCell(label));
        table.addCell(valueCell(value));
        document.add(table);
        return this;
    }

    // ── Tables ──

    public Table createTable(String[] headers, float[] widths) {
        Table table = new Table(UnitValue.createPercentArray(widths))
                .useAllAvailableWidth()
                .setMarginTop(6)
                .setMarginBottom(10);

        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                    .setBackgroundColor(HEADER_BG)
                    .setPadding(6)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            table.addHeaderCell(cell);
        }

        return table;
    }

    public void addTableRow(Table table, String[] values, boolean alternate) {
        for (String value : values) {
            Cell cell = new Cell()
                    .add(new Paragraph(value != null ? value : "").setFont(regular).setFontSize(9))
                    .setPadding(5)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.5f));
            if (alternate) {
                cell.setBackgroundColor(ROW_ALT);
            }
            table.addCell(cell);
        }
    }

    public void addTableRow(Table table, String[] values, boolean alternate, TextAlignment[] alignments) {
        for (int i = 0; i < values.length; i++) {
            Cell cell = new Cell()
                    .add(new Paragraph(values[i] != null ? values[i] : "").setFont(regular).setFontSize(9))
                    .setPadding(5)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.5f));
            if (alternate) {
                cell.setBackgroundColor(ROW_ALT);
            }
            if (alignments != null && i < alignments.length) {
                cell.setTextAlignment(alignments[i]);
            }
            table.addCell(cell);
        }
    }

    public PdfBuilder addTable(Table table) {
        document.add(table);
        return this;
    }

    // ── Summary / totals row ──

    public PdfBuilder addTotalRow(String label, String value) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 70, 30 }))
                .useAllAvailableWidth()
                .setMarginBottom(2);

        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(10).setFontColor(PRIMARY))
                .setPadding(6)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFont(bold).setFontSize(11).setFontColor(ACCENT))
                .setPadding(6)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(LIGHT_BG);

        table.addCell(labelCell);
        table.addCell(valueCell);
        document.add(table);
        return this;
    }

    // ── Divider ──

    public PdfBuilder addDivider() {
        Paragraph p = new Paragraph("")
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setMarginTop(8)
                .setMarginBottom(8);
        document.add(p);
        return this;
    }

    // ── Spacer ──

    public PdfBuilder addSpacer(float height) {
        document.add(new Paragraph("").setMarginBottom(height));
        return this;
    }

    // ── Footer text ──

    public PdfBuilder addFooter(String text) {
        Paragraph p = new Paragraph(text)
                .setFont(regular)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(p);
        return this;
    }

    // ── Text block ──

    public PdfBuilder addText(String text) {
        document.add(new Paragraph(text).setFont(regular).setFontSize(10).setMarginBottom(4));
        return this;
    }

    public PdfBuilder addBoldText(String text) {
        document.add(new Paragraph(text).setFont(bold).setFontSize(10).setMarginBottom(4));
        return this;
    }

    // ── Build ──

    public byte[] build() {
        document.close();
        return baos.toByteArray();
    }

    // ── Internal helpers ──

    private Cell labelCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(ColorConstants.GRAY))
                .setPadding(4)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell valueCell(String text) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "-").setFont(regular).setFontSize(9).setFontColor(PRIMARY))
                .setPadding(4)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell emptyCell() {
        return new Cell().setBorder(Border.NO_BORDER).setPadding(4);
    }
}
