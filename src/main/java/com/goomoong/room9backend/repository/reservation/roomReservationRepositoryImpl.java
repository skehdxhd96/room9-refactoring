package com.goomoong.room9backend.repository.reservation;

import com.goomoong.room9backend.domain.reservation.ReserveStatus;
import com.goomoong.room9backend.domain.reservation.dto.ReservationDto;
import com.goomoong.room9backend.domain.reservation.roomReservation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.goomoong.room9backend.domain.reservation.QroomReservation.roomReservation;

@RequiredArgsConstructor
public class roomReservationRepositoryImpl implements roomReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<roomReservation> getAllList(Long roomId) {
        return queryFactory.select(roomReservation)
                .from(roomReservation)
                .where(roomReservation.room.id.eq(roomId),
                        roomReservation.reserveStatus.eq(ReserveStatus.COMPLETE)
                                .or(roomReservation.reserveStatus.eq(ReserveStatus.DONE)))
                .fetch();
    }

    @Override
    public List<roomReservation> getMyBookList(Long userId) {
        return queryFactory.select(roomReservation)
                .from(roomReservation)
                .join(roomReservation.room).fetchJoin()
                .where(roomReservation.users.id.eq(userId),
                        roomReservation.reserveStatus.eq(ReserveStatus.COMPLETE)
                                .or(roomReservation.reserveStatus.eq(ReserveStatus.DONE)))
                .fetch();
    }
}
