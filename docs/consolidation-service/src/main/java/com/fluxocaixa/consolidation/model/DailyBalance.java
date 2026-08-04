package com.fluxocaixa.consolidation.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_daily_balance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String merchantId;
    private LocalDate date;
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private BigDecimal finalBalance;
}