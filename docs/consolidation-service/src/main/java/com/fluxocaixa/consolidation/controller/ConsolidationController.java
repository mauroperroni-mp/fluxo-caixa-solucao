package com.fluxocaixa.consolidation.controller;

import com.fluxocaixa.consolidation.model.DailyBalance;
import com.fluxocaixa.consolidation.service.ConsolidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/consolidation")
@RequiredArgsConstructor
public class ConsolidationController {

    private final ConsolidationService consolidationService;

    /**
     * Endpoint para consultar o saldo diário consolidado de um determinado comerciante em uma data.
     * Exemplo de uso: GET /api/v1/consolidation/daily?merchantId=loja-123&date=2026-08-04
     */
    @GetMapping("/daily")
    public ResponseEntity<DailyBalance> getDailyBalance(
            @RequestParam String merchantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        // Se a data não for informada na requisição, assume a data de hoje
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        
        DailyBalance balance = consolidationService.getDailyBalance(merchantId, targetDate);
        return ResponseEntity.ok(balance);
    }
}