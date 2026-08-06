package com.fluxocaixa.consolidation.controller;

import com.fluxocaixa.consolidation.dto.DailyBalanceDTO;
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

    @GetMapping("/daily")
    public ResponseEntity<DailyBalanceDTO> getDailyBalance(
            @RequestParam String merchantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        DailyBalanceDTO dailyBalance = consolidationService.getDailyBalance(merchantId, date);
        return ResponseEntity.ok(dailyBalance);
    }
}
