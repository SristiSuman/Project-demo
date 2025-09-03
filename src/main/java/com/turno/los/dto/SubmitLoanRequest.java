package com.turno.los.dto;

import com.turno.los.entity.enums.LoanType;
import javax.validation.constraints.*;

public class SubmitLoanRequest {
    @NotBlank public String loanId;
    @NotBlank public String customerName;
    @NotBlank public String customerPhone;
    @NotNull @Positive public Double amount;
    @NotNull public LoanType type;
}
