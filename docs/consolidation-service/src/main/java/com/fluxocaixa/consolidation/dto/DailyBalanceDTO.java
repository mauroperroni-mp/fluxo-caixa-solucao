package com.fluxocaixa.consolidation.dto;

import com.fluxocaixa.consolidation.model.DailyBalance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyBalanceDTO(
    String merchantId,
    LocalDate date,
    BigDecimal totalCredit,
    BigDecimal totalDebit,
    BigDecimal finalBalance
) {
    /**
     * Método utilitário para converter uma entidade DailyBalance em DailyBalanceDTO
     */
    public static DailyBalanceDTO fromEntity(DailyBalance entity) {
        return new DailyBalanceDTO(
            entity.getMerchantId(),
            entity.getDate(),
            entity.getTotalCredit(),
            entity.getTotalDebit(),
            entity.getFinalBalance()
        );
    }
}