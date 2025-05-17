package com.api.payment.event;

import java.math.BigDecimal;

public record RefundPaymentEvent(int orderId, BigDecimal amount) {}
