package com.api.payment.presenter.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PaymentPresenterResponse(
    String id,
    String orderId,
    BigDecimal amount,
    String cardNumber,
    String paymentMethod,
    String status,
    LocalDateTime createdAt) {}
