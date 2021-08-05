package com.goomoong.room9backend.domain.review;

import com.goomoong.room9backend.domain.Room;
import com.goomoong.room9backend.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Review {

    @Id @GeneratedValue
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private String reviewContent;

    private LocalDateTime reviewCreated;

    private int reviewScore;

    @Builder
    public Review(Long id, User user, Room room, String reviewContent, int reviewScore){
        this.id = id;
        this.user = user;
        this.room = room;
        this.reviewContent = reviewContent;
        this.reviewCreated = LocalDateTime.now();
        this.reviewScore = reviewScore;
    }

    public void update(String reviewContent, int reviewScore) {
        this.reviewContent = reviewContent;
        this.reviewScore = reviewScore;
        this.reviewCreated = LocalDateTime.now();
    }

}
