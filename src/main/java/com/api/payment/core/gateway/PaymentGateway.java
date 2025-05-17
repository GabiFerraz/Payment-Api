package com.api.payment.core.gateway;

import com.api.payment.core.domain.Payment;
import java.util.Optional;

public interface PaymentGateway {

  Payment processPayment(final Payment payment);

  Optional<Payment> findByOrderId(final String orderId);

  Payment save(final Payment payment);
}
