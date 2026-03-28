package com.astraval.coreflow.modules.analytics.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.analytics.dto.BusinessGrowthDto;
import com.astraval.coreflow.modules.analytics.dto.CashFlowDto;
import com.astraval.coreflow.modules.analytics.dto.DashboardKpiDto;
import com.astraval.coreflow.modules.analytics.dto.ItemFrequencyDto;
import com.astraval.coreflow.modules.analytics.dto.MonthlyTrendDto;
import com.astraval.coreflow.modules.analytics.dto.OrderFrequencyDto;
import com.astraval.coreflow.modules.analytics.dto.PaymentFrequencyDto;
import com.astraval.coreflow.modules.analytics.dto.PaymentModeDistributionDto;
import com.astraval.coreflow.modules.analytics.dto.ProfitByItemDto;
import com.astraval.coreflow.modules.analytics.dto.RevenueExpenseDto;
import com.astraval.coreflow.modules.analytics.dto.RunningAmountDto;
import com.astraval.coreflow.modules.analytics.dto.SalesPurchaseByItemDto;
import com.astraval.coreflow.modules.analytics.dto.SalesPurchaseByPartyDto;
import com.astraval.coreflow.modules.analytics.dto.SalesPurchaseSummaryDto;
import com.astraval.coreflow.modules.analytics.dto.TopItemDto;
import com.astraval.coreflow.modules.analytics.service.AnalyticsService;

@RestController
@RequestMapping("/api/companies")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    // ========================
    // Section 1: Order Frequency
    // ========================

    @GetMapping("/{companyId}/analytics/sales/order-frequency")
    public ApiResponse<List<OrderFrequencyDto>> salesOrderFrequency(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesOrderFrequency(companyId, startDate, endDate),
                "Sales order frequency retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/order-frequency")
    public ApiResponse<List<OrderFrequencyDto>> purchaseOrderFrequency(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchaseOrderFrequency(companyId, startDate, endDate),
                "Purchase order frequency retrieved");
    }

    // ========================
    // Section 1: Payment Frequency
    // ========================

    @GetMapping("/{companyId}/analytics/sales/payment-frequency")
    public ApiResponse<List<PaymentFrequencyDto>> salesPaymentFrequency(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesPaymentFrequency(companyId, startDate, endDate),
                "Sales payment frequency retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/payment-frequency")
    public ApiResponse<List<PaymentFrequencyDto>> purchasePaymentFrequency(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchasePaymentFrequency(companyId, startDate, endDate),
                "Purchase payment frequency retrieved");
    }

    // ========================
    // Section 1: Item Frequency
    // ========================

    @GetMapping("/{companyId}/analytics/sales/item-frequency")
    public ApiResponse<List<ItemFrequencyDto>> salesItemFrequency(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesItemFrequency(companyId, startDate, endDate),
                "Sales item frequency retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/item-frequency")
    public ApiResponse<List<ItemFrequencyDto>> purchaseItemFrequency(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchaseItemFrequency(companyId, startDate, endDate),
                "Purchase item frequency retrieved");
    }

    // ========================
    // Section 1: Running Order Amount
    // ========================

    @GetMapping("/{companyId}/analytics/sales/running-order-amount")
    public ApiResponse<List<RunningAmountDto>> salesRunningOrderAmount(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesRunningOrderAmount(companyId, startDate, endDate),
                "Sales running order amount retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/running-order-amount")
    public ApiResponse<List<RunningAmountDto>> purchaseRunningOrderAmount(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchaseRunningOrderAmount(companyId, startDate, endDate),
                "Purchase running order amount retrieved");
    }

    // ========================
    // Section 1: Running Payment Amount
    // ========================

    @GetMapping("/{companyId}/analytics/sales/running-payment-amount")
    public ApiResponse<List<RunningAmountDto>> salesRunningPaymentAmount(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesRunningPaymentAmount(companyId, startDate, endDate),
                "Sales running payment amount retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/running-payment-amount")
    public ApiResponse<List<RunningAmountDto>> purchaseRunningPaymentAmount(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchaseRunningPaymentAmount(companyId, startDate, endDate),
                "Purchase running payment amount retrieved");
    }

    // ========================
    // Section 2: Sales/Purchase by Party
    // ========================

    @GetMapping("/{companyId}/analytics/sales/by-customer")
    public ApiResponse<List<SalesPurchaseByPartyDto>> salesByCustomer(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesByCustomer(companyId, startDate, endDate),
                "Sales by customer retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/by-vendor")
    public ApiResponse<List<SalesPurchaseByPartyDto>> purchaseByVendor(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchaseByVendor(companyId, startDate, endDate),
                "Purchase by vendor retrieved");
    }

    // ========================
    // Section 2: Sales/Purchase by Item
    // ========================

    @GetMapping("/{companyId}/analytics/sales/by-item")
    public ApiResponse<List<SalesPurchaseByItemDto>> salesByItem(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesByItem(companyId, startDate, endDate),
                "Sales by item retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/by-item")
    public ApiResponse<List<SalesPurchaseByItemDto>> purchaseByItem(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchaseByItem(companyId, startDate, endDate),
                "Purchase by item retrieved");
    }

    // ========================
    // Section 2: Sales/Purchase Summary
    // ========================

    @GetMapping("/{companyId}/analytics/sales/summary")
    public ApiResponse<SalesPurchaseSummaryDto> salesSummary(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getSalesSummary(companyId, startDate, endDate),
                "Sales summary retrieved");
    }

    @GetMapping("/{companyId}/analytics/purchase/summary")
    public ApiResponse<SalesPurchaseSummaryDto> purchaseSummary(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPurchaseSummary(companyId, startDate, endDate),
                "Purchase summary retrieved");
    }

    // ========================
    // Section 2: Profit by Item
    // ========================

    @GetMapping("/{companyId}/analytics/profit/by-item")
    public ApiResponse<List<ProfitByItemDto>> profitByItem(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getProfitByItem(companyId, startDate, endDate),
                "Profit by item retrieved");
    }

    // ========================
    // Section 3: Cash Flow
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/cash-flow")
    public ApiResponse<CashFlowDto> cashFlow(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getCashFlow(companyId, startDate, endDate),
                "Cash flow retrieved");
    }

    // ========================
    // Section 3: Revenue vs Expense
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/revenue-expense")
    public ApiResponse<List<RevenueExpenseDto>> revenueVsExpense(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getRevenueVsExpense(companyId, startDate, endDate),
                "Revenue vs expense retrieved");
    }

    // ========================
    // Section 3: Top Selling Items
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/top-selling-items")
    public ApiResponse<List<TopItemDto>> topSellingItems(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponseFactory.ok(analyticsService.getTopSellingItems(companyId, startDate, endDate, limit),
                "Top selling items retrieved");
    }

    // ========================
    // Section 3: Top Profitable Items
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/top-profitable-items")
    public ApiResponse<List<TopItemDto>> topProfitableItems(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponseFactory.ok(analyticsService.getTopProfitableItems(companyId, startDate, endDate, limit),
                "Top profitable items retrieved");
    }

    // ========================
    // Section 3: Payment Mode Distribution
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/payment-mode-distribution")
    public ApiResponse<List<PaymentModeDistributionDto>> paymentModeDistribution(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getPaymentModeDistribution(companyId, startDate, endDate),
                "Payment mode distribution retrieved");
    }

    // ========================
    // Section 3: Business Growth
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/business-growth")
    public ApiResponse<BusinessGrowthDto> businessGrowth(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getBusinessGrowth(companyId, startDate, endDate),
                "Business growth retrieved");
    }

    // ========================
    // Section 3: Dashboard KPI
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/kpi")
    public ApiResponse<DashboardKpiDto> dashboardKpi(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getDashboardKpi(companyId, startDate, endDate),
                "Dashboard KPI retrieved");
    }

    // ========================
    // Section 3: Monthly Trend
    // ========================

    @GetMapping("/{companyId}/analytics/dashboard/monthly-trend")
    public ApiResponse<List<MonthlyTrendDto>> monthlyTrend(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseFactory.ok(analyticsService.getMonthlyTrend(companyId, startDate, endDate),
                "Monthly trend retrieved");
    }
}
