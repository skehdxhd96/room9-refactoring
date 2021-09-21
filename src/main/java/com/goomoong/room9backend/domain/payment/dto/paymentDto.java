package com.goomoong.room9backend.domain.payment.dto;

import lombok.*;

public class paymentDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class request {
        private String paymentId;
        private String paymentMethod;
        private Boolean paymentStatus;
        private Integer paymentAmount;
        private String paymentErrorMsg;
    }
}
