package com.astraval.coreflow.main_modules.orderdetails.service;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.customer.Customers;
import com.astraval.coreflow.main_modules.filestorage.FileStorage;
import com.astraval.coreflow.main_modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.main_modules.orderdetails.OrderDetails;
import com.astraval.coreflow.main_modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.main_modules.orderitemdetails.OrderItemDetails;
import com.astraval.coreflow.main_modules.orderitemdetails.OrderItemDetailsRepository;
import com.astraval.coreflow.main_modules.vendor.Vendors;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderBillPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DeviceRgb TITLE_BG = new DeviceRgb(25, 25, 25);
    private static final DeviceRgb TITLE_FG = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER = new DeviceRgb(40, 40, 40);
    private static final DeviceRgb MUTED = new DeviceRgb(90, 90, 90);

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemDetailsRepository orderItemDetailsRepository;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    public byte[] generateOrderBill(Long companyId, Long orderId) throws IOException {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!Boolean.TRUE.equals(order.getHasBill())) {
            throw new RuntimeException("Bill is not available for this order");
        }

        List<OrderItemDetails> items = orderItemDetailsRepository.findByOrderId(orderId);

        Companies seller = order.getSellerCompany();
        Customers customer = order.getCustomers();
        Vendors vendor = order.getVendors();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos))) {
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(30, 30, 30, 30);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont italic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            buildHeader(doc, seller, regular, bold);
            buildBillMeta(doc, order, customer, vendor, regular, bold);
            buildItemsTable(doc, items, order, regular, bold);
            buildFooter(doc, seller, regular, bold, italic);

            doc.close();
        }

        return baos.toByteArray();
    }

    private void buildHeader(Document doc, Companies seller, PdfFont regular, PdfFont bold) {
        Table header = new Table(UnitValue.createPercentArray(new float[] { 20, 55, 25 }))
                .useAllAvailableWidth()
                .setMarginBottom(4);

        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        Image logo = loadSellerLogo(seller);
        if (logo != null) {
            logo.setWidth(70).setHeight(70).setHorizontalAlignment(HorizontalAlignment.LEFT);
            logoCell.add(logo);
        } else {
            logoCell.add(new Paragraph("LOGO")
                    .setFont(bold)
                    .setFontSize(11)
                    .setFontColor(MUTED)
                    .setTextAlignment(TextAlignment.CENTER));
        }
        header.addCell(logoCell);

        Cell titleCell = new Cell().setBorder(Border.NO_BORDER);
        titleCell.add(new Paragraph("BILL / CASH MEMO")
                .setFont(bold)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setUnderline()
                .setMarginBottom(2));
        titleCell.add(new Paragraph(nullSafe(seller != null ? seller.getCompanyName() : null, "Company Name"))
                .setFont(bold)
                .setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));
        String slogan = seller != null ? seller.getIndustry() : null;
        if (slogan != null && !slogan.isBlank()) {
            Paragraph sloganP = new Paragraph(slogan)
                    .setFont(regular)
                    .setFontSize(10)
                    .setFontColor(TITLE_FG)
                    .setBackgroundColor(TITLE_BG)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingTop(3)
                    .setPaddingBottom(3)
                    .setPaddingLeft(10)
                    .setPaddingRight(10)
                    .setMarginBottom(4);
            titleCell.add(sloganP);
        }
        String tagLine = buildCompanyTagLine(seller);
        if (!tagLine.isBlank()) {
            titleCell.add(new Paragraph(tagLine)
                    .setFont(regular)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER));
        }
        header.addCell(titleCell);

        Cell contactCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        String panLine = seller != null && seller.getPan() != null && !seller.getPan().isBlank()
                ? "PAN: " + seller.getPan() : null;
        String gstLine = seller != null && seller.getGstNo() != null && !seller.getGstNo().isBlank()
                ? "GST: " + seller.getGstNo() : null;
        String hsnLine = seller != null && seller.getHsnCode() != null && !seller.getHsnCode().isBlank()
                ? "HSN: " + seller.getHsnCode() : null;

        if (panLine != null) contactCell.add(contactLine(panLine, bold));
        if (gstLine != null) contactCell.add(contactLine(gstLine, bold));
        if (hsnLine != null) contactCell.add(contactLine(hsnLine, bold));
        header.addCell(contactCell);

        doc.add(header);
        doc.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(BORDER, 0.8f))
                .setMarginBottom(8));
    }

    private Paragraph contactLine(String text, PdfFont bold) {
        return new Paragraph(text)
                .setFont(bold)
                .setFontSize(9)
                .setMarginBottom(1);
    }

    private void buildBillMeta(Document doc, OrderDetails order, Customers customer, Vendors vendor,
                               PdfFont regular, PdfFont bold) {
        Table meta = new Table(UnitValue.createPercentArray(new float[] { 60, 40 }))
                .useAllAvailableWidth()
                .setMarginBottom(6);

        Cell billNoCell = new Cell().setBorder(Border.NO_BORDER);
        billNoCell.add(labelValue("Bill No", nullSafe(order.getOrderNumber(), "-"), regular, bold));
        meta.addCell(billNoCell);

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        String dateStr = order.getOrderDate() != null ? order.getOrderDate().toLocalDate().format(DATE_FMT) : "-";
        dateCell.add(labelValue("Date", dateStr, regular, bold));
        meta.addCell(dateCell);

        doc.add(meta);

        String buyerName = customer != null && customer.getDisplayName() != null
                ? customer.getDisplayName()
                : (vendor != null && vendor.getDisplayName() != null ? vendor.getDisplayName() : "-");
        String buyerLine2 = buildBuyerLine2(customer, vendor);

        Table toTable = new Table(UnitValue.createPercentArray(new float[] { 100 }))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        Cell toCell = new Cell().setBorder(Border.NO_BORDER);
        toCell.add(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("To: ").setFont(bold).setFontSize(10))
                .add(new com.itextpdf.layout.element.Text(buyerName).setFont(regular).setFontSize(10))
                .setBorderBottom(new SolidBorder(BORDER, 0.5f))
                .setPaddingBottom(2)
                .setMarginBottom(2));
        toCell.add(new Paragraph(buyerLine2.isBlank() ? " " : buyerLine2)
                .setFont(regular)
                .setFontSize(9)
                .setFontColor(MUTED)
                .setBorderBottom(new SolidBorder(BORDER, 0.5f))
                .setPaddingBottom(2));
        toTable.addCell(toCell);

        doc.add(toTable);
    }

    private Paragraph labelValue(String label, String value, PdfFont regular, PdfFont bold) {
        return new Paragraph()
                .add(new com.itextpdf.layout.element.Text(label + ": ").setFont(bold).setFontSize(10))
                .add(new com.itextpdf.layout.element.Text(value).setFont(regular).setFontSize(10))
                .setMarginBottom(2);
    }

    private void buildItemsTable(Document doc, List<OrderItemDetails> items, OrderDetails order,
                                 PdfFont regular, PdfFont bold) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 7, 55, 15, 15, 8 }))
                .useAllAvailableWidth();

        table.addHeaderCell(headerCell("S.no", bold).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(headerCell("P A R T I C U L A R S", bold).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(headerCell("Rate", bold).setTextAlignment(TextAlignment.CENTER));
        Cell amountHeader = new Cell(1, 2)
                .add(new Paragraph("Amount").setFont(bold).setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(BORDER, 0.8f))
                .setPadding(4);
        table.addHeaderCell(amountHeader);

        // Sub-header row: blank | blank | blank | Rs. | P.
        table.addCell(subHeaderCell("", regular));
        table.addCell(subHeaderCell("", regular));
        table.addCell(subHeaderCell("", regular));
        table.addCell(subHeaderCell("Rs.", bold).setTextAlignment(TextAlignment.CENTER));
        table.addCell(subHeaderCell("P.", bold).setTextAlignment(TextAlignment.CENTER));

        int sno = 1;
        int filled = 0;
        for (OrderItemDetails item : items) {
            String itemName = item.getItemId() != null ? item.getItemId().getItemName() : "Item";
            String desc = item.getItemDescription();
            String particular = itemName;
            if (desc != null && !desc.isBlank()) {
                particular = itemName + " — " + desc;
            }
            Double qty = item.getQuantity();
            Double rate = item.getUpdatedPrice();
            Double amount = item.getItemTotal() != null ? item.getItemTotal() :
                    (qty != null && rate != null ? qty * rate : null);

            String particularLine = particular;
            if (qty != null && qty > 0) {
                particularLine += "  (Qty: " + trimNumber(qty) + ")";
            }

            table.addCell(bodyCell(String.valueOf(sno), regular).setTextAlignment(TextAlignment.CENTER));
            table.addCell(bodyCell(particularLine, regular));
            table.addCell(bodyCell(rate != null ? trimNumber(rate) : "-", regular).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(bodyCell(rupees(amount), regular).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(bodyCell(paise(amount), regular).setTextAlignment(TextAlignment.CENTER));

            sno++;
            filled++;
        }

        addChargeRow(table, "Tax", order.getTaxAmount(), regular, bold, filled++);
        addChargeRow(table, "Delivery Charges", order.getDeliveryCharge(), regular, bold, filled++);
        addChargeRow(table, "Discount", order.getDiscountAmount() != null ? -order.getDiscountAmount() : null,
                regular, bold, filled++);

        int minRows = 10;
        while (filled < minRows) {
            table.addCell(emptyBodyCell());
            table.addCell(emptyBodyCell());
            table.addCell(emptyBodyCell());
            table.addCell(emptyBodyCell());
            table.addCell(emptyBodyCell());
            filled++;
        }

        // Total row
        Cell totalLabel = new Cell(1, 3)
                .add(new Paragraph("Total").setFont(bold).setFontSize(11).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(new SolidBorder(BORDER, 0.8f))
                .setPadding(5);
        table.addCell(totalLabel);
        Double total = order.getTotalAmount();
        table.addCell(new Cell()
                .add(new Paragraph(rupees(total)).setFont(bold).setFontSize(11).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(new SolidBorder(BORDER, 0.8f))
                .setPadding(5));
        table.addCell(new Cell()
                .add(new Paragraph(paise(total)).setFont(bold).setFontSize(11).setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(BORDER, 0.8f))
                .setPadding(5));

        doc.add(table);
    }

    private void addChargeRow(Table table, String label, Double amount, PdfFont regular, PdfFont bold, int sno) {
        if (amount == null || amount == 0.0) return;
        table.addCell(bodyCell("", regular));
        table.addCell(bodyCell(label, bold).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(bodyCell("", regular));
        table.addCell(bodyCell(rupees(amount), regular).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(bodyCell(paise(amount), regular).setTextAlignment(TextAlignment.CENTER));
    }

    private void buildFooter(Document doc, Companies seller, PdfFont regular, PdfFont bold, PdfFont italic) {
        Paragraph forLine = new Paragraph()
                .add(new com.itextpdf.layout.element.Text("For ").setFont(regular).setFontSize(11))
                .add(new com.itextpdf.layout.element.Text(nullSafe(seller != null ? seller.getCompanyName() : null, ""))
                        .setFont(bold).setFontSize(11))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(18);
        doc.add(forLine);

        Paragraph signature = new Paragraph("Signature")
                .setFont(bold)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(40);
        doc.add(signature);
    }

    private Cell headerCell(String text, PdfFont bold) {
        return new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(10))
                .setBorder(new SolidBorder(BORDER, 0.8f))
                .setPadding(4);
    }

    private Cell subHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(9).setFontColor(MUTED))
                .setBorder(new SolidBorder(BORDER, 0.8f))
                .setPadding(2);
    }

    private Cell bodyCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(10))
                .setBorder(new SolidBorder(BORDER, 0.5f))
                .setPadding(4);
    }

    private Cell emptyBodyCell() {
        return new Cell()
                .add(new Paragraph(" ").setFontSize(10))
                .setBorder(new SolidBorder(BORDER, 0.5f))
                .setPadding(4);
    }

    private Image loadSellerLogo(Companies seller) {
        if (seller == null || seller.getFsId() == null || seller.getFsId().isBlank()) return null;
        try {
            FileStorage fs = fileStorageRepository.findByFsId(seller.getFsId()).orElse(null);
            if (fs == null || fs.getFilePath() == null) return null;
            Path path = Paths.get(fs.getFilePath());
            if (!Files.exists(path)) return null;
            return new Image(ImageDataFactory.create(Files.readAllBytes(path)));
        } catch (Exception e) {
            return null;
        }
    }

    private String buildCompanyTagLine(Companies seller) {
        if (seller == null) return "";
        StringBuilder sb = new StringBuilder();
        if (seller.getPan() != null && !seller.getPan().isBlank()) sb.append("PAN: ").append(seller.getPan());
        if (seller.getGstNo() != null && !seller.getGstNo().isBlank()) {
            if (sb.length() > 0) sb.append("   |   ");
            sb.append("GST: ").append(seller.getGstNo());
        }
        return sb.toString();
    }

    private String buildBuyerLine2(Customers customer, Vendors vendor) {
        StringBuilder sb = new StringBuilder();
        String phone = null;
        String email = null;
        String gst = null;
        if (customer != null) {
            phone = customer.getPhone();
            email = customer.getEmail();
            gst = customer.getGst();
        } else if (vendor != null) {
            phone = vendor.getPhone();
            email = vendor.getEmail();
            gst = vendor.getGst();
        }
        appendSegment(sb, phone);
        appendSegment(sb, email);
        if (gst != null && !gst.isBlank()) appendSegment(sb, "GST: " + gst);
        return sb.toString();
    }

    private void appendSegment(StringBuilder sb, String value) {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append("   |   ");
        sb.append(value);
    }

    private String nullSafe(String v, String fallback) {
        return v != null && !v.isBlank() ? v : fallback;
    }

    private String trimNumber(Double value) {
        if (value == null) return "-";
        if (value == value.longValue()) return String.valueOf(value.longValue());
        return String.format("%.2f", value);
    }

    private String rupees(Double value) {
        if (value == null) return "-";
        long rupees = (long) Math.floor(Math.abs(value));
        return (value < 0 ? "-" : "") + String.valueOf(rupees);
    }

    private String paise(Double value) {
        if (value == null) return "";
        long paise = Math.round((Math.abs(value) - Math.floor(Math.abs(value))) * 100);
        return String.format("%02d", paise);
    }
}
