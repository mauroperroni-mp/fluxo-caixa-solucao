package com.fluxocaixa.consolidation.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${mq.queues.transaction-consolidation}")
    private String queueName;

    /**
     * Declara a fila durável no RabbitMQ.
     * Garante que a fila seja criada caso o consolidation-service suba antes do transaction-service.
     */
    @Bean
    public Queue consolidationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    /**
     * Define o conversor de mensagens para JSON.
     * Permite que o @RabbitListener no ConsolidationService deserialize o payload JSON 
     * recebido na fila diretamente para o objeto TransactionMessageDTO.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
