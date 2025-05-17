package com.api.payment.entrypoint.controller;

import com.api.payment.core.dto.PaymentDto;
import com.api.payment.core.usecase.ProcessPayment;
import com.api.payment.presenter.PaymentPresenter;
import com.api.payment.presenter.response.PaymentPresenterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

  private final ProcessPayment processPayment;
  private final PaymentPresenter presenter;

  @PostMapping
  public ResponseEntity<PaymentPresenterResponse> processPayment(@RequestBody PaymentDto request) {
    final var payment =
        processPayment.execute(
            request.orderId(), request.amount(), request.cardNumber(), request.paymentMethod());

    return new ResponseEntity<>(this.presenter.parseToResponse(payment), HttpStatus.OK);
  }
}
