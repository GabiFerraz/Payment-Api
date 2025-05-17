package com.api.payment.infra.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "order_id", nullable = false, unique = true)
  private String orderId;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "card_number", nullable = false)
  private String cardNumber;

  @Column(name = "payment_method", nullable = false)
  private String paymentMethod;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
