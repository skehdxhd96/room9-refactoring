package com.goomoong.room9backend.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


public class roomData {

    @Data
    @AllArgsConstructor
    @Builder
    public static class GET<T> {
        private int count;
        private T room;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class price {
        private Long totalPrice;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class liked {
        private Integer currentLiked;
        private Boolean currentStatus;
    }
}
