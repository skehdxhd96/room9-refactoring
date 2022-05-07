package com.goomoong.room9backend.domain.room.dto;

import com.goomoong.room9backend.domain.room.RoomConfiguration;
import com.goomoong.room9backend.exception.RoomConfException;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class confDto {

    private String confType;
    private Integer count;

    public confDto(RoomConfiguration roomConfiguration) {
        this.confType = roomConfiguration.getConfType();
        this.count = roomConfiguration.getCount();
    }
}