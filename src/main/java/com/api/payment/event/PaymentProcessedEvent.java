package com.api.payment.event;

public record PaymentProcessedEvent(String orderId, boolean success) {}
