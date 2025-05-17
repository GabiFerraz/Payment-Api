package com.api.payment.core.usecase;

import com.api.payment.core.domain.Payment;
import com.api.payment.core.gateway.EventPublisher;
import com.api.payment.core.gateway.PaymentGateway;
import com.api.payment.event.PaymentProcessedEvent;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessPayment {

  private final PaymentGateway paymentGateway;
  private final EventPublisher eventPublisher;

  public Payment execute(
      final String orderId,
      BigDecimal amount,
      final String cardNumber,
      final String paymentMethod) {

    final var payment =
        Payment.create(UUID.randomUUID().toString(), orderId, amount, cardNumber, paymentMethod);
    final var processedPayment = this.paymentGateway.processPayment(payment);

    this.paymentGateway.save(processedPayment);
    this.eventPublisher.publish(
        new PaymentProcessedEvent(orderId, "APPROVED".equals(processedPayment.status())));

    return processedPayment;
  }
}
