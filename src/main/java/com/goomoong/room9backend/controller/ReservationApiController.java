package com.goomoong.room9backend.controller;

import com.goomoong.room9backend.domain.reservation.dto.ReservationDto;
import com.goomoong.room9backend.security.userdetails.CustomUserDetails;
import com.goomoong.room9backend.service.reservation.reservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationApiController {

    private final reservationService reservationService;

    /**
     * 예약 -> 결제 -> 예약내역 반환
     */
    @PostMapping("/room/book/{roomId}")
    public ReservationDto.response roomBooked(@PathVariable Long roomId, @RequestBody ReservationDto.request request,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        return reservationService.reserveRoom(currentUser.getUser(), roomId, request);
    }

    /**
     * 선택한 방의 전체 예약 내역을 보여주는 api(이미 예약된 날짜를 보여주는 api).
     */
    @GetMapping("/room/book/{roomId}/list")
    public ReservationDto.bookWithCount<List<ReservationDto.booked>> getBookList(@PathVariable Long roomId) {
        List<ReservationDto.booked> allBookingList = reservationService.getAllBookingList(roomId);
        return new ReservationDto.bookWithCount(allBookingList.size(), roomId ,allBookingList);
    }

    /**
     * Guest 자신의 예약 내역 확인하기.
     */
    @GetMapping("/room/mybook")
    public ReservationDto.bookData<List<ReservationDto.MyList>> myBookList(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<ReservationDto.MyList> myAllBook = reservationService.getMyAllBook(currentUser.getId());
        return new ReservationDto.bookData<>(myAllBook.size(), myAllBook);
    }
}
