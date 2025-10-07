package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.ServiceCode;
import com.project.paypass_renewal.domain.User;
import com.project.paypass_renewal.support.UserTestUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("리포지토리_신규유저_저장_테스트")
    void saveNewUserTest(){
        // given
        User user = UserTestUtils.createDummyUser();

        // when
        userRepository.save(user);

        // then
        Long id = userRepository.findById(user.getId()).get().getId();

        assertThat(id).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("유저링크_존재유무_테스트")
    void checkExistsLinkCode(){
        // given
        User user = UserTestUtils.createDummyUser();

        // when
        boolean firstCheck = userRepository.existsByLinkCode(user.getLinkCode());

        userRepository.save(user);
        boolean secondCheck = userRepository.existsByLinkCode(user.getLinkCode());

        // then
        assertThat(firstCheck).isFalse();
        assertThat(secondCheck).isTrue();
    }

    @Test
    @DisplayName("유저_번호_존재유무_테스트")
    void checkExistsNumberTest() {
        // given
        User user = UserTestUtils.createDummyUser();

        // when
        boolean firstCheck = userRepository.existsByNumber(user.getNumber());

        userRepository.save(user);
        boolean secondCheck = userRepository.existsByNumber(user.getNumber());

        // then
        assertThat(firstCheck).isFalse();
        assertThat(secondCheck).isTrue();
    }

    @Test
    @DisplayName("이용자_리스트_조회_테스트")
    void findUserListTest(){
        // given
        User userOne = new User("test1", "abc123", LocalDate.of(2000, 01, 01), "01011111111", "12352", null, "AG1DE1", ServiceCode.CARE_SERVICE);
        User userTwo = new User("test2", "abc123", LocalDate.of(2000, 01, 02), "01022222222", "12152", null, "AG1DEV", ServiceCode.CARE_SERVICE);
        User userThree = new User("test3", "abc123", LocalDate.of(2000, 01, 03), "01033333333", "12552", null, "AG1DEF", ServiceCode.CARE_SERVICE);
        User userFour = new User("test4", "abc123", LocalDate.of(2000, 01, 04), "01044444444", "12544", null, "AG144F", ServiceCode.CARE_SERVICE);

        userRepository.save(userOne);
        userRepository.save(userTwo);
        userRepository.save(userThree);
        userRepository.save(userFour);

        List<String> userNumbers = List.of("01011111111", "01022222222", "01033333333");
        // when
        List<User> UserList = userRepository.findByNumberIn(userNumbers);

        // then
        assertThat(UserList).hasSize(3);
        assertThat(UserList.get(0).getNumber()).isEqualTo("01011111111");
        assertThat(UserList.get(1).getNumber()).isEqualTo("01022222222");
        assertThat(UserList.get(2).getNumber()).isEqualTo("01033333333");
    }

    @Test
    @DisplayName("유저_번호로_유저_조회_테스트")
    void findByUserTest() {
        // given
        User user = UserTestUtils.createDummyUser();

        // when
        userRepository.save(user);
        User foundUser = userRepository.findByNumber(user.getNumber());

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getNumber()).isEqualTo(user.getNumber());
    }


}
