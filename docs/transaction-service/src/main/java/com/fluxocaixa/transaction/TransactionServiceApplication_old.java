// Service
package com.fluxocaixa.transaction.service;

import com.fluxocaixa.transaction.dto.TransactionDTO;
import com.fluxocaixa.transaction.model.Transaction;
import com.fluxocaixa.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${mq.exchanges.transaction}")
    private String exchange;

    @Value("${mq.routing-keys.transaction-created}")
    private String routingKey;

    @Transactional
    public Transaction createTransaction(TransactionDTO dto) {
        Transaction tx = Transaction.builder()
                .merchantId(dto.merchantId())
                .type(dto.type().toUpperCase())
                .amount(dto.amount())
                .description(dto.description())
                .date(dto.date() != null ? dto.date() : LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        tx = repository.save(tx);
        
        // Publica na fila assincronamente
        rabbitTemplate.convertAndSend(exchange, routingKey, tx);

        return tx;
    }
}