package com.api.payment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "order.events";
  public static final String PROCESS_PAYMENT_QUEUE = "process-payment";
  public static final String REFUND_PAYMENT_QUEUE = "refund-payment";
  public static final String PAYMENT_PROCESSED_QUEUE = "payment-processed";

  @Bean
  public TopicExchange orderExchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  public Queue processPaymentQueue() {
    return new Queue(PROCESS_PAYMENT_QUEUE, true);
  }

  @Bean
  public Queue refundPaymentQueue() {
    return new Queue(REFUND_PAYMENT_QUEUE, true);
  }

  @Bean
  public Queue paymentProcessedQueue() {
    return new Queue(PAYMENT_PROCESSED_QUEUE, true);
  }

  @Bean
  public Binding processPaymentBinding(Queue processPaymentQueue, TopicExchange orderExchange) {
    return BindingBuilder.bind(processPaymentQueue).to(orderExchange).with(PROCESS_PAYMENT_QUEUE);
  }

  @Bean
  public Binding refundPaymentBinding(Queue refundPaymentQueue, TopicExchange orderExchange) {
    return BindingBuilder.bind(refundPaymentQueue).to(orderExchange).with(REFUND_PAYMENT_QUEUE);
  }

  @Bean
  public Binding paymentProcessedBinding(Queue paymentProcessedQueue, TopicExchange orderExchange) {
    return BindingBuilder.bind(paymentProcessedQueue)
        .to(orderExchange)
        .with(PAYMENT_PROCESSED_QUEUE);
  }
}
