// Controller
package com.fluxocaixa.transaction.controller;

import com.fluxocaixa.transaction.dto.TransactionDTO;
import com.fluxocaixa.transaction.model.Transaction;
import com.fluxocaixa.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody @Valid TransactionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTransaction(dto));
    }
}