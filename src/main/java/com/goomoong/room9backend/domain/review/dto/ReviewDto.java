package com.goomoong.room9backend.domain.review.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDto{

    private Long id;
    private String name;
    private String nickname;
    private String thumbnailImgUrl;
    private String reviewContent;
    private String reviewCreated;
    private String reviewUpdated;
    private int reviewScore;
}
