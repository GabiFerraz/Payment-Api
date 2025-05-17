package com.api.payment.core.gateway;

public interface EventPublisher {

  void publish(final Object event);
}
