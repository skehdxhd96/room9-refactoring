package com.goomoong.room9backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goomoong.room9backend.config.MockSecurityFilter;
import com.goomoong.room9backend.domain.chat.dto.*;
import com.goomoong.room9backend.domain.user.Role;
import com.goomoong.room9backend.domain.user.User;
import com.goomoong.room9backend.security.userdetails.CustomUserDetails;
import com.goomoong.room9backend.service.chat.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.goomoong.room9backend.ApiDocumentUtils.getDocumentRequest;
import static com.goomoong.room9backend.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ChatApiControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ChatService chatService;

    private User user;
    private CustomUserDetails currentUser;

    @BeforeEach
    void setUp(RestDocumentationContextProvider contextProvider) {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(contextProvider))
                .apply(springSecurity(new MockSecurityFilter()))
                .build();
        user = User.builder().id(1L).name("guest").role(Role.GUEST).build();
        currentUser = CustomUserDetails.create(user);
    }

    @Test
    void getChatRoomsApi() throws Exception {
        //given
        ChatRoomsDto.ChatRoomDto roomDto = ChatRoomsDto.ChatRoomDto.builder()
                .chatRoomId(1L)
                .isHost(false)
                .roomTitle("love house")
                .checkinDate(LocalDateTime.of(2021, 9, 13, 12, 0))
                .checkoutDate(LocalDateTime.of(2021, 9, 14, 12, 0))
                .build();
        List<ChatRoomsDto.ChatRoomDto> rooms = new ArrayList<>();
        rooms.add(roomDto);
        ChatRoomsDto roomsDto = ChatRoomsDto.builder().rooms(rooms).build();
        given(chatService.getChatRooms(user)).willReturn(roomsDto);
        List<Integer> startDate = new ArrayList<>();
        List<Integer> finalDate = new ArrayList<>();
        startDate.add(2021);
        startDate.add(9);
        startDate.add(13);
        startDate.add(12);
        startDate.add(0);
        finalDate.add(2021);
        finalDate.add(9);
        finalDate.add(14);
        finalDate.add(12);
        finalDate.add(0);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.get("/api/v1/chat/chatroom")
                .principal(new UsernamePasswordAuthenticationToken(currentUser, null))
                .header("Authorization", "Bearer accessToken")
        );

        //then
        result
                .andDo(print())
                .andDo(document("chat/getChatRooms",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("사용자 토큰")
                        ),
                        responseFields(
                                fieldWithPath("rooms.[].chatRoomId").description("채팅방 id"),
                                fieldWithPath("rooms.[].isHost").description("현재 사용자의 게스트/호스트 구분"),
                                fieldWithPath("rooms.[].roomTitle").description("호스트의 방 이름"),
                                fieldWithPath("rooms.[].checkinDate").description("체크인 날짜"),
                                fieldWithPath("rooms.[].checkoutDate").description("체크아웃 날짜")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms[0].chatRoomId").value(1L))
                .andExpect(jsonPath("$.rooms[0].isHost").value(false))
                .andExpect(jsonPath("$.rooms[0].roomTitle").value("love house"))
                .andExpect(jsonPath("$.rooms[0].checkinDate").value(startDate))
                .andExpect(jsonPath("$.rooms[0].checkoutDate").value(finalDate));
    }

    @Test
    void createChatRoomApi() throws Exception {
        //given
        IdResponseDto responseDto = IdResponseDto.builder().id(1L).build();
        ChatRoomRequestDto requestDto = ChatRoomRequestDto.builder().roomId(1L).build();
        given(chatService.createChatRoom(requestDto, user)).willReturn(responseDto);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.post("/api/v1/chat/chatroom")
                .principal(new UsernamePasswordAuthenticationToken(currentUser, null))
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(requestDto))
        );

        //then
        result
                .andDo(print())
                .andDo(document("chat/createChatRoom",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("사용자 토큰")
                        ),
                        requestFields(
                                fieldWithPath("roomId").description("호스트의 방 id")
                        )
                ))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteChatRoomApi() throws Exception {
        //given
        IdResponseDto responseDto = IdResponseDto.builder().id(1L).build();
        given(chatService.deleteChatRoom(1L)).willReturn(responseDto);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/chat/chatroom/{id}", 1L));

        //then
        result
                .andDo(print())
                .andDo(document("chat/deleteChatRoom",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("삭제할 채팅방 id")
                        ),
                        responseFields(
                                fieldWithPath("id").description("삭제한 채팅방 id")
                        )
                ))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createChatMessageApi() throws Exception {
        //given
        IdResponseDto responseDto = IdResponseDto.builder().id(1L).build();
        ChatMessageRequestDto requestDto = ChatMessageRequestDto.builder().content("test").build();
        given(chatService.createChatMessage(1L, user, requestDto)).willReturn(responseDto);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.post("/api/v1/chat/chatroom/{id}/message", 1L)
                .principal(new UsernamePasswordAuthenticationToken(currentUser, null))
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(requestDto))
        );

        //then
        result
                .andDo(print())
                .andDo(document("chat/createChatMessage",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("사용자 토큰")
                        ),
                        pathParameters(
                                parameterWithName("id").description("채팅방 id")
                        ),
                        requestFields(
                                fieldWithPath("content").description("메시지 내용")
                        )
                ))
                .andExpect(status().isCreated());
    }

    @Test
    void getChatMessagesApi() throws Exception {
        //given
        ChatMessagesDto.ChatMessageDto messageDto = ChatMessagesDto.ChatMessageDto.builder()
                .messageId(1L)
                .userId(1L)
                .content("test")
                .createdDate(LocalDateTime.of(2021, 9, 14, 12, 0))
                .updatedDate(LocalDateTime.of(2021, 9, 14, 12, 0))
                .build();
        List<ChatMessagesDto.ChatMessageDto> messages = new ArrayList<>();
        messages.add(messageDto);
        ChatMessagesDto messagesDto = ChatMessagesDto.builder().messages(messages).build();
        given(chatService.getChatMessages(1L)).willReturn(messagesDto);
        List<Integer> resultDate = new ArrayList<>();
        resultDate.add(2021);
        resultDate.add(9);
        resultDate.add(14);
        resultDate.add(12);
        resultDate.add(0);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.get("/api/v1/chat/chatroom/{id}/message", 1L));

        //then
        result
                .andDo(print())
                .andDo(document("chat/getChatMessages",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("채팅방 id")
                        ),
                        responseFields(
                                fieldWithPath("messages.[].messageId").description("메시지 id"),
                                fieldWithPath("messages.[].userId").description("작성자 id"),
                                fieldWithPath("messages.[].content").description("메시지 내용"),
                                fieldWithPath("messages.[].createdDate").description("메시지 생성 날짜"),
                                fieldWithPath("messages.[].updatedDate").description("메시지 수정 날짜")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].messageId").value(1L))
                .andExpect(jsonPath("$.messages[0].userId").value(1L))
                .andExpect(jsonPath("$.messages[0].content").value("test"))
                .andExpect(jsonPath("$.messages[0].createdDate").value(resultDate))
                .andExpect(jsonPath("$.messages[0].updatedDate").value(resultDate));
    }

    @Test
    void editChatMessageApi() throws Exception {
        //given
        IdResponseDto responseDto = IdResponseDto.builder().id(1L).build();
        ChatMessageRequestDto requestDto = ChatMessageRequestDto.builder().content("test").build();
        given(chatService.editChatMessage(1L, requestDto)).willReturn(responseDto);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.put("/api/v1/chat/chatroom/{roomid}/message/{messageid}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(requestDto))
        );

        //then
        result
                .andDo(print())
                .andDo(document("chat/editChatMessage",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("roomid").description("채팅방 id"),
                                parameterWithName("messageid").description("삭제할 메시지 id")
                        ),
                        requestFields(
                                fieldWithPath("content").description("수정할 메시지 내용")
                        )
                ))
                .andExpect(status().isOk());
    }

    @Test
    void deleteChatMessageApi() throws Exception {
        //given
        IdResponseDto responseDto = IdResponseDto.builder().id(1L).build();
        given(chatService.deleteChatMessage(1L)).willReturn(responseDto);

        //when
        ResultActions result = mvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/chat/chatroom/{roomid}/message/{messageid}", 1L, 1L));

        //then
        result
                .andDo(print())
                .andDo(document("chat/deleteChatMessage",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("roomid").description("채팅방 id"),
                                parameterWithName("messageid").description("삭제할 메시지 id")
                        ),
                        responseFields(
                                fieldWithPath("id").description("삭제한 메시지 id")
                        )
                ))
                .andExpect(jsonPath("$.id").value(1L));
    }
}