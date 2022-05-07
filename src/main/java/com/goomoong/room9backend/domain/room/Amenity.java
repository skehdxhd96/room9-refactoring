package com.goomoong.room9backend.domain.room;

import com.goomoong.room9backend.domain.room.dto.amenityDto;
import com.goomoong.room9backend.exception.RoomConfException;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Getter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Amenity {

    private String facility; // 부대시설

    public static Set<Amenity> createRoomFacility(List<amenityDto> facilities) {
        try {
            Set<Amenity> amenities = new LinkedHashSet<>();
            for (amenityDto facility : facilities) { amenities.add(new Amenity(facility.getFacility())); }
            return amenities;
        } catch(Exception e) {
            throw new RoomConfException("하나 이상의 부대시설을 등록해야 합니다. ex) 전자레인지, 냉장고 ..");
        }
    }
}