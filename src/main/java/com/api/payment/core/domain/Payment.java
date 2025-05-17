package com.api.payment.core.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
    String id,
    String orderId,
    BigDecimal amount,
    String cardNumber,
    String paymentMethod,
    String status,
    LocalDateTime createdAt) {

  public static Payment create(
      String id, String orderId, BigDecimal amount, String cardNumber, String paymentMethod) {
    return new Payment(
        id, orderId, amount, cardNumber, paymentMethod, "PENDING", LocalDateTime.now());
  }

  public Payment approve() {
    return new Payment(id, orderId, amount, cardNumber, paymentMethod, "APPROVED", createdAt);
  }

  public Payment reject() {
    return new Payment(id, orderId, amount, cardNumber, paymentMethod, "REJECTED", createdAt);
  }

  public Payment refund() {
    return new Payment(id, orderId, amount, cardNumber, paymentMethod, "REFUNDED", createdAt);
  }
}
