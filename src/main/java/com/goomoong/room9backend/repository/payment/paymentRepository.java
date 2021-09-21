package com.goomoong.room9backend.repository.payment;

import com.goomoong.room9backend.domain.payment.payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface paymentRepository extends JpaRepository<payment, String> {
}
