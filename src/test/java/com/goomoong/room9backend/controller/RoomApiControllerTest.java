package com.goomoong.room9backend.controller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goomoong.room9backend.config.AWSConfig;
import com.goomoong.room9backend.config.MockSecurityFilter;
import com.goomoong.room9backend.config.QuerydslConfig;
import com.goomoong.room9backend.config.SecurityConfig;
import com.goomoong.room9backend.domain.file.File;
import com.goomoong.room9backend.domain.file.RoomImg;
import com.goomoong.room9backend.domain.file.dto.GetRoomfileDto;
import com.goomoong.room9backend.domain.reservation.ReserveStatus;
import com.goomoong.room9backend.domain.reservation.roomReservation;
import com.goomoong.room9backend.domain.review.Review;
import com.goomoong.room9backend.domain.review.dto.CreateReviewRequestDto;
import com.goomoong.room9backend.domain.review.dto.scoreDto;
import com.goomoong.room9backend.domain.room.Amenity;
import com.goomoong.room9backend.domain.room.Room;
import com.goomoong.room9backend.domain.room.RoomConfiguration;
import com.goomoong.room9backend.domain.room.dto.*;
import com.goomoong.room9backend.domain.user.Role;
import com.goomoong.room9backend.domain.user.User;
import com.goomoong.room9backend.repository.user.UserRepository;
import com.goomoong.room9backend.security.userdetails.CustomUserDetails;
import com.goomoong.room9backend.security.userdetails.CustomUserDetailsService;
import com.goomoong.room9backend.service.ReviewService;
import com.goomoong.room9backend.service.UserService;
import com.goomoong.room9backend.service.file.FileService;
import com.goomoong.room9backend.service.file.S3Uploader;
import com.goomoong.room9backend.service.room.RoomSearchService;
import com.goomoong.room9backend.service.room.RoomService;
import com.goomoong.room9backend.util.AboutDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.goomoong.room9backend.ApiDocumentUtils.getDocumentRequest;
import static com.goomoong.room9backend.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class RoomApiControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;
    @MockBean
    private RoomService roomService;
    @MockBean
    private RoomSearchService roomSearchService;
    @MockBean
    private UserService userService;
    @MockBean
    private ReviewService reviewService;

    private User user;
    private Room room1;
    private List<Room> rooms = new ArrayList<>();
    private List<Room> filterRooms = new ArrayList<>();
    private List<Room> myRoom = new ArrayList<>();
    private List<GetCommonRoom> glist = new ArrayList<>();
    private GetDetailRoom getDetailRoom;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {

        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity(new MockSecurityFilter()))
                .build();

        user = User.builder()
                .id(1L)
                .accountId("1")
                .name("mock")
                .nickname("mockusername")
                .role(Role.HOST)
                .thumbnailImgUrl("mock.jpg")
                .email("mock@abc")
                .birthday("0101")
                .gender("male")
                .intro("test").build();;

        Set<RoomConfiguration> rset = new LinkedHashSet<>();
        rset.add(RoomConfiguration.builder().confType("?????????").count(3).build());
        rset.add(RoomConfiguration.builder().confType("???").count(5).build());

        Set<Amenity> aset = new LinkedHashSet<>();
        aset.add(Amenity.builder().facility("???????????????").build());
        aset.add(Amenity.builder().facility("?????????").build());


        File file1 = File.builder().id(1L).fileName("test1").originalName("test1.png").extension("png").url("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????pngFIle.png").build();
        File file2 = File.builder().id(2L).fileName("test2").originalName("test2.jpg").extension("jpg").url("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????jpgFIle.jpg").build();

        List<RoomImg> rllist = new ArrayList<>();
        rllist.add(RoomImg.builder().id(1L).file(file1).build());
        rllist.add(RoomImg.builder().id(2L).file(file2).build());

        room1 = Room.builder()
                .id(1L)
                .users(user)
                .roomConfigures(rset)
                .amenities(aset)
                .roomImg(rllist)
                .limited(10)
                .price(10000)
                .title("???????????????")
                .content("??????1?????????.")
                .detailLocation("??????")
                .rule("??????????????? ???????????? ??? ????????? ????????? ???????????????.")
                .charge(1000)
                .disinfectionCount(7)
                .disinfectionRank("??????")
                .liked(3).build();

        Room room2 = Room.builder()
                .id(2L)
                .users(user)
                .roomConfigures(rset)
                .amenities(aset)
                .roomImg(rllist)
                .limited(4)
                .price(20000)
                .title("????????????")
                .content("??????2?????????.")
                .detailLocation("??????")
                .rule("???????????? ????????? ???????????? ?????????????????????.")
                .charge(500)
                .liked(7).build();

        Room room3 = Room.builder()
                .id(3L)
                .users(user)
                .roomConfigures(rset)
                .amenities(aset)
                .roomImg(rllist)
                .limited(8)
                .price(12000)
                .title("??????")
                .content("??????3?????????.")
                .detailLocation("????????? ?????????")
                .rule("????????????")
                .charge(2000)
                .liked(5).build();

        rooms.add(room1);
        rooms.add(room2);
        filterRooms.add(room3);
        filterRooms.add(room1);
        myRoom.add(room1);
        myRoom.add(room2);
        myRoom.add(room3);

        List<roomReservation> reserveList = new ArrayList<>();
        reserveList.add(roomReservation.builder()
                .room(room1)
                .users(user)
                .reserveStatus(ReserveStatus.COMPLETE)
                .startDate(AboutDate.getLocalDateTimeFromString("2021-08-31"))
                .finalDate(AboutDate.getLocalDateTimeFromString("2021-09-04"))
                .personnel(5)
                .petWhether(false)
                .build());

        scoreDto scoredto = scoreDto.builder()
                .avgScore(2.5)
                .reviewCount(147)
                .build();
        scoreDto scoreDto2 = new scoreDto();
        GetCommonRoom getCommonRoom = new GetCommonRoom(room1, scoreDto2);
        glist.add(getCommonRoom);
        getDetailRoom = new GetDetailRoom(room1, scoreDto2, true, reserveList);
    }

    @Test
    public void ???_??????() throws Exception {

        //given
        InputStream is1 = new ClassPathResource("mock/images/jpgFile.jpg").getInputStream();
        MockMultipartFile m1 = new MockMultipartFile("images", "jpgFile.jpg", "image/jpg", is1.readAllBytes());

        given(roomService.addRoom(any(), any())).willReturn(1L);

        //when
        ResultActions result = mvc.perform(multipart("/room/create")
                .file(m1)
                .param("conf.confType", "?????????")
                .param("conf.count", "2")
                .param("conf.confType", "??????")
                .param("conf.count", "2")
                .param("facilities", "???????????????")
                .param("facilities", "??????????????????")
                .param("limit", "10")
                .param("price", "10000")
                .param("title", "testTitle")
                .param("content", "testContent")
                .param("detailLocation", "testLocation")
                .param("rule", "testRule")
                .param("addCharge", "1000")
                .principal(new UsernamePasswordAuthenticationToken(CustomUserDetails.create(user), null))
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.MULTIPART_FORM_DATA));

        //then
        result.andExpect(status().isCreated())
                .andDo(document("room-create",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("????????? ????????? Bearer Token")
                        ),
                        requestParts(
                                partWithName("images").description("????????? ??????(?????? 1??? ??????)")
                        ),
                        requestParameters(
                                parameterWithName("conf.confType").description("?????? ?????? ??????(?????? 1??? ??????)"),
                                parameterWithName("conf.count").description("?????? ?????? ??????(?????? 1??? ??????)"),
                                parameterWithName("facilities").description("?????? ??????(?????? 1??? ??????)"),
                                parameterWithName("limit").description("?????? ?????? ??????"),
                                parameterWithName("price").description("?????? ??????"),
                                parameterWithName("title").description("?????? ??????"),
                                parameterWithName("content").description("?????? ?????? ??????"),
                                parameterWithName("detailLocation").description("?????? ??????"),
                                parameterWithName("rule").description("?????? ??????"),
                                parameterWithName("addCharge").description("?????? ?????? ????????? ???????????? ??????(??????:???)")
                        ),
                        responseFields(
                                fieldWithPath("roomId").type(JsonFieldType.NUMBER).description("????????? ??? ?????????")
                        )
                ));
    }

    @Test
    @DisplayName(value = "?????? ???????????? : ????????????")
    public void getAllRoomTest() throws Exception{

        //given
        given(roomService.findAll()).willReturn(glist);

        //when
        ResultActions results = mvc.perform(get("/room"));

        //then
        results
                .andDo(print())
                .andDo(document("room-getAll",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("??? ?????? ??????"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("????????? ?????? ????????? ???"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("?????? ?????? ???")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("???????????????"))
                .andExpect(jsonPath("$.room[0].location").value("??????"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????jpgFIle.jpg"));
    }

    @Test
    @DisplayName(value = "?????? ?????? ??????????????????. / ?????? : ??????(??????) / ????????????(??????) / ??????(??????) / ?????????(??????) / ?????????(??????)")
    public void filterTest() throws Exception{

        //given
        given(roomSearchService.search(any())).willReturn(glist);

        //when
        ResultActions results = mvc.perform(
                get("/room/search")
                        .param("title", "???????????????")
                        .param("limitPrice", "10")
                        .param("detailLocation", "??????")
                        .param("limitPeople", "10000")
                        .param("orderStandard", "LIKEDDESC"));

        //then
        results
                .andDo(print())
                .andDo(document("room-filter",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("title").optional().description("????????? ????????? ???????????? ????????? ????????? ??????"),
                                parameterWithName("detailLocation").optional().description("????????? ????????? ???????????? ????????? ????????? ??????"),
                                parameterWithName("limitPrice").optional().description("????????? ?????? ????????? ????????? ??????"),
                                parameterWithName("limitPeople").optional().description("????????? ?????? ????????? ????????? ??????"),
                                parameterWithName("orderStandard").description("?????? ??????[LIKEDDESC(????????? ?????????), LIKEDASC, CREATEDASC, CREATEDDESC(?????????, default)]")),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("??? ?????? ??????"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("????????? ?????? ????????? ???"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("?????? ?????? ???")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("???????????????"))
                .andExpect(jsonPath("$.room[0].location").value("??????"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????jpgFIle.jpg"));
    }

    @Test
    @DisplayName(value = "???????????? ?????? ????????? ?????? api")
    public void getMyRoomListTest() throws Exception {
        //given
        given(roomService.getMyRoom(any())).willReturn(glist);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.get("/room/myRoom")
                .principal(new UsernamePasswordAuthenticationToken(CustomUserDetails.create(user), null))
                .header("Authorization", "Bearer accessToken"));

        //then
        results
                .andDo(print())
                .andDo(document("room-myRoomList",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("????????? ????????? Bearer Token")
                        ),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("??? ?????? ??????"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("????????? ?????? ????????? ???"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("?????? ?????? ???")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("???????????????"))
                .andExpect(jsonPath("$.room[0].location").value("??????"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????jpgFIle.jpg"));
    }

    @Test
    @DisplayName(value = "??? ????????????")
    public void getRoomDetailTest() throws Exception{
        //given
        given(roomService.getRoomDetail(any(), any())).willReturn(getDetailRoom);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.get("/room/{roomId}", 1L)
                .principal(new UsernamePasswordAuthenticationToken(CustomUserDetails.create(user), null))
                .header("Authorization", "Bearer accessToken"));

        //then
        results
                .andDo(print())
                .andDo(document("room-detail",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("????????? ????????? Bearer Token")
                        ),
                        pathParameters(
                                parameterWithName("roomId").description("?????? ?????????")
                        ),
                        responseFields(
                                fieldWithPath("roomId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                fieldWithPath("username").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("userImg").type(JsonFieldType.STRING).description("?????? ?????? ?????? ??????????????????"),
                                fieldWithPath("userIntro").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("userGender").type(JsonFieldType.STRING).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("location").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("limitPeople").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("like").type(JsonFieldType.NUMBER).description("????????? ?????? ????????? ???"),
                                fieldWithPath("images[].url").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("?????? ????????? ??????"),
                                fieldWithPath("rule").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("charge").type(JsonFieldType.NUMBER).description("?????? ?????? ????????? ????????????"),
                                fieldWithPath("disinfectionCount").type(JsonFieldType.NUMBER).description("???????????? ?????? ??????"),
                                fieldWithPath("disinfectionRank").type(JsonFieldType.STRING).description("?????? ????????? ?????? ??????"),
                                fieldWithPath("avgScore").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("reviewCount").type(JsonFieldType.NUMBER).description("?????? ?????? ???"),
                                fieldWithPath("room_configuration[].confType").type(JsonFieldType.STRING).description("?????? ???????????? ??????(ex ?????????, ??????...)"),
                                fieldWithPath("room_configuration[].count").type(JsonFieldType.NUMBER).description("?????? ???????????? ??????"),
                                fieldWithPath("room_amenity[].facility").type(JsonFieldType.STRING).description("?????? ???????????? ??????(ex ???????????????, ????????? ..)"),
                                fieldWithPath("reserveList[].startDate").type(JsonFieldType.STRING).description("?????? ????????? ????????????. ????????????"),
                                fieldWithPath("reserveList[].finalDate").type(JsonFieldType.STRING).description("?????? ????????? ????????????. ????????????"),
                                fieldWithPath("currentLikeStatus").type(JsonFieldType.BOOLEAN).description("?????? ?????????????????? ????????? ????????? ???????????? ??????")
                        )
                ))
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.username").value("mockusername"))
                .andExpect(jsonPath("$.userImg").value("mock.jpg"))
                .andExpect(jsonPath("$.userIntro").value("test"))
                .andExpect(jsonPath("$.userGender").value("male"))
                .andExpect(jsonPath("$.title").value("???????????????"))
                .andExpect(jsonPath("$.location").value("??????"))
                .andExpect(jsonPath("$.limitPeople").value(10))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.like").value(3))
                .andExpect(jsonPath("$.images[0].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????pngFIle.png"))
                .andExpect(jsonPath("$.images[1].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????jpgFIle.jpg"))
                .andExpect(jsonPath("$.content").value("??????1?????????."))
                .andExpect(jsonPath("$.rule").value("??????????????? ???????????? ??? ????????? ????????? ???????????????."))
                .andExpect(jsonPath("$.charge").value(1000))
                .andExpect(jsonPath("$.disinfectionCount").value(7))
                .andExpect(jsonPath("$.disinfectionRank").value("??????"))
                .andExpect(jsonPath("$.avgScore").value(0.0))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.room_configuration[0].confType").value("?????????"))
                .andExpect(jsonPath("$.room_configuration[1].confType").value("???"))
                .andExpect(jsonPath("$.room_configuration[0].count").value(3))
                .andExpect(jsonPath("$.room_configuration[1].count").value(5))
                .andExpect(jsonPath("$.room_amenity[0].facility").value("???????????????"))
                .andExpect(jsonPath("$.room_amenity[1].facility").value("?????????"))
                .andExpect(jsonPath("$.reserveList[0].startDate").value("2021-08-31"))
                .andExpect(jsonPath("$.reserveList[0].finalDate").value("2021-09-04"))
                .andExpect(jsonPath("$.currentLikeStatus").value("true"));
    }

    @Test
    @DisplayName(value = "????????? ????????? ?????????????????? ????????? ???????????? api test")
    public void getTotalPriceTest() throws Exception{

        roomData.price price = roomData.price.builder()
                .totalPrice(Long.valueOf(48000))
                .originalPrice(Long.valueOf(40000))
                .charge(Long.valueOf(8000))
                .build();

        //given
        given(roomService.getTotalPrice(anyLong(), any())).willReturn(price);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.get("/room/price/{roomId}", 1L)
                                    .param("personnel", "12")
                                    .param("startDate", "2021-08-29")
                                    .param("finalDate", "2021-09-02"));

        //then
        results
                .andDo(print())
                .andDo(document("room-reservePrice",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("roomId").description("?????? ?????????")
                        ),
                        requestParameters(
                                parameterWithName("startDate").optional().description("?????? ???????????? (\"yyyy-mm-dd\")"),
                                parameterWithName("finalDate").optional().description("?????? ??????????????? (\"yyyy-mm-dd\")"),
                                parameterWithName("personnel").optional().description("??????????????? ?????? ?????? ???")
                        ),
                        responseFields(
                                fieldWithPath("totalPrice").type(JsonFieldType.NUMBER).description("??? ??????(???????????? + ????????????)"),
                                fieldWithPath("originalPrice").type(JsonFieldType.NUMBER).description("?????? ??????(?????? x ?????????)"),
                                fieldWithPath("charge").type(JsonFieldType.NUMBER).description("?????? ??????(???????????? x ????????? x ????????????)")
                        )
                ))
                .andExpect(jsonPath("$.totalPrice").value(48000));
    }

    @Test
    @DisplayName(value = "?????? ??? ??????")
    public void getRoomRandom() throws Exception{
        //given
        given(roomSearchService.randSearch()).willReturn(glist);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.get("/room/random"));

        //then
        results
                .andDo(print())
                .andDo(document("room-random",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("??? ?????? ??????"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("????????? ?????? ????????? ???"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("?????? ?????? ???")
                        )
                ));
    }

    @Test
    @DisplayName(value = "????????? ??? ??????")
    public void getRoomPopular() throws Exception{
        //given
        given(roomSearchService.search(any(searchDto.class))).willReturn(glist);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.get("/room/popular"));

        //then
        results
                .andDo(print())
                .andDo(document("room-popular",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("??? ?????? ??????"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("????????? ?????? ????????? ???"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("?????? ?????? ???")
                        )
                ));
    }

    @Test
    @DisplayName("?????? ?????????(???)")
    public void roomWithGood() throws Exception{
        //given
        roomData.liked rled = roomData.liked.builder().currentLiked(4).currentStatus(true).build();

        given(roomService.AboutGoodToRoom(any(), any())).willReturn(rled);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.post("/room/good/{roomId}", 1L)
                .principal(new UsernamePasswordAuthenticationToken(CustomUserDetails.create(user), null))
                .header("Authorization", "Bearer accessToken"));

        //then
        results
                .andDo(print())
                .andDo(document("room-good",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("????????? ????????? Bearer Token")
                        ),
                        pathParameters(
                                parameterWithName("roomId").description("?????? ?????????")
                        ),
                        responseFields(
                                fieldWithPath("currentLiked").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                fieldWithPath("currentStatus").type(JsonFieldType.BOOLEAN).description("?????? ????????? ???????????? ??????")
                        )
                ));
    }

    @Test
    @DisplayName(value = "?????? ???(?????????) ??? ?????? ?????????")
    public void getMyWish() throws Exception{
        //given
        given(roomService.getRoomWithGood(any())).willReturn(glist);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.get("/room/MyWish")
                .principal(new UsernamePasswordAuthenticationToken(CustomUserDetails.create(user), null))
                .header("Authorization", "Bearer accessToken"));

        //then
        results
                .andDo(print())
                .andDo(document("room-myWish",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("????????? ????????? Bearer Token")
                        ),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("??? ?????? ??????"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("?????? ?????? ?????? ?????????"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("????????? ?????? ????????? ???"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ??????"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("?????? ?????? ???")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("???????????????"))
                .andExpect(jsonPath("$.room[0].location").value("??????"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://room9.s3.ap-northeast-2.amazonaws.com/???????????????/??????????????????????????????jpgFIle.jpg"));
    }
}
