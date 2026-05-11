package com.astraval.coreflow.main_modules.expense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.expense.dto.CreateUpdateExpenseAccountDto;
import com.astraval.coreflow.main_modules.expense.dto.ExpenseAccountDto;

@Service
public class ExpenseAccountService {

    @Autowired
    private ExpenseAccountRepository expenseAccountRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public Long createExpenseAccount(Long companyId, CreateUpdateExpenseAccountDto request) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        String accountType = resolveAccountType(request.getAccountType());
        String accountName = normalizeRequired(request.getAccountName(), "Account name is required");

        if (expenseAccountRepository.existsByCompanyCompanyIdAndAccountNameIgnoreCase(companyId, accountName)) {
            throw new RuntimeException("Expense account '" + accountName + "' already exists for this company");
        }

        ExpenseAccount account = new ExpenseAccount();
        account.setCompany(company);
        account.setAccountType(accountType);
        account.setAccountName(accountName);
        account.setIsActive(true);

        return expenseAccountRepository.save(account).getExpenseAccountId();
    }

    public List<ExpenseAccountDto> getExpenseAccounts(Long companyId) {
        return expenseAccountRepository.findByCompanyCompanyIdOrderByAccountName(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ExpenseAccountDto> getActiveExpenseAccounts(Long companyId) {
        return expenseAccountRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByAccountName(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ExpenseAccountDto getExpenseAccount(Long companyId, Long expenseAccountId) {
        return toDto(getAccount(companyId, expenseAccountId));
    }

    @Transactional
    public void updateExpenseAccount(Long companyId, Long expenseAccountId, CreateUpdateExpenseAccountDto request) {
        ExpenseAccount account = getAccount(companyId, expenseAccountId);

        String accountType = resolveAccountType(request.getAccountType());
        String accountName = normalizeRequired(request.getAccountName(), "Account name is required");

        if (expenseAccountRepository.existsByCompanyCompanyIdAndAccountNameIgnoreCaseAndExpenseAccountIdNot(
                companyId,
                accountName,
                expenseAccountId)) {
            throw new RuntimeException("Expense account '" + accountName + "' already exists for this company");
        }

        account.setAccountType(accountType);
        account.setAccountName(accountName);
        expenseAccountRepository.save(account);
    }

    @Transactional
    public void deactivateExpenseAccount(Long companyId, Long expenseAccountId) {
        ExpenseAccount account = getAccount(companyId, expenseAccountId);
        account.setIsActive(false);
        expenseAccountRepository.save(account);
    }

    @Transactional
    public void activateExpenseAccount(Long companyId, Long expenseAccountId) {
        ExpenseAccount account = getAccount(companyId, expenseAccountId);
        account.setIsActive(true);
        expenseAccountRepository.save(account);
    }

    private ExpenseAccount getAccount(Long companyId, Long expenseAccountId) {
        return expenseAccountRepository.findByExpenseAccountIdAndCompanyCompanyId(expenseAccountId, companyId)
                .orElseThrow(() -> new RuntimeException("Expense account not found with ID: " + expenseAccountId));
    }

    private String resolveAccountType(String accountType) {
        String normalized = ExpenseAccountTypes.normalize(accountType);
        if (normalized == null) {
            throw new RuntimeException("Invalid account type. " + ExpenseAccountTypes.validTypesMessage());
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }

    private ExpenseAccountDto toDto(ExpenseAccount account) {
        return new ExpenseAccountDto(
                account.getExpenseAccountId(),
                account.getAccountType(),
                account.getAccountName(),
                account.getIsActive(),
                account.getCreatedDt(),
                account.getLastModifiedDt());
    }
}
