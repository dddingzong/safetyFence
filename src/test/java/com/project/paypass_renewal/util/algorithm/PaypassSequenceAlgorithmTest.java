package com.project.paypass_renewal.util.algorithm;


import com.project.paypass_renewal.domain.PaypassGeofence;
import com.project.paypass_renewal.util.algorithm.constants.AlgorithmTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PaypassSequenceAlgorithmTest {

    @Autowired
    private PaypassSequenceAlgorithm paypassSequenceAlgorithm;

    @Test
    @DisplayName("PaypassSequenceAlgorithm_테스트_간단한_순차")
    void paypassSequenceAlgorithmBasicTest() {
        // given
        List<PaypassGeofence> paypassGeofenceList = AlgorithmTestConstants.BASIC_TEST_LIST;
        // when
        Map<String, List<Long>> sequenceGeofenceMap = paypassSequenceAlgorithm.algorithmStart(paypassGeofenceList);

        // then
        assertThat(sequenceGeofenceMap).isNotEmpty();
        assertThat(sequenceGeofenceMap.get("100100014_1")).isEqualTo(List.of(1L,2L,3L));
        assertThat(sequenceGeofenceMap.get("100100017_1")).isEqualTo(List.of(1L,2L,3L));
        assertThat(sequenceGeofenceMap.get("100100023_1")).isEqualTo(List.of(2L,3L));
    }

    @Test
    @DisplayName("PaypassSequenceAlgorithm_테스트_건너편_정류장")
    void paypassSequenceAlgorithmOppositeTest() {
        // given
        List<PaypassGeofence> paypassGeofenceList = AlgorithmTestConstants.OPPOSITE_TEST_LIST;
        // when
        Map<String, List<Long>> sequenceGeofenceMap = paypassSequenceAlgorithm.algorithmStart(paypassGeofenceList);

        // then
        assertThat(sequenceGeofenceMap).isNotEmpty();
        assertThat(sequenceGeofenceMap.get("100100014_1")).isEqualTo(List.of(1L,2L,3L));
        assertThat(sequenceGeofenceMap.get("100100014_2")).isEqualTo(List.of(5L,6L));
        assertThat(sequenceGeofenceMap.get("100100017_1")).isEqualTo(List.of(1L,2L,3L));
        assertThat(sequenceGeofenceMap.get("100100017_2")).isEqualTo(List.of(5L, 6L));
        assertThat(sequenceGeofenceMap.get("100100023_1")).isEqualTo(List.of(2L,3L));
        assertThat(sequenceGeofenceMap.get("100100023_2")).isEqualTo(List.of(5L, 6L));
    }

    @Test
    @DisplayName("PaypassSequenceAlgorithm_테스트_역순")
    void paypassSequenceAlgorithmReverseTest() {
        // given

        List<PaypassGeofence> paypassGeofenceList = AlgorithmTestConstants.REVERSE_TEST_LIST;
        // when
        Map<String, List<Long>> sequenceGeofenceMap = paypassSequenceAlgorithm.algorithmStart(paypassGeofenceList);

        // then
        assertThat(sequenceGeofenceMap).isEmpty();
    }

    @Test
    @DisplayName("PaypassSequenceAlgorithm_테스트_건너편_정류장_다수")
    void paypassSequenceAlgorithmManyOppositeStationTest() {
        // given

        List<PaypassGeofence> paypassGeofenceList = AlgorithmTestConstants.MANY_OPPOSITE_TEST_LIST;
        // when
        Map<String, List<Long>> sequenceGeofenceMap = paypassSequenceAlgorithm.algorithmStart(paypassGeofenceList);

        // then
        assertThat(sequenceGeofenceMap).isNotEmpty();
        assertThat(sequenceGeofenceMap.get("100100014_1")).isEqualTo(List.of(1L, 2L, 3L));
        assertThat(sequenceGeofenceMap.get("100100017_1")).isEqualTo(List.of(1L, 2L, 3L));
        assertThat(sequenceGeofenceMap.get("100100023_1")).isEqualTo(List.of(2L, 3L));
    }

    @Test
    @DisplayName("PaypassSequenceAlgorithm_테스트_복잡한_혼합")
    void paypassSequenceAlgorithmMixedTest() {

        List<PaypassGeofence> list = AlgorithmTestConstants.MIXED_TEST_LIST;
        Map<String, List<Long>> result = paypassSequenceAlgorithm.algorithmStart(list);

        assertThat(result.get("100100014_1")).isEqualTo(List.of(3L, 4L, 5L));
        assertThat(result.get("100100014_2")).isEqualTo(List.of(10L, 11L));
        assertThat(result.get("100100014_3")).isEqualTo(List.of(13L, 14L));
        assertThat(result.get("100100017_1")).isEqualTo(List.of(3L, 4L, 5L));
        assertThat(result.get("100100023_1")).isEqualTo(List.of(3L, 4L, 5L));
        assertThat(result.get("100100023_2")).isEqualTo(List.of(10L, 11L));
        assertThat(result.get("100100023_3")).isEqualTo(List.of(13L, 14L));
    }

    @Test
    @DisplayName("PaypassSequenceAlgorithm_테스트_복수정류장_혼합")
    void paypassSequenceAlgorithmDoubleTest() {

        List<PaypassGeofence> list = AlgorithmTestConstants.DOUBLE_TEST_LIST;

        Map<String, List<Long>> result = paypassSequenceAlgorithm.algorithmStart(list);

        assertThat(result.get("100100148_1")).isEqualTo(List.of(10L, 11L, 12L, 13L, 14L));
        assertThat(result.get("100100148_2")).isEqualTo(List.of(17L, 18L, 19L, 20L));
        assertThat(result.get("100100150_1")).isEqualTo(List.of(10L, 11L, 12L));
        assertThat(result.get("100100151_1")).isEqualTo(List.of(10L, 11L, 12L));
        assertThat(result.get("100100165_1")).isEqualTo(List.of(38L, 39L, 40L, 41L, 42L));
        assertThat(result.get("100100179_1")).isEqualTo(List.of(49L, 50L));
        assertThat(result.get("100100192_1")).isEqualTo(List.of(30L, 31L, 32L));
        assertThat(result.get("100100193_1")).isEqualTo(List.of(24L, 25L, 26L));
        assertThat(result.get("100100425_1")).isEqualTo(List.of(29L, 30L, 31L));

    }



}
