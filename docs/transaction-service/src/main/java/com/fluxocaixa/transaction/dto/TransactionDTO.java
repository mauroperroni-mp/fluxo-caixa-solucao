// DTO
package com.fluxocaixa.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
    @NotNull String merchantId,
    @NotNull String type, // CREDIT ou DEBIT
    @NotNull @Positive BigDecimal amount,
    String description,
    LocalDate date
) {}
