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
import com.astraval.coreflow.main_modules.expense.dto.CreateUpdateExpenseDto;
import com.astraval.coreflow.main_modules.expense.dto.ExpenseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies/{companyId}/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ApiResponse<Map<String, Long>> createExpense(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateUpdateExpenseDto request) {
        try {
            Long id = expenseService.createExpense(companyId, request);
            return ApiResponseFactory.created(Map.of("expenseId", id), "Expense created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping
    public ApiResponse<List<ExpenseDto>> getExpenses(
            @PathVariable Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            List<ExpenseDto> expenses = activeOnly
                    ? expenseService.getActiveExpenses(companyId)
                    : expenseService.getExpenses(companyId);
            return ApiResponseFactory.accepted(expenses, "Expenses retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{expenseId}")
    public ApiResponse<ExpenseDto> getExpense(
            @PathVariable Long companyId,
            @PathVariable Long expenseId) {
        try {
            ExpenseDto expense = expenseService.getExpense(companyId, expenseId);
            return ApiResponseFactory.accepted(expense, "Expense retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{expenseId}")
    public ApiResponse<Void> updateExpense(
            @PathVariable Long companyId,
            @PathVariable Long expenseId,
            @Valid @RequestBody CreateUpdateExpenseDto request) {
        try {
            expenseService.updateExpense(companyId, expenseId, request);
            return ApiResponseFactory.updated(null, "Expense updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{expenseId}/deactivate")
    public ApiResponse<Void> deactivateExpense(
            @PathVariable Long companyId,
            @PathVariable Long expenseId) {
        try {
            expenseService.deactivateExpense(companyId, expenseId);
            return ApiResponseFactory.updated(null, "Expense deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{expenseId}/activate")
    public ApiResponse<Void> activateExpense(
            @PathVariable Long companyId,
            @PathVariable Long expenseId) {
        try {
            expenseService.activateExpense(companyId, expenseId);
            return ApiResponseFactory.updated(null, "Expense activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
