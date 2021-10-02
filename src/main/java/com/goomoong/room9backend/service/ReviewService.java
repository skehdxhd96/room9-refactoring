package com.goomoong.room9backend.service;

import com.goomoong.room9backend.domain.review.Review;
import com.goomoong.room9backend.domain.review.dto.ReviewDto;
import com.goomoong.room9backend.domain.review.dto.ReviewSearchDto;
import com.goomoong.room9backend.domain.review.dto.scoreDto;
import com.goomoong.room9backend.exception.NoSuchReviewException;
import com.goomoong.room9backend.exception.NotAllowedUserException;
import com.goomoong.room9backend.repository.review.ReviewRepository;
import com.goomoong.room9backend.util.AboutDate;
import com.goomoong.room9backend.util.AboutScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public Review save(Review review){
        reviewRepository.save(review);
        return review;
    }

    @Transactional
    public void update(Long userId, Long id,  String reviewContent, int reviewScore){
        Review review =  reviewRepository.findById(id).orElseThrow(() -> new NoSuchReviewException("존재하지 않는 리뷰입니다."));

        if (userId != review.getUser().getId())
            throw new NotAllowedUserException("권한이 없는 사용자입니다.");

        review.update(reviewContent, reviewScore);
    }

    @Transactional
    public void delete(Long userId, Long id){
        Review review = reviewRepository.findById(id).orElseThrow(() -> new NoSuchReviewException("존재하지 않는 리뷰입니다."));

        if (userId != review.getUser().getId())
            throw new NotAllowedUserException("권한이 없는 사용자입니다.");

        reviewRepository.delete(findById(id));
    }

    public Review findById(Long id){
        return reviewRepository.findById(id).orElseThrow(() -> new NoSuchReviewException("존재하지 않는 리뷰입니다."));
    }

    public List<Review> findLatestReview(){
        return reviewRepository.findTop3ByOrderByIdDesc();
    }

    public List<Review> findByUserAndRoom(ReviewSearchDto reviewSearchDto){
        return reviewRepository.findByUserAndRoom(reviewSearchDto);
    }

    public List<ReviewDto> selectReview(List<Review> reviews){
        return reviews.stream().map(r -> ReviewDto.builder()
                        .id(r.getId())
                        .name(r.getUser().getName())
                        .nickname(r.getUser().getNickname())
                        .thumbnailImgUrl(r.getUser().getThumbnailImgUrl())
                        .reviewContent(r.getReviewContent())
                        .reviewScore(r.getReviewScore())
                        .reviewCreated(AboutDate.getStringFromLocalDateTime(r.getCreatedDate()))
                        .reviewUpdated(AboutDate.getStringFromLocalDateTime(r.getUpdatedDate()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    public scoreDto getAvgScoreAndCount(Long roomId) {
        List<Review> reviewDatas = reviewRepository.findAvgScoreAndCountByRoom(roomId).orElse(null);
        if(reviewDatas.equals(null)) {
            return new scoreDto();
        }

        return scoreDto.builder()
                .avgScore(AboutScore.getAvgScore(reviewDatas))
                .reviewCount(reviewDatas.size())
                .build();
    }

}
