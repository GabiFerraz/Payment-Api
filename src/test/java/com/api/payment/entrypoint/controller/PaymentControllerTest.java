package com.api.payment.entrypoint.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.api.payment.core.domain.Payment;
import com.api.payment.core.dto.PaymentDto;
import com.api.payment.core.usecase.ProcessPayment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PaymentControllerTest {

  private static final String BASE_URL = "/api/payments";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ProcessPayment processPayment;

  @Test
  void shouldProcessPaymentSuccessfully() throws Exception {
    final var request =
        new PaymentDto("order-123", new BigDecimal("100.00"), "1234567890123456", "CREDIT_CARD");
    final var response =
        new Payment(
            "1",
            "order-123",
            new BigDecimal("100.00"),
            "1234567890123456",
            "CREDIT_CARD",
            "APPROVED",
            LocalDateTime.now());

    doReturn(response).when(processPayment).execute(any(), any(), any(), any());

    this.mockMvc
        .perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("1"))
        .andExpect(jsonPath("$.orderId").value("order-123"))
        .andExpect(jsonPath("$.amount").value(100.00))
        .andExpect(jsonPath("$.cardNumber").value("1234567890123456"))
        .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
        .andExpect(jsonPath("$.status").value("APPROVED"));
  }
}
