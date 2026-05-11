package com.astraval.coreflow.main_modules.expense.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUpdateExpenseAccountDto {

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotBlank(message = "Account name is required")
    private String accountName;
}
