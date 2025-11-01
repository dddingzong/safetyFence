package com.project.safetyFence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.safetyFence.link.domain.Link;
import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.link.dto.LinkRequestDto;
import com.project.safetyFence.mypage.dto.NumberRequestDto;
import com.project.safetyFence.link.LinkRepository;
import com.project.safetyFence.user.UserRepository;
import jakarta.persistence.EntityManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private EntityManager entityManager;

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

    @Test
    @DisplayName("addLinkUser - 링크 추가 성공")
    void addLinkUser_Success() throws Exception {
        // given - 타겟 사용자 생성
        User targetUser = new User("01099999999", "김철수", "password", LocalDate.of(1985, 3, 20), "target-link-code");
        targetUser.updateApiKey("target-api-key-123456789012345678901234");
        userRepository.save(targetUser);

        LinkRequestDto requestDto = new LinkRequestDto("target-link-code", "친구");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        int initialLinkCount = testUser.getLinks().size();

        // when & then
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Link user added successfully"))
                .andDo(print());

        // Verify link was added
        User updatedUser = userRepository.findByNumberWithLinks(TEST_NUMBER);
        assertThat(updatedUser.getLinks()).hasSize(initialLinkCount + 1);

        // Verify link details
        Link addedLink = updatedUser.getLinks().stream()
                .filter(link -> "01099999999".equals(link.getUserNumber()))
                .findFirst()
                .orElseThrow();
        assertThat(addedLink.getRelation()).isEqualTo("친구");
    }

    @Test
    @DisplayName("addLinkUser - 링크 추가 후 DB 검증")
    void addLinkUser_VerifyDatabasePersistence() throws Exception {
        // given
        User targetUser = new User("01088888888", "이영희", "password", LocalDate.of(1992, 7, 10), "target2-link-code");
        userRepository.save(targetUser);

        LinkRequestDto requestDto = new LinkRequestDto("target2-link-code", "가족");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // then - 데이터베이스에서 직접 확인
        User user = userRepository.findByNumberWithLinks(TEST_NUMBER);
        assertThat(user.getLinks()).anyMatch(link ->
                "01088888888".equals(link.getUserNumber()) &&
                        "가족".equals(link.getRelation())
        );
    }

    @Test
    @DisplayName("addLinkUser - API Key 없이 요청 시 401 에러")
    void addLinkUser_NoApiKey_Unauthorized() throws Exception {
        // given
        LinkRequestDto requestDto = new LinkRequestDto("some-link-code", "친구");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/link/addUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("addLinkUser - 잘못된 API Key로 요청 시 401 에러")
    void addLinkUser_InvalidApiKey_Unauthorized() throws Exception {
        // given
        LinkRequestDto requestDto = new LinkRequestDto("some-link-code", "친구");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", "invalid-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("addLinkUser - 여러 링크 추가")
    void addLinkUser_MultipleLinks() throws Exception {
        // given
        User target1 = new User("01077777777", "박민수", "password", LocalDate.of(1988, 1, 15), "link-code-1");
        User target2 = new User("01066666666", "정수진", "password", LocalDate.of(1991, 9, 25), "link-code-2");
        userRepository.save(target1);
        userRepository.save(target2);

        // when - add first link
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LinkRequestDto("link-code-1", "동료"))))
                .andExpect(status().isOk());

        // when - add second link
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LinkRequestDto("link-code-2", "친구"))))
                .andExpect(status().isOk());

        // then - verify both links added
        User updatedUser = userRepository.findByNumberWithLinks(TEST_NUMBER);
        assertThat(updatedUser.getLinks()).hasSize(5); // 초기 3개 + 새로 추가한 2개
        assertThat(updatedUser.getLinks()).anyMatch(link -> "01077777777".equals(link.getUserNumber()));
        assertThat(updatedUser.getLinks()).anyMatch(link -> "01066666666".equals(link.getUserNumber()));
    }

    @Test
    @DisplayName("addLinkUser - 존재하지 않는 linkCode로 추가 시 에러")
    void addLinkUser_NonExistentLinkCode_ThrowsException() throws Exception {
        // given
        LinkRequestDto requestDto = new LinkRequestDto("non-existent-code", "친구");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("일치하는 유저 코드가 존재하지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("addLinkUser - 자기 자신 추가 시 에러")
    void addLinkUser_AddSelf_ThrowsException() throws Exception {
        // given - 자신의 linkCode 사용
        LinkRequestDto requestDto = new LinkRequestDto("test-link-code", "나");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("자기 자신을 링크로 추가할 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("addLinkUser - 중복 추가 시 에러")
    void addLinkUser_DuplicateAdd_ThrowsException() throws Exception {
        // given - 타겟 사용자 생성
        User targetUser = new User("01055555555", "최민지", "password", LocalDate.of(1993, 11, 5), "duplicate-test-code");
        userRepository.save(targetUser);

        LinkRequestDto requestDto = new LinkRequestDto("duplicate-test-code", "친구");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when - 첫 번째 추가 (성공)
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // then - 두 번째 추가 시도 (실패)
        mockMvc.perform(post("/link/addUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 링크에 추가된 사용자입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("deleteLinkUser - 링크 삭제 성공")
    void deleteLinkUser_Success() throws Exception {
        // given
        NumberRequestDto requestDto = new NumberRequestDto("01011111111");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        int initialLinkCount = testUser.getLinks().size();

        // when
        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Link user deleted successfully"))
                .andDo(print());

        entityManager.flush();
        entityManager.clear();

        // then - DB 검증
        User updatedUser = userRepository.findByNumberWithLinks(TEST_NUMBER);
        assertThat(updatedUser.getLinks()).hasSize(initialLinkCount - 1);
        assertThat(updatedUser.getLinks()).noneMatch(link -> "01011111111".equals(link.getUserNumber()));
    }

    @Test
    @DisplayName("deleteLinkUser - DB에서 삭제 검증 (orphanRemoval)")
    void deleteLinkUser_VerifyDatabaseDeletion() throws Exception {
        // given
        User user = userRepository.findByNumberWithLinks(TEST_NUMBER);
        Link linkToDelete = user.getLinks().stream()
                .filter(link -> "01022222222".equals(link.getUserNumber()))
                .findFirst()
                .orElseThrow();
        Long linkId = linkToDelete.getId();

        NumberRequestDto requestDto = new NumberRequestDto("01022222222");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // then - DB에서 실제 삭제되었는지 확인
        assertThat(linkRepository.findById(linkId)).isEmpty();
    }

    @Test
    @DisplayName("deleteLinkUser - API Key 없이 요청 시 401 에러")
    void deleteLinkUser_NoApiKey_Unauthorized() throws Exception {
        // given
        NumberRequestDto requestDto = new NumberRequestDto("01011111111");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(delete("/link/deleteUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("deleteLinkUser - 잘못된 API Key로 요청 시 401 에러")
    void deleteLinkUser_InvalidApiKey_Unauthorized() throws Exception {
        // given
        NumberRequestDto requestDto = new NumberRequestDto("01011111111");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", "invalid-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("deleteLinkUser - 존재하지 않는 전화번호로 삭제 시 에러")
    void deleteLinkUser_NonExistentNumber_ThrowsException() throws Exception {
        // given
        NumberRequestDto requestDto = new NumberRequestDto("01099999999");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("링크 삭제 중 문제가 발생했습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("deleteLinkUser - 여러 링크 중 하나만 삭제")
    void deleteLinkUser_DeleteOneOfMultiple() throws Exception {
        // given
        NumberRequestDto requestDto = new NumberRequestDto("01022222222");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // then - 나머지 링크는 유지되는지 확인
        User updatedUser = userRepository.findByNumberWithLinks(TEST_NUMBER);
        assertThat(updatedUser.getLinks()).hasSize(2);
        assertThat(updatedUser.getLinks()).anyMatch(link -> "01011111111".equals(link.getUserNumber()));
        assertThat(updatedUser.getLinks()).anyMatch(link -> "01033333333".equals(link.getUserNumber()));
        assertThat(updatedUser.getLinks()).noneMatch(link -> "01022222222".equals(link.getUserNumber()));
    }

    @Test
    @DisplayName("deleteLinkUser - 모든 링크 삭제")
    void deleteLinkUser_DeleteAllLinks() throws Exception {
        // when - 모든 링크 삭제
        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NumberRequestDto("01011111111"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NumberRequestDto("01022222222"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/link/deleteUser")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NumberRequestDto("01033333333"))))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // then - 모든 링크가 삭제되었는지 확인
        User updatedUser = userRepository.findByNumberWithLinks(TEST_NUMBER);
        assertThat(updatedUser.getLinks()).isEmpty();
    }
}
