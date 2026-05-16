package com.astraval.coreflow.main_modules.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.employee_module.enums.SalaryPeriodStatus;
import com.astraval.coreflow.employee_module.salary.EmployeeSalaryPeriod;
import com.astraval.coreflow.employee_module.salary.EmployeeSalaryPeriodRepository;
import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.customer.CustomerRepository;
import com.astraval.coreflow.main_modules.customer.Customers;
import com.astraval.coreflow.main_modules.expense.dto.CreateUpdateExpenseDto;
import com.astraval.coreflow.main_modules.expense.dto.ExpenseDto;
import com.astraval.coreflow.main_modules.vendor.VendorRepository;
import com.astraval.coreflow.main_modules.vendor.Vendors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseAccountRepository expenseAccountRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeSalaryPeriodRepository salaryPeriodRepository;

    @Transactional
    public Long createExpense(Long companyId, CreateUpdateExpenseDto request) {
        Companies company = getCompanyOrThrow(companyId);

        Expense expense = new Expense();
        expense.setCompany(company);
        applyExpenseDetails(companyId, expense, request);
        expense.setIsActive(true);

        Expense saved = expenseRepository.save(expense);
        syncSalaryPeriodPaymentStatus(saved.getSalaryPeriod());
        return saved.getExpenseId();
    }

    public List<ExpenseDto> getExpenses(Long companyId) {
        getCompanyOrThrow(companyId);
        return expenseRepository.findByCompanyIdWithDetails(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ExpenseDto> getActiveExpenses(Long companyId) {
        getCompanyOrThrow(companyId);
        return expenseRepository.findActiveByCompanyIdWithDetails(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ExpenseDto getExpense(Long companyId, Long expenseId) {
        return toDto(getExpenseEntity(companyId, expenseId));
    }

    @Transactional
    public void updateExpense(Long companyId, Long expenseId, CreateUpdateExpenseDto request) {
        Expense expense = getExpenseEntity(companyId, expenseId);
        EmployeeSalaryPeriod previousSalaryPeriod = expense.getSalaryPeriod();
        applyExpenseDetails(companyId, expense, request);
        Expense saved = expenseRepository.save(expense);
        syncSalaryPeriodPaymentStatus(previousSalaryPeriod);
        syncSalaryPeriodPaymentStatus(saved.getSalaryPeriod());
    }

    @Transactional
    public void deactivateExpense(Long companyId, Long expenseId) {
        Expense expense = getExpenseEntity(companyId, expenseId);
        expense.setIsActive(false);
        expenseRepository.save(expense);
        syncSalaryPeriodPaymentStatus(expense.getSalaryPeriod());
    }

    @Transactional
    public void activateExpense(Long companyId, Long expenseId) {
        Expense expense = getExpenseEntity(companyId, expenseId);
        expense.setIsActive(true);
        expenseRepository.save(expense);
        syncSalaryPeriodPaymentStatus(expense.getSalaryPeriod());
    }

    private void applyExpenseDetails(Long companyId, Expense expense, CreateUpdateExpenseDto request) {
        ExpenseAccount account = expenseAccountRepository
                .findByExpenseAccountIdAndCompanyCompanyId(request.getExpenseAccountId(), companyId)
                .orElseThrow(() -> new RuntimeException(
                        "Expense account not found with ID: " + request.getExpenseAccountId()));

        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new RuntimeException("Expense account '" + account.getAccountName() + "' is inactive");
        }

        Vendors vendor = null;
        if (request.getVendorId() != null) {
            vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(request.getVendorId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + request.getVendorId()));
        }

        Customers customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByCustomerIdAndCompanyCompanyId(request.getCustomerId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
        }

        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMode(normalizeRequired(request.getPaymentMode(), "Payment mode is required"));
        expense.setAmount(request.getAmount());
        expense.setExpenseAccount(account);
        expense.setInvoiceNo(normalizeOptional(request.getInvoiceNo()));
        expense.setVendor(vendor);
        expense.setCustomer(customer);
        expense.setRemark(normalizeOptional(request.getRemark()));
        expense.setSalaryPeriod(resolveSalaryPeriod(companyId, request.getSalaryPeriodId(), request.getAmount()));
    }

    private Expense getExpenseEntity(Long companyId, Long expenseId) {
        getCompanyOrThrow(companyId);
        return expenseRepository.findByExpenseIdAndCompanyIdWithDetails(expenseId, companyId)
                .orElseThrow(() -> new RuntimeException("Expense not found with ID: " + expenseId));
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private ExpenseDto toDto(Expense expense) {
        ExpenseAccount account = expense.getExpenseAccount();
        Vendors vendor = expense.getVendor();
        Customers customer = expense.getCustomer();
        return new ExpenseDto(
                expense.getExpenseId(),
                expense.getExpenseDate(),
                expense.getPaymentMode(),
                expense.getAmount(),
                account != null ? account.getExpenseAccountId() : null,
                account != null ? account.getAccountName() : null,
                account != null ? account.getAccountType() : null,
                expense.getInvoiceNo(),
                vendor != null ? vendor.getVendorId() : null,
                vendor != null ? vendor.getDisplayName() : null,
                customer != null ? customer.getCustomerId() : null,
                customer != null ? customer.getDisplayName() : null,
                expense.getRemark(),
                expense.getSalaryPeriod() != null ? expense.getSalaryPeriod().getSalaryPeriodId() : null,
                expense.getIsActive(),
                expense.getCreatedDt(),
                expense.getLastModifiedDt());
    }

    private EmployeeSalaryPeriod resolveSalaryPeriod(Long companyId, Long salaryPeriodId, BigDecimal amount) {
        if (salaryPeriodId == null) {
            return null;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Salary payment amount must be greater than zero");
        }

        EmployeeSalaryPeriod salaryPeriod = salaryPeriodRepository
                .findBySalaryPeriodIdAndCompanyCompanyId(salaryPeriodId, companyId)
                .orElseThrow(() -> new RuntimeException("Salary period not found with ID: " + salaryPeriodId));

        if (salaryPeriod.getStatus() == SalaryPeriodStatus.DRAFT) {
            throw new RuntimeException("Salary period must be approved before recording payment");
        }

        return salaryPeriod;
    }

    private Companies getCompanyOrThrow(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
    }

    private void syncSalaryPeriodPaymentStatus(EmployeeSalaryPeriod salaryPeriod) {
        if (salaryPeriod == null) {
            return;
        }

        EmployeeSalaryPeriod managed = salaryPeriodRepository
                .findById(salaryPeriod.getSalaryPeriodId())
                .orElse(null);

        if (managed == null || managed.getStatus() == SalaryPeriodStatus.DRAFT) {
            return;
        }

        BigDecimal paidAmount = expenseRepository.sumActiveAmountBySalaryPeriodId(managed.getSalaryPeriodId());
        if (managed.getExpense() != null && Boolean.TRUE.equals(managed.getExpense().getIsActive())) {
            paidAmount = paidAmount.add(managed.getExpense().getAmount());
        }
        BigDecimal netAmount = managed.getNetAmount() == null ? BigDecimal.ZERO : managed.getNetAmount();

        if (paidAmount.compareTo(netAmount) >= 0 && netAmount.compareTo(BigDecimal.ZERO) > 0) {
            managed.setStatus(SalaryPeriodStatus.PAID);
            if (managed.getPaidDt() == null) {
                managed.setPaidDt(LocalDateTime.now());
            }
        } else {
            managed.setStatus(SalaryPeriodStatus.APPROVED);
            managed.setPaidDt(null);
        }

        salaryPeriodRepository.save(managed);
    }
}
