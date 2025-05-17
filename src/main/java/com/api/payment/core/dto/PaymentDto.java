package com.api.payment.core.dto;

import java.math.BigDecimal;

public record PaymentDto(
    String orderId, BigDecimal amount, String cardNumber, String paymentMethod) {}
