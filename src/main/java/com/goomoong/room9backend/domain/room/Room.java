package com.goomoong.room9backend.domain.room;

import com.goomoong.room9backend.domain.base.BaseEntity;
import com.goomoong.room9backend.domain.file.RoomImg;
import com.goomoong.room9backend.domain.reservation.roomReservation;
import com.goomoong.room9backend.domain.review.Review;
import com.goomoong.room9backend.domain.room.dto.CreatedRequestRoomDto;
import com.goomoong.room9backend.domain.room.dto.UpdateRequestRoomDto;
import com.goomoong.room9backend.domain.room.dto.confDto;
import com.goomoong.room9backend.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Room_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User users;

    @OneToMany(mappedBy = "room")
    private List<roomReservation> roomReservations = new ArrayList<>();

    @OneToMany(mappedBy = "room")
    private List<RoomImg> roomImg = new ArrayList<>();

    @OneToMany(mappedBy = "room")
    private List<Review> reviews = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "Room_Amenity",
            joinColumns = @JoinColumn(name = "Room_Id"))
    @Column(name = "Facility")
    private Set<Amenity> amenities = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "Room_Configuration",
            joinColumns = @JoinColumn(name = "Room_Id"))
    private Set<RoomConfiguration> roomConfigures = new LinkedHashSet<>();

    private int limited; // 제한 인원
    private int price; // 가격
    private String title; // 제목
    private String content; // 설명
    private String detailLocation; // 상세 위치
    private String rule; // 규칙
    private int charge; // 추가인원 요금
    private int liked; // 좋아요

    //== 양방향 연관관계 메서드 ==//
    public void setUsers(User users) {
        this.users = users;
        users.getRooms().add(this);
    }

    public static Room createRoom(User users, CreatedRequestRoomDto request,
                                  Set<RoomConfiguration> roomConfig, Set<Amenity> roomAmenities) {

        return Room.builder()
                .users(users)
                .limited(request.getLimit())
                .price(request.getPrice())
                .title(request.getTitle())
                .content(request.getContent())
                .detailLocation(request.getDetailLocation())
                .rule(request.getRule())
                .charge(request.getAddCharge())
                .liked(0)
                .roomConfigures(roomConfig)
                .amenities(roomAmenities)
                .build();
    }

    public void withGood(Boolean likeStatus) {
        this.liked = likeStatus == true ? (this.liked + 1) : (this.liked - 1);
    }

    public void modifyRoom(UpdateRequestRoomDto request) {

        this.limited = request.getLimit();
        this.price = request.getPrice();
        this.title = request.getTitle();
        this.content = request.getContent();
        this.detailLocation = request.getDetailLocation();
        this.rule = request.getRule();
        this.charge = request.getAddCharge();

        this.getRoomConfigures().removeAll(this.getRoomConfigures());
        for (confDto confDto : request.getConf()) {
            this.getRoomConfigures()
                    .add(new RoomConfiguration(confDto.getConfType(), confDto.getCount()));
        }

        this.getAmenities().removeAll(this.getAmenities());
        for (String facility : request.getFacilities()) {
            this.getAmenities().add(new Amenity(facility));
        }
    }
}
