package com.api.payment.event;

import java.math.BigDecimal;

public record RefundPaymentEvent(String orderId, BigDecimal amount) {}
