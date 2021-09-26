package com.goomoong.room9backend.repository.reservation;

import com.goomoong.room9backend.domain.reservation.roomReservation;

import java.util.List;

public interface roomReservationRepositoryCustom {

    /**
     * 선택한 방의 전체 예약내역을 가져온다.
     * 예약이 불가능한 날짜 리스트
     */
    List<roomReservation> getAllList(Long roomId);

    /**
     * 자신이 예약한 숙소 리스트 조회
     */
    List<roomReservation> getMyBookList(Long userId);

    /**
     * 호스트용 예약내역 조회
     */
    List<roomReservation> getBookedListForHost(Long roomId, Long userId);
}
