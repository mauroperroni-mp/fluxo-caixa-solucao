package com.fluxocaixa.transaction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${mq.exchanges.transaction}")
    private String exchangeName;

    @Value("${mq.queues.transaction-consolidation}")
    private String queueName;

    @Value("${mq.routing-keys.transaction-created}")
    private String routingKey;

    // 1. Declara a Exchange (Direct ou Topic)
    @Bean
    public DirectExchange transactionExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    // 2. Declara a Fila
    @Bean
    public Queue consolidationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    // 3. Associa (Binding) a Fila à Exchange usando a Routing Key
    @Bean
    public Binding binding(Queue consolidationQueue, DirectExchange transactionExchange) {
        return BindingBuilder
                .bind(consolidationQueue)
                .to(transactionExchange)
                .with(routingKey);
    }

    // 4. Configura o conversor de mensagens para JSON (fundamental para desacoplamento e interoperabilidade)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
