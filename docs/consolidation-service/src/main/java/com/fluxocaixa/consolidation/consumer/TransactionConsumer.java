package com.fluxocaixa.consolidation.consumer;

import com.fluxocaixa.consolidation.dto.TransactionMessageDTO;
import com.fluxocaixa.consolidation.service.ConsolidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final ConsolidationService consolidationService;

    /**
     * Consome os eventos de transação criada vindos da fila do RabbitMQ.
     * Em caso de falha no processamento, a exceção lançada fará o RabbitMQ 
     * redirecionar a mensagem para a DLQ (Dead Letter Queue) configurada.
     */
    @RabbitListener(queues = "${mq.queues.transaction-created}")
    public void consumeTransactionCreatedEvent(TransactionMessageDTO message) {
        log.info("Evento TransactionCreated recebido da fila para o merchant: {}, tipo: {}, valor: {}", 
                message.merchantId(), message.type(), message.amount());

        try {
            consolidationService.processTransaction(message);
            log.info("Transação ID: {} processada com sucesso no consolidado diário.", message.id());
        } catch (Exception e) {
            log.error("Erro ao processar mensagem no consolidado. ID: {}, erro: {}", message.id(), e.getMessage(), e);
            throw e; // Lança a exceção para acionar a retry policy / DLQ do RabbitMQ
        }
    }
}