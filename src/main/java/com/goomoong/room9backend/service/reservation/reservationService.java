package com.goomoong.room9backend.service.reservation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goomoong.room9backend.domain.payment.dto.paymentDto;
import com.goomoong.room9backend.domain.payment.payment;
import com.goomoong.room9backend.domain.reservation.ReserveStatus;
import com.goomoong.room9backend.domain.reservation.dto.ReservationDto;
import com.goomoong.room9backend.domain.reservation.roomReservation;
import com.goomoong.room9backend.domain.room.Room;
import com.goomoong.room9backend.domain.room.dto.priceDto;
import com.goomoong.room9backend.domain.user.Role;
import com.goomoong.room9backend.domain.user.User;
import com.goomoong.room9backend.exception.DuplicateDateException;
import com.goomoong.room9backend.exception.FailtoPayException;
import com.goomoong.room9backend.exception.NoSuchRoomException;
import com.goomoong.room9backend.exception.ReservationNotAddException;
import com.goomoong.room9backend.repository.payment.paymentRepository;
import com.goomoong.room9backend.repository.reservation.roomReservationRepository;
import com.goomoong.room9backend.repository.room.RoomRepository;
import com.goomoong.room9backend.service.UserService;
import com.goomoong.room9backend.service.room.RoomService;
import com.goomoong.room9backend.util.AboutDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class reservationService {

    private final roomReservationRepository roomReservationRepository;
    private final RoomRepository roomRepository;
    private final paymentRepository paymentRepository;
    private final RoomService roomService;
    private final UserService userService;

    @Transactional
    public ReservationDto.response reserveRoom(User user, Long roomId, ReservationDto.request request){
        /**
         * 3. ReserveStatus : Complete -> Done은 나중에 구현
         */

        if(user.getRole() == Role.HOST) {
            throw new ReservationNotAddException();
        }

        if(!reserve_OK(request, roomId)) {
            throw new DuplicateDateException("이미 예약된 날짜입니다.");
        }

        roomReservation reserve = roomReservation.builder()
                .users(user)
                .room(roomRepository.findById(roomId).orElseThrow(() -> new NoSuchRoomException("존재하지 않는 방입니다.")))
                .startDate(AboutDate.getLocalDateTimeFromString(request.getStartDate()))
                .finalDate(AboutDate.getLocalDateTimeFromString(request.getFinalDate()))
                .reserveStatus(ReserveStatus.WAITING)
                .personnel(request.getPersonnel())
                .petWhether(request.getPetWhether())
                .build();

        roomReservationRepository.save(reserve);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            paymentDto.request deserializedPayment = objectMapper.readValue(request.getAboutPayment(), paymentDto.request.class);
            Boolean paymentStatus = deserializedPayment.getPaymentStatus();

            if(paymentStatus) {
                reserve.setReserveStatus(ReserveStatus.COMPLETE);}

            paymentRepository.save(payment.builder()
                    .Id(deserializedPayment.getPaymentId())
                    .roomReservation(reserve)
                    .totalPrice(deserializedPayment.getPaymentAmount())
                    .payMethod(deserializedPayment.getPaymentMethod())
                    .paymentStatus(paymentStatus)
                    .build());

            return ReservationDto.response.builder()
                    .reservationId(reserve.getId())
                    .title(reserve.getRoom().getTitle())
                    .detailLocation(reserve.getRoom().getDetailLocation())
                    .rule(reserve.getRoom().getRule())
                    .petWhether(reserve.getPetWhether())
                    .totalAmount(deserializedPayment.getPaymentAmount())
                    .startDate(AboutDate.getStringFromLocalDateTime(reserve.getStartDate()))
                    .finalDate(AboutDate.getStringFromLocalDateTime(reserve.getFinalDate()))
                    .reserveSuccess(paymentStatus)
                    .errorMsg(deserializedPayment.getPaymentErrorMsg())
                    .build();

        } catch (JsonProcessingException e) {
            throw new FailtoPayException("데이터 파싱 오류가 발생했습니다.");
        }
    }

    public boolean reserve_OK(ReservationDto.request request,Long roomId) {

        List<ReservationDto.booked> books = getAllBookingList(roomId);

        for (ReservationDto.booked book : books) {
            if (AboutDate.compareDay(request.getFinalDate(), book.getStartDate()) >= 1 ||
                    AboutDate.compareDay(book.getFinalDate(), request.getStartDate()) >= 1) {
                continue;
            }
            return false;
        }
        return true;
    }

    public List<ReservationDto.booked> getAllBookingList(Long roomId) {
        List<roomReservation> books = roomReservationRepository.getAllList(roomId);

        List<ReservationDto.booked> finalList = new ArrayList<>();
        for (roomReservation book : books) {
            finalList.add(ReservationDto.booked.builder()
                    .startDate(AboutDate.getStringFromLocalDateTime(book.getStartDate()))
                    .finalDate(AboutDate.getStringFromLocalDateTime(book.getFinalDate()))
                    .build());
        }

        return finalList;
    }

    public List<ReservationDto.MyList> getMyAllBook(Long userId) {
        return roomReservationRepository.getMyBookList(userId).stream()
                .map(a -> new ReservationDto.MyList(a)).collect(Collectors.toList());
    }
}
