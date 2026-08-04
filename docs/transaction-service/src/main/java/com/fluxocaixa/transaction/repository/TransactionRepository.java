package com.fluxocaixa.transaction.repository;

import com.fluxocaixa.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // Busca todas as transações de um determinado comerciante
    List<Transaction> findByMerchantId(String merchantId);

    // Busca transações de um comerciante por data específica
    List<Transaction> findByMerchantIdAndDate(String merchantId, LocalDate date);
}