package com.api.payment.event;

public record PaymentProcessedEvent(int orderId, boolean success) {}
