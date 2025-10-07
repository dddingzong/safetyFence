package com.project.paypass_renewal.util.algorithm.constants;

import java.util.List;
import java.util.Map;

public class SequenceTestConstants {

    public static final Map<String, List<Long>> SEQUENCE_BASIC_TEST_LIST = Map.of(
            "100100014_1", List.of(1L, 2L, 3L),
            "100100017_1", List.of(1L, 2L, 3L),
            "100100023_1", List.of(2L, 3L)
    );

    public static final Map<String, List<Long>> SEQUENCE_OPPOSITE_TEST_LIST = Map.of(
            "100100014_1", List.of(1L, 2L, 3L),
            "100100014_2", List.of(5L, 6L),
            "100100017_1", List.of(1L, 2L, 3L),
            "100100017_2", List.of(5L, 6L),
            "100100023_1", List.of(2L, 3L),
            "100100023_2", List.of(5L, 6L)
    );

    public static final Map<String, List<Long>> SEQUENCE_MANY_OPPOSITE_TEST_LIST = Map.of(
            "100100014_1", List.of(1L, 2L, 3L),
            "100100017_1", List.of(1L, 2L, 3L),
            "100100023_1", List.of(2L, 3L)
    );

    public static final Map<String, List<Long>> SEQUENCE_MIXED_TEST_LIST = Map.of(
            "100100014_1", List.of(3L, 4L, 5L),
            "100100014_2", List.of(10L, 11L),
            "100100014_3", List.of(13L, 14L),
            "100100017_1", List.of(3L, 4L, 5L),
            "100100023_1", List.of(3L, 4L, 5L),
            "100100023_2", List.of(10L, 11L),
            "100100023_3", List.of(13L, 14L)
    );

    public static final Map<String, List<Long>> SEQUENCE_DOUBLE_TEST_LIST = Map.of(
            "100100148_1", List.of(10L, 11L, 12L, 13L, 14L),
            "100100148_2", List.of(17L, 18L, 19L, 20L),
            "100100150_1", List.of(10L, 11L, 12L),
            "100100151_1", List.of(10L, 11L, 12L),
            "100100165_1", List.of(38L, 39L, 40L, 41L, 42L),
            "100100179_1", List.of(49L, 50L),
            "100100192_1", List.of(30L, 31L, 32L),
            "100100193_1", List.of(24L, 25L, 26L),
            "100100425_1", List.of(29L, 30L, 31L)
    );

    public static final Map<String, List<Long>> TIME_NOT_SATISFY_TEST_LIST = Map.of(
            "100100014_1", List.of(1L, 2L),
            "100100017_1", List.of(1L, 2L)
    );


}
