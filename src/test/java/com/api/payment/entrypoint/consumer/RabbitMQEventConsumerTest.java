package com.api.payment.entrypoint.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.api.payment.core.domain.Payment;
import com.api.payment.core.gateway.PaymentGateway;
import com.api.payment.core.usecase.ProcessPayment;
import com.api.payment.event.ProcessPaymentEvent;
import com.api.payment.event.RefundPaymentEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RabbitMQEventConsumerTest {

  private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 5, 19, 20, 30);

  private final ProcessPayment processPayment = mock(ProcessPayment.class);
  private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
  private final RabbitMQEventConsumer eventConsumer =
      new RabbitMQEventConsumer(processPayment, paymentGateway);

  @Test
  void shouldProcessPaymentEventSuccessfully() {
    final var event =
        new ProcessPaymentEvent(
            "order-123", new BigDecimal("100.00"), "1234567890123456", "CREDIT_CARD");
    final var payment =
        new Payment(
            "1",
            "order-123",
            new BigDecimal("100.00"),
            "1234567890123456",
            "CREDIT_CARD",
            "APPROVED",
            FIXED_TIME);

    when(processPayment.execute(
            any(String.class), any(BigDecimal.class), any(String.class), any(String.class)))
        .thenReturn(payment);

    eventConsumer.consumeProcessPaymentEvent(event);

    final ArgumentCaptor<String> orderIdCaptor = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
    final ArgumentCaptor<String> cardNumberCaptor = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> paymentMethodCaptor = ArgumentCaptor.forClass(String.class);

    verify(processPayment)
        .execute(
            orderIdCaptor.capture(),
            amountCaptor.capture(),
            cardNumberCaptor.capture(),
            paymentMethodCaptor.capture());

    assertThat(orderIdCaptor.getValue()).isEqualTo(event.orderId());
    assertThat(amountCaptor.getValue()).isEqualTo(event.amount());
    assertThat(cardNumberCaptor.getValue()).isEqualTo(event.cardNumber());
    assertThat(paymentMethodCaptor.getValue()).isEqualTo(event.paymentMethod());
  }

  @Test
  void shouldHandleExceptionInProcessPaymentEvent() {
    final var event =
        new ProcessPaymentEvent(
            "order-123", new BigDecimal("100.00"), "1234567890123456", "CREDIT_CARD");

    when(processPayment.execute(
            any(String.class), any(BigDecimal.class), any(String.class), any(String.class)))
        .thenThrow(new RuntimeException("Processing failed"));

    eventConsumer.consumeProcessPaymentEvent(event);

    verify(processPayment)
        .execute(
            eq(event.orderId()),
            eq(event.amount()),
            eq(event.cardNumber()),
            eq(event.paymentMethod()));

    verifyNoMoreInteractions(processPayment, paymentGateway);
  }

  @Test
  void shouldRefundPaymentWhenStatusIsApproved() {
    final var event = new RefundPaymentEvent("order-123", new BigDecimal("100.00"));
    final var payment =
        new Payment(
            "1",
            "order-123",
            new BigDecimal("100.00"),
            "1234567890123456",
            "CREDIT_CARD",
            "APPROVED",
            FIXED_TIME);
    final var refundedPayment = payment.refund();

    when(paymentGateway.findByOrderId(event.orderId())).thenReturn(Optional.of(payment));
    when(paymentGateway.save(any(Payment.class))).thenReturn(refundedPayment);

    eventConsumer.consumeRefundPaymentEvent(event);

    final ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentGateway).findByOrderId(event.orderId());
    verify(paymentGateway).save(paymentCaptor.capture());

    assertThat(paymentCaptor.getValue()).usingRecursiveComparison().isEqualTo(refundedPayment);
    assertThat(paymentCaptor.getValue().status()).isEqualTo("REFUNDED");
  }

  @Test
  void shouldNotRefundPaymentWhenStatusIsNotApproved() {
    final var event = new RefundPaymentEvent("order-123", new BigDecimal("100.00"));
    final var payment =
        new Payment(
            "1",
            "order-123",
            new BigDecimal("100.00"),
            "1234567890123456",
            "CREDIT_CARD",
            "REJECTED",
            FIXED_TIME);

    when(paymentGateway.findByOrderId(event.orderId())).thenReturn(Optional.of(payment));

    eventConsumer.consumeRefundPaymentEvent(event);

    verify(paymentGateway).findByOrderId(event.orderId());
  }

  @Test
  void shouldNotRefundPaymentWhenPaymentNotFound() {
    final var event = new RefundPaymentEvent("order-123", new BigDecimal("100.00"));

    when(paymentGateway.findByOrderId(event.orderId())).thenReturn(Optional.empty());

    eventConsumer.consumeRefundPaymentEvent(event);

    verify(paymentGateway).findByOrderId(event.orderId());
  }

  @Test
  void shouldHandleExceptionInRefundPaymentEvent() {
    final var event = new RefundPaymentEvent("order-123", new BigDecimal("100.00"));

    when(paymentGateway.findByOrderId(event.orderId()))
        .thenThrow(new RuntimeException("Database error"));

    eventConsumer.consumeRefundPaymentEvent(event);

    verify(paymentGateway).findByOrderId(event.orderId());
  }
}
