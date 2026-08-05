// Controller
package com.fluxocaixa.transaction.controller;

import com.fluxocaixa.transaction.dto.TransactionDTO;
import com.fluxocaixa.transaction.model.Transaction;
import com.fluxocaixa.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Endpoint para criar um novo lançamento (Crédito ou Débito).
     * Retorna HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        Transaction createdTransaction = transactionService.createTransaction(transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    /**
     * Endpoint para buscar lançamentos de um comerciante.
     * Permite filtrar opcionalmente por data.
     * Exemplo: GET /api/v1/transactions/merchant123?date=2026-08-05
     */
    @GetMapping("/{merchantId}")
    public ResponseEntity<List<Transaction>> getTransactionsByMerchant(
            @PathVariable String merchantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Transaction> transactions;

        if (date != null) {
            transactions = transactionService.findByMerchantIdAndDate(merchantId, date);
        } else {
            transactions = transactionService.findByMerchantId(merchantId);
        }

        return ResponseEntity.ok(transactions);
    }
}
