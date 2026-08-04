package com.fluxocaixa.consolidation.repository;

import com.fluxocaixa.consolidation.model.DailyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {

    // Busca o saldo consolidado de um comerciante em uma data específica
    Optional<DailyBalance> findByMerchantIdAndDate(String merchantId, LocalDate date);
}