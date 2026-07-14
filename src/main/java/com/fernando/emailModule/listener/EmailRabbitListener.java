package com.fernando.emailModule.listener;

import com.fernando.emailModule.service.EmailService;
import com.fernando.iop.user.dto.EmailEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailRabbitListener {

    private final EmailService emailService;

    public EmailRabbitListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "fila-email")
    public void processarFila(EmailEventDTO evento) {
        emailService.emailSender(evento.email(), evento.token(), evento.tipoEventoEnum().name());
    }


}
