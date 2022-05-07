package com.goomoong.room9backend.controller;

import com.goomoong.room9backend.domain.user.User;
import com.goomoong.room9backend.domain.user.dto.*;
import com.goomoong.room9backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;

    @GetMapping("/api/v1/users")
    public AllUsersResponseDto getAllUsers() {
        List<UserResponseDto> allUsers =  userService.findAllUsers().stream().map(user ->
            UserResponseDto.builder().id(user.getId()).nickname(user.getNickname()).thumbnailImgUrl(user.getThumbnailImgUrl()).email(user.getEmail()).birthday(user.getBirthday()).gender(user.getGender()).intro(user.getIntro()).build()
                ).collect(Collectors.toList());
        return new AllUsersResponseDto(allUsers);
    }

//    @GetMapping("/api/v1/users/info")
//    public UserResponseDto getUserInfoByToken(@AuthenticationPrincipal CustomUserDetails currentUser) {
//        User findUser = userService.findById(currentUser.getId());
//
//        return UserResponseDto.builder()
//                .id(findUser.getId())
//                .nickname(findUser.getNickname())
//                .role(findUser.getRole())
//                .thumbnailImgUrl(findUser.getThumbnailImgUrl())
//                .email(findUser.getEmail())
//                .birthday(findUser.getBirthday())
//                .gender(findUser.getGender())
//                .intro(findUser.getIntro())
//                .build();
//    }

    @GetMapping("/api/v1/users/{id}")
    public UserResponseDto getUserInfo(@PathVariable Long id) {
        User findUser = userService.findById(id);

        return UserResponseDto.builder()
                .id(findUser.getId())
                .nickname(findUser.getNickname())
                .role(findUser.getRole())
                .thumbnailImgUrl(findUser.getThumbnailImgUrl())
                .email(findUser.getEmail())
                .birthday(findUser.getBirthday())
                .gender(findUser.getGender())
                .intro(findUser.getIntro())
                .build();
    }

    @PostMapping("/api/v1/users/{id}/role")
    public IdResponseDto changeRole(@PathVariable Long id) {
        User user = userService.changeRole(id);

        return IdResponseDto.builder().id(user.getId()).build();
    }

    @PostMapping("/api/v1/users/{id}")
    public IdResponseDto update(@PathVariable Long id, UpdateRequestDto requestDto) {
        User updateUser = userService.update(id, requestDto.getNickname(), requestDto.getFile(), requestDto.getEmail(),
                requestDto.getBirthday(), requestDto.getGender(), requestDto.getIntro());

        return IdResponseDto.builder().id(updateUser.getId()).build();
    }
}
