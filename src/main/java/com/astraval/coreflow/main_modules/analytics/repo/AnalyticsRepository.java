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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND p.payment_status != 'PAYMENT_REFUND'
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
                  AND p.payment_status != 'PAYMENT_REFUND'
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                      AND p.payment_status != 'PAYMENT_REFUND'
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
                      AND p.payment_status != 'PAYMENT_REFUND'
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                              AND p.payment_status != 'PAYMENT_REFUND'
                              AND p.payment_date < :startDate), 0)
                        - COALESCE((SELECT SUM(p.amount) FROM payments p
                            JOIN vendors v ON p.vendor = v.vendor_id
                            WHERE v.comp_id = :companyId AND p.is_active = true
                              AND p.payment_status != 'PAYMENT_REFUND'
                              AND p.payment_date < :startDate), 0)
                        - COALESCE((SELECT SUM(e.amount) FROM expenses e
                            WHERE e.comp_id = :companyId AND e.is_active = true
                              AND e.expense_date < CAST(:startDate AS date)), 0) AS opening
                ),
                monthly_incoming AS (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS m, COALESCE(SUM(p.amount), 0) AS total
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_REFUND'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ),
                monthly_outgoing AS (
                    SELECT m, COALESCE(SUM(total), 0) AS total
                    FROM (
                        SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS m, COALESCE(SUM(p.amount), 0) AS total
                        FROM payments p
                        JOIN vendors v ON p.vendor = v.vendor_id
                        WHERE v.comp_id = :companyId AND p.is_active = true
                          AND p.payment_status != 'PAYMENT_REFUND'
                          AND p.payment_date BETWEEN :startDate AND :endDate
                        GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                        UNION ALL
                        SELECT TO_CHAR(e.expense_date, 'YYYY-MM') AS m, COALESCE(SUM(e.amount), 0) AS total
                        FROM expenses e
                        WHERE e.comp_id = :companyId AND e.is_active = true
                          AND e.expense_date BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)
                        GROUP BY TO_CHAR(e.expense_date, 'YYYY-MM')
                    ) outgoing
                    GROUP BY m
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
                    COALESCE(purchases.expense, 0) + COALESCE(expense_entries.expense, 0) AS expense,
                    COALESCE(sales.revenue, 0) - (COALESCE(purchases.expense, 0) + COALESCE(expense_entries.expense, 0)) AS net_profit,
                    SUM(COALESCE(sales.revenue, 0)) OVER (ORDER BY months.month) AS running_revenue,
                    SUM(COALESCE(purchases.expense, 0) + COALESCE(expense_entries.expense, 0)) OVER (ORDER BY months.month) AS running_expense,
                    SUM(COALESCE(sales.revenue, 0) - (COALESCE(purchases.expense, 0) + COALESCE(expense_entries.expense, 0))) OVER (ORDER BY months.month) AS running_net_profit
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
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) sales ON months.month = sales.s_month
                LEFT JOIN (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS p_month, SUM(od.total_amount) AS expense
                    FROM order_details od
                    JOIN vendors v ON od.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) purchases ON months.month = purchases.p_month
                LEFT JOIN (
                    SELECT TO_CHAR(e.expense_date, 'YYYY-MM') AS e_month, SUM(e.amount) AS expense
                    FROM expenses e
                    WHERE e.comp_id = :companyId
                      AND e.is_active = true
                      AND e.expense_date BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)
                    GROUP BY TO_CHAR(e.expense_date, 'YYYY-MM')
                ) expense_entries ON months.month = expense_entries.e_month
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                      AND p.payment_status != 'PAYMENT_REFUND'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    UNION ALL
                    SELECT p.mode_of_payment, p.amount
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_REFUND'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    UNION ALL
                    SELECT e.payment_mode AS mode_of_payment, e.amount
                    FROM expenses e
                    WHERE e.comp_id = :companyId AND e.is_active = true
                      AND e.expense_date BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)
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
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
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
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                ),
                purchases AS (
                    SELECT COUNT(od.order_id) AS total_orders,
                           COALESCE(SUM(od.total_amount), 0) AS total_amount,
                           COALESCE(SUM(od.paid_amount), 0) AS total_paid
                    FROM order_details od
                    JOIN vendors v ON od.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                ),
                payments_received AS (
                    SELECT COUNT(*) AS cnt, COALESCE(SUM(p.amount), 0) AS total
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_REFUND'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                ),
                payments_made AS (
                    SELECT COUNT(*) AS cnt, COALESCE(SUM(p.amount), 0) AS total
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_REFUND'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                ),
                expense_entries AS (
                    SELECT COUNT(*) AS cnt, COALESCE(SUM(e.amount), 0) AS total
                    FROM expenses e
                    WHERE e.comp_id = :companyId
                      AND e.is_active = true
                      AND e.expense_date BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)
                )
                SELECT
                    (SELECT total_amount FROM sales) AS total_revenue,
                    (SELECT total_amount FROM purchases) + (SELECT total FROM expense_entries) AS total_expense,
                    (SELECT total_amount FROM sales) - ((SELECT total_amount FROM purchases) + (SELECT total FROM expense_entries)) AS net_profit,
                    (SELECT total_orders FROM sales) AS total_sales_orders,
                    (SELECT total_orders FROM purchases) AS total_purchase_orders,
                    (SELECT cnt FROM payments_received) AS total_payments_received,
                    (SELECT cnt FROM payments_made) + (SELECT cnt FROM expense_entries) AS total_payments_made,
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
                    COALESCE(purchases.amount, 0) + COALESCE(expense_entries.amount, 0) AS purchase_amount,
                    COALESCE(pr.amount, 0) AS payment_received,
                    COALESCE(pm.amount, 0) + COALESCE(expense_entries.amount, 0) AS payment_made
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
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) sales ON months.month = sales.month
                LEFT JOIN (
                    SELECT TO_CHAR(od.order_date, 'YYYY-MM') AS month, SUM(od.total_amount) AS amount
                    FROM order_details od
                    JOIN vendors v ON od.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND od.is_active = true
                      AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
                      AND od.order_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(od.order_date, 'YYYY-MM')
                ) purchases ON months.month = purchases.month
                LEFT JOIN (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, SUM(p.amount) AS amount
                    FROM payments p
                    JOIN customers c ON p.customer = c.customer_id
                    WHERE c.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_REFUND'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ) pr ON months.month = pr.month
                LEFT JOIN (
                    SELECT TO_CHAR(p.payment_date, 'YYYY-MM') AS month, SUM(p.amount) AS amount
                    FROM payments p
                    JOIN vendors v ON p.vendor = v.vendor_id
                    WHERE v.comp_id = :companyId AND p.is_active = true
                      AND p.payment_status != 'PAYMENT_REFUND'
                      AND p.payment_date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(p.payment_date, 'YYYY-MM')
                ) pm ON months.month = pm.month
                LEFT JOIN (
                    SELECT TO_CHAR(e.expense_date, 'YYYY-MM') AS month, SUM(e.amount) AS amount
                    FROM expenses e
                    WHERE e.comp_id = :companyId AND e.is_active = true
                      AND e.expense_date BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)
                    GROUP BY TO_CHAR(e.expense_date, 'YYYY-MM')
                ) expense_entries ON months.month = expense_entries.month
                ORDER BY months.month
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    public List<Object[]> orderHistory(
            Long companyId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String orderType,
            String paidState,
            List<String> statuses) {
        String sql = """
                SELECT
                    od.order_id,
                    CASE WHEN c.comp_id = :companyId THEN 'SALES' ELSE 'PURCHASE' END AS order_type,
                    od.order_date,
                    CASE
                        WHEN c.comp_id = :companyId THEN COALESCE(NULLIF(c.display_name, ''), c.customer_name, '')
                        ELSE COALESCE(NULLIF(v.display_name, ''), v.vendor_name, '')
                    END AS party_name,
                    COALESCE(cor.local_order_number, od.order_number, '') AS local_order_number,
                    od.order_status,
                    COALESCE(SUM(oid.quantity), 0) AS total_item_quantity,
                    COALESCE(od.total_amount, 0) AS total_amount,
                    COALESCE(od.paid_amount, 0) AS paid_amount,
                    CASE
                        WHEN COALESCE(od.total_amount, 0) > 0
                            THEN ROUND((COALESCE(od.paid_amount, 0) / od.total_amount) * 100)
                        ELSE 0
                    END AS paid_percentage
                FROM order_details od
                LEFT JOIN customers c ON od.customer = c.customer_id
                LEFT JOIN vendors v ON od.vendor = v.vendor_id
                LEFT JOIN order_item_details oid ON oid.order_id = od.order_id
                LEFT JOIN company_order_ref cor
                    ON cor.order_id = od.order_id
                   AND cor.company_id = :companyId
                WHERE od.is_active = true
                  AND (c.comp_id = :companyId OR v.comp_id = :companyId)
                  AND od.order_status NOT IN ('QUOTATION','QUOTATION_VIEWED','QUOTATION_ACCEPTED','QUOTATION_DECLINED','ORDER_CANCELLED')
                  AND od.order_date BETWEEN :startDate AND :endDate
                  AND (:orderType = 'ALL'
                       OR (:orderType = 'SALES' AND c.comp_id = :companyId)
                       OR (:orderType = 'PURCHASE' AND v.comp_id = :companyId))
                  AND (
                        :paidState = 'ALL'
                        OR (:paidState = 'UNPAID' AND COALESCE(od.paid_amount, 0) <= 0)
                        OR (:paidState = 'PARTIAL' AND COALESCE(od.paid_amount, 0) > 0 AND COALESCE(od.paid_amount, 0) < COALESCE(od.total_amount, 0))
                        OR (:paidState = 'PAID' AND COALESCE(od.total_amount, 0) > 0 AND COALESCE(od.paid_amount, 0) >= COALESCE(od.total_amount, 0))
                      )
                  AND (:statusesEmpty = true OR od.order_status IN :statuses)
                GROUP BY od.order_id, order_type, od.order_date, c.comp_id, c.display_name, c.customer_name, v.display_name, v.vendor_name, cor.local_order_number, od.order_number, od.order_status, od.total_amount, od.paid_amount
                ORDER BY od.order_date DESC, od.order_id DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("orderType", orderType);
        q.setParameter("paidState", paidState);
        q.setParameter("statusesEmpty", statuses == null || statuses.isEmpty());
        q.setParameter("statuses", statuses == null || statuses.isEmpty() ? List.of("DUMMY_STATUS") : statuses);
        return q.getResultList();
    }

    public List<Object[]> paymentHistory(
            Long companyId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String paymentType,
            List<String> statuses) {
        String sql = """
                SELECT
                    p.payment_id,
                    CASE WHEN c.comp_id = :companyId THEN 'RECEIVED' ELSE 'MADE' END AS payment_type,
                    p.payment_date,
                    CASE
                        WHEN c.comp_id = :companyId THEN COALESCE(NULLIF(c.display_name, ''), c.customer_name, '')
                        ELSE COALESCE(NULLIF(v.display_name, ''), v.vendor_name, '')
                    END AS party_name,
                    COALESCE(cpr.local_payment_number, p.payment_number, '') AS local_payment_number,
                    p.payment_status,
                    p.mode_of_payment,
                    COALESCE(p.amount, 0) AS amount
                FROM payments p
                LEFT JOIN customers c ON p.customer = c.customer_id
                LEFT JOIN vendors v ON p.vendor = v.vendor_id
                LEFT JOIN company_payment_ref cpr
                    ON cpr.payment_id = p.payment_id
                   AND cpr.company_id = :companyId
                WHERE p.is_active = true
                  AND (c.comp_id = :companyId OR v.comp_id = :companyId)
                  AND p.payment_status <> 'PAYMENT_REFUND'
                  AND p.payment_date BETWEEN :startDate AND :endDate
                  AND (:paymentType = 'ALL'
                       OR (:paymentType = 'RECEIVED' AND c.comp_id = :companyId)
                       OR (:paymentType = 'MADE' AND v.comp_id = :companyId))
                  AND (:statusesEmpty = true OR p.payment_status IN :statuses)
                ORDER BY p.payment_date DESC, p.payment_id DESC
                """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("companyId", companyId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("paymentType", paymentType);
        q.setParameter("statusesEmpty", statuses == null || statuses.isEmpty());
        q.setParameter("statuses", statuses == null || statuses.isEmpty() ? List.of("DUMMY_STATUS") : statuses);
        return q.getResultList();
    }
}
