package com.api.payment.entrypoint.consumer;

import com.api.payment.config.RabbitMQConfig;
import com.api.payment.core.gateway.PaymentGateway;
import com.api.payment.core.usecase.ProcessPayment;
import com.api.payment.event.ProcessPaymentEvent;
import com.api.payment.event.RefundPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventConsumer {

  private final ProcessPayment processPayment;
  private final PaymentGateway paymentGateway;

  @RabbitListener(queues = RabbitMQConfig.PROCESS_PAYMENT_QUEUE)
  public void consumeProcessPaymentEvent(ProcessPaymentEvent event) {
    log.info("Received ProcessPaymentEvent for orderId: {}", event.orderId());

    processPayment.execute(
        event.orderId(), event.amount(), event.cardNumber(), event.paymentMethod());
  }

  @RabbitListener(queues = RabbitMQConfig.REFUND_PAYMENT_QUEUE)
  public void consumeRefundPaymentEvent(RefundPaymentEvent event) {
    log.info(
        "Received RefundPaymentEvent for orderId: {}, amount: {}", event.orderId(), event.amount());

    final var payment = paymentGateway.findByOrderId(String.valueOf(event.orderId()));
    if (payment.isPresent()) {
      final var paymentFound = payment.get();
      final var refundedPayment = paymentFound.refund();

      paymentGateway.save(refundedPayment);
      log.info("Payment for orderId: {} updated to REFUNDED", event.orderId());
    } else {
      log.warn("No payment found for orderId: {}", event.orderId());
    }
  }
}
