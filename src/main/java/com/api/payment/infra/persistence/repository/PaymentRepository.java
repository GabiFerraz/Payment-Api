package com.api.payment.infra.persistence.repository;

import com.api.payment.infra.persistence.entity.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {

  Optional<PaymentEntity> findByOrderId(final String orderId);
}
