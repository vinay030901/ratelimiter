package com.vinay.ratelimiter.worker.service.provider;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;

@Service
@RequiredArgsConstructor
@Log4j2
public class
EmailProvider implements NotificationProvider{

    @Value("${aws.region:ap-south-1}")
    private String awsRegion;

    @Override
    public void send(NotificationRequest request) {
        log.info("Sending email to {}", request.destination());
        try (SesClient sesClient = SesClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build()) {
                
            // create the destination
            Destination destination = Destination.builder()
                    .toAddresses(request.destination())
                    .build();
            
            // create email content
            Content subject = Content.builder()
                    .data(request.subject())
                    .charset("UTF-8")
                    .build();
            
            Content bodyContent = Content.builder()
                    .data(request.message())
                    .charset("UTF-8")
                    .build();
            
            Body body = Body.builder()
                    .text(bodyContent)
                    .build();
            
            // build the message
            Message message = Message.builder()
                    .subject(subject)
                    .body(body)
                    .build();
            
            // send the email
            sesClient.sendEmail(builder -> builder
                    .source("recruitvinaykumar2023@gmail.com")
                    .destination(destination)
                    .message(message));
        } catch (Exception e) {
            log.error("Failed to send email to {}", request.destination(), e);
        }
    }

    @Override
    public String getChannel() {
        log.info("Getting channel");
        return "EMAIL";
    }
}
