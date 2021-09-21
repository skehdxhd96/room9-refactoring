package com.goomoong.room9backend.domain.reservation.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.goomoong.room9backend.domain.reservation.roomReservation;
import com.goomoong.room9backend.util.AboutDate;
import lombok.*;
import org.apache.tomcat.jni.Local;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class request {

        private String startDate;
        private String finalDate;
        private Integer personnel;
        private Boolean petWhether;
        private String aboutPayment;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class response {
        private Long reservationId;
        private String title;
        private String detailLocation;
        private String rule;
        private Boolean petWhether;
        private Integer totalAmount;
        private String startDate;
        private String finalDate;
        private Boolean reserveSuccess;
        private String errorMsg;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class bookWithCount<T> {
        private int count;
        private Long roomId;
        private T booked;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class bookData<T> {
        private int count;
        private T booked;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class booked {
        private String startDate;
        private String finalDate;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyList {

        private Long roomId;
        private Integer personnel;
        private String startDate;
        private String finalDate;
        private String detailLocation;
        private String title;

        public MyList(roomReservation roomReservation) {
            this.roomId = roomReservation.getRoom().getId();
            this.personnel = roomReservation.getPersonnel();
            this.startDate = AboutDate.getStringFromLocalDateTime(roomReservation.getStartDate());
            this.finalDate = AboutDate.getStringFromLocalDateTime(roomReservation.getStartDate());
            this.detailLocation = roomReservation.getRoom().getDetailLocation();
            this.title = roomReservation.getRoom().getTitle();
        }
    }
}
