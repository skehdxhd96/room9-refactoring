package com.goomoong.room9backend.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class searchDto {

    private String title;
    private Integer limitPrice;
    private String detailLocation;
    private Integer limitPeople;
}
