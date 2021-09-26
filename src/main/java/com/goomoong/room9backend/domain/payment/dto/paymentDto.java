package com.goomoong.room9backend.domain.payment.dto;

import lombok.*;

public class paymentDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class request {
        private String merchant_uid;
        private String pg_provider;
        private Boolean success;
        private Integer paid_amount;
        private String error_msg;
    }
}
