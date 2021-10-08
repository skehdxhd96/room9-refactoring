package com.goomoong.room9backend.domain.room.dto;

import com.goomoong.room9backend.domain.reservation.dto.ReservationDto;
import com.goomoong.room9backend.domain.reservation.roomReservation;
import com.goomoong.room9backend.domain.review.Review;
import com.goomoong.room9backend.domain.review.dto.ReviewDto;
import com.goomoong.room9backend.domain.review.dto.scoreDto;
import com.goomoong.room9backend.domain.room.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetDetailRoom extends GetCommonRoom {

    private String content;
    private String rule;
    private int charge; // 추가요금
    private Integer disinfectionCount;
    private String disinfectionRank;

    private List<confDto> room_configuration = new ArrayList<>();
    private List<amenityDto> room_amenity = new ArrayList<>();

    //사용자 정보
    private String userImg;
    private String userIntro;
    private String userGender;

    //이미 예약된 리스트들
    private List<ReservationDto.booked> reserveList;

    //현재 내가 좋아요 했는지 여부
    private Boolean currentLikeStatus;

    public GetDetailRoom(Room room, scoreDto scoreDto, Boolean LikeExists, List<roomReservation> reserveList) {
        super(room, scoreDto);
        this.content = room.getContent();
        this.rule = room.getRule();
        this.charge = room.getCharge();
        this.disinfectionCount = room.getDisinfectionCount();
        this.disinfectionRank = room.getDisinfectionRank();
        this.userImg = room.getUsers().getThumbnailImgUrl();
        this.userIntro = room.getUsers().getIntro();
        this.userGender = room.getUsers().getGender();
        this.currentLikeStatus = LikeExists;
        this.room_configuration = room.getRoomConfigures()
                .stream().map(c -> new confDto(c)).collect(Collectors.toList());
        this.room_amenity = room.getAmenities()
                .stream().map(a -> new amenityDto(a)).collect(Collectors.toList());
        this.reserveList = reserveList
                .stream().map(r -> new ReservationDto.booked(r)).collect(Collectors.toList());
    }
}
