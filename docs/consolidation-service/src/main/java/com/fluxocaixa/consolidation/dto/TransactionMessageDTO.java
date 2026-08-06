package com.fluxocaixa.consolidation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionMessageDTO(
        Long transactionId,
        String merchantId,
        BigDecimal amount,
        String type,
        LocalDateTime timestamp
) {}
