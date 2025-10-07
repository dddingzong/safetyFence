package com.project.paypass_renewal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.paypass_renewal.domain.User;
import com.project.paypass_renewal.domain.dto.request.LoginRequestDto;
import com.project.paypass_renewal.domain.dto.request.UserRequestDto;
import com.project.paypass_renewal.exception.handler.GlobalExceptionHandler;
import com.project.paypass_renewal.service.UserService;
import com.project.paypass_renewal.support.UserDtoTestUtil;
import com.project.paypass_renewal.support.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("컨트롤러_신규유저_DTO_활용_저장_테스트")
    void createNewUserTest() throws Exception {
        // given
        final String url = "/login/newUser";
        UserRequestDto userRequestDto = UserDtoTestUtil.createDummyUserDto();

        User user = UserTestUtils.createDummyUser();

        // stub
        when(userService.checkDuplicateNumber(any(String.class))).thenReturn(false);
        // userService.saveNewUser 호출 시 dummyUser 반환하도록 설정
        when(userService.saveNewUser(any(UserRequestDto.class))).thenReturn(user);

        String json = objectMapper.writeValueAsString(userRequestDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("01012345678"));
    }

    @Test
    @DisplayName("컨트롤러_신규유저_JSON_활용_저장_테스트")
    void createNewUserUsingJsonTest() throws Exception {
        // given
        final String url = "/login/newUser";
        String json = """
                {
                    "homeAddress": "32521",
                    "birth": "2025-06-02",
                    "centerAddress": null,
                    "name": "정종인",
                    "number": "01089091234",
                    "serviceCode": "CARE_SERVICE",
                    "password": "abs123",
                    "homeStreetAddress": "서울시 노원구 노원로 564",
                    "homeStreetAddressDetail": "1001-102",
                    "centerStreetAddress": "서울 노원구 노원로18길 41"
                }
                """;

        User user = UserTestUtils.createDummyUser();

        // stub
        when(userService.checkDuplicateNumber(any(String.class))).thenReturn(false);
        // userService.saveNewUser 호출 시 dummyUser 반환하도록 설정
        when(userService.saveNewUser(any(UserRequestDto.class))).thenReturn(user);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("01012345678"));
    }

    @Test
    @DisplayName("컨트롤러_신규유저_비밀번호_이상_저장_테스트")
    void createNewUserLongPasswordTest() throws Exception {
        // given
        final String url = "/login/newUser";
        String json = """
                {
                    "homeAddress": "32521",
                    "birth": "2025-06-02",
                    "centerAddress": null,
                    "name": "정종인",
                    "number": "01089091234",
                    "serviceCode": "CARE_SERVICE",
                    "password": "nNa1D2A123dAA01",
                    "homeStreetAddress": "서울시 노원구 노원로 564",
                    "homeStreetAddressDetail": "1001-102",
                    "centerStreetAddress": "서울 노원구 노원로18길 41"
                }
                """;

        User user = UserTestUtils.createDummyUser();

        // stub
        when(userService.checkDuplicateNumber(any(String.class))).thenReturn(false);
        // userService.saveNewUser 호출 시 dummyUser 반환하도록 설정
        when(userService.saveNewUser(any(UserRequestDto.class))).thenReturn(user);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("01012345678"));
    }

    @Test
    @DisplayName("컨트롤러_신규유저_번호중복_테스트")
    void duplicateNumberTest() throws Exception {
        // given
        final String url = "/login/newUser";
        UserRequestDto userRequestDto = UserDtoTestUtil.createDummyUserDto();

        // stub
        when(userService.checkDuplicateNumber(any(String.class))).thenReturn(true);

        String json = objectMapper.writeValueAsString(userRequestDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("유저_로그인_요청_아이디미존재시_예외발생_테스트")
    void userSignInWithoutNumberTest() throws Exception{
        // given
        final String url = "/login/signIn";
        LoginRequestDto loginRequestDto = new LoginRequestDto("01012341234", "abc123");

        // stub
        when(userService.checkExistNumber(any(String.class))).thenReturn(false);

        String json = objectMapper.writeValueAsString(loginRequestDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("존재하지 않는 전화번호 입니다."));
    }

    @Test
    @DisplayName("유저_로그인_요청_비밀번호_매칭_실패_예외발생_테스트")
    void userSignInNotMatchPasswordTest() throws Exception{
        // given
        final String url = "/login/signIn";
        LoginRequestDto loginRequestDto = new LoginRequestDto("01012341234", "abc123");

        // stub
        when(userService.checkExistNumber(any(String.class))).thenReturn(true);
        when(userService.matchPassword(any(LoginRequestDto.class))).thenReturn(false);

        String json = objectMapper.writeValueAsString(loginRequestDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("유저_로그인_요청_성공_테스트")
    void userSignInSuccessTest() throws Exception{
        // given
        final String url = "/login/signIn";
        LoginRequestDto loginRequestDto = new LoginRequestDto("01012341234", "abc123");

        // stub
        when(userService.checkExistNumber(any(String.class))).thenReturn(true);
        when(userService.matchPassword(any(LoginRequestDto.class))).thenReturn(true);

        String json = objectMapper.writeValueAsString(loginRequestDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("message").value("loginSuccess"));
    }

}
