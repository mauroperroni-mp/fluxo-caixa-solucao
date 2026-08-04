
// Entity
package com.fluxocaixa.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transaction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String merchantId;
    private String type; // CREDIT / DEBIT
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private LocalDateTime createdAt;
}