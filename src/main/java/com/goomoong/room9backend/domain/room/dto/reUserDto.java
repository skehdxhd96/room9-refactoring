package com.goomoong.room9backend.domain.room.dto;
import com.goomoong.room9backend.domain.user.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class reUserDto {
    private Long id;
    private String accountId;
    private String name;
    private String nickname;
    private Role role;
    private String thumbnailImgUrl;
    private String email;
    private String birthday;
    private String gender;
    private String intro;
}
