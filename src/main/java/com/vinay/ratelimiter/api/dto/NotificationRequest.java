package com.vinay.ratelimiter.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @JsonProperty("requestId") String requestId,
        @JsonProperty("userId") @NotBlank String userId,
        @JsonProperty("channel") @NotBlank String channel,
        @JsonProperty("destination") @NotBlank String destination,
        @JsonProperty("message") @NotBlank String message
) {
}

// this is a record
// it is immutable and provide private final to the fields
// the compiler auto-generates:
//private final fields
//constructor
//getters (requestId(), userId(), etc.)
//equals, hashCode, toString
