package com.fernando.iop.message.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.queue}")
    private String queueEmail;
    @Value("${app.rabbitmq.routing-key}")
    private String routEmail;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(this.exchangeName);
    }

    @Bean
    public Queue queueEmailMethod() {
        return new Queue(this.queueEmail, true);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(queueEmailMethod()).to(exchange()).with(routEmail);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }


}
