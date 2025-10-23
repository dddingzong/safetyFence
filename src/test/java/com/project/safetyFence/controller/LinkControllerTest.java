package com.project.safetyFence.controller;

import com.project.safetyFence.domain.Link;
import com.project.safetyFence.domain.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String testApiKey;
    private static final String TEST_NUMBER = "01012345678";

    @BeforeEach
    void setUp() {
        // Test user with API Key
        testUser = new User(TEST_NUMBER, "홍길동", "password123", LocalDate.of(1990, 1, 1), "test-link-code");
        testApiKey = "test-api-key-12345678901234567890123456789012";
        testUser.updateApiKey(testApiKey);

        // Create links
        Link link1 = new Link(testUser, "01011111111", "아들");
        Link link2 = new Link(testUser, "01022222222", "딸");
        Link link3 = new Link(testUser, "01033333333", "친구");

        testUser.addLink(link1);
        testUser.addLink(link2);
        testUser.addLink(link3);

        // Save user (cascade will save links)
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("getUserLink - 링크 목록 조회 성공")
    void getUserLink_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/link/list")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].userNumber").exists())
                .andExpect(jsonPath("$[0].relation").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("getUserLink - 링크 상세 정보 검증")
    void getUserLink_VerifyDetails() throws Exception {
        // when & then
        mockMvc.perform(get("/link/list")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userNumber == '01011111111')].relation").value("아들"))
                .andExpect(jsonPath("$[?(@.userNumber == '01022222222')].relation").value("딸"))
                .andExpect(jsonPath("$[?(@.userNumber == '01033333333')].relation").value("친구"))
                .andDo(print());
    }

    @Test
    @DisplayName("getUserLink - API Key 없이 요청 시 401 에러")
    void getUserLink_NoApiKey_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/link/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("getUserLink - 잘못된 API Key로 요청 시 401 에러")
    void getUserLink_InvalidApiKey_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/link/list")
                        .header("X-API-Key", "invalid-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("getUserLink - 링크가 없을 때 빈 배열 반환")
    void getUserLink_NoLinks_ReturnsEmptyArray() throws Exception {
        // given - 링크가 없는 새로운 사용자
        User userWithoutLinks = new User("01099999999", "김철수", "password", LocalDate.of(1995, 5, 5), "no-links");
        String apiKeyNoLinks = "no-links-api-key-123456789012345678901234";
        userWithoutLinks.updateApiKey(apiKeyNoLinks);
        userRepository.save(userWithoutLinks);

        // when & then
        mockMvc.perform(get("/link/list")
                        .header("X-API-Key", apiKeyNoLinks)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("getUserLink - 모든 링크가 응답에 포함되는지 확인")
    void getUserLink_AllLinksIncluded() throws Exception {
        // when & then
        mockMvc.perform(get("/link/list")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userNumber").isArray())
                .andExpect(jsonPath("$[?(@.userNumber == '01011111111')]").exists())
                .andExpect(jsonPath("$[?(@.userNumber == '01022222222')]").exists())
                .andExpect(jsonPath("$[?(@.userNumber == '01033333333')]").exists())
                .andDo(print());
    }
}
