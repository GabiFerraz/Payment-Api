package com.api.payment.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentTest {

  private static final String ID = "1";
  private static final String ORDER_ID = "order-123";
  private static final BigDecimal AMOUNT = new BigDecimal("100.00");
  private static final String CARD_NUMBER = "1234567890123456";
  private static final String PAYMENT_METHOD = "CREDIT_CARD";

  @Test
  void shouldCreatePaymentSuccessfully() {
    final var payment = Payment.create(ID, ORDER_ID, AMOUNT, CARD_NUMBER, PAYMENT_METHOD);

    assertThat(payment.id()).isEqualTo(ID);
    assertThat(payment.orderId()).isEqualTo(ORDER_ID);
    assertThat(payment.amount()).isEqualTo(AMOUNT);
    assertThat(payment.cardNumber()).isEqualTo(CARD_NUMBER);
    assertThat(payment.paymentMethod()).isEqualTo(PAYMENT_METHOD);
    assertThat(payment.status()).isEqualTo("PENDING");
  }

  @Test
  void shouldApprovePaymentSuccessfully() {
    final var payment = Payment.create(ID, ORDER_ID, AMOUNT, CARD_NUMBER, PAYMENT_METHOD);

    final var approvedPayment = payment.approve();

    assertThat(approvedPayment.id()).isEqualTo(ID);
    assertThat(approvedPayment.orderId()).isEqualTo(ORDER_ID);
    assertThat(approvedPayment.amount()).isEqualTo(AMOUNT);
    assertThat(approvedPayment.cardNumber()).isEqualTo(CARD_NUMBER);
    assertThat(approvedPayment.paymentMethod()).isEqualTo(PAYMENT_METHOD);
    assertThat(approvedPayment.status()).isEqualTo("APPROVED");
    assertThat(approvedPayment).isNotSameAs(payment);
  }

  @Test
  void shouldRejectPaymentSuccessfully() {
    final var payment = Payment.create(ID, ORDER_ID, AMOUNT, CARD_NUMBER, PAYMENT_METHOD);

    final var rejectedPayment = payment.reject();

    assertThat(rejectedPayment.id()).isEqualTo(ID);
    assertThat(rejectedPayment.orderId()).isEqualTo(ORDER_ID);
    assertThat(rejectedPayment.amount()).isEqualTo(AMOUNT);
    assertThat(rejectedPayment.cardNumber()).isEqualTo(CARD_NUMBER);
    assertThat(rejectedPayment.paymentMethod()).isEqualTo(PAYMENT_METHOD);
    assertThat(rejectedPayment.status()).isEqualTo("REJECTED");
    assertThat(rejectedPayment).isNotSameAs(payment);
  }

  @Test
  void shouldRefundPaymentSuccessfully() {
    final var payment = Payment.create(ID, ORDER_ID, AMOUNT, CARD_NUMBER, PAYMENT_METHOD);

    final var refundedPayment = payment.refund();

    assertThat(refundedPayment.id()).isEqualTo(ID);
    assertThat(refundedPayment.orderId()).isEqualTo(ORDER_ID);
    assertThat(refundedPayment.amount()).isEqualTo(AMOUNT);
    assertThat(refundedPayment.cardNumber()).isEqualTo(CARD_NUMBER);
    assertThat(refundedPayment.paymentMethod()).isEqualTo(PAYMENT_METHOD);
    assertThat(refundedPayment.status()).isEqualTo("REFUNDED");
    assertThat(refundedPayment).isNotSameAs(payment);
  }

  @Test
  void shouldMaintainConsistencyInMultipleStateTransitions() {
    final var payment = Payment.create(ID, ORDER_ID, AMOUNT, CARD_NUMBER, PAYMENT_METHOD);

    final var approvedPayment = payment.approve();
    final var refundedPayment = approvedPayment.refund();
    final var rejectedPayment = payment.reject();

    assertThat(approvedPayment.status()).isEqualTo("APPROVED");
    assertThat(refundedPayment.status()).isEqualTo("REFUNDED");
    assertThat(rejectedPayment.status()).isEqualTo("REJECTED");
    assertThat(approvedPayment.id()).isEqualTo(payment.id());
    assertThat(refundedPayment.orderId()).isEqualTo(payment.orderId());
    assertThat(rejectedPayment.amount()).isEqualTo(payment.amount());
    assertThat(approvedPayment.cardNumber()).isEqualTo(payment.cardNumber());
    assertThat(refundedPayment.paymentMethod()).isEqualTo(payment.paymentMethod());
    assertThat(rejectedPayment.createdAt()).isEqualTo(payment.createdAt());
  }
}
