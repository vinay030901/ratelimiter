package com.vinay.ratelimiter.worker.service.provider;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.vinay.ratelimiter.api.dto.NotificationRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SmsProvider implements NotificationProvider {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.message-service-sid}")
    private String messagingServiceSid;

    // Initialize Twilio once
    private boolean initialized = false;

    @PostConstruct
    private synchronized void initTwilio() {
        log.info("Initializing Twilio");
        if (!initialized) {
            Twilio.init(accountSid, authToken);
            initialized = true;
        }
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("Sending SMS");
        // Twilio API call here
        String to = request.destination();
        String body = request.message();
        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    messagingServiceSid,
                    body
            ).create();
            log.info("SMS sent successfully with SID: {}", message.getSid());
        } catch (Exception e) {
            // Log error or rethrow as needed
            log.error("Failed to send SMS", e);
        }
    }

    @Override
    public String getChannel() {
        log.info("Getting channel");
        return "SMS";
    }
}
