package com.project.safetyFence.util.algorithm;

import com.project.safetyFence.domain.PaypassGeofence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaypassSequenceAlgorithm {

    public Map<String, List<Long>>algorithmStart(List<PaypassGeofence> paypassGeofenceList) {
        log.info("PaypassSequenceAlgorithm이 시작됩니다.");

        List<PaypassGeofence> sortedList = sortByUserFenceInTime(paypassGeofenceList);

        Map<String, List<Long>> sequenceGeofenceMap = sequenceLogic(sortedList);

        return sequenceGeofenceMap;
    }

    private Map<String, List<Long>> sequenceLogic(List<PaypassGeofence> paypassGeofenceList) {
        log.info("paypassGeofenceList = " + paypassGeofenceList);

        // busInfo만 존재하는 list 생성
        List<String> busInfoList = extractBusInfoList(paypassGeofenceList);
        log.info("busInfoList = {}", busInfoList);

        // busInfoMap에서 key값 추가
        Map<String, List<Long>> busInfoMap = buildBusInfoMap(busInfoList);
        log.info("busInfoMap = {}", busInfoMap);

        Map<String, List<Long>> extractOppositeStation = extractOppositeStation(busInfoMap);
        log.info("extractOppositeStation = {}", extractOppositeStation);

        // 모든 경우에 수 중에서 가장 연속된 구간이 긴 busInfoMap만 살리기
        Map<String, List<Long>> maxContinuousMap = filterMaxContinuousMap(extractOppositeStation, busInfoList);
        log.info("maxContinuousMap = {}", maxContinuousMap);

        // sequence가 순차적으로 증가하는지 검사
        // sequence의 일정 부분만 조건 만족 시 해당 부분의 sequence만 추출
        // 조건 만족 시 해당 sequence를 가지는 map 추가
        Map<String, List<Long>> finalMap = extractContinuousSequences(maxContinuousMap);
        log.info("continuousBusInfoMap = {}", finalMap);

        return finalMap;
    }

    private List<String> extractBusInfoList(List<PaypassGeofence> geofenceList) {
        List<String> result = new ArrayList<>();
        for (PaypassGeofence geofence : geofenceList) {
            result.add(geofence.getBusInfo());
        }
        return result;
    }

    private Map<String, List<Long>> buildBusInfoMap(List<String> busInfoList) {
        Set<String> routeIds = extractRouteIds(busInfoList);
        Map<String, List<Long>> busInfoMap = initializeRouteMap(routeIds);
        fillRouteMap(busInfoMap, busInfoList);
        Map<String, List<Long>> divided = divideBySequenceDelimiter(busInfoMap);
        return labelPlainKeys(divided);
    }

    private Map<String, List<Long>> extractOppositeStation(Map<String, List<Long>> busInfoMap) {
        Map<String, List<Long>> result = new TreeMap<>();

        for (Map.Entry<String, List<Long>> entry : busInfoMap.entrySet()) {
            List<Long> list = entry.getValue();
            int size = list.size();
            List<Long> best = new ArrayList<>();

            for (int i = 0; i < (1 << size); i++) {
                List<Long> temp = new ArrayList<>();
                for (int j = 0; j < size; j++) {
                    if ((i & (1 << j)) != 0) temp.add(list.get(j));
                }
                if (isConsecutivelyGrouped(temp) && temp.size() > best.size()) {
                    best = temp;
                }
            }
            result.put(entry.getKey(), best);
        }

        return result;
    }

    private boolean isConsecutivelyGrouped(List<Long> list) {
        if (list.size() < 2) return false;
        int i = 0;
        while (i < list.size()) {
            int j = i;
            while (j + 1 < list.size() && list.get(j + 1) == list.get(j) + 1) j++;
            if (j == i) return false;
            i = j + 1;
        }
        return true;
    }

    private Map<String, List<Long>> filterMaxContinuousMap(Map<String, List<Long>> map, List<String> rawInfoList) {
        Map<String, Long> continuousCount = calculateContinuousCounts(map);
        log.info("continuousCountMap = {}", continuousCount);

        Map<String, Long> maxMap = findMaxPerRoute(continuousCount, rawInfoList);
        log.info("maxMap = {}", maxMap);

        List<String> candidates = findCandidates(continuousCount, maxMap);
        log.info("maxContinuousList = {}", candidates);

        return extractMaxRouteSequences(map, maxMap.keySet(), candidates);
    }

    private Set<String> extractRouteIds(List<String> infoList) {
        Set<String> routeIds = new HashSet<>();
        for (String station : infoList) {
            for (String entry : station.replaceAll("^\\{|\\}$", "").split("},\\{")) {
                String routeId = entry.split(",")[0];
                routeIds.add(routeId);
            }
        }
        return routeIds;
    }

    private Map<String, List<Long>> initializeRouteMap(Set<String> routeIds) {
        Map<String, List<Long>> map = new TreeMap<>();
        for (String id : routeIds) {
            map.put(id, new ArrayList<>());
        }
        return map;
    }

    private void fillRouteMap(Map<String, List<Long>> map, List<String> infoList) {
        for (String station : infoList) {
            List<String> entries = Arrays.asList(station.replaceAll("^\\{|\\}$", "").split("},\\{"));
            List<String> checkedEntries = mergeDuplicateRouteIds(entries);
            for (String entry : checkedEntries) {
                String[] parts = entry.split(",");
                String routeId = parts[0];
                Long sequence = Long.parseLong(parts[1]);
                map.get(routeId).add(sequence);
            }
        }
    }

    private List<String> mergeDuplicateRouteIds(List<String> entries) {
        Map<String, StringBuilder> grouped = new TreeMap<>();
        for (String entry : entries) {
            String[] parts = entry.split(",");
            String routeId = parts[0], seq = parts[1];
            grouped.putIfAbsent(routeId, new StringBuilder());
            if (grouped.get(routeId).length() > 0) grouped.get(routeId).append("000");
            grouped.get(routeId).append(seq);
        }
        List<String> result = new ArrayList<>();
        for (var e : grouped.entrySet()) {
            result.add(e.getKey() + "," + e.getValue());
        }
        return result;
    }

    private Map<String, List<Long>> divideBySequenceDelimiter(Map<String, List<Long>> map) {
        boolean needsRepeat;
        do {
            needsRepeat = false;
            Map<String, List<Long>> updated = new TreeMap<>();
            List<String> removeKeys = new ArrayList<>();

            for (var e : map.entrySet()) {
                String routeId = e.getKey();
                List<Long> list = e.getValue();

                for (int i = 0; i < list.size(); i++) {
                    String seqStr = String.valueOf(list.get(i));
                    if (seqStr.length() > 4 && seqStr.contains("000")) {
                        int splitIdx = seqStr.lastIndexOf("000");
                        String left = seqStr.substring(0, splitIdx);
                        String right = seqStr.substring(splitIdx + 3);

                        Long s1 = Long.parseLong(left);
                        Long s2 = Long.parseLong(right);

                        list.set(i, s1);
                        updated.put(routeId + "_1", new ArrayList<>(list));

                        list.set(i, s2);
                        updated.put(routeId + "_2", new ArrayList<>(list));

                        removeKeys.add(routeId);
                        needsRepeat = true;
                        break;
                    }
                }
            }

            map.keySet().removeAll(removeKeys);
            map.putAll(updated);

        } while (needsRepeat);

        return map;
    }

    private Map<String, List<Long>> labelPlainKeys(Map<String, List<Long>> map) {
        Map<String, List<Long>> labeled = new TreeMap<>();
        for (var e : map.entrySet()) {
            String id = e.getKey();
            if (!id.contains("_")) id += "_1";
            labeled.put(id, e.getValue());
        }
        return labeled;
    }

    private Map<String, Long> calculateContinuousCounts(Map<String, List<Long>> map) {
        Map<String, Long> result = new TreeMap<>();
        for (var e : map.entrySet()) {
            result.put(e.getKey(), computeMaxSequenceLength(e.getValue()));
        }
        return result;
    }

    private Long computeMaxSequenceLength(List<Long> list) {
        Long total = 0L, count = 1L;
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).equals(list.get(i - 1) + 1)) count++;
            else {
                if (count > 1) total += count;
                count = 1L;
            }
        }
        if (count > 1) total += count;
        return total;
    }

    private Map<String, Long> findMaxPerRoute(Map<String, Long> countMap, List<String> rawInfoList) {
        Map<String, Long> result = new TreeMap<>();
        Set<String> routeIds = extractRouteIds(rawInfoList);
        for (String base : routeIds) {
            long max = 0;
            for (var e : countMap.entrySet()) {
                if (e.getKey().startsWith(base + "_") || e.getKey().equals(base)) {
                    max = Math.max(max, e.getValue());
                }
            }
            if (max > 0) result.put(base, max);
        }
        return result;
    }

    private List<String> findCandidates(Map<String, Long> countMap, Map<String, Long> maxMap) {
        List<String> result = new ArrayList<>();
        for (var e : maxMap.entrySet()) {
            String base = e.getKey();
            Long max = e.getValue();
            for (var entry : countMap.entrySet()) {
                String fullKey = entry.getKey();
                Long val = entry.getValue();
                if ((fullKey.startsWith(base + "_") || fullKey.equals(base)) && val.equals(max)) {
                    result.add(fullKey);
                }
            }
        }
        return result;
    }

    private Map<String, List<Long>> extractMaxRouteSequences(Map<String, List<Long>> map, Set<String> baseKeys, List<String> candidates) {
        Map<String, List<Long>> result = new TreeMap<>();
        for (String base : baseKeys) {
            String selected = null;
            List<Long> bestList = new ArrayList<>();
            int minSplit = Integer.MAX_VALUE;
            for (String candidate : candidates) {
                if (candidate.startsWith(base)) {
                    List<Long> seqList = map.get(candidate);
                    int splitCount = extractSequential(seqList).size();
                    if (splitCount < minSplit) {
                        selected = candidate;
                        bestList = seqList;
                        minSplit = splitCount;
                    }
                }
            }
            if (selected == null) throw new RuntimeException("No valid sequence found for " + base);
            result.put(selected, bestList);
        }
        return result;
    }

    private Map<String, List<Long>> extractContinuousSequences(Map<String, List<Long>> map) {
        Map<String, List<Long>> result = new TreeMap<>();
        for (var e : map.entrySet()) {
            String routeId = e.getKey();
            List<List<Long>> groups = extractSequential(e.getValue());
            for (List<Long> group : groups) {
                String newKey = generateUniqueKey(result, routeId);
                result.put(newKey, group);
            }
        }
        return result;
    }

    private List<List<Long>> extractSequential(List<Long> list) {
        List<List<Long>> result = new ArrayList<>();
        List<Long> temp = new ArrayList<>();
        temp.add(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).equals(list.get(i - 1) + 1)) {
                temp.add(list.get(i));
            } else {
                if (temp.size() > 1) result.add(new ArrayList<>(temp));
                temp.clear();
                temp.add(list.get(i));
            }
        }
        if (temp.size() > 1) result.add(temp);
        return result;
    }

    private String generateUniqueKey(Map<String, List<Long>> map, String routeId) {
        int suffix = 1;
        String base = routeId.contains("_") ? routeId.split("_")[0] : routeId;
        String key;
        do {
            key = base + "_" + suffix++;
        } while (map.containsKey(key));
        return key;
    }

    private List<PaypassGeofence> sortByUserFenceInTime(List<PaypassGeofence> geofenceLocationList) {
        return geofenceLocationList.stream()
                .sorted(Comparator.comparing(PaypassGeofence::getFenceInTime))
                .toList();
    }



}

