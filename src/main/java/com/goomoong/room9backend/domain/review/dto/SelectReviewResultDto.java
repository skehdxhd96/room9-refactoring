package com.goomoong.room9backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SelectReviewResultDto<T>{

    private T data;
}