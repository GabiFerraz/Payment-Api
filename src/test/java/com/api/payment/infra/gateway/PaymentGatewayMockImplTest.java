package com.api.payment.infra.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.api.payment.core.domain.Payment;
import com.api.payment.infra.gateway.exception.GatewayException;
import com.api.payment.infra.persistence.entity.PaymentEntity;
import com.api.payment.infra.persistence.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PaymentGatewayMockImplTest {

  private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
  private final PaymentGatewayMockImpl paymentGateway =
      new PaymentGatewayMockImpl(paymentRepository);

  @Test
  void shouldApprovePaymentWhenAmountLessThan1000() {
    final var payment =
        new Payment(
            "1",
            "order-123",
            new BigDecimal("100.00"),
            "1234567890123456",
            "CREDIT_CARD",
            "PENDING",
            LocalDateTime.of(2025, 5, 19, 20, 30));

    final var result = paymentGateway.processPayment(payment);

    assertThat(result).usingRecursiveComparison().isEqualTo(payment.approve());
    assertThat(result.status()).isEqualTo("APPROVED");
  }

  @Test
  void shouldRejectPaymentWhenAmountGreaterOrEqualTo1000() {
    final var payment =
        new Payment(
            "1",
            "order-123",
            new BigDecimal("1500.00"),
            "1234567890123456",
            "CREDIT_CARD",
            "PENDING",
            LocalDateTime.of(2025, 5, 19, 20, 30));

    final var result = paymentGateway.processPayment(payment);

    assertThat(result).usingRecursiveComparison().isEqualTo(payment.reject());
    assertThat(result.status()).isEqualTo("REJECTED");
  }

  @Test
  void shouldFindPaymentByOrderIdWhenExists() {
    final var orderId = "order-123";
    final var paymentEntity =
        PaymentEntity.builder()
            .orderId("order-123")
            .amount(new BigDecimal("100.00"))
            .cardNumber("1234567890123456")
            .paymentMethod("CREDIT_CARD")
            .status("PENDING")
            .createdAt(LocalDateTime.of(2025, 5, 19, 20, 30))
            .build();
    paymentEntity.setId(1);
    final var expectedPayment =
        new Payment(
            "1",
            paymentEntity.getOrderId(),
            paymentEntity.getAmount(),
            paymentEntity.getCardNumber(),
            paymentEntity.getPaymentMethod(),
            paymentEntity.getStatus(),
            paymentEntity.getCreatedAt());

    when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentEntity));

    final var result = paymentGateway.findByOrderId(orderId);

    assertThat(result).isPresent();
    assertThat(result.get()).usingRecursiveComparison().isEqualTo(expectedPayment);

    verify(paymentRepository).findByOrderId(orderId);
  }

  @Test
  void shouldReturnEmptyWhenPaymentNotFound() {
    final var orderId = "order-123";

    when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

    final var result = paymentGateway.findByOrderId(orderId);

    assertThat(result).isEmpty();

    verify(paymentRepository).findByOrderId(orderId);
  }

  @Test
  void shouldThrowExceptionWhenOccursErrorFindingPayment() {
    final var orderId = "order-123";

    when(paymentRepository.findByOrderId(orderId))
        .thenThrow(new IllegalArgumentException("Invalid orderId"));

    assertThatThrownBy(() -> paymentGateway.findByOrderId(orderId))
        .isInstanceOf(GatewayException.class)
        .hasMessage("No payment found for orderId: " + orderId);

    verify(paymentRepository).findByOrderId(orderId);
  }

  @Test
  void shouldThrowExceptionWhenOccursErrorSavingPayment() {
    final var entity =
        PaymentEntity.builder()
            .orderId("order-123")
            .amount(new BigDecimal("100.00"))
            .cardNumber("1234567890123456")
            .paymentMethod("CREDIT_CARD")
            .status("PENDING")
            .createdAt(LocalDateTime.of(2025, 5, 19, 20, 30))
            .build();
    final var payment =
        new Payment(
            "1",
            "order-123",
            new BigDecimal("1500.00"),
            "1234567890123456",
            "CREDIT_CARD",
            "PENDING",
            LocalDateTime.of(2025, 5, 19, 20, 30));

    doReturn(Optional.of(entity)).when(paymentRepository).findByOrderId(entity.getOrderId());
    when(paymentRepository.save(any())).thenThrow(IllegalArgumentException.class);

    assertThatThrownBy(() -> paymentGateway.save(payment))
        .isInstanceOf(GatewayException.class)
        .hasMessage("Failed to save payment for orderId: " + entity.getOrderId());

    verify(paymentRepository).findByOrderId(entity.getOrderId());
    verify(paymentRepository).save(any());
  }
}
