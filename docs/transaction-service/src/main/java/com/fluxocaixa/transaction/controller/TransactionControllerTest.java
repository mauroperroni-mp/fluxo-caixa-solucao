package com.fluxocaixa.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxocaixa.transaction.dto.TransactionDTO;
import com.fluxocaixa.transaction.model.Transaction;
import com.fluxocaixa.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService service;

    @Test
    @DisplayName("Deve retornar 201 Created ao enviar uma transação válida")
    void createTransaction_ShouldReturn201() throws Exception {
        TransactionDTO dto = new TransactionDTO("m123", new BigDecimal("150.50"), "CREDIT", "Venda teste", LocalDate.now());
        
        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .merchantId("m123")
                .amount(new BigDecimal("150.50"))
                .type("CREDIT")
                .createdAt(LocalDateTime.now())
                .build();

        when(service.createTransaction(any(TransactionDTO.class))).thenReturn(savedTransaction);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.merchantId").value("m123"))
                .andExpect(jsonPath("$.amount").value(150.50));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request se os campos obrigatórios forem inválidos")
    void createTransaction_ShouldReturn400_WhenInvalid() throws Exception {
        TransactionDTO invalidDto = new TransactionDTO("", new BigDecimal("-10.00"), "", null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
