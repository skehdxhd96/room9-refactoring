package com.goomoong.room9backend.repository.payment;

import com.goomoong.room9backend.domain.payment.payment;

public interface paymentRepositoryCustom {
    payment findByRoomReservationId(Long roomReservationId);
}
