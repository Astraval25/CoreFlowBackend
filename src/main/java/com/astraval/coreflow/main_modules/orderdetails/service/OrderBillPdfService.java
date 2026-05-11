package com.astraval.coreflow.main_modules.orderdetails.service;

import com.astraval.coreflow.main_modules.address.Address;
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
import com.itextpdf.layout.element.Text;
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
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class OrderBillPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat INR = NumberFormat.getInstance(new Locale("en", "IN"));

    private static final DeviceRgb SLOGAN_BG = new DeviceRgb(20, 20, 20);
    private static final DeviceRgb SLOGAN_FG = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER = new DeviceRgb(35, 35, 35);
    private static final DeviceRgb SOFT_BORDER = new DeviceRgb(170, 170, 170);
    private static final DeviceRgb MUTED = new DeviceRgb(95, 95, 95);
    private static final DeviceRgb LOGO_BG = new DeviceRgb(235, 235, 235);

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
            doc.setMargins(34, 34, 34, 34);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont italic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
            PdfFont title = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);

            buildHeader(doc, seller, regular, bold, title);
            buildBillMeta(doc, order, customer, vendor, regular, bold);
            buildItemsTable(doc, items, order, regular, bold);
            buildFooter(doc, seller, regular, bold, italic);

            doc.close();
        }

        return baos.toByteArray();
    }

    private void buildHeader(Document doc, Companies seller, PdfFont regular, PdfFont bold, PdfFont title) {
        Table header = new Table(UnitValue.createPercentArray(new float[] { 22, 56, 22 }))
                .useAllAvailableWidth();

        header.addCell(logoCell(seller, bold));
        header.addCell(titleCell(seller, regular, bold, title));
        header.addCell(businessIdCell(seller, regular, bold));

        doc.add(header);

        doc.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(BORDER, 1f))
                .setMarginTop(4)
                .setMarginBottom(10));
    }

    private Cell logoCell(Companies seller, PdfFont bold) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        Image logo = loadSellerLogo(seller);
        if (logo != null) {
            logo.setWidth(80).setAutoScale(false)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            cell.add(logo);
        } else {
            cell.add(new Paragraph("LOGO")
                    .setFont(bold).setFontSize(12)
                    .setFontColor(MUTED)
                    .setBackgroundColor(LOGO_BG)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingTop(26).setPaddingBottom(26)
                    .setPaddingLeft(10).setPaddingRight(10)
                    .setWidth(82));
        }
        return cell;
    }

    private Cell titleCell(Companies seller, PdfFont regular, PdfFont bold, PdfFont title) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        cell.add(new Paragraph("BILL / CASH MEMO")
                .setFont(bold).setFontSize(11)
                .setTextAlignment(TextAlignment.CENTER)
                .setUnderline()
                .setMarginBottom(2));

        String name = nullSafe(seller != null ? seller.getCompanyName() : null, "Company Name");
        cell.add(new Paragraph(name)
                .setFont(title).setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        String slogan = seller != null ? seller.getIndustry() : null;
        if (notBlank(slogan)) {
            cell.add(new Paragraph(slogan)
                    .setFont(regular).setFontSize(10)
                    .setFontColor(SLOGAN_FG)
                    .setBackgroundColor(SLOGAN_BG)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingTop(3).setPaddingBottom(3)
                    .setPaddingLeft(30).setPaddingRight(30)
                    .setMarginBottom(0));
        }
        return cell;
    }

    private Cell businessIdCell(Companies seller, PdfFont regular, PdfFont bold) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT);

        if (seller == null) {
            cell.add(new Paragraph(" "));
            return cell;
        }

        boolean any = false;
        if (notBlank(seller.getPan())) { cell.add(kvLine("PAN", seller.getPan(), regular, bold)); any = true; }
        if (notBlank(seller.getGstNo())) { cell.add(kvLine("GST", seller.getGstNo(), regular, bold)); any = true; }
        if (notBlank(seller.getHsnCode())) { cell.add(kvLine("HSN", seller.getHsnCode(), regular, bold)); any = true; }
        if (!any) cell.add(new Paragraph(" "));
        return cell;
    }

    private Paragraph kvLine(String key, String value, PdfFont regular, PdfFont bold) {
        return new Paragraph()
                .add(new Text(key + ": ").setFont(bold).setFontSize(9))
                .add(new Text(value).setFont(regular).setFontSize(9))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(1);
    }

    private void buildBillMeta(Document doc, OrderDetails order, Customers customer, Vendors vendor,
                               PdfFont regular, PdfFont bold) {
        Table meta = new Table(UnitValue.createPercentArray(new float[] { 60, 40 }))
                .useAllAvailableWidth()
                .setMarginBottom(8);

        meta.addCell(billField("Bill No", nullSafe(order.getOrderNumber(), ""), regular, bold, TextAlignment.LEFT));
        String dateStr = order.getOrderDate() != null
                ? order.getOrderDate().toLocalDate().format(DATE_FMT) : "";
        meta.addCell(billField("Date", dateStr, regular, bold, TextAlignment.RIGHT));
        doc.add(meta);

        String buyerName = resolveBuyerName(customer, vendor);
        String buyerAddress = resolveBuyerAddress(customer, vendor);
        String buyerContact = buildBuyerContact(customer, vendor);

        Table to = new Table(UnitValue.createPercentArray(new float[] { 100 }))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        Cell nameLine = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BORDER, 0.6f))
                .setPaddingTop(2).setPaddingBottom(4);
        nameLine.add(new Paragraph()
                .add(new Text("To: ").setFont(bold).setFontSize(10))
                .add(new Text(buyerName).setFont(regular).setFontSize(10))
                .setMarginBottom(0));
        to.addCell(nameLine);

        if (!buyerAddress.isBlank()) {
            Cell addrLine = new Cell().setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(BORDER, 0.6f))
                    .setPaddingTop(3).setPaddingBottom(4);
            addrLine.add(new Paragraph(buyerAddress)
                    .setFont(regular).setFontSize(9).setFontColor(MUTED)
                    .setMarginBottom(0));
            to.addCell(addrLine);
        }

        if (!buyerContact.isBlank()) {
            Cell contact = new Cell().setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(BORDER, 0.6f))
                    .setPaddingTop(3).setPaddingBottom(4);
            contact.add(new Paragraph(buyerContact)
                    .setFont(regular).setFontSize(9).setFontColor(MUTED)
                    .setMarginBottom(0));
            to.addCell(contact);
        }
        doc.add(to);
    }

    private Cell billField(String label, String value, PdfFont regular, PdfFont bold, TextAlignment alignment) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BORDER, 0.6f))
                .setTextAlignment(alignment)
                .setPaddingBottom(3);
        cell.add(new Paragraph()
                .add(new Text(label + ": ").setFont(bold).setFontSize(10))
                .add(new Text(value).setFont(regular).setFontSize(10)));
        return cell;
    }

    private void buildItemsTable(Document doc, List<OrderItemDetails> items, OrderDetails order,
                                 PdfFont regular, PdfFont bold) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 7, 55, 15, 15, 8 }))
                .useAllAvailableWidth();

        // Header row
        table.addHeaderCell(headerCell("S.no", bold).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(headerCell("P A R T I C U L A R S", bold).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(headerCell("Rate", bold).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell(1, 2)
                .add(new Paragraph("Amount").setFont(bold).setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(BORDER, 0.8f))
                .setPadding(5));

        // Rs. / P. sub-header under Amount
        table.addCell(subHeaderCell("", regular));
        table.addCell(subHeaderCell("", regular));
        table.addCell(subHeaderCell("", regular));
        table.addCell(subHeaderCell("Rs.", bold).setTextAlignment(TextAlignment.CENTER));
        table.addCell(subHeaderCell("P.", bold).setTextAlignment(TextAlignment.CENTER));

        int filled = 0;
        int sno = 1;
        for (OrderItemDetails item : items) {
            String itemName = item.getItemId() != null ? item.getItemId().getItemName() : "Item";
            String desc = item.getItemDescription();
            Double qty = item.getQuantity();
            Double rate = item.getUpdatedPrice();
            Double amount = item.getItemTotal() != null ? item.getItemTotal()
                    : (qty != null && rate != null ? qty * rate : null);

            Paragraph particular = new Paragraph()
                    .add(new Text(itemName).setFont(bold).setFontSize(10));
            if (notBlank(desc)) {
                particular.add(new Text("\n" + desc).setFont(regular).setFontSize(9).setFontColor(MUTED));
            }
            if (qty != null && qty > 0) {
                particular.add(new Text("   (Qty: " + trimNumber(qty) + ")")
                        .setFont(regular).setFontSize(9).setFontColor(MUTED));
            }

            table.addCell(bodyCell(String.valueOf(sno), regular).setTextAlignment(TextAlignment.CENTER));
            table.addCell(bodyCellP(particular));
            table.addCell(bodyCell(rate != null ? trimNumber(rate) : "-", regular)
                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(bodyCell(rupees(amount), regular).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(bodyCell(paise(amount), regular).setTextAlignment(TextAlignment.CENTER));

            sno++;
            filled++;
        }

        filled += addChargeRow(table, "Tax", order.getTaxAmount(), regular, bold);
        filled += addChargeRow(table, "Delivery Charges", order.getDeliveryCharge(), regular, bold);
        filled += addChargeRow(table, "Discount",
                order.getDiscountAmount() != null ? -order.getDiscountAmount() : null, regular, bold);

        // Pad with empty rows so the table always has a presentable body
        int minRows = 14;
        while (filled < minRows) {
            for (int c = 0; c < 5; c++) table.addCell(emptyBodyCell());
            filled++;
        }

        // Total row spans S.no + Particulars + Rate
        table.addCell(new Cell(1, 3)
                .add(new Paragraph("Total").setFont(bold).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(new SolidBorder(BORDER, 1f))
                .setPadding(6));
        Double total = order.getTotalAmount();
        table.addCell(new Cell()
                .add(new Paragraph(rupees(total)).setFont(bold).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(new SolidBorder(BORDER, 1f))
                .setPadding(6));
        table.addCell(new Cell()
                .add(new Paragraph(paise(total)).setFont(bold).setFontSize(12).setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(BORDER, 1f))
                .setPadding(6));

        doc.add(table);

        String inWords = amountInWords(total);
        if (!inWords.isBlank()) {
            doc.add(new Paragraph()
                    .add(new Text("Amount in words: ").setFont(bold).setFontSize(9))
                    .add(new Text(inWords).setFont(regular).setFontSize(9))
                    .setFontColor(MUTED)
                    .setMarginTop(4));
        }
    }

    private int addChargeRow(Table table, String label, Double amount, PdfFont regular, PdfFont bold) {
        if (amount == null || amount == 0.0) return 0;
        table.addCell(bodyCell("", regular));
        table.addCell(bodyCell(label, bold).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(bodyCell("", regular));
        table.addCell(bodyCell(rupees(amount), regular).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(bodyCell(paise(amount), regular).setTextAlignment(TextAlignment.CENTER));
        return 1;
    }

    // â”€â”€ Footer
    private void buildFooter(Document doc, Companies seller, PdfFont regular, PdfFont bold, PdfFont italic) {
        doc.add(new Paragraph()
                .add(new Text("For ").setFont(regular).setFontSize(11))
                .add(new Text(nullSafe(seller != null ? seller.getCompanyName() : null, ""))
                        .setFont(bold).setFontSize(11))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(16));

        Table sig = new Table(UnitValue.createPercentArray(new float[] { 65, 35 }))
                .useAllAvailableWidth()
                .setMarginTop(44);
        sig.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(" ")));
        sig.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(BORDER, 0.6f))
                .setPaddingTop(3)
                .add(new Paragraph("Authorized Signatory")
                        .setFont(bold).setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER)));
        doc.add(sig);

        doc.add(new Paragraph("This is a computer-generated bill.")
                .setFont(italic).setFontSize(8).setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(18));
    }

    private Cell headerCell(String text, PdfFont bold) {
        return new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(10))
                .setBorder(new SolidBorder(BORDER, 0.9f))
                .setPadding(5);
    }

    private Cell subHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(9).setFontColor(MUTED))
                .setBorder(new SolidBorder(BORDER, 0.7f))
                .setPadding(2);
    }

    private Cell bodyCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(BORDER, 0.8f))
                .setBorderRight(new SolidBorder(BORDER, 0.8f))
                .setPadding(5);
    }

    private Cell bodyCellP(Paragraph paragraph) {
        return new Cell()
                .add(paragraph)
                .setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(BORDER, 0.8f))
                .setBorderRight(new SolidBorder(BORDER, 0.8f))
                .setPadding(5);
    }

    private Cell emptyBodyCell() {
        return new Cell()
                .add(new Paragraph(" ").setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(BORDER, 0.8f))
                .setBorderRight(new SolidBorder(BORDER, 0.8f))
                .setPadding(5);
    }

    // -- Data helpers --
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

    private String resolveBuyerName(Customers customer, Vendors vendor) {
        if (customer != null && notBlank(customer.getDisplayName())) return customer.getDisplayName();
        if (vendor != null && notBlank(vendor.getDisplayName())) return vendor.getDisplayName();
        return "-";
    }

    private String resolveBuyerAddress(Customers customer, Vendors vendor) {
        Address addr = null;
        if (customer != null) {
            addr = customer.getBillingAddrId() != null ? customer.getBillingAddrId() : customer.getShippingAddrId();
        } else if (vendor != null) {
            addr = vendor.getBillingAddrId() != null ? vendor.getBillingAddrId() : vendor.getShippingAddrId();
        }
        if (addr == null) return "";

        StringBuilder sb = new StringBuilder();
        appendSegment(sb, addr.getLine1(), ", ");
        appendSegment(sb, addr.getLine2(), ", ");
        appendSegment(sb, addr.getCity(), ", ");
        appendSegment(sb, addr.getState(), ", ");
        if (addr.getPincode() != null) appendSegment(sb, String.valueOf(addr.getPincode()), " - ");
        appendSegment(sb, addr.getCountry(), ", ");
        return sb.toString();
    }

    private String buildBuyerContact(Customers customer, Vendors vendor) {
        String phone = null, email = null, gst = null;
        if (customer != null) {
            phone = customer.getPhone();
            email = customer.getEmail();
            gst = customer.getGst();
        } else if (vendor != null) {
            phone = vendor.getPhone();
            email = vendor.getEmail();
            gst = vendor.getGst();
        }
        StringBuilder sb = new StringBuilder();
        if (notBlank(phone)) appendSegment(sb, "Mob: " + phone, "   |   ");
        if (notBlank(email)) appendSegment(sb, email, "   |   ");
        if (notBlank(gst)) appendSegment(sb, "GST: " + gst, "   |   ");
        return sb.toString();
    }

    private void appendSegment(StringBuilder sb, String value, String sep) {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append(sep);
        sb.append(value);
    }

    private boolean notBlank(String v) { return v != null && !v.isBlank(); }

    private String nullSafe(String v, String fallback) {
        return notBlank(v) ? v : fallback;
    }

    private String trimNumber(Double value) {
        if (value == null) return "-";
        if (value == value.longValue()) return String.valueOf(value.longValue());
        return String.format("%.2f", value);
    }

    private String rupees(Double value) {
        if (value == null) return "-";
        long r = (long) Math.floor(Math.abs(value));
        return (value < 0 ? "-" : "") + INR.format(r);
    }

    private String paise(Double value) {
        if (value == null) return "";
        long p = Math.round((Math.abs(value) - Math.floor(Math.abs(value))) * 100);
        return String.format("%02d", p);
    }

    private static final String[] UNITS = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };
    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private String amountInWords(Double value) {
        if (value == null) return "";
        long rupees = (long) Math.floor(Math.abs(value));
        long paise = Math.round((Math.abs(value) - Math.floor(Math.abs(value))) * 100);
        StringBuilder sb = new StringBuilder();
        if (rupees > 0) sb.append(intToWordsIndian(rupees)).append(" Rupees");
        if (paise > 0) {
            if (sb.length() > 0) sb.append(" and ");
            sb.append(intToWordsIndian(paise)).append(" Paise");
        }
        if (sb.length() == 0) return "Zero Rupees Only";
        sb.append(" Only");
        return sb.toString();
    }

    private String intToWordsIndian(long n) {
        if (n == 0) return "Zero";
        StringBuilder sb = new StringBuilder();
        long crore = n / 10000000; n %= 10000000;
        long lakh = n / 100000;    n %= 100000;
        long thousand = n / 1000;  n %= 1000;
        long hundred = n / 100;    n %= 100;
        if (crore > 0) sb.append(twoDigits(crore)).append(" Crore ");
        if (lakh > 0) sb.append(twoDigits(lakh)).append(" Lakh ");
        if (thousand > 0) sb.append(twoDigits(thousand)).append(" Thousand ");
        if (hundred > 0) sb.append(UNITS[(int) hundred]).append(" Hundred ");
        if (n > 0) {
            if (sb.length() > 0) sb.append("and ");
            sb.append(twoDigits(n));
        }
        return sb.toString().trim();
    }

    private String twoDigits(long n) {
        if (n < 20) return UNITS[(int) n];
        long t = n / 10, u = n % 10;
        return TENS[(int) t] + (u > 0 ? " " + UNITS[(int) u] : "");
    }
}
