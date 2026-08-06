package com.fluxocaixa.consolidation.service;

import com.fluxocaixa.consolidation.dto.DailyBalanceDTO;
import com.fluxocaixa.consolidation.dto.TransactionMessageDTO;
import com.fluxocaixa.consolidation.model.DailyBalance;
import com.fluxocaixa.consolidation.repository.DailyBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsolidationServiceTest {

    @Mock
    private DailyBalanceRepository repository;

    @InjectMocks
    private ConsolidationService service;

    private TransactionMessageDTO creditMessage;
    private TransactionMessageDTO debitMessage;

    @BeforeEach
    void setUp() {
        creditMessage = new TransactionMessageDTO(1L, "m123", new BigDecimal("200.00"), "CREDIT", LocalDateTime.now());
        debitMessage = new TransactionMessageDTO(2L, "m123", new BigDecimal("50.00"), "DEBIT", LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve processar evento de CRÉDITO e atualizar o saldo consolidado corretamente")
    void processTransactionEvent_Credit() {
        when(repository.findByMerchantIdAndDate(any(), any())).thenReturn(Optional.empty());

        service.processTransactionEvent(creditMessage);

        verify(repository, times(1)).save(argThat(balance ->
                balance.getMerchantId().equals("m123") &&
                balance.getTotalCredit().compareTo(new BigDecimal("200.00")) == 0 &&
                balance.getFinalBalance().compareTo(new BigDecimal("200.00")) == 0
        ));
    }

    @Test
    @DisplayName("Deve processar evento de DÉBITO deduzindo do saldo existente")
    void processTransactionEvent_Debit() {
        DailyBalance existingBalance = DailyBalance.builder()
                .merchantId("m123")
                .date(LocalDate.now())
                .totalCredit(new BigDecimal("200.00"))
                .totalDebit(BigDecimal.ZERO)
                .finalBalance(new BigDecimal("200.00"))
                .build();

        when(repository.findByMerchantIdAndDate(any(), any())).thenReturn(Optional.of(existingBalance));

        service.processTransactionEvent(debitMessage);

        verify(repository, times(1)).save(argThat(balance ->
                balance.getTotalDebit().compareTo(new BigDecimal("50.00")) == 0 &&
                balance.getFinalBalance().compareTo(new BigDecimal("150.00")) == 0
        ));
    }

    @Test
    @DisplayName("Deve retornar DTO zerado se não houver lançamentos para a data consultada")
    void getDailyBalance_EmptyDate_ShouldReturnZeroBalance() {
        when(repository.findByMerchantIdAndDate(any(), any())).thenReturn(Optional.empty());

        DailyBalanceDTO result = service.getDailyBalance("m999", LocalDate.now());

        assertNotNull(result);
        assertEquals("m999", result.merchantId());
        assertEquals(BigDecimal.ZERO, result.finalBalance());
    }
}
