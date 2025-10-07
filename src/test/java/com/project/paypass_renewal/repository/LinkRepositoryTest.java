package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.Link;
import com.project.paypass_renewal.domain.dto.request.LinkRequestDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class LinkRepositoryTest {

    @Autowired
    LinkRepository linkRepository;

    @Test
    @DisplayName("링크_저장_테스트")
    void saveNewLinkTest(){
        // given
        Link link = new Link("01012345678", "01011111111", "어머님");

        // when
        Link savedLink = linkRepository.save(link);

        // then
        assertThat(savedLink.getId()).isEqualTo(link.getId());
        assertThat(savedLink.getSupporterNumber()).isEqualTo(link.getSupporterNumber());
        assertThat(savedLink.getUserNumber()).isEqualTo(link.getUserNumber());
    }

    @Test
    @DisplayName("이용자_번호_조회_테스트")
    void findUserNumberBySupporterNumberTest(){
        // given
        String supporterNumber = "01012345678";
        String supporterNumberDummy = "01012341234";

        String userNumberOne = "01011111111";
        String userNumberTwo = "01022222222";

        String relation = "어머님";

        Link linkOne = new Link(supporterNumber, userNumberOne, relation);
        Link linkTwo = new Link(supporterNumber, userNumberTwo, relation);
        Link linkDummy = new Link(supporterNumberDummy, userNumberOne, relation);

        linkRepository.save(linkOne);
        linkRepository.save(linkTwo);
        linkRepository.save(linkDummy);

        // when
        List<String> userNumbers = linkRepository.findUserNumbersBySupporterNumber(supporterNumber);

        // then
        assertThat(userNumbers).hasSize(2);
        assertThat(userNumbers).containsExactlyInAnyOrder(userNumberOne, userNumberTwo);
    }

    @Test
    @DisplayName("링크_중복_검사_테스트")
    void checkDuplicateLinkTest() {
        // given
        String supporterNumber = "01012345678";
        String userNumber = "01011111111";
        String relation = "어머님";

        Link link = new Link(supporterNumber, userNumber, relation);

        // when
        boolean firstCheck = linkRepository.existsBySupporterNumberAndUserNumber(supporterNumber, userNumber);
        linkRepository.save(link);
        boolean secondCheck = linkRepository.existsBySupporterNumberAndUserNumber(supporterNumber, userNumber);

        // then
        assertThat(firstCheck).isFalse();
        assertThat(secondCheck).isTrue();
    }

    @Test
    @DisplayName("링크_삭제_테스트")
    void deleteLinkTest() {
        // given
        Link dummyLinkOne = new Link("01012345678", "01011111111", "어머님");
        Link dummyLinkTwo = new Link("01012345678", "01022222222", "어머님");

        linkRepository.save(dummyLinkOne);
        linkRepository.save(dummyLinkTwo);

        // when
        int deleteCount = linkRepository.deleteBySupporterNumberAndUserNumber(dummyLinkOne.getSupporterNumber(), dummyLinkOne.getUserNumber());

        List<String> userNumbers = linkRepository.findUserNumbersBySupporterNumber(dummyLinkOne.getSupporterNumber());
        // then

        assertThat(deleteCount).isEqualTo(1);
        assertThat(userNumbers).doesNotContain(dummyLinkOne.getUserNumber());
    }


}
