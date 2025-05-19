package com.api.payment.infra.gateway;

import com.api.payment.core.domain.Payment;
import com.api.payment.core.gateway.PaymentGateway;
import com.api.payment.infra.gateway.exception.GatewayException;
import com.api.payment.infra.persistence.entity.PaymentEntity;
import com.api.payment.infra.persistence.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayMockImpl implements PaymentGateway {

  private final PaymentRepository paymentRepository;

  @Override
  public Payment processPayment(final Payment payment) {
    if (payment.amount().compareTo(new BigDecimal("1000")) < 0) {
      return payment.approve();
    } else {
      return payment.reject();
    }
  }

  @Override
  public Optional<Payment> findByOrderId(final String orderId) {
    try {
      final var entity = paymentRepository.findByOrderId(orderId);

      return entity.map(this::toResponse);
    } catch (IllegalArgumentException e) {
      throw new GatewayException("No payment found for orderId: " + orderId);
    }
  }

  @Override
  public Payment save(final Payment payment) {
    try {
      log.info("Saving payment for orderId: {}, status: {}", payment.orderId(), payment.status());
      final var entity =
          PaymentEntity.builder()
              .orderId(payment.orderId())
              .amount(payment.amount())
              .cardNumber(payment.cardNumber())
              .paymentMethod(payment.paymentMethod())
              .status(payment.status())
              .createdAt(payment.createdAt())
              .build();

      final var saved = this.paymentRepository.save(entity);
      final var response = this.toResponse(saved);

      log.info("Saved payment for orderId: {}, status: {}", payment.orderId(), payment.status());

      return response;
    } catch (IllegalArgumentException e) {
      log.error("Error saving payment for orderId: {}", payment.orderId(), e);
      throw new GatewayException("Failed to save payment for orderId: " + payment.orderId());
    }
  }

  private Payment toResponse(final PaymentEntity entity) {
    return new Payment(
        entity.getId().toString(),
        entity.getOrderId(),
        entity.getAmount(),
        entity.getCardNumber(),
        entity.getPaymentMethod(),
        entity.getStatus(),
        entity.getCreatedAt());
  }
}
