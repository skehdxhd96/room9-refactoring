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
        rset.add(RoomConfiguration.builder().confType("화장실").count(3).build());
        rset.add(RoomConfiguration.builder().confType("방").count(5).build());

        Set<Amenity> aset = new LinkedHashSet<>();
        aset.add(Amenity.builder().facility("전자레인지").build());
        aset.add(Amenity.builder().facility("도어락").build());


        File file1 = File.builder().id(1L).fileName("test1").originalName("test1.png").extension("png").url("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호pngFIle.png").build();
        File file2 = File.builder().id(2L).fileName("test2").originalName("test2.jpg").extension("jpg").url("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호jpgFIle.jpg").build();

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
                .title("아메리카노")
                .content("내용1입니다.")
                .detailLocation("서울")
                .rule("상세페이지 들어가기 전 간단한 정보만 표기합니다.")
                .charge(1000)
                .liked(3).build();

        Room room2 = Room.builder()
                .id(2L)
                .users(user)
                .roomConfigures(rset)
                .amenities(aset)
                .roomImg(rllist)
                .limited(4)
                .price(20000)
                .title("카페라떼")
                .content("내용2입니다.")
                .detailLocation("인천")
                .rule("리스트로 간략한 데이터만 미리보기합니다.")
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
                .title("아아")
                .content("내용3입니다.")
                .detailLocation("서울시 중랑구")
                .rule("테스트룰")
                .charge(2000)
                .liked(5).build();

        rooms.add(room1);
        rooms.add(room2);
        filterRooms.add(room3);
        filterRooms.add(room1);
        myRoom.add(room1);
        myRoom.add(room2);
        myRoom.add(room3);

        scoreDto scoredto = scoreDto.builder()
                .avgScore(2.5)
                .reviewCount(147)
                .build();
        scoreDto scoreDto2 = new scoreDto();
        GetCommonRoom getCommonRoom = new GetCommonRoom(room1, scoreDto2);
        glist.add(getCommonRoom);
        getDetailRoom = new GetDetailRoom(room1, scoreDto2);
    }

    @Test
    public void 방_생성() throws Exception {

        //given
        InputStream is1 = new ClassPathResource("mock/images/jpgFile.jpg").getInputStream();
        MockMultipartFile m1 = new MockMultipartFile("images", "jpgFile.jpg", "image/jpg", is1.readAllBytes());

        given(roomService.addRoom(any(), any())).willReturn(1L);

        //when
        ResultActions result = mvc.perform(multipart("/room/create")
                .file(m1)
                .param("conf.confType", "화장실")
                .param("conf.count", "2")
                .param("conf.confType", "침실")
                .param("conf.count", "2")
                .param("facilities", "전자레인지")
                .param("facilities", "에어프라이기")
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
                                headerWithName("Authorization").description("카카오 사용자 Bearer Token")
                        ),
                        requestParts(
                                partWithName("images").description("이미지 파일(최소 1개 이상)")
                        ),
                        requestParameters(
                                parameterWithName("conf.confType").description("구성 요소 타입(최소 1개 이상)"),
                                parameterWithName("conf.count").description("구성 요소 숫자(최소 1개 이상)"),
                                parameterWithName("facilities").description("부대 시설(최소 1개 이상)"),
                                parameterWithName("limit").description("숙소 제한 인원"),
                                parameterWithName("price").description("숙소 가격"),
                                parameterWithName("title").description("숙소 제목"),
                                parameterWithName("content").description("숙소 소개 내용"),
                                parameterWithName("detailLocation").description("숙소 위치"),
                                parameterWithName("rule").description("숙소 규칙"),
                                parameterWithName("addCharge").description("제한 인원 초과시 발생되는 비용(단위:명)")
                        ),
                        responseFields(
                                fieldWithPath("roomId").type(JsonFieldType.NUMBER).description("생성된 방 아이디")
                        )
                ));
    }

    @Test
    @DisplayName(value = "숙소 전체조회 : 간략하게")
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
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("총 숙소 개수"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("숙소 아이디"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("숙소 만든 사람 닉네임"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("숙소 제목"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("숙소 위치"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("제한 인원"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("숙소 가격"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("숙소에 찍힌 좋아요 수"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("숙소 이미지"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("숙소 댓글 평점 평균"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("숙소 댓글 수")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("아메리카노"))
                .andExpect(jsonPath("$.room[0].location").value("서울"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호jpgFIle.jpg"));
    }

    @Test
    @DisplayName(value = "필터 조회 테스트입니다. / 필터 : 가격(이하) / 제한인원(이하) / 지역(포함) / 타이틀(포함) / 좋아요(정렬)")
    public void filterTest() throws Exception{

        //given
        given(roomSearchService.search(any())).willReturn(glist);

        //when
        ResultActions results = mvc.perform(
                get("/room/search")
                        .param("title", "아메리카노")
                        .param("limitPrice", "10")
                        .param("detailLocation", "서울")
                        .param("limitPeople", "10000")
                        .param("orderStandard", "LIKEDDESC"));

        //then
        results
                .andDo(print())
                .andDo(document("room-filter",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("title").optional().description("입력한 단어를 포함하는 제목의 숙소만 검색"),
                                parameterWithName("detailLocation").optional().description("입력한 단어를 포함하는 지역의 숙소만 검색"),
                                parameterWithName("limitPrice").optional().description("입력한 가격 이하의 숙소만 검색"),
                                parameterWithName("limitPeople").optional().description("입력한 인원 이하의 숙소만 검색"),
                                parameterWithName("orderStandard").description("정렬 기준[LIKEDDESC(좋아요 많은순), LIKEDASC, CREATEDASC, CREATEDDESC(최신순, default)]")),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("총 숙소 개수"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("숙소 아이디"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("숙소 만든 사람 닉네임"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("숙소 제목"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("숙소 위치"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("제한 인원"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("숙소 가격"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("숙소에 찍힌 좋아요 수"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("숙소 이미지"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("숙소 댓글 평점 평균"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("숙소 댓글 수")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("아메리카노"))
                .andExpect(jsonPath("$.room[0].location").value("서울"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호jpgFIle.jpg"));
    }

    @Test
    @DisplayName(value = "내가만든 숙소 리스트 조회 api")
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
                                headerWithName("Authorization").description("카카오 사용자 Bearer Token")
                        ),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("총 숙소 개수"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("숙소 아이디"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("숙소 만든 사람 닉네임"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("숙소 제목"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("숙소 위치"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("제한 인원"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("숙소 가격"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("숙소에 찍힌 좋아요 수"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("숙소 이미지"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("숙소 댓글 평점 평균"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("숙소 댓글 수")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("아메리카노"))
                .andExpect(jsonPath("$.room[0].location").value("서울"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호jpgFIle.jpg"));
    }

    @Test
    @DisplayName(value = "방 상세보기")
    public void getRoomDetailTest() throws Exception{
        //given
        given(roomService.getRoomDetail(any())).willReturn(getDetailRoom);

        //when
        ResultActions results = mvc.perform(RestDocumentationRequestBuilders.get("/room/{roomId}", 1L));

        //then
        results
                .andDo(print())
                .andDo(document("room-detail",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("roomId").description("숙소 아이디")
                        ),
                        responseFields(
                                fieldWithPath("roomId").type(JsonFieldType.NUMBER).description("숙소 아이디"),
                                fieldWithPath("username").type(JsonFieldType.STRING).description("숙소 만든 사람 닉네임"),
                                fieldWithPath("userImg").type(JsonFieldType.STRING).description("숙소 만든 사람 썸네일이미지"),
                                fieldWithPath("userIntro").type(JsonFieldType.STRING).description("숙소 만든 사람 인트로"),
                                fieldWithPath("userGender").type(JsonFieldType.STRING).description("숙소 만든 사람 성별"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("숙소 제목"),
                                fieldWithPath("location").type(JsonFieldType.STRING).description("숙소 위치"),
                                fieldWithPath("limitPeople").type(JsonFieldType.NUMBER).description("제한 인원"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER).description("숙소 가격"),
                                fieldWithPath("like").type(JsonFieldType.NUMBER).description("숙소에 찍힌 좋아요 수"),
                                fieldWithPath("images[].url").type(JsonFieldType.STRING).description("숙소 이미지"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("숙소 소개글 본문"),
                                fieldWithPath("rule").type(JsonFieldType.STRING).description("숙소 규칙"),
                                fieldWithPath("charge").type(JsonFieldType.NUMBER).description("제한 인원 초과시 추가요금"),
                                fieldWithPath("avgScore").type(JsonFieldType.NUMBER).description("숙소 댓글 평점 평균"),
                                fieldWithPath("reviewCount").type(JsonFieldType.NUMBER).description("숙소 댓글 수"),
                                fieldWithPath("room_configuration[].confType").type(JsonFieldType.STRING).description("숙소 구성요소 타입(ex 화장실, 침실...)"),
                                fieldWithPath("room_configuration[].count").type(JsonFieldType.NUMBER).description("숙소 구성요소 개수"),
                                fieldWithPath("room_amenity[].facility").type(JsonFieldType.STRING).description("숙소 부대시설 이름(ex 전자레인지, 도어락 ..)")
                        )
                ))
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.username").value("mockusername"))
                .andExpect(jsonPath("$.userImg").value("mock.jpg"))
                .andExpect(jsonPath("$.userIntro").value("test"))
                .andExpect(jsonPath("$.userGender").value("male"))
                .andExpect(jsonPath("$.title").value("아메리카노"))
                .andExpect(jsonPath("$.location").value("서울"))
                .andExpect(jsonPath("$.limitPeople").value(10))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.like").value(3))
                .andExpect(jsonPath("$.images[0].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호pngFIle.png"))
                .andExpect(jsonPath("$.images[1].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호jpgFIle.jpg"))
                .andExpect(jsonPath("$.content").value("내용1입니다."))
                .andExpect(jsonPath("$.rule").value("상세페이지 들어가기 전 간단한 정보만 표기합니다."))
                .andExpect(jsonPath("$.charge").value(1000))
                .andExpect(jsonPath("$.avgScore").value(0.0))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.room_configuration[0].confType").value("화장실"))
                .andExpect(jsonPath("$.room_configuration[1].confType").value("방"))
                .andExpect(jsonPath("$.room_configuration[0].count").value(3))
                .andExpect(jsonPath("$.room_configuration[1].count").value(5))
                .andExpect(jsonPath("$.room_amenity[0].facility").value("전자레인지"))
                .andExpect(jsonPath("$.room_amenity[1].facility").value("도어락"));
    }

    @Test
    @DisplayName(value = "날짜와 인원을 선택할때마다 가격을 조회하는 api test")
    public void getTotalPriceTest() throws Exception{

        //given
        given(roomService.getTotalPrice(anyLong(), any())).willReturn(42000L);

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
                                parameterWithName("roomId").description("숙소 아이디")
                        ),
                        requestParameters(
                                parameterWithName("startDate").optional().description("예약 시작날짜 (\"yyyy-mm-dd\")"),
                                parameterWithName("finalDate").optional().description("예약 마지막날짜 (\"yyyy-mm-dd\")"),
                                parameterWithName("personnel").optional().description("예약하려고 하는 인원 수")
                        ),
                        responseFields(
                                fieldWithPath("totalPrice").type(JsonFieldType.NUMBER).description("총 가격")
                        )
                ))
                .andExpect(jsonPath("$.totalPrice").value(42000));
    }

    @Test
    @DisplayName(value = "랜덤 방 조회")
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
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("총 숙소 개수"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("숙소 아이디"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("숙소 만든 사람 닉네임"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("숙소 제목"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("숙소 위치"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("제한 인원"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("숙소 가격"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("숙소에 찍힌 좋아요 수"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("숙소 이미지"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("숙소 댓글 평점 평균"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("숙소 댓글 수")
                        )
                ));
    }

    @Test
    @DisplayName(value = "인기순 방 조회")
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
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("총 숙소 개수"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("숙소 아이디"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("숙소 만든 사람 닉네임"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("숙소 제목"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("숙소 위치"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("제한 인원"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("숙소 가격"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("숙소에 찍힌 좋아요 수"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("숙소 이미지"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("숙소 댓글 평점 평균"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("숙소 댓글 수")
                        )
                ));
    }

    @Test
    @DisplayName("숙소 좋아요(찜)")
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
                                headerWithName("Authorization").description("카카오 사용자 Bearer Token")
                        ),
                        pathParameters(
                                parameterWithName("roomId").description("숙소 아이디")
                        ),
                        responseFields(
                                fieldWithPath("currentLiked").type(JsonFieldType.NUMBER).description("현재 좋아요 수"),
                                fieldWithPath("currentStatus").type(JsonFieldType.BOOLEAN).description("내가 좋아요 눌렀는지 여부")
                        )
                ));
    }

    @Test
    @DisplayName(value = "내가 찜(좋아요) 한 숙소 리스트")
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
                                headerWithName("Authorization").description("카카오 사용자 Bearer Token")
                        ),
                        responseFields(
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("총 숙소 개수"),
                                fieldWithPath("room.[].roomId").type(JsonFieldType.NUMBER).description("숙소 아이디"),
                                fieldWithPath("room.[].username").type(JsonFieldType.STRING).description("숙소 만든 사람 닉네임"),
                                fieldWithPath("room.[].title").type(JsonFieldType.STRING).description("숙소 제목"),
                                fieldWithPath("room.[].location").type(JsonFieldType.STRING).description("숙소 위치"),
                                fieldWithPath("room.[].limitPeople").type(JsonFieldType.NUMBER).description("제한 인원"),
                                fieldWithPath("room.[].price").type(JsonFieldType.NUMBER).description("숙소 가격"),
                                fieldWithPath("room.[].like").type(JsonFieldType.NUMBER).description("숙소에 찍힌 좋아요 수"),
                                fieldWithPath("room.[].images[].url").type(JsonFieldType.STRING).description("숙소 이미지"),
                                fieldWithPath("room.[].avgScore").type(JsonFieldType.NUMBER).description("숙소 댓글 평점 평균"),
                                fieldWithPath("room.[].reviewCount").type(JsonFieldType.NUMBER).description("숙소 댓글 수")
                        )
                ))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.room[0].roomId").value(1L))
                .andExpect(jsonPath("$.room[0].username").value("mockusername"))
                .andExpect(jsonPath("$.room[0].title").value("아메리카노"))
                .andExpect(jsonPath("$.room[0].location").value("서울"))
                .andExpect(jsonPath("$.room[0].limitPeople").value(10))
                .andExpect(jsonPath("$.room[0].price").value(10000))
                .andExpect(jsonPath("$.room[0].like").value(3))
                .andExpect(jsonPath("$.room[0].avgScore").value(0.0))
                .andExpect(jsonPath("$.room[0].reviewCount").value(0))
                .andExpect(jsonPath("$.room[0].images[0].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호pngFIle.png"))
                .andExpect(jsonPath("$.room[0].images[1].url").value("https://roomimg.s3.ap-northeast-2.amazonaws.com/도메인이름/랜덤으로생성된느번호jpgFIle.jpg"));
    }
}
