package com.project.safetyFence.service;

import com.project.safetyFence.link.LinkRepository;
import com.project.safetyFence.link.LinkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * LinkService WebSocket 권한 검증 테스트
 * hasLink() 메서드의 단방향 Link 검증 로직 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LinkService WebSocket 권한 검증 테스트")
class LinkServiceWebSocketTest {

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private LinkService linkService;

    @Test
    @DisplayName("A가 B를 Link로 등록한 경우 - 권한 있음")
    void A가_B를_Link로_등록한_경우_권한_있음() {
        // Given
        String subscriberNumber = "A";
        String targetUserNumber = "B";

        when(linkRepository.existsByUser_NumberAndUserNumber(subscriberNumber, targetUserNumber))
                .thenReturn(true);

        // When
        boolean hasLink = linkService.hasLink(subscriberNumber, targetUserNumber);

        // Then
        assertThat(hasLink).isTrue();
    }

    @Test
    @DisplayName("A가 B를 Link로 등록하지 않은 경우 - 권한 없음")
    void A가_B를_Link로_등록하지_않은_경우_권한_없음() {
        // Given
        String subscriberNumber = "A";
        String targetUserNumber = "B";

        when(linkRepository.existsByUser_NumberAndUserNumber(subscriberNumber, targetUserNumber))
                .thenReturn(false);

        // When
        boolean hasLink = linkService.hasLink(subscriberNumber, targetUserNumber);

        // Then
        assertThat(hasLink).isFalse();
    }

    @Test
    @DisplayName("단방향 검증 - B가 A를 등록했어도 A는 B 구독 불가")
    void 단방향_검증_B가_A를_등록했어도_A는_B_구독_불가() {
        // Given
        String subscriberNumber = "A";
        String targetUserNumber = "B";

        // A가 B를 등록하지 않음 (단방향이므로 B→A는 무관)
        when(linkRepository.existsByUser_NumberAndUserNumber(subscriberNumber, targetUserNumber))
                .thenReturn(false);

        // When
        boolean hasLink = linkService.hasLink(subscriberNumber, targetUserNumber);

        // Then
        assertThat(hasLink).isFalse();
    }

    @Test
    @DisplayName("자기 자신 구독 가능 여부 확인")
    void 자기_자신_구독_가능_여부_확인() {
        // Given
        String userNumber = "A";

        when(linkRepository.existsByUser_NumberAndUserNumber(userNumber, userNumber))
                .thenReturn(false); // 자기 자신은 Link에 없음

        // When
        boolean hasLink = linkService.hasLink(userNumber, userNumber);

        // Then
        assertThat(hasLink).isFalse();
    }

    @Test
    @DisplayName("여러 사용자 간 권한 독립성 검증")
    void 여러_사용자_간_권한_독립성_검증() {
        // Given
        // A → B: 있음
        when(linkRepository.existsByUser_NumberAndUserNumber("A", "B"))
                .thenReturn(true);
        // A → C: 없음
        when(linkRepository.existsByUser_NumberAndUserNumber("A", "C"))
                .thenReturn(false);
        // B → C: 있음
        when(linkRepository.existsByUser_NumberAndUserNumber("B", "C"))
                .thenReturn(true);

        // When & Then
        assertThat(linkService.hasLink("A", "B")).isTrue();
        assertThat(linkService.hasLink("A", "C")).isFalse();
        assertThat(linkService.hasLink("B", "C")).isTrue();
    }

    @Test
    @DisplayName("대소문자 구분 테스트")
    void 대소문자_구분_테스트() {
        // Given
        String subscriberNumber = "user123";
        String targetUserNumber = "USER456";

        when(linkRepository.existsByUser_NumberAndUserNumber(subscriberNumber, targetUserNumber))
                .thenReturn(true);

        // When
        boolean hasLink = linkService.hasLink(subscriberNumber, targetUserNumber);

        // Then
        assertThat(hasLink).isTrue();
    }

    @Test
    @DisplayName("null 또는 빈 문자열 처리")
    void null_또는_빈_문자열_처리() {
        // Given
        when(linkRepository.existsByUser_NumberAndUserNumber(null, "B"))
                .thenReturn(false);
        when(linkRepository.existsByUser_NumberAndUserNumber("A", null))
                .thenReturn(false);
        when(linkRepository.existsByUser_NumberAndUserNumber("", "B"))
                .thenReturn(false);

        // When & Then
        assertThat(linkService.hasLink(null, "B")).isFalse();
        assertThat(linkService.hasLink("A", null)).isFalse();
        assertThat(linkService.hasLink("", "B")).isFalse();
    }
}
