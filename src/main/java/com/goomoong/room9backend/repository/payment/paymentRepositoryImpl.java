package com.goomoong.room9backend.repository.payment;

import com.goomoong.room9backend.domain.payment.payment;
import com.goomoong.room9backend.domain.reservation.ReserveStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import static com.goomoong.room9backend.domain.payment.Qpayment.payment;

@RequiredArgsConstructor
public class paymentRepositoryImpl implements paymentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public payment findByRoomReservationId(Long roomReservationId) {
        return queryFactory.select(payment)
                .from(payment)
                .join(payment.roomReservation).fetchJoin()
                .where(payment.roomReservation.Id.eq(roomReservationId),
                        payment.roomReservation.reserveStatus.eq(ReserveStatus.COMPLETE)
                                .or(payment.roomReservation.reserveStatus.eq(ReserveStatus.DONE)))
                .fetchOne();
    }
}
