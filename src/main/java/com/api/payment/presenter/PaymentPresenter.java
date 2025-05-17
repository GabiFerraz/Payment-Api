package com.api.payment.presenter;

import com.api.payment.core.domain.Payment;
import com.api.payment.presenter.response.PaymentPresenterResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentPresenter {

  public PaymentPresenterResponse parseToResponse(final Payment payment) {
    return PaymentPresenterResponse.builder()
        .id(payment.id())
        .orderId(payment.orderId())
        .amount(payment.amount())
        .cardNumber(payment.cardNumber())
        .paymentMethod(payment.paymentMethod())
        .status(payment.status())
        .createdAt(payment.createdAt())
        .build();
  }
}
