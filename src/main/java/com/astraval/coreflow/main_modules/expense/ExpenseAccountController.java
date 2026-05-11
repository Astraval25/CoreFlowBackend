package com.astraval.coreflow.main_modules.expense;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.main_modules.expense.dto.CreateUpdateExpenseAccountDto;
import com.astraval.coreflow.main_modules.expense.dto.ExpenseAccountDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies/{companyId}/expense-accounts")
public class ExpenseAccountController {

    @Autowired
    private ExpenseAccountService expenseAccountService;

    @PostMapping
    public ApiResponse<Map<String, Long>> createExpenseAccount(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateUpdateExpenseAccountDto request) {
        try {
            Long id = expenseAccountService.createExpenseAccount(companyId, request);
            return ApiResponseFactory.created(Map.of("expenseAccountId", id), "Expense account created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping
    public ApiResponse<List<ExpenseAccountDto>> getExpenseAccounts(
            @PathVariable Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            List<ExpenseAccountDto> accounts = activeOnly
                    ? expenseAccountService.getActiveExpenseAccounts(companyId)
                    : expenseAccountService.getExpenseAccounts(companyId);
            return ApiResponseFactory.accepted(accounts, "Expense accounts retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/account-types")
    public ApiResponse<List<String>> getExpenseAccountTypes() {
        return ApiResponseFactory.accepted(ExpenseAccountTypes.ALLOWED_TYPES, "Expense account types retrieved successfully");
    }

    @GetMapping("/{expenseAccountId}")
    public ApiResponse<ExpenseAccountDto> getExpenseAccount(
            @PathVariable Long companyId,
            @PathVariable Long expenseAccountId) {
        try {
            ExpenseAccountDto account = expenseAccountService.getExpenseAccount(companyId, expenseAccountId);
            return ApiResponseFactory.accepted(account, "Expense account retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{expenseAccountId}")
    public ApiResponse<Void> updateExpenseAccount(
            @PathVariable Long companyId,
            @PathVariable Long expenseAccountId,
            @Valid @RequestBody CreateUpdateExpenseAccountDto request) {
        try {
            expenseAccountService.updateExpenseAccount(companyId, expenseAccountId, request);
            return ApiResponseFactory.updated(null, "Expense account updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{expenseAccountId}/deactivate")
    public ApiResponse<Void> deactivateExpenseAccount(
            @PathVariable Long companyId,
            @PathVariable Long expenseAccountId) {
        try {
            expenseAccountService.deactivateExpenseAccount(companyId, expenseAccountId);
            return ApiResponseFactory.updated(null, "Expense account deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{expenseAccountId}/activate")
    public ApiResponse<Void> activateExpenseAccount(
            @PathVariable Long companyId,
            @PathVariable Long expenseAccountId) {
        try {
            expenseAccountService.activateExpenseAccount(companyId, expenseAccountId);
            return ApiResponseFactory.updated(null, "Expense account activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
