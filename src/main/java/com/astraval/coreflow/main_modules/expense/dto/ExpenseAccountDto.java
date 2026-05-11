package com.astraval.coreflow.main_modules.expense.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseAccountDto {
    private Long expenseAccountId;
    private String accountType;
    private String accountName;
    private Boolean isActive;
    private LocalDateTime createdDt;
    private LocalDateTime lastModifiedDt;
}
