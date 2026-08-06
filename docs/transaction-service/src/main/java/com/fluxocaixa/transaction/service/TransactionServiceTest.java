package com.fluxocaixa.transaction.service;

import com.fluxocaixa.transaction.dto.TransactionDTO;
import com.fluxocaixa.transaction.model.Transaction;
import com.fluxocaixa.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TransactionService service;

    private TransactionDTO dto;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        dto = new TransactionDTO("m123", new BigDecimal("100.00"), "CREDIT", "Venda de produto", LocalDate.now());
        
        transaction = Transaction.builder()
                .id(1L)
                .merchantId("m123")
                .amount(new BigDecimal("100.00"))
                .type("CREDIT")
                .description("Venda de produto")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar transação com sucesso e publicar mensagem no RabbitMQ")
    void createTransaction_Success() {
        when(repository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction created = service.createTransaction(dto);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("m123", created.getMerchantId());
        assertEquals(new BigDecimal("100.00"), created.getAmount());

        verify(repository, times(1)).save(any(Transaction.class));
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any());
    }
}
