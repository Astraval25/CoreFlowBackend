package com.astraval.coreflow.employee_module.salary;

import com.astraval.coreflow.common.pdf.PdfBuilder;
import com.astraval.coreflow.employee_module.enums.SalaryLineType;
import com.astraval.coreflow.employee_module.enums.SalaryType;
import com.astraval.coreflow.employee_module.salary.dto.SalaryLineDto;
import com.astraval.coreflow.employee_module.salary.dto.SalaryPeriodDetailDto;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class SalarySlipPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Autowired
    private SalaryService salaryService;

    public byte[] generateSalarySlip(Long companyId, Long salaryPeriodId) throws IOException {
        SalaryPeriodDetailDto detail = salaryService.getSalaryPeriodDetail(companyId, salaryPeriodId);

        PdfBuilder pdf = PdfBuilder.create();

        // Title
        pdf.addTitle("Salary Slip");
        pdf.addSubTitle("Period: " + detail.getFromDate().format(DATE_FMT) + " — " + detail.getToDate().format(DATE_FMT));

        // Employee info grid
        pdf.addSectionHeader("Employee Details");
        pdf.addInfoGrid(new String[][]{
                {"Employee Name", detail.getEmployeeName()},
                {"Employee Code", detail.getEmployeeCode()},
                {"Salary Type", detail.getSalaryType().name()},
                {"Status", detail.getStatus().name()}
        });

        // Attendance summary (for monthly employees)
        if (detail.getSalaryType() == SalaryType.MONTHLY) {
            pdf.addSectionHeader("Attendance Summary");
            pdf.addInfoGrid(new String[][]{
                    {"Days in Period", String.valueOf(detail.getWorkingDaysInMonth())},
                    {"Days Present", fmt(detail.getDaysPresent())},
                    {"Days Absent", fmt(detail.getDaysAbsent())},
                    {"LOP Days", fmt(detail.getLopDays())}
            });
        }

        // Earnings & deductions table
        pdf.addSectionHeader("Salary Breakdown");

        String[] headers = {"#", "Description", "Qty", "Rate", "Amount"};
        float[] widths = {8, 42, 15, 15, 20};
        TextAlignment[] aligns = {
                TextAlignment.CENTER, TextAlignment.LEFT, TextAlignment.RIGHT,
                TextAlignment.RIGHT, TextAlignment.RIGHT
        };

        Table table = pdf.createTable(headers, widths);

        int row = 0;
        for (SalaryLineDto line : detail.getLines()) {
            row++;
            String desc = line.getDescription();
            if (line.getWorkName() != null) {
                desc = line.getWorkName() + (line.getDescription() != null ? " — " + line.getDescription() : "");
            }

            String qty = line.getTotalQty() != null ? fmt(line.getTotalQty()) : "-";
            String rate = line.getRateUsed() != null ? fmt(line.getRateUsed()) : "-";

            // Show sign for deductions
            String amount;
            if (line.getLineType() == SalaryLineType.DEDUCTION && line.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                amount = "- " + fmt(line.getAmount().negate());
            } else {
                amount = fmt(line.getAmount());
            }

            if (line.getUnit() != null) {
                qty = qty + " " + line.getUnit().name();
            }

            pdf.addTableRow(table, new String[]{
                    String.valueOf(row), desc, qty, rate, amount
            }, row % 2 == 0, aligns);
        }

        pdf.addTable(table);

        // Totals
        pdf.addDivider();
        pdf.addTotalRow("Gross Amount", "₹ " + fmt(detail.getGrossAmount()));

        BigDecimal totalDeductions = BigDecimal.ZERO;
        if (detail.getLopDeduction() != null) totalDeductions = totalDeductions.add(detail.getLopDeduction());
        if (detail.getOtherDeductions() != null) totalDeductions = totalDeductions.add(detail.getOtherDeductions());

        if (totalDeductions.compareTo(BigDecimal.ZERO) > 0) {
            pdf.addTotalRow("Total Deductions", "- ₹ " + fmt(totalDeductions));
        }

        pdf.addTotalRow("Net Payable", "₹ " + fmt(detail.getNetAmount()));

        // Payment info
        if (detail.getPaymentRef() != null) {
            pdf.addSpacer(8);
            pdf.addKeyValue("Payment Ref", detail.getPaymentRef());
        }
        if (detail.getPaidDt() != null) {
            pdf.addKeyValue("Paid On", detail.getPaidDt().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a")));
        }

        // Footer
        pdf.addFooter("This is a system-generated salary slip. No signature required.");

        return pdf.build();
    }

    private String fmt(BigDecimal value) {
        if (value == null) return "-";
        return value.stripTrailingZeros().toPlainString();
    }
}
