package com.project.paypass_renewal.util;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserUtilsTest {

    @Test
    @DisplayName("유저링크_난수생성_테스트")
    void generateLinkCodeTest() {
        // given & when
        String linkCode = UserUtils.generateLinkCode();

        // then
        Assertions.assertThat(linkCode).hasSize(6);
    }
}
