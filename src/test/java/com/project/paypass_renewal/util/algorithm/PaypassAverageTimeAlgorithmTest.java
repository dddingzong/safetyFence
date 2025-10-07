package com.project.paypass_renewal.util.algorithm;

import com.project.paypass_renewal.domain.PaypassGeofence;
import com.project.paypass_renewal.repository.BusTimeRepository;
import com.project.paypass_renewal.util.algorithm.constants.AlgorithmTestConstants;
import com.project.paypass_renewal.util.algorithm.constants.SequenceTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PaypassAverageTimeAlgorithmTest {

    @Autowired
    private PaypassAverageTimeAlgorithm paypassAverageTimeAlgorithm;

    @Autowired
    BusTimeRepository busTimeRepository;

    @Test
    @DisplayName("paypassAverageTimeAlgorithm_테스트_기본")
    void paypassAverageTimeAlgorithmBasicTest() {
        // given
        Map<String, List<Long>> sequenceGeofenceMap = SequenceTestConstants.SEQUENCE_BASIC_TEST_LIST;
        List<PaypassGeofence> basicTestList = AlgorithmTestConstants.BASIC_TEST_LIST;

        // when
        Map<String, List<Long>> averageTimeGeofenceMap = paypassAverageTimeAlgorithm.algorithmStart(sequenceGeofenceMap, basicTestList);

        // then
        assertThat(averageTimeGeofenceMap).isNotNull();
        assertThat(averageTimeGeofenceMap.size()).isEqualTo(SequenceTestConstants.SEQUENCE_BASIC_TEST_LIST.size());
    }

    @Test
    @DisplayName("paypassAverageTimeAlgorithm_테스트_건너편_정류장")
    void paypassAverageTimeAlgorithmOppositeTest() {
        // given
        Map<String, List<Long>> sequenceGeofenceMap = SequenceTestConstants.SEQUENCE_OPPOSITE_TEST_LIST;
        List<PaypassGeofence> oppositeTestList = AlgorithmTestConstants.OPPOSITE_TEST_LIST;

        // when
        Map<String, List<Long>> averageTimeGeofenceMap = paypassAverageTimeAlgorithm.algorithmStart(sequenceGeofenceMap, oppositeTestList);

        // then
        assertThat(averageTimeGeofenceMap).isNotNull();
        assertThat(averageTimeGeofenceMap.size()).isEqualTo(SequenceTestConstants.SEQUENCE_OPPOSITE_TEST_LIST.size());
    }

    @Test
    @DisplayName("paypassAverageTimeAlgorithm_테스트_건너편_정류장_다수")
    void paypassAverageTimeAlgorithmManyOppositeTest() {
        // given
        Map<String, List<Long>> sequenceGeofenceMap = SequenceTestConstants.SEQUENCE_MANY_OPPOSITE_TEST_LIST;
        List<PaypassGeofence> manyOppositeTestList = AlgorithmTestConstants.MANY_OPPOSITE_TEST_LIST;

        // when
        Map<String, List<Long>> averageTimeGeofenceMap = paypassAverageTimeAlgorithm.algorithmStart(sequenceGeofenceMap, manyOppositeTestList);

        // then
        assertThat(averageTimeGeofenceMap).isNotNull();
        assertThat(averageTimeGeofenceMap.size()).isEqualTo(SequenceTestConstants.SEQUENCE_MANY_OPPOSITE_TEST_LIST.size());
    }

    @Test
    @DisplayName("paypassAverageTimeAlgorithm_테스트_혼합")
    void paypassAverageTimeAlgorithmMixedTest() {
        // given
        Map<String, List<Long>> sequenceGeofenceMap = SequenceTestConstants.SEQUENCE_MIXED_TEST_LIST;
        List<PaypassGeofence> mixedTestList = AlgorithmTestConstants.MIXED_TEST_LIST;

        // when
        Map<String, List<Long>> averageTimeGeofenceMap = paypassAverageTimeAlgorithm.algorithmStart(sequenceGeofenceMap, mixedTestList);

        // then
        assertThat(averageTimeGeofenceMap).isNotNull();
        assertThat(averageTimeGeofenceMap.size()).isEqualTo(SequenceTestConstants.SEQUENCE_MIXED_TEST_LIST.size());
    }

    @Test
    @DisplayName("paypassAverageTimeAlgorithm_테스트_복수정류장_혼합")
    void paypassAverageTimeAlgorithmDoubleTest() {
        // given
        Map<String, List<Long>> sequenceGeofenceMap = SequenceTestConstants.SEQUENCE_DOUBLE_TEST_LIST;
        List<PaypassGeofence> doubleTestList = AlgorithmTestConstants.DOUBLE_TEST_LIST;

        // when
        Map<String, List<Long>> averageTimeGeofenceMap = paypassAverageTimeAlgorithm.algorithmStart(sequenceGeofenceMap, doubleTestList);

        // then
        assertThat(averageTimeGeofenceMap).isNotNull();
        assertThat(averageTimeGeofenceMap.size()).isEqualTo(SequenceTestConstants.SEQUENCE_DOUBLE_TEST_LIST.size());
    }

    @Test
    @DisplayName("paypassAverageTimeAlgorithm_테스트_불만족")
    void paypassAverageTimeAlgorithmDissatisfactionTest() {
        // given
        Map<String, List<Long>> sequenceGeofenceMap = SequenceTestConstants.TIME_NOT_SATISFY_TEST_LIST;
        List<PaypassGeofence> dissatisfactionTestList = AlgorithmTestConstants.TIME_NOT_SATISFY_TEST_LIST;

        // when
        Map<String, List<Long>> averageTimeGeofenceMap = paypassAverageTimeAlgorithm.algorithmStart(sequenceGeofenceMap, dissatisfactionTestList);

        // then
        assertThat(averageTimeGeofenceMap).isNotNull();
        assertThat(averageTimeGeofenceMap.get("100100014_1")).isEqualTo(List.of(1L, 2L));
        assertThat(averageTimeGeofenceMap.get("100100017_1")).isEqualTo(List.of(1L, 2L));
    }

}
