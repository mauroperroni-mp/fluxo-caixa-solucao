package com.fluxocaixa.consolidation.controller;

import com.fluxocaixa.consolidation.dto.DailyBalanceDTO;
import com.fluxocaixa.consolidation.service.ConsolidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsolidationController.class)
class ConsolidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsolidationService consolidationService;

    @Test
    @DisplayName("Deve consultar o saldo diário e retornar HTTP 200 OK")
    void getDailyBalance_ShouldReturn200() throws Exception {
        LocalDate today = LocalDate.now();
        DailyBalanceDTO responseDto = new DailyBalanceDTO("m123", today, new BigDecimal("300.00"), new BigDecimal("100.00"), new BigDecimal("200.00"));

        when(consolidationService.getDailyBalance(eq("m123"), any())).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/consolidation/daily")
                        .param("merchantId", "m123")
                        .param("date", today.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value("m123"))
                .andExpect(jsonPath("$.totalCredit").value(300.00))
                .andExpect(jsonPath("$.totalDebit").value(100.00))
                .andExpect(jsonPath("$.finalBalance").value(200.00));
    }
}
