package com.vinay.ratelimiter.worker.service.provider;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailProvider implements NotificationProvider{

    private final JavaMailSender javaMailSender;

    @Override
    public void send(NotificationRequest request) {
        log.info("Sending email");
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        String CONSTANT_EMAIL_SOURCE = "recruitvinaykumar2023@gmail.com";
        mailMessage.setFrom(CONSTANT_EMAIL_SOURCE);
        mailMessage.setTo(request.destination());
        mailMessage.setSubject(request.subject());
        mailMessage.setText(request.message());
        javaMailSender.send(mailMessage);
    }

    @Override
    public String getChannel() {
        log.info("Getting channel");
        return "EMAIL";
    }
}
