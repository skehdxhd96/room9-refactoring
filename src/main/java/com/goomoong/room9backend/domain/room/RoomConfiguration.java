package com.goomoong.room9backend.domain.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goomoong.room9backend.domain.room.dto.confDto;
import com.goomoong.room9backend.exception.NoSuchRoomException;
import com.goomoong.room9backend.exception.RoomConfException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;

@Getter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class RoomConfiguration {

    private String confType;
    private Integer count;

    public static Set<RoomConfiguration> createRoomConfig(List<confDto> confs) {
        try {
            Set<RoomConfiguration> roomConfig = new LinkedHashSet<>();
            for (confDto conf : confs) {
                roomConfig.add(new RoomConfiguration(conf.getConfType(), conf.getCount())); }
            return roomConfig;
        } catch (Exception e) {
            throw new RoomConfException("하나 이상의 구성요소를 등록해야 합니다. ex) 침실, 화장실 ..");
        }
    }
}