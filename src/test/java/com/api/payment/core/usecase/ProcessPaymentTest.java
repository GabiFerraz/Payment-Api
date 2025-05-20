package com.api.payment.core.usecase;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.api.payment.core.domain.Payment;
import com.api.payment.core.gateway.EventPublisher;
import com.api.payment.core.gateway.PaymentGateway;
import com.api.payment.event.PaymentProcessedEvent;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProcessPaymentTest {

  private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
  private final EventPublisher eventPublisher = mock(EventPublisher.class);
  private final ProcessPayment processPayment = new ProcessPayment(paymentGateway, eventPublisher);

  @Test
  void shouldProcessPaymentSuccessfullyWhenApproved() {
    final var payment =
        Payment.create(
            randomUUID().toString(),
            "order-123",
            new BigDecimal("100.00"),
            "1234567890123456",
            "CREDIT_CARD");
    final var processedPayment = payment.approve();

    when(paymentGateway.processPayment(any())).thenReturn(processedPayment);
    when(paymentGateway.save(any())).thenReturn(processedPayment);
    doNothing().when(eventPublisher).publish(any());

    final var response =
        processPayment.execute(
            payment.orderId(), payment.amount(), payment.cardNumber(), payment.paymentMethod());

    assertThat(response).usingRecursiveComparison().isEqualTo(processedPayment);

    final ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentGateway).processPayment(paymentCaptor.capture());

    final var capturedPayment = paymentCaptor.getAllValues().get(0);
    assertThat(capturedPayment.orderId()).isEqualTo(payment.orderId());
    assertThat(capturedPayment.amount()).isEqualTo(payment.amount());
    assertThat(capturedPayment.cardNumber()).isEqualTo(payment.cardNumber());
    assertThat(capturedPayment.paymentMethod()).isEqualTo(payment.paymentMethod());
    assertThat(capturedPayment.status()).isEqualTo("PENDING");

    final ArgumentCaptor<Payment> savePaymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentGateway).save(savePaymentCaptor.capture());

    final var capturedSavePayment = savePaymentCaptor.getAllValues().get(0);
    assertThat(capturedSavePayment.orderId()).isEqualTo(payment.orderId());
    assertThat(capturedSavePayment.amount()).isEqualTo(payment.amount());
    assertThat(capturedSavePayment.cardNumber()).isEqualTo(payment.cardNumber());
    assertThat(capturedSavePayment.paymentMethod()).isEqualTo(payment.paymentMethod());
    assertThat(capturedSavePayment.status()).isEqualTo("APPROVED");

    final ArgumentCaptor<PaymentProcessedEvent> eventCaptor =
        ArgumentCaptor.forClass(PaymentProcessedEvent.class);
    verify(eventPublisher).publish(eventCaptor.capture());

    final var capturedEvent = eventCaptor.getValue();
    assertThat(capturedEvent.orderId()).isEqualTo(payment.orderId());
    assertThat(capturedEvent.success()).isTrue();
  }
}
