package com.fluxocaixa.transaction.service;

import com.fluxocaixa.transaction.dto.TransactionDTO;
import com.fluxocaixa.transaction.dto.TransactionMessageDTO;
import com.fluxocaixa.transaction.model.Transaction;
import com.fluxocaixa.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${mq.exchanges.transaction}")
    private String exchange;

    @Value("${mq.routing-keys.transaction-created}")
    private String routingKey;

    /**
     * Registra um novo lançamento (crédito ou débito) e envia o evento assincronamente para a fila.
     */
    @Transactional
    public Transaction createTransaction(TransactionDTO dto) {
        Transaction tx = Transaction.builder()
                .merchantId(dto.merchantId())
                .type(dto.type() != null ? dto.type().toUpperCase() : null)
                .amount(dto.amount())
                .description(dto.description())
                .date(dto.date() != null ? dto.date() : LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        // 1. Persiste a transação no banco de dados do controle de lançamentos
        tx = repository.save(tx);
        log.info("Lançamento salvo com sucesso no banco de dados. ID: {}", tx.getId());

        // 2. Prepara o DTO de mensagem (evento) para a fila
        TransactionMessageDTO eventPayload = new TransactionMessageDTO(
                tx.getId(),
                tx.getMerchantId(),
                tx.getAmount(),
                tx.getType(),
                tx.getCreatedAt()
        );

        // 3. Publica o evento na fila (RabbitMQ)
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, eventPayload);
            log.info("Evento TransactionCreated enviado para o broker para o merchant: {}", tx.getMerchantId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento na fila RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao notificar serviço de mensageria", e); // Força rollback se o envio falhar
        }

        return tx;
    }

    /**
     * Lista todas as transações de um comerciante.
     */
    @Transactional(readOnly = true)
    public List<Transaction> findByMerchantId(String merchantId) {
        return repository.findByMerchantId(merchantId);
    }

    /**
     * Lista transações de um comerciante por data específica.
     */
    @Transactional(readOnly = true)
    public List<Transaction> findByMerchantIdAndDate(String merchantId, LocalDate date) {
        return repository.findByMerchantIdAndDate(merchantId, date);
    }
}
