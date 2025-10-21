package com.project.safetyFence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.SignInRequestDto;
import com.project.safetyFence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private static final String TEST_NUMBER = "01012345678";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        testUser = new User(TEST_NUMBER, "tester", TEST_PASSWORD, LocalDate.of(1990, 1, 1), "test-link-code");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("signIn success test")
    void signIn_Success() throws Exception {
        // given
        SignInRequestDto requestDto = new SignInRequestDto(TEST_NUMBER, TEST_PASSWORD);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/user/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("login success"))
                .andDo(print());
    }

    @Test
    @DisplayName("signIn fail - number not exist")
    void signIn_Fail_NumberNotExist() throws Exception {
        // given
        String nonExistentNumber = "01099999999";
        SignInRequestDto requestDto = new SignInRequestDto(nonExistentNumber, TEST_PASSWORD);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/user/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("존재하지 않는 번호입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("signIn fail - wrong password")
    void signIn_Fail_WrongPassword() throws Exception {
        // given
        String wrongPassword = "wrongPassword";
        SignInRequestDto requestDto = new SignInRequestDto(TEST_NUMBER, wrongPassword);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/user/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("비밀번호가 일치하지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("signIn fail - empty number")
    void signIn_Fail_EmptyNumber() throws Exception {
        // given
        SignInRequestDto requestDto = new SignInRequestDto("", TEST_PASSWORD);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/user/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("존재하지 않는 번호입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("signIn fail - empty password")
    void signIn_Fail_EmptyPassword() throws Exception {
        // given
        SignInRequestDto requestDto = new SignInRequestDto(TEST_NUMBER, "");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/user/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("비밀번호가 일치하지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("signIn fail - null values")
    void signIn_Fail_NullValues() throws Exception {
        // given
        SignInRequestDto requestDto = new SignInRequestDto(null, null);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/user/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
