package com.astraval.coreflow.main_modules.analytics.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
@SuppressWarnings("unchecked")
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager em;

    // ========================
    // Section 1: Order Frequency
    // ========================

    public List<Object[]> salesOrderFrequency(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS month, COUNT(*) AS order_count
                FROM order_details od
                JOIN customers c ON od.customer = c.customer_id
                WHERE c.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> purchaseOrderFrequency(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS month, COUNT(*) AS order_count
                FROM order_details od
                JOIN vendors v ON od.vendor = v.vendor_id
                WHERE v.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 1: Payment Frequency
    // ========================

    public List<Object[]> salesPaymentFrequency(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, COUNT(*) AS payment_count
                FROM payments p
                JOIN customers c ON p.customer = c.customer_id
                WHERE c.comp_id = :companyId
                  AND p.is_active = true
                  AND p.payment_status != 'PAYMENT_DECLINED'
                  AND p.payment_date BETWEEN :startDate AND :endDate
                GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> purchasePaymentFrequency(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, COUNT(*) AS payment_count
                FROM payments p
                JOIN vendors v ON p.vendor = v.vendor_id
                WHERE v.comp_id = :companyId
                  AND p.is_active = true
                  AND p.payment_status != 'PAYMENT_DECLINED'
                  AND p.payment_date BETWEEN :startDate AND :endDate
                GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 1: Item Frequency
    // ========================

    public List<Object[]> salesItemFrequency(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT i.item_id, i.item_name, COALESCE(SUM(oid.quantity), 0) AS total_quantity, COUNT(DISTINCT od.order_id) AS order_count
                FROM order_item_details oid
                JOIN items i ON oid.item_id = i.item_id
                JOIN order_details od ON oid.order_id = od.order_id
                JOIN customers c ON od.customer = c.customer_id
                WHERE c.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY i.item_id, i.item_name
                ORDER BY total_quantity DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> purchaseItemFrequency(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT i.item_id, i.item_name, COALESCE(SUM(oid.quantity), 0) AS total_quantity, COUNT(DISTINCT od.order_id) AS order_count
                FROM order_item_details oid
                JOIN items i ON oid.item_id = i.item_id
                JOIN order_details od ON oid.order_id = od.order_id
                JOIN vendors v ON od.vendor = v.vendor_id
                WHERE v.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY i.item_id, i.item_name
                ORDER BY total_quantity DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 1: Running Order Amount
    // ========================

    public List<Object[]> salesRunningOrderAmount(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT month, SUM(monthly_total) OVER (ORDER BY month) AS cumulative_amount
                FROM (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS month, COALESCE(SUM(od.total_amount), 0) AS monthly_total
                    FROM order_details od
                    JOIN customers c ON od.customer = c.customer_id
                    WHERE c.comp_id = :companyId
                      AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) sub
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> purchaseRunningOrderAmount(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT month, SUM(monthly_total) OVER (ORDER BY month) AS cumulative_amount
                FROM (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS month, COALESCE(SUM(od.total_amount), 0) AS monthly_total
                    FROM order_details od
                    JOIN vendors v ON od.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId
                      AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) sub
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 1: Running Payment Amount
    // ========================

    public List<Object[]> salesRunningPaymentAmount(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT month, SUM(monthly_total) OVER (ORDER BY month) AS cumulative_amount
                FROM (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, COALESCE(SUM(p.amount), 0) AS monthly_total
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId
                      AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ) sub
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> purchaseRunningPaymentAmount(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT month, SUM(monthly_total) OVER (ORDER BY month) AS cumulative_amount
                FROM (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, COALESCE(SUM(p.amount), 0) AS monthly_total
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId
                      AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ) sub
                ORDER BY month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 2: Sales/Purchase by Party
    // ========================

    public List<Object[]> salesByCustomer(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT c.customer_id, c.display_name,
                    COUNT(od.order_id) AS total_orders,
                    COALESCE(SUM(od.total_amount), 0) AS total_amount,
                    COALESCE(SUM(od.paid_amount), 0) AS paid_amount,
                    COALESCE(SUM(od.total_amount), 0) - COALESCE(SUM(od.paid_amount), 0) AS due_amount
                FROM order_details od
                JOIN customers c ON od.customer = c.customer_id
                WHERE c.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY c.customer_id, c.display_name
                ORDER BY total_amount DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> purchaseByVendor(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT v.vendor_id, v.display_name,
                    COUNT(od.order_id) AS total_orders,
                    COALESCE(SUM(od.total_amount), 0) AS total_amount,
                    COALESCE(SUM(od.paid_amount), 0) AS paid_amount,
                    COALESCE(SUM(od.total_amount), 0) - COALESCE(SUM(od.paid_amount), 0) AS due_amount
                FROM order_details od
                JOIN vendors v ON od.vendor = v.vendor_id
                WHERE v.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY v.vendor_id, v.display_name
                ORDER BY total_amount DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 2: Sales/Purchase by Item
    // ========================

    public List<Object[]> salesByItem(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT i.item_id, i.item_name, COALESCE(SUM(oid.quantity), 0) AS total_quantity,
                    COALESCE(SUM(oid.item_total), 0) AS total_amount
                FROM order_item_details oid
                JOIN items i ON oid.item_id = i.item_id
                JOIN order_details od ON oid.order_id = od.order_id
                JOIN customers c ON od.customer = c.customer_id
                WHERE c.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY i.item_id, i.item_name
                ORDER BY total_amount DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> purchaseByItem(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT i.item_id, i.item_name, COALESCE(SUM(oid.quantity), 0) AS total_quantity,
                    COALESCE(SUM(oid.item_total), 0) AS total_amount
                FROM order_item_details oid
                JOIN items i ON oid.item_id = i.item_id
                JOIN order_details od ON oid.order_id = od.order_id
                JOIN vendors v ON od.vendor = v.vendor_id
                WHERE v.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY i.item_id, i.item_name
                ORDER BY total_amount DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 2: Sales/Purchase Summary
    // ========================

    public Object[] salesSummary(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT COUNT(od.order_id) AS total_orders,
                    COALESCE(SUM(od.total_amount), 0) AS total_amount,
                    COALESCE(SUM(od.paid_amount), 0) AS total_paid,
                    COALESCE(SUM(od.total_amount), 0) - COALESCE(SUM(od.paid_amount), 0) AS total_due,
                    CASE WHEN COUNT(od.order_id) > 0 THEN COALESCE(SUM(od.total_amount), 0) / COUNT(od.order_id) ELSE 0 END AS avg_order_value
                FROM order_details od
                JOIN customers c ON od.customer = c.customer_id
                WHERE c.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return (Object[]) q.getSingleResult();
    }

    public Object[] purchaseSummary(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT COUNT(od.order_id) AS total_orders,
                    COALESCE(SUM(od.total_amount), 0) AS total_amount,
                    COALESCE(SUM(od.paid_amount), 0) AS total_paid,
                    COALESCE(SUM(od.total_amount), 0) - COALESCE(SUM(od.paid_amount), 0) AS total_due,
                    CASE WHEN COUNT(od.order_id) > 0 THEN COALESCE(SUM(od.total_amount), 0) / COUNT(od.order_id) ELSE 0 END AS avg_order_value
                FROM order_details od
                JOIN vendors v ON od.vendor = v.vendor_id
                WHERE v.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return (Object[]) q.getSingleResult();
    }

    // ========================
    // Section 2: Profit by Item
    // ========================

    public List<Object[]> profitByItem(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT i.item_id, i.item_name,
                    COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.item_total END), 0) AS total_sales,
                    COALESCE(SUM(CASE WHEN v.comp_id = :companyId THEN oid.item_total END), 0) AS total_purchase,
                    COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.item_total END), 0)
                        - COALESCE(SUM(CASE WHEN v.comp_id = :companyId THEN oid.item_total END), 0) AS profit,
                    CASE WHEN COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.item_total END), 0) > 0
                        THEN ((COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.item_total END), 0)
                               - COALESCE(SUM(CASE WHEN v.comp_id = :companyId THEN oid.item_total END), 0))
                              / COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.item_total END), 0)) * 100
                        ELSE 0 END AS profit_margin
                FROM order_item_details oid
                JOIN items i ON oid.item_id = i.item_id
                JOIN order_details od ON oid.order_id = od.order_id
                LEFT JOIN customers c ON od.customer = c.customer_id
                LEFT JOIN vendors v ON od.vendor = v.vendor_id
                WHERE (c.comp_id = :companyId OR v.comp_id = :companyId)
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY i.item_id, i.item_name
                ORDER BY profit DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 3: Cash Flow
    // ========================

    public List<Object[]> cashFlow(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                WITH initial_balance AS (
                    SELECT
                        COALESCE((SELECT SUM(p.amount) FROM payments p
                            JOIN customers c ON p.customer = c.customer_id
                            WHERE c.comp_id = :companyId AND p.is_active = true
                              AND p.payment_status != 'PAYMENT_DECLINED'
                              AND p.payment_date < :startDate), 0)
                        - COALESCE((SELECT SUM(p.amount) FROM payments p
                            JOIN vendors v ON p.vendor = v.vendor_id
                            WHERE v.comp_id = :companyId AND p.is_active = true
                              AND p.payment_status != 'PAYMENT_DECLINED'
                              AND p.payment_date < :startDate), 0) AS opening
                ),
                monthly_incoming AS (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS m, COALESCE(SUM(p.amount), 0) AS total
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ),
                monthly_outgoing AS (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS m, COALESCE(SUM(p.amount), 0) AS total
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ),
                monthly AS (
                    SELECT g.month,
                        COALESCE(mi.total, 0) AS incoming,
                        COALESCE(mo.total, 0) AS outgoing
                    FROM (
                        SELECT TO_CHAR(gs.month, 'YYYY-MM') AS month
                        FROM generate_series(DATE_TRUNC('month', CAST(:startDate AS timestamp)),
                                             DATE_TRUNC('month', CAST(:endDate AS timestamp)), '1 month') gs(month)
                    ) g
                    LEFT JOIN monthly_incoming mi ON g.month = mi.m
                    LEFT JOIN monthly_outgoing mo ON g.month = mo.m
                )
                SELECT m.month,
                    (SELECT opening FROM initial_balance)
                        + COALESCE(SUM(m2.incoming - m2.outgoing) OVER (ORDER BY m2.month ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING), 0) AS opening_balance,
                    m.incoming,
                    m.outgoing,
                    (SELECT opening FROM initial_balance)
                        + SUM(m2.incoming - m2.outgoing) OVER (ORDER BY m2.month) AS closing_balance
                FROM monthly m
                JOIN monthly m2 ON m.month = m2.month
                ORDER BY m.month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 3: Revenue vs Expense (monthly)
    // ========================

    public List<Object[]> revenueVsExpense(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT months.month,
                    COALESCE(sales.revenue, 0) AS revenue,
                    COALESCE(purchases.expense, 0) AS expense,
                    COALESCE(sales.revenue, 0) - COALESCE(purchases.expense, 0) AS net_profit,
                    SUM(COALESCE(sales.revenue, 0)) OVER (ORDER BY months.month) AS running_revenue,
                    SUM(COALESCE(purchases.expense, 0)) OVER (ORDER BY months.month) AS running_expense,
                    SUM(COALESCE(sales.revenue, 0) - COALESCE(purchases.expense, 0)) OVER (ORDER BY months.month) AS running_net_profit
                FROM (
                    SELECT TO_CHAR(gs.month, 'YYYY-MM') AS month
                    FROM generate_series(DATE_TRUNC('month', CAST(:startDate AS timestamp)),
                                         DATE_TRUNC('month', CAST(:endDate AS timestamp)), '1 month') gs(month)
                ) months
                LEFT JOIN (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS s_month, SUM(od.total_amount) AS revenue
                    FROM order_details od
                    JOIN customers c ON od.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) sales ON months.month = sales.s_month
                LEFT JOIN (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS p_month, SUM(od.total_amount) AS expense
                    FROM order_details od
                    JOIN vendors v ON od.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) purchases ON months.month = purchases.p_month
                ORDER BY months.month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 3: Top Selling Items
    // ========================

    public List<Object[]> topSellingItems(Long companyId, LocalDateTime startDate, LocalDateTime endDate, int limit) {
        String sql = """
                SELECT i.item_id, i.item_name, COALESCE(SUM(oid.item_total), 0) AS total_amount,
                    COALESCE(SUM(oid.quantity), 0) AS total_quantity
                FROM order_item_details oid
                JOIN items i ON oid.item_id = i.item_id
                JOIN order_details od ON oid.order_id = od.order_id
                JOIN customers c ON od.customer = c.customer_id
                WHERE c.comp_id = :companyId
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY i.item_id, i.item_name
                ORDER BY total_quantity DESC
                LIMIT :limit
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("limit", limit);
        return q.getResultList();
    }

    // ========================
    // Section 3: Top Profitable Items
    // ========================

    public List<Object[]> topProfitableItems(Long companyId, LocalDateTime startDate, LocalDateTime endDate, int limit) {
        String sql = """
                SELECT i.item_id, i.item_name,
                    COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.item_total END), 0) AS total_sales,
                    COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.quantity END), 0) AS total_quantity
                FROM order_item_details oid
                JOIN items i ON oid.item_id = i.item_id
                JOIN order_details od ON oid.order_id = od.order_id
                LEFT JOIN customers c ON od.customer = c.customer_id
                LEFT JOIN vendors v ON od.vendor = v.vendor_id
                WHERE (c.comp_id = :companyId OR v.comp_id = :companyId)
                  AND od.is_active = true
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                GROUP BY i.item_id, i.item_name
                ORDER BY (COALESCE(SUM(CASE WHEN c.comp_id = :companyId THEN oid.item_total END), 0)
                         - COALESCE(SUM(CASE WHEN v.comp_id = :companyId THEN oid.item_total END), 0)) DESC
                LIMIT :limit
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("limit", limit);
        return q.getResultList();
    }

    // ========================
    // Section 3: Payment Mode Distribution
    // ========================

    public List<Object[]> paymentModeDistribution(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                WITH all_payments AS (
                    SELECT p.mode_of_payment, p.amount
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    UNION ALL
                    SELECT p.mode_of_payment, p.amount
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                ),
                totals AS (
                    SELECT COALESCE(SUM(amount), 0) AS grand_total FROM all_payments
                )
                SELECT ap.mode_of_payment AS mode,
                    COALESCE(SUM(ap.amount), 0) AS total_amount,
                    COUNT(*) AS transaction_count,
                    CASE WHEN (SELECT grand_total FROM totals) > 0
                        THEN (COALESCE(SUM(ap.amount), 0) / (SELECT grand_total FROM totals)) * 100
                        ELSE 0 END AS percentage
                FROM all_payments ap
                GROUP BY ap.mode_of_payment
                ORDER BY total_amount DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 3: Business Growth
    // ========================


    public List<Object[]> businessGrowth(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT m.month,
                    COALESCE(s.monthly_amount, 0) AS current_month_amount,
                    SUM(COALESCE(s.monthly_amount, 0)) OVER (ORDER BY m.month) AS running_total,
                    LAG(COALESCE(s.monthly_amount, 0), 1, 0.0) OVER (ORDER BY m.month) AS previous_month_amount,
                    CASE WHEN LAG(COALESCE(s.monthly_amount, 0), 1, 0.0) OVER (ORDER BY m.month) > 0
                        THEN ((COALESCE(s.monthly_amount, 0) - LAG(COALESCE(s.monthly_amount, 0), 1, 0.0) OVER (ORDER BY m.month))
                              / LAG(COALESCE(s.monthly_amount, 0), 1, 0.0) OVER (ORDER BY m.month)) * 100
                        ELSE 0 END AS growth_percentage
                FROM (
                    SELECT TO_CHAR(gs.month, 'YYYY-MM') AS month
                    FROM genera

                LEFT JOIN (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS s_month, SUM(od.total_amount) AS monthly_amount
                    FROM order_details od
                    JOIN customers c ON od.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) s ON m.month = s.s_month
                ORDER BY m.month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    // ========================
    // Section 3: Dashboard KPI
    // ========================

    public Object[] dashboardKpi(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                WITH sales AS (
                    SELECT COUNT(od.order_id) AS total_orders,
                           COALESCE(SUM(od.total_amount), 0) AS total_amount,
                           COALESCE(SUM(od.paid_amount), 0) AS total_paid
                    FROM order_details od
                    JOIN customers c ON od.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                ),
                purchases AS (
                    SELECT COUNT(od.order_id) AS total_orders,
                           COALESCE(SUM(od.total_amount), 0) AS total_amount,
                           COALESCE(SUM(od.paid_amount), 0) AS total_paid
                    FROM order_details od
                    JOIN vendors v ON od.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                ),
                payments_received AS (
                    SELECT COUNT(*) AS cnt, COALESCE(SUM(p.amount), 0) AS total
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                ),
                payments_made AS (
                    SELECT COUNT(*) AS cnt, COALESCE(SUM(p.amount), 0) AS total
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                )
                SELECT
                    (SELECT total_amount FROM sales) AS total_revenue,
                    (SELECT total_amount FROM purchases) AS total_expense,
                    (SELECT total_amount FROM sales) - (SELECT total_amount FROM purchases) AS net_profit,
                    (SELECT total_orders FROM sales) AS total_sales_orders,
                    (SELECT total_orders FROM purchases) AS total_purchase_orders,
                    (SELECT cnt FROM payments_received) AS total_payments_received,
                    (SELECT cnt FROM payments_made) AS total_payments_made,
                    CASE WHEN (SELECT total_orders FROM sales) + (SELECT total_orders FROM purchases) > 0
                        THEN ((SELECT total_amount FROM sales) + (SELECT total_amount FROM purchases))
                             / ((SELECT total_orders FROM sales) + (SELECT total_orders FROM purchases))
                        ELSE 0 END AS avg_order_value,
                    (SELECT total_amount FROM sales) - (SELECT total_paid FROM sales) AS outstanding_receivables,
                    (SELECT total_amount FROM purchases) - (SELECT total_paid FROM purchases) AS outstanding_payables
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return (Object[]) q.getSingleResult();
    }

    // ========================
    // Section 3: Monthly Trend
    // ========================

    public List<Object[]> monthlyTrend(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT months.month,
                    COALESCE(sales.amount, 0) AS sales_amount,
                    COALESCE(purchases.amount, 0) AS purchase_amount,
                    COALESCE(pr.amount, 0) AS payment_received,
                    COALESCE(pm.amount, 0) AS payment_made
                FROM (
                    SELECT TO_CHAR(gs.month, 'YYYY-MM') AS month
                    FROM generate_series(DATE_TRUNC('month', CAST(:startDate AS timestamp)),
                                         DATE_TRUNC('month', CAST(:endDate AS timestamp)), '1 month') gs(month)
                ) months
                LEFT JOIN (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS month, SUM(od.total_amount) AS amount
                    FROM order_details od
                    JOIN customers c ON od.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) sales ON months.month = sales.month
                LEFT JOIN (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS month, SUM(od.total_amount) AS amount
                    FROM order_details od
                    JOIN vendors v ON od.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) purchases ON months.month = purchases.month
                LEFT JOIN (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, SUM(p.amount) AS amount
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ) pr ON months.month = pr.month
                LEFT JOIN (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, SUM(p.amount) AS amount
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_DECLINED'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ) pm ON months.month = pm.month
                ORDER BY months.month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }
}
