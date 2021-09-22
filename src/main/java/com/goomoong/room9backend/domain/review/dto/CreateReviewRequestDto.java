package com.goomoong.room9backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequestDto {

    private Long roomId;
    private String reviewContent;
    private int reviewScore;
}
