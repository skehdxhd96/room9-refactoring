package com.goomoong.room9backend.controller;

import com.goomoong.room9backend.domain.room.Room;
import com.goomoong.room9backend.domain.user.User;
import com.goomoong.room9backend.service.ReviewService;
import com.goomoong.room9backend.service.room.RoomSearchService;
import com.goomoong.room9backend.service.room.RoomService;
import com.goomoong.room9backend.domain.room.dto.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RoomApiController {

    private final RoomService roomService;
    private final RoomSearchService roomSearchService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/room/create")
    public CreatedResponseRoomDto createdRoom(@Valid CreatedRequestRoomDto request) {
        return new CreatedResponseRoomDto(roomService.addRoom(request));
    }

    @GetMapping("/room")
    public roomData.GET<List<GetCommonRoom>> getAllRooms() {

        List<GetCommonRoom> allRooms = roomService.findAll();
        return new roomData.GET<>(allRooms.size(), allRooms);
    }

//    @GetMapping("/room/{roomId}")
//    public GetDetailRoom getDetailRoom(@PathVariable("roomId") Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
//        return roomService.getRoomDetail(id, currentUser.getUser());
//    }

//    @GetMapping("/room/myRoom")
//    public roomData.GET<List<GetCommonRoom>> getMyRoomList(@AuthenticationPrincipal CustomUserDetails currentUser) {
//        List<GetCommonRoom> myRooms = roomService.getMyRoom(currentUser.getUser());
//        return new roomData.GET<>(myRooms.size(), myRooms);
//    }

    @GetMapping("/room/search")
    public roomData.GET<List<GetCommonRoom>> getRoomWithFilter(@Valid searchDto search) {

        List<GetCommonRoom> roomList = roomSearchService.search(search);

        return new roomData.GET<>(roomList.size(), roomList);
    }

    @GetMapping("/room/price/{roomId}")
    public roomData.price getRoomPrice(@PathVariable("roomId") Long id, @ModelAttribute priceDto priceDto) {
        return roomService.getTotalPrice(id, priceDto);
    }

    @GetMapping("/room/popular")
    public roomData.GET<List<GetCommonRoom>> getPopularRooms(){
        searchDto search = new searchDto();
        search.setOrderStandard(OrderDto.LIKEDDESC);
        List<GetCommonRoom> roomList = roomSearchService.search(search);

        return new roomData.GET<>(roomList.size(), roomList);
    }

    @GetMapping("/room/random")
    public roomData.GET<List<GetCommonRoom>> getRandomRooms(){
        List<GetCommonRoom> roomList = roomSearchService.randSearch();
        return new roomData.GET<>(roomList.size(), roomList);
    }

//    @PostMapping("/room/good/{roomId}")
//    public roomData.liked addRoomGood(@PathVariable("roomId") Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
//        return roomService.AboutGoodToRoom(id, currentUser.getUser());
//    }
//
//    @GetMapping("/room/MyWish")
//    public roomData.GET<List<GetCommonRoom>> getMyWishList(@AuthenticationPrincipal CustomUserDetails currentUser) {
//        List<GetCommonRoom> roomList = roomService.getRoomWithGood(currentUser.getUser());
//
//        return new roomData.GET<>(roomList.size(), roomList);
//    }
}
