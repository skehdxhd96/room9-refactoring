package com.goomoong.room9backend.domain.room.dto;

import com.goomoong.room9backend.domain.room.Amenity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatedRequestRoomDto {

    //Configuration
    private List<confDto> conf;

    //Amenity
    private List<amenityDto> facilities;

    //images
    private List<MultipartFile> images;

    private reUserDto reUserDto;

    //room
    private int limit; // 제한인원
    private int price; // 가격
    private String title; // 방 제목
    private String content; // 방 소개글(내용)
    private String detailLocation; // 방 상세 위치
    private String rule; // 방 규칙
    private int addCharge; // 추가요금
}
