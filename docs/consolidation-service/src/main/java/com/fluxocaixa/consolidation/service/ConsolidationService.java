package com.fluxocaixa.consolidation.service;

import com.fluxocaixa.consolidation.dto.TransactionMessageDTO;
import com.fluxocaixa.consolidation.model.DailyBalance;
import com.fluxocaixa.consolidation.repository.DailyBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsolidationService {

    private final DailyBalanceRepository repository;

    /**
     * Processa um evento de transação recebido da fila de forma assíncrona
     * e atualiza o saldo consolidado do comerciante para a data correspondente.
     */
    @Transactional
    public void processTransaction(TransactionMessageDTO message) {
        log.info("Processando consolidação para MerchantId: {}, Data: {}", message.merchantId(), message.date());

        // Busca o saldo existente para a data ou cria uma nova estrutura zerada
        DailyBalance balance = repository.findByMerchantIdAndDate(message.merchantId(), message.date())
                .orElseGet(() -> {
                    log.info("Novo registro de saldo diário criado para MerchantId: {} na data: {}", message.merchantId(), message.date());
                    return DailyBalance.builder()
                            .merchantId(message.merchantId())
                            .date(message.date())
                            .totalCredit(BigDecimal.ZERO)
                            .totalDebit(BigDecimal.ZERO)
                            .finalBalance(BigDecimal.ZERO)
                            .build();
                });

        // Aplica o crédito ou débito
        if ("CREDIT".equalsIgnoreCase(message.type())) {
            balance.setTotalCredit(balance.getTotalCredit().add(message.amount()));
            balance.setFinalBalance(balance.getFinalBalance().add(message.amount()));
        } else if ("DEBIT".equalsIgnoreCase(message.type())) {
            balance.setTotalDebit(balance.getTotalDebit().add(message.amount()));
            balance.setFinalBalance(balance.getFinalBalance().subtract(message.amount()));
        } else {
            log.warn("Tipo de transação desconhecido recebido: {}", message.type());
            throw new IllegalArgumentException("Tipo de transação inválido: " + message.type());
        }

        // Salva o registro atualizado no banco de dados de leitura/consolidação
        repository.save(balance);
        log.info("Saldo consolidado atualizado. MerchantId: {}, Novo Saldo Final: {}", balance.getMerchantId(), balance.getFinalBalance());
    }

    /**
     * Retorna o saldo consolidado diário de um comerciante em uma determinada data.
     */
    @Transactional(readOnly = true)
    public DailyBalance getDailyBalance(String merchantId, LocalDate date) {
        return repository.findByMerchantIdAndDate(merchantId, date)
                .orElse(DailyBalance.builder()
                        .merchantId(merchantId)
                        .date(date)
                        .totalCredit(BigDecimal.ZERO)
                        .totalDebit(BigDecimal.ZERO)
                        .finalBalance(BigDecimal.ZERO)
                        .build());
    }
}