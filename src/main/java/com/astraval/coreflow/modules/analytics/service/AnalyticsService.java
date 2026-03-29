package com.astraval.coreflow.modules.analytics.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.astraval.coreflow.modules.analytics.repo.AnalyticsRepository;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    // ========================
    // Section 1: Order Frequency
    // ========================

    public List<OrderFrequencyDto> getSalesOrderFrequency(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.salesOrderFrequency(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new OrderFrequencyDto(
                (String) r[0],
                ((Number) r[1]).longValue()
        )).toList();
    }

    public List<OrderFrequencyDto> getPurchaseOrderFrequency(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.purchaseOrderFrequency(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new OrderFrequencyDto(
                (String) r[0],
                ((Number) r[1]).longValue()
        )).toList();
    }

    // ========================
    // Section 1: Payment Frequency
    // ========================

    public List<PaymentFrequencyDto> getSalesPaymentFrequency(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.salesPaymentFrequency(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new PaymentFrequencyDto(
                (String) r[0],
                ((Number) r[1]).longValue()
        )).toList();
    }

    public List<PaymentFrequencyDto> getPurchasePaymentFrequency(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.purchasePaymentFrequency(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new PaymentFrequencyDto(
                (String) r[0],
                ((Number) r[1]).longValue()
        )).toList();
    }

    // ========================
    // Section 1: Item Frequency
    // ========================

    public List<ItemFrequencyDto> getSalesItemFrequency(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.salesItemFrequency(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new ItemFrequencyDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                toDouble(r[2]),
                ((Number) r[3]).longValue()
        )).toList();
    }

    public List<ItemFrequencyDto> getPurchaseItemFrequency(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.purchaseItemFrequency(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new ItemFrequencyDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                toDouble(r[2]),
                ((Number) r[3]).longValue()
        )).toList();
    }

    // ========================
    // Section 1: Running Order Amount
    // ========================

    public List<RunningAmountDto> getSalesRunningOrderAmount(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.salesRunningOrderAmount(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new RunningAmountDto(
                (String) r[0],
                toDouble(r[1])
        )).toList();
    }

    public List<RunningAmountDto> getPurchaseRunningOrderAmount(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.purchaseRunningOrderAmount(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new RunningAmountDto(
                (String) r[0],
                toDouble(r[1])
        )).toList();
    }

    // ========================
    // Section 1: Running Payment Amount
    // ========================

    public List<RunningAmountDto> getSalesRunningPaymentAmount(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.salesRunningPaymentAmount(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new RunningAmountDto(
                (String) r[0],
                toDouble(r[1])
        )).toList();
    }

    public List<RunningAmountDto> getPurchaseRunningPaymentAmount(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.purchaseRunningPaymentAmount(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new RunningAmountDto(
                (String) r[0],
                toDouble(r[1])
        )).toList();
    }

    // ========================
    // Section 2: Sales/Purchase by Party
    // ========================

    public List<SalesPurchaseByPartyDto> getSalesByCustomer(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.salesByCustomer(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new SalesPurchaseByPartyDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                ((Number) r[2]).longValue(),
                toDouble(r[3]),
                toDouble(r[4]),
                toDouble(r[5])
        )).toList();
    }

    public List<SalesPurchaseByPartyDto> getPurchaseByVendor(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.purchaseByVendor(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new SalesPurchaseByPartyDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                ((Number) r[2]).longValue(),
                toDouble(r[3]),
                toDouble(r[4]),
                toDouble(r[5])
        )).toList();
    }

    // ========================
    // Section 2: Sales/Purchase by Item
    // ========================

    public List<SalesPurchaseByItemDto> getSalesByItem(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.salesByItem(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new SalesPurchaseByItemDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                toDouble(r[2]),
                toDouble(r[3])
        )).toList();
    }

    public List<SalesPurchaseByItemDto> getPurchaseByItem(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.purchaseByItem(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new SalesPurchaseByItemDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                toDouble(r[2]),
                toDouble(r[3])
        )).toList();
    }

    // ========================
    // Section 2: Sales/Purchase Summary
    // ========================

    public SalesPurchaseSummaryDto getSalesSummary(Long companyId, LocalDate startDate, LocalDate endDate) {
        Object[] r = analyticsRepository.salesSummary(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return new SalesPurchaseSummaryDto(
                ((Number) r[0]).longValue(),
                toDouble(r[1]),
                toDouble(r[2]),
                toDouble(r[3]),
                toDouble(r[4])
        );
    }

    public SalesPurchaseSummaryDto getPurchaseSummary(Long companyId, LocalDate startDate, LocalDate endDate) {
        Object[] r = analyticsRepository.purchaseSummary(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return new SalesPurchaseSummaryDto(
                ((Number) r[0]).longValue(),
                toDouble(r[1]),
                toDouble(r[2]),
                toDouble(r[3]),
                toDouble(r[4])
        );
    }

    // ========================
    // Section 2: Profit by Item
    // ========================

    public List<ProfitByItemDto> getProfitByItem(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.profitByItem(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new ProfitByItemDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                toDouble(r[2]),
                toDouble(r[3]),
                toDouble(r[4]),
                toDouble(r[5])
        )).toList();
    }

    // ========================
    // Section 3: Cash Flow
    // ========================

    public List<CashFlowDto> getCashFlow(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.cashFlow(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new CashFlowDto(
                (String) r[0],
                toDouble(r[1]),
                toDouble(r[2]),
                toDouble(r[3]),
                toDouble(r[4])
        )).toList();
    }

    // ========================
    // Section 3: Revenue vs Expense
    // ========================

    public List<RevenueExpenseDto> getRevenueVsExpense(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.revenueVsExpense(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new RevenueExpenseDto(
                (String) r[0],
                toDouble(r[1]),
                toDouble(r[2]),
                toDouble(r[3]),
                toDouble(r[4]),
                toDouble(r[5]),
                toDouble(r[6])
        )).toList();
    }

    // ========================
    // Section 3: Top Selling Items
    // ========================

    public List<TopItemDto> getTopSellingItems(Long companyId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Object[]> rows = analyticsRepository.topSellingItems(companyId, toStartOfDay(startDate), toEndOfDay(endDate), limit);
        return rows.stream().map(r -> new TopItemDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                toDouble(r[2]),
                toDouble(r[3])
        )).toList();
    }

    // ========================
    // Section 3: Top Profitable Items
    // ========================

    public List<TopItemDto> getTopProfitableItems(Long companyId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Object[]> rows = analyticsRepository.topProfitableItems(companyId, toStartOfDay(startDate), toEndOfDay(endDate), limit);
        return rows.stream().map(r -> new TopItemDto(
                ((Number) r[0]).longValue(),
                (String) r[1],
                toDouble(r[2]),
                toDouble(r[3])
        )).toList();
    }

    // ========================
    // Section 3: Payment Mode Distribution
    // ========================

    public List<PaymentModeDistributionDto> getPaymentModeDistribution(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.paymentModeDistribution(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new PaymentModeDistributionDto(
                (String) r[0],
                toDouble(r[1]),
                ((Number) r[2]).longValue(),
                toDouble(r[3])
        )).toList();
    }

    // ========================
    // Section 3: Business Growth
    // ========================

    public List<BusinessGrowthDto> getBusinessGrowth(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.businessGrowth(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new BusinessGrowthDto(
                (String) r[0],
                toDouble(r[1]),
                toDouble(r[2]),
                toDouble(r[3]),
                toDouble(r[4])
        )).toList();
    }

    // ========================
    // Section 3: Dashboard KPI
    // ========================

    public DashboardKpiDto getDashboardKpi(Long companyId, LocalDate startDate, LocalDate endDate) {
        Object[] r = analyticsRepository.dashboardKpi(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return new DashboardKpiDto(
                toDouble(r[0]),
                toDouble(r[1]),
                toDouble(r[2]),
                ((Number) r[3]).longValue(),
                ((Number) r[4]).longValue(),
                ((Number) r[5]).longValue(),
                ((Number) r[6]).longValue(),
                toDouble(r[7]),
                toDouble(r[8]),
                toDouble(r[9])
        );
    }

    // ========================
    // Section 3: Monthly Trend
    // ========================

    public List<MonthlyTrendDto> getMonthlyTrend(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = analyticsRepository.monthlyTrend(companyId, toStartOfDay(startDate), toEndOfDay(endDate));
        return rows.stream().map(r -> new MonthlyTrendDto(
                (String) r[0],
                toDouble(r[1]),
                toDouble(r[2]),
                toDouble(r[3]),
                toDouble(r[4])
        )).toList();
    }

    private Double toDouble(Object val) {
        if (val == null) return 0.0;
        return ((Number) val).doubleValue();
    }
}
