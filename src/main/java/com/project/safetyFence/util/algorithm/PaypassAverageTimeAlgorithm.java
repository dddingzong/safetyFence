package com.project.safetyFence.util.algorithm;

import com.project.safetyFence.domain.PaypassGeofence;
import com.project.safetyFence.domain.data.BusTime;
import com.project.safetyFence.repository.BusTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaypassAverageTimeAlgorithm {

    private final BusTimeRepository busTimeRepository;

    public Map<String, List<Long>> algorithmStart(Map<String, List<Long>> basicMap, List<PaypassGeofence> geofenceLocations) {
        Map<String, List<Long>> treeMap = new TreeMap<>(basicMap);

        // timeMap을 생성하기 위해 GeofenceLocationMap을 생성
        // BusInfoMap과 동일한 형식으로 존재해야한다.
        Map<String, List<PaypassGeofence>> continuousGeofenceLocationMap = createContinuousGeofenceLocationMap(treeMap, geofenceLocations);

        // timeMap 생성
        Map<String, List<Map<String, LocalDateTime>>> timeMap = createTimeMap(continuousGeofenceLocationMap);
        log.info("timeMap = " + timeMap);

        // timeMap과 basicMap을 활용하여 조건에 맞지 않는 인자 제거
        Map<String, List<Long>> averageMap = createAverageMap(treeMap, timeMap);
        log.info("averageMap = " + averageMap);

        return averageMap;
    }

    private Map<String, List<PaypassGeofence>> createContinuousGeofenceLocationMap(Map<String, List<Long>> basicMap, List<PaypassGeofence> geofenceLocations) {
        // basicMap에 존재하는 routeId를 활용하여 만들기
        List<String> basicMapKeyList = makeRouteIdList(basicMap);
        log.info("basicMapKeyList = " + basicMapKeyList);

        // basicMapKeyList 사용해서 GeofenceLocationMap 만들기
        Map<String, List<PaypassGeofence>> geofenceLocationMap = makeGeofenceLocationMap(basicMapKeyList, geofenceLocations);
        log.info("geofenceLocationMap = " + geofenceLocationMap);

        Map<String, List<PaypassGeofence>> continuousGeofenceLocationMap = makeContinuousGeofenceLocationMap(geofenceLocationMap, basicMap);
        log.info("continuousGeofenceLocationMap = " + continuousGeofenceLocationMap);

        return continuousGeofenceLocationMap;
    }

    private Map<String, List<Map<String, LocalDateTime>>> createTimeMap(Map<String, List<PaypassGeofence>> continuousGeofenceLocationMap) {
        Map<String, List<Map<String,LocalDateTime>>> timeMap = new TreeMap<>();

        for (var routeIdAndGeofenceLocationList : continuousGeofenceLocationMap.entrySet()) {
            String routeId = routeIdAndGeofenceLocationList.getKey();
            List<PaypassGeofence> geofenceLocationList = routeIdAndGeofenceLocationList.getValue();

            List<Map<String, LocalDateTime>> dateList = new ArrayList<>();
            for (PaypassGeofence geofenceLocation : geofenceLocationList) {
                Map<String, LocalDateTime> dateMap = new TreeMap<>();
                dateMap.put("fenceInTime", geofenceLocation.getFenceInTime());
                dateMap.put("fenceOutTime", geofenceLocation.getFenceOutTime());
                dateList.add(dateMap);
            }

            timeMap.put(routeId,dateList);
        }
        return timeMap;
    }

    private Map<String, List<Long>> createAverageMap(Map<String, List<Long>> basicMap, Map<String, List<Map<String, LocalDateTime>>> timeMap) {
        // 먼저 필터링 후 하나의 List로 넘기기
        Map<String, List<Long>> filteredMap = filterTime(basicMap, timeMap);
        log.info("filteredMap = " + filteredMap);

        // 연속된 부분으로 분할
        Map<String, List<Long>> continuousBusInfoMap = makeContinuousBusInfoMap(filteredMap);

        return continuousBusInfoMap;
    }

    private Map<String, List<Long>> filterTime(Map<String, List<Long>> basicMap, Map<String, List<Map<String, LocalDateTime>>> timeMap) {
        Map<String, List<Long>> filteredMap = new TreeMap<>();

        List<String> basicMapKeyList = makeRouteIdList(basicMap);

        for (String pureRouteId : basicMapKeyList) {
            ArrayList<Long> filteredList = new ArrayList<>();
            List<BusTime> busTimeList = busTimeRepository.findByRouteId(pureRouteId);

            for (var routeIdAndSequenceList : basicMap.entrySet()) {
                String routeId = routeIdAndSequenceList.getKey();
                List<Long> sequenceList = routeIdAndSequenceList.getValue();
                List<Map<String, LocalDateTime>> timeList = timeMap.get(routeId);

                if (routeId.contains(pureRouteId)) {
                    // 데이터에서 time 가져오고 비교하기
                    List<Long> checkedList =  checkAverageTime(sequenceList, timeList, busTimeList);

                    filteredList.addAll(checkedList);
                }
            }

            // 데이터 넣기
            String routeIdDashOne = pureRouteId+"_1";
            filteredMap.put(routeIdDashOne, filteredList);
        }
        return filteredMap;
    }

    private Map<String, List<Long>> makeContinuousBusInfoMap(Map<String, List<Long>> busInfoMap) {
        Map<String, List<Long>> continuousBusInfoMap = new TreeMap<>();

        System.out.println("busInfoMap = " + busInfoMap);

        for (String routeId : busInfoMap.keySet()) {
            List<Long> sequenceList = busInfoMap.get(routeId);
            List<List<Long>> continuousSequenceList = checkSequential(sequenceList);

            for (List<Long> continuousSequences : continuousSequenceList) {
                // 여기서 Map으로 저장
                // Map Ket Name _? 으로 생성해야한다

                // route가 이미 존재하면 다음 _? 값을 배정해야한다
                // _가 이미 붙어있는 상황을 생각해야한다.
                String keyName = assignKeyName(continuousBusInfoMap, routeId);

                continuousBusInfoMap.put(keyName, continuousSequences);
            }
        }

        return continuousBusInfoMap;
    }

    private List<List<Long>> checkSequential(List<Long> sequenceList) {
        List<List<Long>> continuousSequenceList = new ArrayList<>();

        // ex) sequenceList = [1,2,3,4,4,6,9] -> continuousSequenceList = [[1,2,3,4]]
        // ex) sequenceList = [1,3,4,5,9,13,21,22] -> continuousSequenceList = [[3,4,5], [21,22]]
        // ex) sequenceList = [1,3,2,4,7,1,0,1,2,3,3,2,6] -> continuousSequenceList = [[0,1,2,3]]
        // ex) sequenceList = [1,2,3,7,8,23,24,1,3] -> continuousSequenceList = [[1,2,3], [7,8], [23, 24]]
        // 연속되는 부분이 있다면 해당 sequence 값을 continuousSequenceList 리스트에 넣기

        System.out.println("확인용");
        System.out.println("sequenceList = " + sequenceList);

        if (sequenceList.isEmpty()){
            return continuousSequenceList;
        }
        List<Long> continuousSequences = new ArrayList<>(List.of(sequenceList.get(0)));



        for (int i = 1; i < sequenceList.size(); i++) {
            Long currentSequence = sequenceList.get(i);
            Long previousSequence = sequenceList.get(i - 1);

            if (currentSequence.equals(previousSequence + 1)) {
                continuousSequences.add(currentSequence);
                continue;
            }

            addContinuousSequencesIfValid(continuousSequenceList, continuousSequences);
            continuousSequences = new ArrayList<>(List.of(currentSequence));
        }

        addContinuousSequencesIfValid(continuousSequenceList, continuousSequences);

        return continuousSequenceList;
    }

    private void addContinuousSequencesIfValid(List<List<Long>> continuousSequenceList, List<Long> continuousSequences) {
        if (continuousSequences.size() > 1) {
            continuousSequenceList.add(continuousSequences);
        }
    }

    private String assignKeyName(Map<String, List<Long>> continuousBusInfoMap, String routeId) {
        String keyName = "";

        if (routeId.contains("_")) {
            List<String> routeIdAndCount = Arrays.asList(routeId.split("_"));
            routeId = routeIdAndCount.get(0);
        }

        // 만약 있으면 _?, 없으면 _1
        Set<String> keySet = continuousBusInfoMap.keySet();

        // routeId가 포함된 패턴
        String pattern = ".*" + routeId + ".*";

        if (keySet.stream().anyMatch(id -> id.matches(pattern))) {
            Long count = findMaxCount(continuousBusInfoMap, routeId);
            keyName = routeId + "_" + (count+1);
        }

        if (keySet.stream().noneMatch(id -> id.matches(pattern))) {
            keyName = routeId + "_1";
        }

        return keyName;
    }

    private Long findMaxCount(Map<String, List<Long>> updatedBusInfoMap, String pureRouteId) {
        Set<String> keySet = updatedBusInfoMap.keySet();
        ArrayList<Long> countList = new ArrayList<>();

        for (String routeId : keySet) {
            if (routeId.contains(pureRouteId)) {
                List<String> routeIdAndCount = Arrays.asList(routeId.split("_"));
                countList.add(Long.parseLong(routeIdAndCount.get(1)));
            }
        }
        return countList.stream().max(Long::compareTo).get();
    }

    private List<Long> checkAverageTime(List<Long> sequenceList, List<Map<String, LocalDateTime>> timeList, List<BusTime> busTimeList) {
        final int timeGap = 30;
        ArrayList<Long> checkedList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        System.out.println("sequenceList = " + sequenceList);
        System.out.println("timeList = " + timeList);
        System.out.println("busTimeList = " + busTimeList);
        
        if (sequenceList.size() != timeList.size()) throw new RuntimeException("checkAverageTime에서 에러발생 전제조건이 맞지않음");

        // sequenceList를 활용해서 데이터 가져오기
        for (int i = 0; i < sequenceList.size(); i++){
            Long sequence = sequenceList.get(i);
            int intSequence = sequence.intValue();

            // 첫번째 인자일 때
            if (i == 0){
                // 평균 이동 시간
                String currentTimeStr = busTimeList.get(intSequence - 1).getArrivalTime().replace("\uFEFF", "").trim();
                String nextTimeStr = busTimeList.get(intSequence).getArrivalTime().replace("\uFEFF", "").trim();

                LocalDateTime currentTime = LocalDateTime.parse(currentTimeStr, formatter);
                LocalDateTime nextTime = LocalDateTime.parse(nextTimeStr, formatter);

                long realTime = Duration.between(currentTime, nextTime).toMinutes();

                // 실제 이동 시간
                LocalDateTime fenceOutTime = timeList.get(i).get("fenceOutTime");
                LocalDateTime fenceInTime = timeList.get(i + 1).get("fenceInTime");

                long dataTime = Duration.between(fenceOutTime,fenceInTime).toMinutes();

                log.info(intSequence + "번: 다음 정거장과의 평균 이동 시간: " + realTime + " 실제 이동 시간: " + dataTime);

                // 차이가 5분 이하일 경우 버스를 탑승했다고 판별
                if (Math.abs(realTime - dataTime) <= timeGap ) {
                    checkedList.add(sequence);
                }
                continue;
            }

            // 중간 인자일 때
            if (i != 0 && i != sequenceList.size() -1){
                // 평균 이동 시간
                String previousTimeStr = busTimeList.get(intSequence - 2).getArrivalTime().replace("\uFEFF", "").trim();
                String currentTimeStr = busTimeList.get(intSequence - 1).getArrivalTime().replace("\uFEFF", "").trim();
                String nextTimeStr = busTimeList.get(intSequence).getArrivalTime().replace("\uFEFF", "").trim();

                LocalDateTime previousTime = LocalDateTime.parse(previousTimeStr, formatter);
                LocalDateTime currentTime = LocalDateTime.parse(currentTimeStr, formatter);
                LocalDateTime nextTime = LocalDateTime.parse(nextTimeStr, formatter);

                long previousRealTime = Duration.between(previousTime, currentTime).toMinutes();
                long nextRealTime = Duration.between(currentTime, nextTime).toMinutes();

                // 실제 이동 시간


                LocalDateTime previousFenceOutTime = timeList.get(i - 1).get("fenceOutTime");
                LocalDateTime previousFenceInTime = timeList.get(i).get("fenceInTime");

                LocalDateTime nextFenceOutTime = timeList.get(i).get("fenceOutTime");
                LocalDateTime nextFenceInTime = timeList.get(i + 1).get("fenceInTime");

                long previousDataTime = Duration.between(previousFenceOutTime,previousFenceInTime).toMinutes();
                long nextDataTime = Duration.between(nextFenceOutTime,nextFenceInTime).toMinutes();

                log.info(intSequence + "번: 이전 정거장과의 평균 이동 시간: " + previousRealTime + " 실제 이동 시간: " + previousDataTime);
                log.info(intSequence + "번: 다음 정거장과의 평균 이동 시간: " + nextRealTime + " 실제 이동 시간: " + nextDataTime);


                // 차이가 5분 이하일 경우 버스를 탑승했다고 판별 (둘 중 하나만 만족해도 탑승했다고 판별)
                if (Math.abs(nextRealTime - nextDataTime) <= timeGap || Math.abs(previousRealTime - previousDataTime) <= timeGap) {
                    checkedList.add(sequence);
                }
                continue;
            }

            // 마지막 인자일 때
            if (i == sequenceList.size() - 1){
                // 평균 이동 시간
                String previousTimeStr = busTimeList.get(intSequence - 2).getArrivalTime().replace("\uFEFF", "").trim();
                String currentTimeStr = busTimeList.get(intSequence - 1).getArrivalTime().replace("\uFEFF", "").trim();

                LocalDateTime previousTime = LocalDateTime.parse(previousTimeStr, formatter);
                LocalDateTime currentTime = LocalDateTime.parse(currentTimeStr, formatter);

                long realTime = Duration.between(previousTime, currentTime).toMinutes();

                // 실제 이동 시간
                LocalDateTime fenceOutTime = timeList.get(i-1).get("fenceOutTime");
                LocalDateTime fenceInTime = timeList.get(i).get("fenceInTime");

                long dataTime = Duration.between(fenceOutTime,fenceInTime).toMinutes();

                log.info(intSequence + "번: 이전 정거장과의 평균 이동 시간: " + realTime + " 실제 이동 시간: " + dataTime);

                // 차이가 5분 이하일 경우 버스를 탑승했다고 판별
                if (Math.abs(realTime - dataTime) <= timeGap ) {
                    checkedList.add(sequence);
                }
                continue;
            }
        }
        return checkedList;
    }

    private List<String> makeRouteIdList(Map<String, List<Long>> basicMap) {
        return basicMap.keySet()
                .stream()
                .map(s -> s.replaceAll("_.*", ""))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private Map<String, List<PaypassGeofence>> makeGeofenceLocationMap(List<String> basicMapKeyList, List<PaypassGeofence> geofenceLocations) {
        Map<String, List<PaypassGeofence>> geofenceLocationMap = new TreeMap<>();

        // basicMapKeyList 돌면서 Map 생성
        for (String pureRouteId : basicMapKeyList) {
            List<PaypassGeofence> valueList = new ArrayList<>();

            for (PaypassGeofence geofenceLocation : geofenceLocations) {
                if (geofenceLocation.getBusInfo().contains(pureRouteId)) {
                    valueList.add(geofenceLocation);
                } // end if
            } // end for

            geofenceLocationMap.put(pureRouteId, valueList);
        }
        return geofenceLocationMap;
    }

    private Map<String, List<PaypassGeofence>> makeContinuousGeofenceLocationMap(Map<String, List<PaypassGeofence>> geofenceLocationMap, Map<String, List<Long>> basicMap) {
        Map<String, List<PaypassGeofence>> continuousGeofenceLocationMap = new TreeMap<>();

        for (var routeIdAndGeofenceLocationList : geofenceLocationMap.entrySet()) {
            int deleteIndex = 0;
            String routeId = routeIdAndGeofenceLocationList.getKey();
            List<PaypassGeofence> geofenceLocationList = routeIdAndGeofenceLocationList.getValue();

            List<Long> oneBusSequenceList = makeOneBusSequenceList(routeId, geofenceLocationList);
            System.out.println("oneBusSequenceList = " + oneBusSequenceList);
            if (oneBusSequenceList.stream().anyMatch(num -> num >= 1000)) {
                oneBusSequenceList = findBestCombination(oneBusSequenceList);
            }

            System.out.println("basicMap = " + basicMap);

            for (var basicRouteIdAndSequence : basicMap.entrySet()) {
                String basicRouteId = basicRouteIdAndSequence.getKey();
                List<Long> basicSequenceList = basicRouteIdAndSequence.getValue();

                if (basicRouteId.contains(routeId)) {
                    int startIndex = getIndexContainSequence(oneBusSequenceList, basicSequenceList);
                    System.out.println("startIndex = " + startIndex);
                    continuousGeofenceLocationMap.put(basicRouteId, geofenceLocationList.subList(startIndex + deleteIndex, startIndex + deleteIndex + basicSequenceList.size()));
                    oneBusSequenceList = oneBusSequenceList.subList(startIndex + basicSequenceList.size(), oneBusSequenceList.size());
                    deleteIndex = deleteIndex + startIndex + basicSequenceList.size();
                }
            }

        }
        return continuousGeofenceLocationMap;
    }

    private List<Long> makeOneBusSequenceList(String routeId, List<PaypassGeofence> geofenceLocationList) {
        ArrayList<Long> oneBusRouteIdList = new ArrayList<>();

        for (PaypassGeofence geofenceLocation : geofenceLocationList) {
            String sequence = "null";

            List<String> oneStationInfoList = Arrays.asList(geofenceLocation.getBusInfo().replaceAll("^\\{|\\}$", "").split("},\\{"));
            for (String oneBusInfo : oneStationInfoList) {
                List<String> busInfoRouteIdAndSequence = Arrays.asList(oneBusInfo.split(","));
                String busInfoRouteId = busInfoRouteIdAndSequence.get(0);
                String busInfoSequence = busInfoRouteIdAndSequence.get(1);

                if (busInfoRouteId.equals(routeId)) {
                    if (sequence.equals("null")) {
                        sequence = busInfoSequence;
                        continue;
                    }
                    if (!sequence.equals("null")) {
                        sequence += "000" + busInfoSequence;
                    }
                } // end if

            } // end for
            oneBusRouteIdList.add(Long.parseLong(sequence));
        }
        return oneBusRouteIdList;
    }

    private int getIndexContainSequence(List<Long> oneBusSequenceList, List<Long> basicSequenceList) {
        int startIndex = -1;

        oneBusSequenceList = extractOppositeStation(oneBusSequenceList);

        System.out.println("oneBusSequenceList = " + oneBusSequenceList);
        System.out.println("basicSequenceList = " + basicSequenceList);

        String stringSequence = String.valueOf(basicSequenceList.get(0));

        for (int i = 0; i < oneBusSequenceList.size(); i++) {
            Long originalSequence = oneBusSequenceList.get(i);
            String stringOriginalSequence = String.valueOf(originalSequence);

            if (checkContainSequence(stringOriginalSequence, stringSequence)) {

                startIndex = i;
                for (int j = 1; j < basicSequenceList.size(); j++){
                    boolean containResult = checkContainSequence(String.valueOf(oneBusSequenceList.get(i + j)), String.valueOf(basicSequenceList.get(j)));
                    if (!containResult) break;
                }
                break;

            }
        }

        if (startIndex == -1) throw new RuntimeException("getIndexContainSequence 메서드에서 오류 발생");

        return startIndex;
    }

    private boolean checkContainSequence(String originalSequence, String sequence) {
        return originalSequence.contains(sequence);
    }

    private List<Long> extractOppositeStation(List<Long> sequenceList) {

        int size = sequenceList.size();
        List<Long> best = new ArrayList<>();

        for (int i = 0; i < (1 << size); i++) {
            List<Long> temp = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if ((i & (1 << j)) != 0) temp.add(sequenceList.get(j));
            }
            if (isConsecutivelyGrouped(temp) && temp.size() > best.size()) {
                best = temp;
            }
        }
        return best;
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

    private static List<Long> findBestCombination(List<Long> inputList) {
        List<Integer> ambiguousIndexes = new ArrayList<>();
        List<Long[]> splitCandidates = new ArrayList<>();

        // 사전 처리: 앞/뒤 나눌 수 있는 숫자 추출
        for (int i = 0; i < inputList.size(); i++) {
            Long num = inputList.get(i);
            String stringNum = String.valueOf(num);
            if (stringNum.contains("000")) {
                int splitIdx = stringNum.lastIndexOf("000");
                String front = stringNum.substring(0, splitIdx);
                String back = stringNum.substring(splitIdx + 3);
                splitCandidates.add(new Long[]{Long.parseLong(front), Long.parseLong(back)});
                ambiguousIndexes.add(i);
            }
        }

        int n = ambiguousIndexes.size();
        int maxLen = 0;
        List<Long> bestSequence = new ArrayList<>();

        // 비트마스크로 가능한 조합 2^n 탐색
        for (int bit = 0; bit < (1 << n); bit++) {
            List<Long> temp = new ArrayList<>(inputList);

            for (int j = 0; j < n; j++) {
                int idx = ambiguousIndexes.get(j);
                Long chosen = ((bit & (1 << j)) == 0) ? splitCandidates.get(j)[0] : splitCandidates.get(j)[1];
                temp.set(idx, chosen);
            }

            int len = maxConsecutiveLength(temp);
            if (len > maxLen) {
                maxLen = len;
                bestSequence = new ArrayList<>(temp);
            }
        }

        return bestSequence;
    }

    private static int maxConsecutiveLength(List<Long> list) {
        int maxLen = 0, curLen = 1;
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) == list.get(i - 1) + 1) {
                curLen++;
            } else {
                maxLen = Math.max(maxLen, curLen);
                curLen = 1;
            }
        }
        return Math.max(maxLen, curLen);
    }
}






