package com.project.paypass_renewal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paypass_renewal.domain.dto.request.CenterAddressRequestDto;
import com.project.paypass_renewal.domain.dto.request.HomeAddressRequestDto;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.MyPageResponseDto;
import com.project.paypass_renewal.exception.handler.mypage.MyPageControllerExceptionHandler;
import com.project.paypass_renewal.service.MyPageService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MyPageControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    MyPageService myPageService;

    @InjectMocks
    MyPageController myPageController;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(myPageController)
                .setControllerAdvice(new MyPageControllerExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("마이페이지_조회_테스트")
    void getMyPageDataTest() throws Exception {
        // given
        final String url = "/myPage/getInformation";

        NumberRequestDto numberDto = new NumberRequestDto("01012345678");
        String json = objectMapper.writeValueAsString(numberDto);

        MyPageResponseDto myPageResponseDto = new MyPageResponseDto(
                "더미유저",
                "01012345678",
                "서울시 노원구 노원로 564",
                "1001-102",
                "서울 노원구 노원로18길 41",
                "123456"
        );

        // stub
        when(myPageService.getMyPageData(any(NumberRequestDto.class))).thenReturn(myPageResponseDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("더미유저"))
                .andExpect(jsonPath("$.number").value("01012345678"))
                .andExpect(jsonPath("$.homeStreetAddress").value("서울시 노원구 노원로 564"));
    }

    @Test
    @DisplayName("유저주소_수정_테스트")
    void updateHomeAddressTest() throws Exception{
        // given
        final String url = "/myPage/updateHomeAddress";
        HomeAddressRequestDto homeAddressRequestDto = new HomeAddressRequestDto("01011112222", "72451", "서울시 테스트 주소", "테스트 자세한 주소");
        String json = objectMapper.writeValueAsString(homeAddressRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string("updateSuccess"));

    }

    @Test
    @DisplayName("유저주소_수정_세부주소_미입력_테스트")
    void updateHomeAddressNotValidDetailAddressBlankTest() throws Exception{
        // given
        final String url = "/myPage/updateHomeAddress";
        HomeAddressRequestDto homeAddressRequestDto = new HomeAddressRequestDto("01011112222", "72451", "서울시 테스트 주소", "");
        String json = objectMapper.writeValueAsString(homeAddressRequestDto);

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
    @DisplayName("유저주소_수정_세부주소_null_테스트")
    void updateHomeAddressNotValidDetailAddressNullTest() throws Exception{
        // given
        final String url = "/myPage/updateHomeAddress";
        HomeAddressRequestDto homeAddressRequestDto = new HomeAddressRequestDto("01011112222", "72451", "서울시 테스트 주소", null);
        String json = objectMapper.writeValueAsString(homeAddressRequestDto);

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
    @DisplayName("센터주소_수정_테스트")
    void updateCenterAddressTest() throws Exception{
        // given
        final String url = "/myPage/updateCenterAddress";
        CenterAddressRequestDto centerAddressRequestDto = new CenterAddressRequestDto("01012345678", "64813", "변경 후 센터주소 테스트");
        String json = objectMapper.writeValueAsString(centerAddressRequestDto);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string("updateSuccess"));
    }

    @Test
    @DisplayName("센터주소_유효성검사_테스트")
    void updateCenterAddressNotValidTest() throws Exception{
        // given
        final String url = "/myPage/updateCenterAddress";
        CenterAddressRequestDto centerAddressRequestDto = new CenterAddressRequestDto("01012345678", "6481", "변경 후 센터주소 테스트");
        String json = objectMapper.writeValueAsString(centerAddressRequestDto);

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


}
