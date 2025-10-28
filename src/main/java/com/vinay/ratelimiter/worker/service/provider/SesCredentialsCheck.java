package com.vinay.ratelimiter.worker.service.provider;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.GetSendQuotaResponse;

public class SesCredentialsCheck {
    public static void main(String[] args) {
        try(SesClient sesClient=SesClient.builder()
                .region(Region.AP_SOUTH_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build()){
            GetSendQuotaResponse quota=sesClient.getSendQuota();
            System.out.println("Max send rate: " + quota.maxSendRate());
            System.out.println("Max 24 hour quota: " + quota.max24HourSend());
            System.out.println("Sent in last 24h: " + quota.sentLast24Hours());
        } catch (Exception e){
            System.err.println("Failed to get SES quota: " + e.getMessage());
        }
    }
}
