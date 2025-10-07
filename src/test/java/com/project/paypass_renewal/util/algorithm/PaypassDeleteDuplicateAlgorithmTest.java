package com.project.paypass_renewal.util.algorithm;

import com.project.paypass_renewal.domain.PaypassGeofence;
import com.project.paypass_renewal.util.algorithm.constants.AlgorithmTestConstants;
import com.project.paypass_renewal.util.algorithm.constants.AverageTimeTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PaypassDeleteDuplicateAlgorithmTest {

    @Autowired
    PaypassDeleteDuplicateAlgorithm paypassDeleteDuplicateAlgorithm;


    @Test
    @DisplayName("PaypassDeleteDuplicateAlgorithm_테스트_기본")
    void paypassDeleteDuplicateAlgorithmBasicTest() {
        // given
        Map<String, List<Long>> averageTimeGeofenceMap = AverageTimeTestConstants.SEQUENCE_BASIC_TEST_LIST;
        List<PaypassGeofence> basicTestList = AlgorithmTestConstants.BASIC_TEST_LIST;

        // when
        Map<List<PaypassGeofence>, List<String>> deleteDuplicateGeofenceMap = paypassDeleteDuplicateAlgorithm.algorithmStart(averageTimeGeofenceMap, basicTestList);

        // then
        assertThat(deleteDuplicateGeofenceMap).isNotNull();
        assertThat(deleteDuplicateGeofenceMap.size()).isEqualTo(1);
        assertThat(deleteDuplicateGeofenceMap.get(basicTestList).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("PaypassDeleteDuplicateAlgorithm_테스트_건너편_정류장")
    void paypassDeleteDuplicateAlgorithmOppositeTest() {
        // given
        Map<String, List<Long>> averageTimeGeofenceMap = AverageTimeTestConstants.SEQUENCE_OPPOSITE_TEST_LIST;
        List<PaypassGeofence> oppositeTestList = AlgorithmTestConstants.OPPOSITE_TEST_LIST;

        List<PaypassGeofence> oppositeTestListResultOne = List.of(
                oppositeTestList.get(0), oppositeTestList.get(2), oppositeTestList.get(3)
        );

        List<PaypassGeofence> oppositeTestListResultTwo = List.of(
                oppositeTestList.get(4), oppositeTestList.get(5)
        );

        // when
        Map<List<PaypassGeofence>, List<String>> deleteDuplicateGeofenceMap = paypassDeleteDuplicateAlgorithm.algorithmStart(averageTimeGeofenceMap, oppositeTestList);

        // then
        assertThat(deleteDuplicateGeofenceMap).isNotNull();
        assertThat(deleteDuplicateGeofenceMap.size()).isEqualTo(2);
        assertThat(deleteDuplicateGeofenceMap.get(oppositeTestListResultOne).size()).isEqualTo(2);
        assertThat(deleteDuplicateGeofenceMap.get(oppositeTestListResultTwo).size()).isEqualTo(3);
    }

    @Test
    @DisplayName("PaypassDeleteDuplicateAlgorithm_테스트_건너편_정류장_다수")
    void paypassDeleteDuplicateAlgorithmManyOppositeTest() {
        // given
        Map<String, List<Long>> averageTimeGeofenceMap = AverageTimeTestConstants.SEQUENCE_MANY_OPPOSITE_TEST_LIST;
        List<PaypassGeofence> manyOppositeTestList = AlgorithmTestConstants.MANY_OPPOSITE_TEST_LIST;

        List<PaypassGeofence> manyOppositeTestListResult = List.of(
                manyOppositeTestList.get(0), manyOppositeTestList.get(2), manyOppositeTestList.get(5)
        );

        // when
        Map<List<PaypassGeofence>, List<String>> deleteDuplicateGeofenceMap = paypassDeleteDuplicateAlgorithm.algorithmStart(averageTimeGeofenceMap, manyOppositeTestList);

        // then
        assertThat(deleteDuplicateGeofenceMap).isNotNull();
        assertThat(deleteDuplicateGeofenceMap.size()).isEqualTo(1);
        assertThat(deleteDuplicateGeofenceMap.get(manyOppositeTestListResult).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("PaypassDeleteDuplicateAlgorithm_테스트_혼합_정류장")
    void paypassDeleteDuplicateAlgorithmMixedTest() {
        // given
        Map<String, List<Long>> averageTimeGeofenceMap = AverageTimeTestConstants.SEQUENCE_MIXED_TEST_LIST;
        List<PaypassGeofence> mixedTestList = AlgorithmTestConstants.MIXED_TEST_LIST;

        List<PaypassGeofence> mixedTestListResultOne = List.of(
                mixedTestList.get(1), mixedTestList.get(3), mixedTestList.get(4)
        );

        List<PaypassGeofence> mixedTestListResultTwo = List.of(
                mixedTestList.get(5), mixedTestList.get(6)
        );

        List<PaypassGeofence> mixedTestListResultThree = List.of(
                mixedTestList.get(7), mixedTestList.get(8)
        );

        // when
        Map<List<PaypassGeofence>, List<String>> deleteDuplicateGeofenceMap = paypassDeleteDuplicateAlgorithm.algorithmStart(averageTimeGeofenceMap, mixedTestList);

        // then
        assertThat(deleteDuplicateGeofenceMap).isNotNull();
        assertThat(deleteDuplicateGeofenceMap.size()).isEqualTo(3);
        assertThat(deleteDuplicateGeofenceMap.get(mixedTestListResultOne).size()).isEqualTo(3);
        assertThat(deleteDuplicateGeofenceMap.get(mixedTestListResultTwo).size()).isEqualTo(2);
        assertThat(deleteDuplicateGeofenceMap.get(mixedTestListResultThree).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("PaypassDeleteDuplicateAlgorithm_테스트_테스트_복수정류장_혼합")
    void paypassDeleteDuplicateAlgorithmDoubleTest() {
        // given
        Map<String, List<Long>> averageTimeGeofenceMap = AverageTimeTestConstants.SEQUENCE_DOUBLE_TEST_LIST;
        List<PaypassGeofence> doubleTestList = AlgorithmTestConstants.DOUBLE_TEST_LIST;

        List<PaypassGeofence> doubleTestListResultOne = List.of(
                doubleTestList.get(0), doubleTestList.get(1), doubleTestList.get(2), doubleTestList.get(3), doubleTestList.get(4)
        );

        List<PaypassGeofence> doubleTestListResultTwo = List.of(
                doubleTestList.get(5), doubleTestList.get(6), doubleTestList.get(7), doubleTestList.get(8)
        );

        // when
        Map<List<PaypassGeofence>, List<String>> deleteDuplicateGeofenceMap = paypassDeleteDuplicateAlgorithm.algorithmStart(averageTimeGeofenceMap, doubleTestList);

        // then
        assertThat(deleteDuplicateGeofenceMap).isNotNull();
        assertThat(deleteDuplicateGeofenceMap.size()).isEqualTo(2);
        assertThat(deleteDuplicateGeofenceMap.get(doubleTestListResultOne).size()).isEqualTo(2);
        assertThat(deleteDuplicateGeofenceMap.get(doubleTestListResultTwo).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("PaypassDeleteDuplicateAlgorithm_테스트_평균_시간_알고리즘_불만족")
    void paypassDeleteDuplicateAlgorithmAverageTimeUnsatisfiedTest() {
        // given
        Map<String, List<Long>> averageTimeGeofenceMap = AverageTimeTestConstants.TIME_NOT_SATISFY_TEST_LIST;
        List<PaypassGeofence> unsatisfiedTestList = AlgorithmTestConstants.TIME_NOT_SATISFY_TEST_LIST;

        List<PaypassGeofence> testListResult = List.of(
                unsatisfiedTestList.get(0), unsatisfiedTestList.get(1)
        );

        // when
        Map<List<PaypassGeofence>, List<String>> deleteDuplicateGeofenceMap = paypassDeleteDuplicateAlgorithm.algorithmStart(averageTimeGeofenceMap, unsatisfiedTestList);

        // then
        assertThat(deleteDuplicateGeofenceMap).isNotNull();
        assertThat(deleteDuplicateGeofenceMap.size()).isEqualTo(1);
        assertThat(deleteDuplicateGeofenceMap.get(testListResult).size()).isEqualTo(2);
    }



}
