package com.fernando.iop.message.service;

import com.fernando.iop.user.dto.EmailEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routEmail;


    public RabbitService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void dispararEvento(EmailEventDTO emailEventDTO) {
        rabbitTemplate.convertAndSend(exchangeName, routEmail, emailEventDTO);
    }
}
