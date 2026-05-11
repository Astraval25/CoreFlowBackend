package com.astraval.coreflow.main_modules.expense;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT e FROM Expense e
            JOIN FETCH e.expenseAccount ea
            LEFT JOIN FETCH e.vendor v
            LEFT JOIN FETCH e.customer c
            WHERE e.company.companyId = :companyId
            ORDER BY e.expenseDate DESC, e.expenseId DESC
            """)
    List<Expense> findByCompanyIdWithDetails(@Param("companyId") Long companyId);

    @Query("""
            SELECT e FROM Expense e
            JOIN FETCH e.expenseAccount ea
            LEFT JOIN FETCH e.vendor v
            LEFT JOIN FETCH e.customer c
            WHERE e.company.companyId = :companyId
              AND e.isActive = true
            ORDER BY e.expenseDate DESC, e.expenseId DESC
            """)
    List<Expense> findActiveByCompanyIdWithDetails(@Param("companyId") Long companyId);

    @Query("""
            SELECT e FROM Expense e
            JOIN FETCH e.expenseAccount ea
            LEFT JOIN FETCH e.vendor v
            LEFT JOIN FETCH e.customer c
            WHERE e.expenseId = :expenseId
              AND e.company.companyId = :companyId
            """)
    Optional<Expense> findByExpenseIdAndCompanyIdWithDetails(
            @Param("expenseId") Long expenseId,
            @Param("companyId") Long companyId);
}
