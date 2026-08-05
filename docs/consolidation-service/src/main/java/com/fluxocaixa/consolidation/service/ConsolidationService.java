package com.fluxocaixa.consolidation.service;

import com.fluxocaixa.consolidation.dto.DailyBalanceDTO;
import com.fluxocaixa.consolidation.dto.TransactionMessageDTO;
import com.fluxocaixa.consolidation.model.DailyBalance;
import com.fluxocaixa.consolidation.repository.DailyBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsolidationService {

    private final DailyBalanceRepository dailyBalanceRepository;

    /**
     * Listener que consome os eventos 'transaction-created' publicados pelo transaction-service.
     * Atualiza o saldo consolidado do comerciante para a data da transação.
     */
    @RabbitListener(queues = "${mq.queues.transaction-consolidation}")
    @Transactional
    public void processTransactionEvent(TransactionMessageDTO eventPayload) {
        log.info("Processando evento de transação ID: {} para o merchant: {}", 
                eventPayload.transactionId(), eventPayload.merchantId());

        LocalDate transactionDate = eventPayload.timestamp() != null ? 
                eventPayload.timestamp().toLocalDate() : LocalDate.now();

        // Busca ou cria o registro de saldo do dia para o merchant
        DailyBalance balance = dailyBalanceRepository
                .findByMerchantIdAndDate(eventPayload.merchantId(), transactionDate)
                .orElseGet(() -> DailyBalance.builder()
                        .merchantId(eventPayload.merchantId())
                        .date(transactionDate)
                        .totalCredit(BigDecimal.ZERO)
                        .totalDebit(BigDecimal.ZERO)
                        .finalBalance(BigDecimal.ZERO)
                        .build());

        // Atualiza os totais de acordo com o tipo do lançamento (CREDIT / DEBIT)
        if ("CREDIT".equalsIgnoreCase(eventPayload.type())) {
            balance.setTotalCredit(balance.getTotalCredit().add(eventPayload.amount()));
        } else if ("DEBIT".equalsIgnoreCase(eventPayload.type())) {
            balance.setTotalDebit(balance.getTotalDebit().add(eventPayload.amount()));
        } else {
            log.warn("Tipo de transação desconhecido recebido: {}", eventPayload.type());
            return;
        }

        // Recalcula o saldo final (Créditos - Débitos)
        balance.setFinalBalance(balance.getTotalCredit().subtract(balance.getTotalDebit()));

        dailyBalanceRepository.save(balance);
        log.info("Saldo consolidado atualizado com sucesso. Merchant: {}, Data: {}, Saldo Final: {}", 
                balance.getMerchantId(), balance.getDate(), balance.getFinalBalance());
    }

    /**
     * Retorna a consolidação do saldo diário de um comerciante para uma data específica.
     */
    @Transactional(readOnly = true)
    public DailyBalanceDTO getDailyBalance(String merchantId, LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();

        DailyBalance balance = dailyBalanceRepository
                .findByMerchantIdAndDate(merchantId, searchDate)
                .orElseGet(() -> DailyBalance.builder()
                        .merchantId(merchantId)
                        .date(searchDate)
                        .totalCredit(BigDecimal.ZERO)
                        .totalDebit(BigDecimal.ZERO)
                        .finalBalance(BigDecimal.ZERO)
                        .build());

        return new DailyBalanceDTO(
                balance.getMerchantId(),
                balance.getDate(),
                balance.getTotalCredit(),
                balance.getTotalDebit(),
                balance.getFinalBalance()
        );
    }
}
