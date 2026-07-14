package com.fernando.emailModule.listener;

import com.fernando.emailModule.service.EmailService;
import com.fernando.iop.user.dto.EmailEventDTO;
import org.apache.tomcat.util.threads.VirtualThreadExecutor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class EmailRabbitListener {

    private final EmailService emailService;

    public EmailRabbitListener(EmailService emailService) {
        this.emailService = emailService;
    }


    @RabbitListener(queues = "fila-email", concurrency = "5")
    public void processarFila(EmailEventDTO evento) {
        emailService.emailSender(evento.email(), evento.token(), evento.tipoEventoEnum().name());
    }


}
