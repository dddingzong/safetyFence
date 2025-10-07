package com.project.paypass_renewal.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.paypass_renewal.controller.UserController;
import com.project.paypass_renewal.domain.dto.request.UserRequestDto;
import com.project.paypass_renewal.exception.handler.user.UserControllerExceptionHandler;
import com.project.paypass_renewal.service.UserService;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRequestDtoValidTest {
    // UserDto에서 받는 값은 name, password, birth, number, homeAddress, centerAddress, serviceCode, homeStreetAddress, homeStreetAddressDetail, centerStreetAddress

    private final String url = "/login/newUser";

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
                .setControllerAdvice(new UserControllerExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("UserDto_name_이름형식_오류")
    void userDtoNameNotValidTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종정종인", "abc123" , LocalDate.of(2000, 5, 1), "01089099721", "01213", "12211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_name_이름형식_빈칸")
    void userDtoNameBlankTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("", "abc123", LocalDate.of(2000, 5, 1), "01089099721", "01213", "12211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_password_비밀번호형식_한국어_오류")
    void userDtoPasswordNotValidKoreanTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abㄱ123", LocalDate.of(2000, 5, 1), "01089099721", "01213", "12211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_password_비밀번호형식_특수기호_오류")
    void userDtoPasswordNotValidKSpecialTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "ab!123", LocalDate.of(2000, 5, 1), "01089099721", "01213", "12211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_password_비밀번호형식_여섯자리미만_오류")
    void userDtoPasswordNotValidUnderTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "ab123", LocalDate.of(2000, 5, 1), "01089099721", "01213", "12211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_password_비밀번호형식_빈칸")
    void userDtoPasswordBlankTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "", LocalDate.of(2000, 5, 1), "01089099721", "01213", "12211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_password_비밀번호형식_null")
    void userDtoPasswordNullTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", null, LocalDate.of(2000, 5, 1), "01089099721", "01213", "12211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }



    @Test
    @DisplayName("UserDto_birth_생년월일형식_오류")
    void userDtoBirthNotValidTest() throws Exception {
        // given
        String json = """
                {
                    "homeAddress": "34521",
                    "birth": "2025-06",
                    "centerAddress": null,
                    "name": "정종인",
                    "number": "01089099721",
                    "serviceCode": "CARE_SERVICE",
                    "password": "abc123",
                    "homeStreetAddress": "서울시 노원구 노원로 564",
                    "homeStreetAddressDetail": "1001-102",
                    "centerStreetAddress": "서울 노원구 노원로18길 41"
                }
                """;

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_birth_생년월일형식_빈칸")
    void userDtoBirthBlankTest() throws Exception {
        // given
        String json = """
                {
                    "homeAddress": "32451",
                    "birth": "",
                    "centerAddress": null,
                    "name": "정종인",
                    "number": "01089099721",
                    "serviceCode": "CARE_SERVICE",
                    "password": "abs123",
                    "homeStreetAddress": "서울시 노원구 노원로 564",
                    "homeStreetAddressDetail": "1001-102",
                    "centerStreetAddress": "서울 노원구 노원로18길 41"
                }
                """;

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    @DisplayName("UserDto_birth_생년월일형식_미래")
    void userDtoBirthFutureTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abc123", LocalDate.of(2050, 5, 1), "01089099721", "01313", "13211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_number_번호형식_하이픈_오류")
    void userDtoNumberNotValidHyphenTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abc123", LocalDate.of(2000, 5, 1), "010-8909-9721", "01233", "12311", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_number_번호형식_자릿수초과_오류")
    void userDtoNumberNotValidCountOverTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abc123", LocalDate.of(2000, 5, 1), "010890997211", "01213", "12321", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_number_번호형식_자릿수미만_오류")
    void userDtoNumberNotValidCountUnderTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abc123", LocalDate.of(2000, 5, 1), "0108909972", "01213", "12321", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_number_번호형식_빈칸")
    void userDtoNumberBlankTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abc123", LocalDate.of(2000, 5, 1), "", "01313", "12321", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_homeAddress_집주소형식_다섯자리미만_오류")
    void userDtoHomeAddressNotValidShortTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "0131", "12311", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_homeAddress_집주소형식_다섯자리초과_오류")
    void userDtoHomeAddressNotValidLongTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "012331", "12321", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_homeAddress_집주소형식_빈칸")
    void userDtoHomeAddressBlankTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "", "12321", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_centerAddress_센터주소형식_다섯자리초과_오류")
    void userDtoCenterAddressNotValidLongTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "15311", "123211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_centerAddress_센터주소형식_다섯자리미만_오류")
    void userDtoCenterAddressNotValidShortTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "15311", "1211", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_centerAddress_센터주소형식_빈칸")
    void userDtoCenterAddressBlankTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "15311", "", ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_centerAddress_센터주소형식_Null")
    void userDtoCenterAddressNullTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "15311", null, ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);
        User user = toEntity(userRequestDto, "ABC123");

        // stub
        when(userService.saveNewUser(any(UserRequestDto.class))).thenReturn(user);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("01089099721"));
    }

    @Test
    @DisplayName("UserDto_serviceCode_코드형식_null")
    void userDtoServiceCodeNullTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "15311", "123121", null, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");
        String json = objectMapper.writeValueAsString(userRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("UserDto_centerStreetAddress_센터주소형식_Null")
    void userDtoCenterStreetAddressNullTest() throws Exception {
        // given
        UserRequestDto userRequestDto = createUserRequestDtoDummy("정종인", "abs123", LocalDate.of(2000, 5, 1), "01089099721", "15311", null, ServiceCode.PAYPASS_SERVICE, "서울시 노원구 노원로 564", "1001-102", null);
        String json = objectMapper.writeValueAsString(userRequestDto);
        User user = toEntity(userRequestDto, "ABC123");

        // stub
        when(userService.saveNewUser(any(UserRequestDto.class))).thenReturn(user);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("01089099721"))
                .andExpect(jsonPath("$.centerAddress").doesNotExist())
                .andExpect(jsonPath("$.centerStreetAddress").doesNotExist());
    }


    private UserRequestDto createUserRequestDtoDummy(String name, String password, LocalDate birth, String number, String homeAddress, String centerAddress, ServiceCode serviceCode, String homeStreetAddress, String homeStreetAddressDetail, String centerStreetAddress) {
        return new UserRequestDto(name, password, birth, number, homeAddress, centerAddress, serviceCode, homeStreetAddress, homeStreetAddressDetail, centerStreetAddress);
    }

    private User toEntity(UserRequestDto userRequestDto, String linkCode){
        String name = userRequestDto.getName();
        String password = userRequestDto.getPassword();
        LocalDate birth = userRequestDto.getBirth();
        String number = userRequestDto.getNumber();
        String homeAddress = userRequestDto.getHomeAddress();
        String centerAddress = userRequestDto.getCenterAddress();
        ServiceCode serviceCode = userRequestDto.getServiceCode();

        User user = new User(name, password, birth, number, homeAddress, centerAddress, linkCode, serviceCode);

        return user;
    }
}
