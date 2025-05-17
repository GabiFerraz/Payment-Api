package com.api.payment.infra.gateway;

import com.api.payment.config.RabbitMQConfig;
import com.api.payment.core.gateway.EventPublisher;
import com.api.payment.infra.gateway.exception.GatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQGateway implements EventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(final Object event) {
    try {
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PAYMENT_PROCESSED_QUEUE, event);
    } catch (Exception e) {
      log.error("Failed to publish event: {}", event, e);
      throw new GatewayException("Failed to publish event: " + event);
    }
  }
}
