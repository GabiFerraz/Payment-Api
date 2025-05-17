package com.api.payment.infra.gateway;

import com.api.payment.core.domain.Payment;
import com.api.payment.core.gateway.PaymentGateway;
import com.api.payment.infra.gateway.exception.GatewayException;
import com.api.payment.infra.persistence.entity.PaymentEntity;
import com.api.payment.infra.persistence.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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

      return this.toResponse(saved);
    } catch (IllegalArgumentException e) {
      throw new GatewayException("No payment found for orderId: " + payment.orderId());
    }
  }

  private Payment toResponse(final PaymentEntity entity) {
    return new Payment(
        String.valueOf(entity.getId()),
        entity.getOrderId(),
        entity.getAmount(),
        entity.getCardNumber(),
        entity.getPaymentMethod(),
        entity.getStatus(),
        entity.getCreatedAt());
  }
}
