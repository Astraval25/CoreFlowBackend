package com.astraval.coreflow.main_modules.expense;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseAccountRepository extends JpaRepository<ExpenseAccount, Long> {

    List<ExpenseAccount> findByCompanyCompanyIdOrderByAccountName(Long companyId);

    List<ExpenseAccount> findByCompanyCompanyIdAndIsActiveTrueOrderByAccountName(Long companyId);

    Optional<ExpenseAccount> findByExpenseAccountIdAndCompanyCompanyId(Long expenseAccountId, Long companyId);

    Optional<ExpenseAccount> findByCompanyCompanyIdAndAccountNameIgnoreCase(Long companyId, String accountName);

    boolean existsByCompanyCompanyIdAndAccountNameIgnoreCase(Long companyId, String accountName);

    boolean existsByCompanyCompanyIdAndAccountNameIgnoreCaseAndExpenseAccountIdNot(
            Long companyId,
            String accountName,
            Long expenseAccountId);
}
