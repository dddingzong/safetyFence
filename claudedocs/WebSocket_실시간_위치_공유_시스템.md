# WebSocket 실시간 위치 공유 시스템 구현 문서

## 📋 목차
1. [개요](#개요)
2. [시스템 아키텍처](#시스템-아키텍처)
3. [기술 스택](#기술-스택)
4. [구현 상세](#구현-상세)
5. [데이터 흐름](#데이터-흐름)
6. [API 명세](#api-명세)
7. [Frontend 통합 가이드](#frontend-통합-가이드)
8. [테스트 방법](#테스트-방법)
9. [성능 및 최적화](#성능-및-최적화)
10. [트러블슈팅](#트러블슈팅)

---

## 개요

### 목적
사용자가 Link로 연결된 다른 사용자의 실시간 위치를 지도에서 확인할 수 있는 시스템

### 주요 기능
- **실시간 위치 전송**: 2초 주기로 위치 업데이트
- **선택적 구독**: Link 목록에서 특정 사용자 선택 시 해당 사용자의 위치만 수신
- **권한 관리**: 단방향 Link 기반 구독 권한 검증
- **캐싱**: 최신 위치 1개만 메모리에 저장 (즉시 전송)
- **조건부 DB 저장**: 100m 이동 또는 1분 경과 시에만 저장

### 시스템 요구사항
- Spring Boot 3.5.0
- Java 17
- MySQL 8.0
- WebSocket 지원 브라우저

---

## 시스템 아키텍처

### 전체 구조도

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend (사용자 A)                 │
│  ┌────────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │ Link 목록      │  │ 사용자 선택   │  │ 지도 (B 위치)  │ │
│  │ - B (친구)  ✓  │  │ B ◉ 선택중   │  │      📍         │ │
│  │ - C (가족)     │  │ C ○          │  │    (B의 위치)   │ │
│  │ - D (동료)     │  │ D ○          │  │                 │ │
│  └────────────────┘  └──────────────┘  └─────────────────┘ │
│         │                   │                    ▲          │
│         │ HTTP              │ WebSocket          │          │
│         │ GET /links        │ STOMP Protocol     │          │
└─────────┼───────────────────┼────────────────────┼──────────┘
          │                   │                    │
          ▼                   ▼                    │
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Server                         │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              REST API Layer (기존)                    │  │
│  │  GET /links → Link 목록 반환                         │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              WebSocket Layer (신규)                   │  │
│  │                                                        │  │
│  │  1️⃣ 연결 수립: /ws                                   │  │
│  │     - SockJS fallback 지원                           │  │
│  │     - STOMP 프로토콜                                 │  │
│  │     - 인증: WebSocketAuthInterceptor                 │  │
│  │                                                        │  │
│  │  2️⃣ 구독 (Subscribe)                                 │  │
│  │     - Endpoint: /topic/location/{userNumber}         │  │
│  │     - 권한 검증: Link 관계 확인                      │  │
│  │     - 즉시 응답: 캐시된 최신 위치 전송               │  │
│  │                                                        │  │
│  │  3️⃣ 발행 (Publish)                                   │  │
│  │     - Endpoint: /app/location                        │  │
│  │     - 브로드캐스트: /topic/location/{userNumber}     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           LocationCacheService                        │  │
│  │  - Caffeine Cache                                     │  │
│  │  - Max Size: 10,000명                                │  │
│  │  - TTL: 5분                                          │  │
│  │  - 사용자당 최신 위치 1개만 저장                     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           LocationService (비동기)                    │  │
│  │  - 조건부 DB 저장                                     │  │
│  │    • 100m 이상 이동 시                               │  │
│  │    • 1분 이상 경과 시                                │  │
│  │  - Haversine 공식으로 거리 계산                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Database (MySQL)                            │  │
│  │  - UserLocation: 위치 이력 저장                      │  │
│  │  - Link: 사용자 관계 관리                            │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend (사용자 B)                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           백그라운드 위치 전송                       │   │
│  │  - navigator.geolocation.watchPosition()            │   │
│  │  - 2초마다 자동 전송                                 │   │
│  │  - WebSocket: /app/location                         │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 컴포넌트 설명

#### 1. WebSocket Layer
- **WebSocketConfig**: STOMP 엔드포인트 및 메시지 브로커 설정
- **WebSocketAuthInterceptor**: 연결 시 사용자 인증 및 세션 관리
- **LocationWebSocketController**: 위치 전송/구독 처리

#### 2. Service Layer
- **LocationCacheService**: Caffeine 기반 메모리 캐시 관리
- **LocationService**: 비동기 조건부 DB 저장
- **LinkService**: Link 관계 검증 (`hasLink()`)

#### 3. Repository Layer
- **LinkRepository**: Link 관계 조회
- **UserLocationRepository**: 위치 이력 저장/조회
- **UserRepository**: 사용자 정보 조회

#### 4. Event Listener
- **WebSocketEventListener**: 연결/해제 이벤트 처리 및 리소스 정리

---

## 기술 스택

### Backend
- **Spring Boot 3.5.0**
- **Spring WebSocket** (STOMP over WebSocket)
- **Caffeine Cache** (메모리 캐싱)
- **Spring Async** (비동기 처리)
- **MySQL 8.0** (위치 이력 저장)
- **JPA/Hibernate** (ORM)
- **Lombok** (보일러플레이트 코드 제거)

### Frontend (권장)
- **React** (UI)
- **@stomp/stompjs** (STOMP 클라이언트)
- **sockjs-client** (SockJS 폴백)
- **Google Maps API** 또는 **Kakao Map API** (지도 표시)

### Protocol
- **WebSocket**: 양방향 실시간 통신
- **STOMP**: Simple Text Oriented Messaging Protocol
- **SockJS**: WebSocket 미지원 브라우저 폴백

---

## 구현 상세

### 1. 의존성 (build.gradle)

```gradle
dependencies {
    // 기존 의존성...

    // WebSocket
    implementation 'org.springframework.boot:spring-boot-starter-websocket'

    // Cache
    implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'
}
```

### 2. WebSocket 설정

#### WebSocketConfig.java
```java
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 prefix
        config.enableSimpleBroker("/topic");

        // 클라이언트가 메시지 보낼 prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 개발: 모든 origin 허용
                .withSockJS();  // SockJS fallback
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인증 인터셉터 등록
        registration.interceptors(webSocketAuthInterceptor);
    }
}
```

**주요 설정**:
- `/topic/*`: 구독 엔드포인트 (클라이언트 → 서버)
- `/app/*`: 발행 엔드포인트 (서버 → 클라이언트)
- `/ws`: WebSocket 연결 엔드포인트
- `withSockJS()`: 구형 브라우저 지원

### 3. 인증 인터셉터

#### WebSocketAuthInterceptor.java
```java
@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class
        );

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 연결 시 userNumber 추출
            String userNumber = accessor.getFirstNativeHeader("userNumber");

            if (userNumber == null || userNumber.isBlank()) {
                throw new IllegalArgumentException("userNumber는 필수입니다.");
            }

            // 세션에 저장
            accessor.getSessionAttributes().put("userNumber", userNumber);

            log.info("WebSocket 연결: userNumber={}, sessionId={}",
                    userNumber, accessor.getSessionId());
        }

        return message;
    }
}
```

**역할**:
- WebSocket 연결 시 `userNumber` 헤더 검증
- 세션에 사용자 정보 저장
- 이후 모든 메시지에서 세션으로부터 사용자 식별

### 4. DTO

#### LocationUpdateDto.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDto {
    private String userNumber;    // 서버에서 설정
    private Double latitude;      // 위도
    private Double longitude;     // 경도
    private Long timestamp;       // 전송 시각 (밀리초)

    // 클라이언트용 생성자
    public LocationUpdateDto(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }
}
```

**특징**:
- 최소한의 데이터만 전송 (네트워크 효율)
- `userNumber`는 서버에서 세션으로부터 자동 설정

### 5. 캐시 서비스

#### LocationCacheService.java
```java
@Slf4j
@Service
public class LocationCacheService {

    private final Cache<String, LocationUpdateDto> locationCache;

    public LocationCacheService() {
        this.locationCache = Caffeine.newBuilder()
                .maximumSize(10_000)  // 최대 10,000개
                .expireAfterWrite(5, TimeUnit.MINUTES)  // 5분 TTL
                .recordStats()
                .build();
    }

    public void updateLocation(String userNumber, LocationUpdateDto location) {
        locationCache.put(userNumber, location);
    }

    public LocationUpdateDto getLatestLocation(String userNumber) {
        return locationCache.getIfPresent(userNumber);
    }

    public void removeLocation(String userNumber) {
        locationCache.invalidate(userNumber);
    }
}
```

**Caffeine Cache 특징**:
- **메모리 효율**: LRU + LFU 하이브리드 알고리즘
- **빠른 성능**: ConcurrentHashMap 기반
- **자동 만료**: 5분간 업데이트 없으면 자동 삭제
- **통계**: `recordStats()`로 캐시 히트율 모니터링 가능

**메모리 사용량 예측**:
```
1,000명 × 50 bytes (DTO) = 50KB
10,000명 × 50 bytes = 500KB
```
→ 매우 가볍습니다!

### 6. WebSocket Controller

#### LocationWebSocketController.java

##### 6-1. 위치 전송 처리
```java
@MessageMapping("/location")
public void updateLocation(
        LocationUpdateDto location,
        @Header("simpSessionAttributes") Map<String, Object> sessionAttributes
) {
    // 1. 세션에서 사용자 번호 추출
    String userNumber = (String) sessionAttributes.get("userNumber");
    location.setUserNumber(userNumber);
    location.setTimestamp(System.currentTimeMillis());

    // 2. 캐시 업데이트
    cacheService.updateLocation(userNumber, location);

    // 3. 구독자들에게 브로드캐스트
    messagingTemplate.convertAndSend(
        "/topic/location/" + userNumber,
        location
    );

    // 4. 비동기 DB 저장
    locationService.saveLocationIfNeeded(location);
}
```

**동작 흐름**:
1. 클라이언트가 `/app/location`으로 위치 전송
2. 서버가 세션에서 `userNumber` 추출
3. 캐시에 최신 위치 저장
4. `/topic/location/{userNumber}` 구독자들에게 브로드캐스트
5. 조건 충족 시 비동기로 DB 저장

##### 6-2. 구독 처리 (권한 검증)
```java
@SubscribeMapping("/topic/location/{targetUserNumber}")
public LocationUpdateDto onSubscribe(
        @DestinationVariable String targetUserNumber,
        @Header("simpSessionAttributes") Map<String, Object> sessionAttributes
) throws AccessDeniedException {
    // 1. 구독자 번호 추출
    String subscriberNumber = (String) sessionAttributes.get("userNumber");

    // 2. 권한 검증: Link 관계 확인
    if (!linkService.hasLink(subscriberNumber, targetUserNumber)) {
        throw new AccessDeniedException(
            "사용자 " + targetUserNumber + "의 위치를 볼 권한이 없습니다."
        );
    }

    // 3. 캐시된 최신 위치 즉시 반환
    return cacheService.getLatestLocation(targetUserNumber);
}
```

**권한 검증 로직**:
```
A가 B의 위치를 구독하려면:
→ Link 테이블에서 A가 B를 Link로 등록했는지 확인
→ 단방향 확인 (B가 A를 등록했는지는 무관)
```

**즉시 응답**:
- 새 구독자에게 캐시된 최신 위치 즉시 전송
- 대기 없이 바로 지도에 마커 표시 가능

### 7. 비동기 DB 저장

#### LocationService.java

##### 7-1. 조건부 저장 로직
```java
@Async
@Transactional
public void saveLocationIfNeeded(LocationUpdateDto locationDto) {
    User user = userRepository.findByNumber(locationDto.getUserNumber());

    // 이전 위치 조회
    Optional<UserLocation> previousLocationOpt =
        userLocationRepository.findLatestByUser(user);

    // 1️⃣ 이전 위치 없으면 무조건 저장
    if (previousLocationOpt.isEmpty()) {
        saveLocation(user, locationDto);
        return;
    }

    UserLocation previousLocation = previousLocationOpt.get();

    // 2️⃣ 거리 계산 (Haversine 공식)
    double distance = calculateDistance(
        previousLocation.getLatitude().doubleValue(),
        previousLocation.getLongitude().doubleValue(),
        locationDto.getLatitude(),
        locationDto.getLongitude()
    );

    // 3️⃣ 시간 차이 계산
    long timeDiff = locationDto.getTimestamp() -
        previousLocation.getSavedTime()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();

    // 4️⃣ 저장 조건 확인
    if (distance >= 100.0) {  // 100m 이상 이동
        saveLocation(user, locationDto);
    } else if (timeDiff >= 60_000) {  // 1분 이상 경과
        saveLocation(user, locationDto);
    }
}
```

**저장 조건**:
| 조건 | 설명 | 결과 |
|------|------|------|
| 첫 위치 | 이전 위치 없음 | ✅ 저장 |
| 100m 이동 | Haversine 거리 ≥ 100m | ✅ 저장 |
| 1분 경과 | 마지막 저장 후 60초 이상 | ✅ 저장 |
| 그 외 | 위 조건 미충족 | ❌ 저장 안 함 |

**DB 저장 빈도 예측**:
```
2초마다 전송 → 시간당 1,800개
조건부 저장 → 시간당 최대 60개
절감율: 약 96%
```

##### 7-2. Haversine 거리 계산
```java
private double calculateDistance(
    double lat1, double lon1,
    double lat2, double lon2
) {
    final double EARTH_RADIUS = 6371000; // 지구 반지름 (미터)

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c;
}
```

**Haversine 공식**:
- 구면 상 두 점 사이의 최단 거리 계산
- GPS 좌표 간 거리 측정에 최적
- 오차 범위: ±0.5% (실용적으로 충분)

### 8. 연결 해제 처리

#### WebSocketEventListener.java
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final LocationCacheService cacheService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String userNumber = (String) sessionAttributes.get("userNumber");

            if (userNumber != null) {
                // 캐시에서 위치 삭제
                cacheService.removeLocation(userNumber);
                log.info("WebSocket 연결 해제: userNumber={}, 캐시 삭제",
                        userNumber);
            }
        }
    }
}
```

**리소스 정리**:
- WebSocket 연결 해제 시 자동 호출
- 캐시에서 사용자 위치 삭제
- 메모리 누수 방지

---

## 데이터 흐름

### Flow 1: 초기 연결 및 Link 목록 조회

```
React (A)                      Server                    Database
   |                              |                          |
   |------ GET /links ----------->|                          |
   |                              |------ SELECT Links ----->|
   |                              |<----- [B, C, D] ---------|
   |<--- [B, C, D] ---------------|                          |
   |                              |                          |
```

**HTTP 요청**:
```
GET /links
Headers:
  userNumber: A
```

**응답**:
```json
[
  { "id": 1, "userNumber": "B", "relation": "친구" },
  { "id": 2, "userNumber": "C", "relation": "가족" },
  { "id": 3, "userNumber": "D", "relation": "동료" }
]
```

### Flow 2: WebSocket 연결 및 구독

```
React (A)                           Server
   |                                   |
   |-- WebSocket CONNECT /ws -------->|
   |   Headers: { userNumber: "A" }   |
   |                                   |
   |         [WebSocketAuthInterceptor]
   |         세션에 userNumber 저장
   |                                   |
   |<-- CONNECTED -------------------|
   |   sessionId: abc123              |
   |                                   |
   |-- SUBSCRIBE ------------------->|
   |   /topic/location/B              |
   |                                   |
   |         [LinkService.hasLink(A, B)]
   |         권한 검증: A가 B를 Link로 등록?
   |                                   |
   |<-- SUBSCRIBED -------------------|
   |<-- MESSAGE ----------------------|  ← 캐시된 B의 최신 위치
   |   { lat: 37.123, lng: 127.456 }  |
   |                                   |
```

**WebSocket 연결 코드** (JavaScript):
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const client = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    userNumber: 'A'  // 인증 정보
  },
  onConnect: () => {
    console.log('연결됨');
  }
});

client.activate();
```

**구독 코드**:
```javascript
client.subscribe('/topic/location/B', (message) => {
  const location = JSON.parse(message.body);
  console.log('B의 위치:', location);
  updateMapMarker(location.latitude, location.longitude);
});
```

### Flow 3: 실시간 위치 업데이트

```
React (B)              Server                    React (A)
   |                      |                          |
   |-- SEND /app/location -->                        |
   |  {                   |                          |
   |    lat: 37.123,      |                          |
   |    lng: 127.456      |                          |
   |  }                   |                          |
   |                      |                          |
   |          [LocationWebSocketController]         |
   |          1. 세션에서 userNumber="B" 추출       |
   |          2. 캐시 업데이트                       |
   |          3. 브로드캐스트                        |
   |                      |                          |
   |                      |-- MESSAGE /topic/location/B ->
   |                      |   {                      |
   |                      |     userNumber: "B",     |
   |                      |     lat: 37.123,         |
   |                      |     lng: 127.456,        |
   |                      |     timestamp: 1729741200|
   |                      |   }                      |
   |                      |                          |
   |                      |              [지도 마커 업데이트]
   |                      |                          |
   |          [LocationService.saveLocationIfNeeded] |
   |          비동기 DB 저장 (조건 충족 시)          |
```

**위치 전송 코드** (JavaScript):
```javascript
// 2초마다 위치 전송
setInterval(() => {
  navigator.geolocation.getCurrentPosition((position) => {
    client.publish({
      destination: '/app/location',
      body: JSON.stringify({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
      })
    });
  });
}, 2000);
```

### Flow 4: 다른 사용자로 전환

```
React (A)                      Server
   |                              |
   |-- UNSUBSCRIBE -------------->|
   |   /topic/location/B          |
   |                              |
   |<-- OK -----------------------|
   |                              |
   |-- SUBSCRIBE ---------------->|
   |   /topic/location/C          |
   |                              |
   |         [LinkService.hasLink(A, C)]
   |         권한 검증
   |                              |
   |<-- SUBSCRIBED ---------------|
   |<-- C의 최신 위치 ------------|
   |                              |
```

**구독 전환 코드**:
```javascript
// 기존 구독 해제
previousSubscription.unsubscribe();

// 새 구독 시작
const newSubscription = client.subscribe(
  `/topic/location/${newUserNumber}`,
  (message) => {
    const location = JSON.parse(message.body);
    updateMapMarker(location.latitude, location.longitude);
  }
);
```

---

## API 명세

### WebSocket Endpoints

#### 1. 연결 엔드포인트

```
WebSocket /ws
```

**연결 방법**:
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const client = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    userNumber: 'YOUR_USER_NUMBER'
  }
});
```

**Headers**:
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| userNumber | String | ✅ Yes | 사용자 번호 (인증) |

**응답**:
- **성공**: CONNECTED 프레임
- **실패**: ERROR 프레임 (userNumber 없음)

---

#### 2. 위치 전송

```
SEND /app/location
```

**Request Body**:
```json
{
  "latitude": 37.123456,
  "longitude": 127.123456
}
```

**Parameters**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| latitude | Double | ✅ Yes | 위도 (-90 ~ 90) |
| longitude | Double | ✅ Yes | 경도 (-180 ~ 180) |

**처리 과정**:
1. 서버가 세션에서 `userNumber` 추출
2. `timestamp` 자동 설정
3. 캐시 업데이트
4. `/topic/location/{userNumber}`로 브로드캐스트
5. 조건부 DB 저장

**주의사항**:
- `userNumber`는 클라이언트가 보내지 않음 (서버가 세션에서 자동 추출)
- `timestamp`도 서버에서 자동 설정

---

#### 3. 위치 구독

```
SUBSCRIBE /topic/location/{userNumber}
```

**Path Variable**:
| Variable | Type | Description |
|----------|------|-------------|
| userNumber | String | 구독하려는 사용자 번호 |

**권한 검증**:
- 구독자가 대상 사용자를 Link로 등록했는지 확인
- 단방향 검증: A가 B를 Link로 등록했으면 A는 B 구독 가능

**즉시 응답** (캐시에 위치가 있는 경우):
```json
{
  "userNumber": "B",
  "latitude": 37.123456,
  "longitude": 127.123456,
  "timestamp": 1729741200000
}
```

**실시간 업데이트**:
대상 사용자가 위치를 전송할 때마다 동일한 형식의 메시지 수신

**에러**:
- **403 AccessDeniedException**: Link 관계 없음
```json
{
  "error": "사용자 B의 위치를 볼 권한이 없습니다."
}
```

---

### REST API (기존)

#### GET /links
Link 목록 조회

**Request**:
```
GET /links
Headers:
  userNumber: A
```

**Response**:
```json
[
  {
    "id": 1,
    "userNumber": "B",
    "relation": "친구"
  },
  {
    "id": 2,
    "userNumber": "C",
    "relation": "가족"
  }
]
```

---

## Frontend 통합 가이드

### 1. 설치

```bash
npm install @stomp/stompjs sockjs-client
```

### 2. WebSocket 서비스 구현

```javascript
// locationWebSocket.js
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class LocationWebSocketService {
  constructor() {
    this.client = null;
    this.subscription = null;
  }

  /**
   * WebSocket 연결
   */
  connect(userNumber, onConnected) {
    const socket = new SockJS('http://localhost:8080/ws');

    this.client = new Client({
      webSocketFactory: () => socket,

      connectHeaders: {
        userNumber: userNumber  // 인증 정보
      },

      onConnect: () => {
        console.log('✅ WebSocket 연결 성공');
        onConnected?.();
      },

      onStompError: (frame) => {
        console.error('❌ STOMP 에러:', frame.headers['message']);
      },

      onWebSocketError: (event) => {
        console.error('❌ WebSocket 에러:', event);
      }
    });

    this.client.activate();
  }

  /**
   * 특정 사용자 위치 구독
   */
  subscribe(targetUserNumber, onLocationUpdate) {
    // 기존 구독 해제
    this.unsubscribe();

    // 새 구독 시작
    this.subscription = this.client.subscribe(
      `/topic/location/${targetUserNumber}`,
      (message) => {
        const location = JSON.parse(message.body);
        console.log('📍 위치 수신:', location);
        onLocationUpdate(location);
      }
    );

    console.log(`📡 구독 시작: ${targetUserNumber}`);
  }

  /**
   * 구독 해제
   */
  unsubscribe() {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
      console.log('📡 구독 해제');
    }
  }

  /**
   * 내 위치 전송
   */
  sendMyLocation(latitude, longitude) {
    if (!this.client?.connected) {
      console.warn('⚠️ WebSocket 연결 안 됨');
      return;
    }

    this.client.publish({
      destination: '/app/location',
      body: JSON.stringify({
        latitude,
        longitude
      })
    });

    console.log(`📤 위치 전송: ${latitude}, ${longitude}`);
  }

  /**
   * 연결 종료
   */
  disconnect() {
    this.unsubscribe();
    this.client?.deactivate();
    console.log('🔌 WebSocket 연결 종료');
  }
}

export default new LocationWebSocketService();
```

### 3. React 컴포넌트 예제

```jsx
// LocationTracker.jsx
import { useState, useEffect } from 'react';
import locationWebSocket from './locationWebSocket';

function LocationTracker() {
  const [links, setLinks] = useState([]);           // Link 목록
  const [selectedUser, setSelectedUser] = useState(null);  // 선택된 사용자
  const [targetLocation, setTargetLocation] = useState(null);  // 상대 위치
  const myUserNumber = "123";  // 실제로는 로그인 정보에서 가져옴

  // 1️⃣ 초기화: Link 목록 조회 + WebSocket 연결
  useEffect(() => {
    // Link 목록 조회 (HTTP)
    fetch('/api/links', {
      headers: { 'userNumber': myUserNumber }
    })
      .then(res => res.json())
      .then(data => setLinks(data))
      .catch(err => console.error('Link 목록 조회 실패:', err));

    // WebSocket 연결
    locationWebSocket.connect(myUserNumber, () => {
      console.log('WebSocket 연결 완료!');
    });

    // 내 위치 주기적 전송 (2초)
    const locationInterval = setInterval(() => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          locationWebSocket.sendMyLocation(
            position.coords.latitude,
            position.coords.longitude
          );
        },
        (error) => {
          console.error('위치 조회 실패:', error);
        }
      );
    }, 2000);

    // 정리
    return () => {
      clearInterval(locationInterval);
      locationWebSocket.disconnect();
    };
  }, []);

  // 2️⃣ 사용자 선택 시 구독 시작
  useEffect(() => {
    if (!selectedUser) return;

    console.log(`👤 선택: ${selectedUser.userNumber}`);

    // 선택한 사용자 위치 구독
    locationWebSocket.subscribe(selectedUser.userNumber, (location) => {
      console.log('📍 위치 업데이트:', location);
      setTargetLocation(location);
    });

    // 정리: 구독 해제
    return () => {
      locationWebSocket.unsubscribe();
    };
  }, [selectedUser]);

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      {/* 왼쪽: Link 목록 */}
      <div style={{ width: '300px', padding: '20px', borderRight: '1px solid #ccc' }}>
        <h3>👥 Link 목록</h3>
        {links.length === 0 && <p>Link가 없습니다.</p>}
        {links.map(link => (
          <button
            key={link.userNumber}
            onClick={() => setSelectedUser(link)}
            style={{
              display: 'block',
              width: '100%',
              padding: '10px',
              margin: '5px 0',
              backgroundColor: selectedUser?.userNumber === link.userNumber ? '#4CAF50' : '#f0f0f0',
              color: selectedUser?.userNumber === link.userNumber ? 'white' : 'black',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer'
            }}
          >
            {link.userNumber} ({link.relation})
          </button>
        ))}
      </div>

      {/* 오른쪽: 지도 */}
      <div style={{ flex: 1, padding: '20px' }}>
        {selectedUser ? (
          <>
            <h3>📍 {selectedUser.userNumber}의 위치</h3>
            {targetLocation ? (
              <div>
                <p>위도: {targetLocation.latitude}</p>
                <p>경도: {targetLocation.longitude}</p>
                <p>업데이트: {new Date(targetLocation.timestamp).toLocaleTimeString()}</p>

                {/* 여기에 지도 컴포넌트 추가 */}
                {/* <Map center={[targetLocation.latitude, targetLocation.longitude]} /> */}
              </div>
            ) : (
              <p>위치를 불러오는 중...</p>
            )}
          </>
        ) : (
          <p>왼쪽에서 사용자를 선택하세요.</p>
        )}
      </div>
    </div>
  );
}

export default LocationTracker;
```

### 4. 지도 통합 (Kakao Map 예제)

```jsx
// KakaoMap.jsx
import { useEffect, useRef } from 'react';

function KakaoMap({ latitude, longitude, userName }) {
  const mapRef = useRef(null);
  const markerRef = useRef(null);

  useEffect(() => {
    // Kakao Maps API 초기화
    const script = document.createElement('script');
    script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY&autoload=false';
    script.onload = () => {
      window.kakao.maps.load(() => {
        const container = mapRef.current;
        const options = {
          center: new window.kakao.maps.LatLng(latitude, longitude),
          level: 3
        };

        const map = new window.kakao.maps.Map(container, options);

        // 마커 생성
        const markerPosition = new window.kakao.maps.LatLng(latitude, longitude);
        const marker = new window.kakao.maps.Marker({
          position: markerPosition,
          map: map
        });

        markerRef.current = { map, marker };
      });
    };
    document.head.appendChild(script);
  }, []);

  // 위치 업데이트 시 마커 이동
  useEffect(() => {
    if (markerRef.current) {
      const { map, marker } = markerRef.current;
      const newPosition = new window.kakao.maps.LatLng(latitude, longitude);

      marker.setPosition(newPosition);
      map.setCenter(newPosition);
    }
  }, [latitude, longitude]);

  return (
    <div>
      <div ref={mapRef} style={{ width: '100%', height: '500px' }} />
      <p style={{ marginTop: '10px', textAlign: 'center' }}>
        📍 {userName}의 현재 위치
      </p>
    </div>
  );
}

export default KakaoMap;
```

---

## 테스트 방법

### 1. 로컬 환경 설정

#### Backend 실행
```bash
cd /Users/chungjongin/Desktop/forProject/safetyFence
./gradlew bootRun
```

#### 로그 확인
```
2025-10-24 14:30:15.123  INFO --- [main] o.s.b.w.e.t.TomcatWebServer  : Tomcat started on port(s): 8080
2025-10-24 14:30:15.456  INFO --- [main] c.p.s.LocationCacheService   : LocationCacheService 초기화 완료: maxSize=10000, TTL=5분
```

### 2. WebSocket 연결 테스트

#### Chrome DevTools Console
```javascript
// 1. SockJS + STOMP 라이브러리 CDN 추가 (HTML)
// <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
// <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js"></script>

// 2. 연결 테스트
const socket = new SockJS('http://localhost:8080/ws');
const client = StompJs.Stomp.over(() => socket);

client.connect(
  { userNumber: 'testUser123' },
  () => {
    console.log('✅ 연결 성공!');

    // 구독 테스트
    client.subscribe('/topic/location/testUser456', (message) => {
      console.log('📍 수신:', JSON.parse(message.body));
    });

    // 위치 전송 테스트
    client.publish({
      destination: '/app/location',
      body: JSON.stringify({
        latitude: 37.123456,
        longitude: 127.123456
      })
    });
  },
  (error) => {
    console.error('❌ 연결 실패:', error);
  }
);
```

### 3. Postman 테스트

Postman은 WebSocket 테스트 지원:

1. **New Request** → **WebSocket**
2. **URL**: `ws://localhost:8080/ws`
3. **Connect** 클릭
4. **Message** 탭에서 STOMP 프레임 전송:

```
CONNECT
userNumber:testUser123

^@
```

```
SUBSCRIBE
id:sub-0
destination:/topic/location/testUser456

^@
```

```
SEND
destination:/app/location
content-type:application/json

{"latitude":37.123456,"longitude":127.123456}
^@
```

### 4. 권한 검증 테스트

#### 시나리오 1: 권한 있음
```
1. User A가 User B를 Link로 등록
2. A가 B의 위치 구독 → ✅ 성공
3. B가 위치 전송 → A가 수신
```

#### 시나리오 2: 권한 없음
```
1. User A가 User C를 Link로 등록 안 함
2. A가 C의 위치 구독 → ❌ AccessDeniedException
```

**에러 로그**:
```
WARN  --- [WebSocket-123] c.p.s.c.LocationWebSocketController : 권한 없음: subscriber=A, target=C
ERROR --- [WebSocket-123] o.s.m.s.s.StompSubProtocolHandler   : AccessDeniedException: 사용자 C의 위치를 볼 권한이 없습니다.
```

### 5. 캐시 동작 테스트

#### 테스트 코드
```java
@SpringBootTest
class LocationCacheServiceTest {

    @Autowired
    private LocationCacheService cacheService;

    @Test
    void 위치_캐시_저장_및_조회() {
        // Given
        LocationUpdateDto location = new LocationUpdateDto(
            "user123", 37.123, 127.456, System.currentTimeMillis()
        );

        // When
        cacheService.updateLocation("user123", location);
        LocationUpdateDto cached = cacheService.getLatestLocation("user123");

        // Then
        assertThat(cached).isNotNull();
        assertThat(cached.getLatitude()).isEqualTo(37.123);
        assertThat(cached.getLongitude()).isEqualTo(127.456);
    }

    @Test
    void 위치_캐시_TTL_테스트() throws InterruptedException {
        // Given
        LocationUpdateDto location = new LocationUpdateDto(
            "user123", 37.123, 127.456, System.currentTimeMillis()
        );
        cacheService.updateLocation("user123", location);

        // When: 5분 대기
        Thread.sleep(5 * 60 * 1000 + 1000);

        // Then: 캐시 만료
        LocationUpdateDto cached = cacheService.getLatestLocation("user123");
        assertThat(cached).isNull();
    }
}
```

### 6. DB 저장 조건 테스트

#### 거리 조건 테스트
```java
@Test
void 거리_100m_이상_이동_시_저장() {
    // Given: 초기 위치 (서울시청)
    LocationUpdateDto initial = new LocationUpdateDto(
        "user123", 37.5665, 126.9780, System.currentTimeMillis()
    );
    locationService.saveLocationIfNeeded(initial);

    // When: 150m 이동 (광화문)
    LocationUpdateDto moved = new LocationUpdateDto(
        "user123", 37.5758, 126.9768, System.currentTimeMillis()
    );
    locationService.saveLocationIfNeeded(moved);

    // Then: 저장됨
    List<UserLocation> locations = userLocationRepository.findAll();
    assertThat(locations).hasSize(2);
}
```

#### 시간 조건 테스트
```java
@Test
void 시간_1분_경과_시_저장() {
    // Given
    LocationUpdateDto t0 = new LocationUpdateDto(
        "user123", 37.123, 127.456, System.currentTimeMillis()
    );
    locationService.saveLocationIfNeeded(t0);

    // When: 1분 후, 같은 위치
    LocationUpdateDto t1 = new LocationUpdateDto(
        "user123", 37.123, 127.456, System.currentTimeMillis() + 61_000
    );
    locationService.saveLocationIfNeeded(t1);

    // Then: 저장됨
    List<UserLocation> locations = userLocationRepository.findAll();
    assertThat(locations).hasSize(2);
}
```

---

## 성능 및 최적화

### 메모리 사용량

#### 캐시 메모리
```
사용자당 메모리 = LocationUpdateDto 크기

LocationUpdateDto:
- userNumber: String (평균 10 chars) ≈ 20 bytes
- latitude: Double = 8 bytes
- longitude: Double = 8 bytes
- timestamp: Long = 8 bytes
Total: ~44 bytes

Caffeine Cache 오버헤드: ~10 bytes/entry

사용자당 총 메모리: ~54 bytes

10,000명: 54 × 10,000 = 540KB
```

**결론**: 10,000명 동시 접속 시 약 **540KB** (매우 가볍습니다!)

#### WebSocket 연결 메모리
```
WebSocket 세션당 메모리: ~1KB

10,000명: 1KB × 10,000 = 10MB
```

**총 메모리 사용량** (10,000명):
```
캐시: 540KB
WebSocket: 10MB
합계: ~10.5MB
```

### 네트워크 대역폭

#### 업스트림 (클라이언트 → 서버)
```
메시지 크기: ~100 bytes (JSON)
전송 주기: 2초
사용자당 대역폭: 100 bytes / 2초 = 50 bytes/s

10,000명: 50 × 10,000 = 500KB/s
```

#### 다운스트림 (서버 → 클라이언트)
```
1명의 위치를 N명이 구독하는 경우:
서버 → 클라이언트: 100 bytes × N

평균 5명이 구독한다고 가정:
10,000명 × 5명 = 50,000개 메시지
50,000 × 50 bytes/s = 2.5MB/s
```

**결론**: 10,000명 환경에서 약 **3MB/s** (일반 서버로 충분)

### DB 부하 최적화

#### 저장 빈도 비교
```
전체 저장 (2초마다):
- 1분: 30개
- 1시간: 1,800개
- 24시간: 43,200개

조건부 저장 (100m 또는 1분):
- 1분: 1개
- 1시간: 60개
- 24시간: 1,440개

절감율: 96.7%
```

#### DB 저장 비동기 처리
```java
@Async  // 별도 스레드에서 실행
public void saveLocationIfNeeded(LocationUpdateDto locationDto) {
    // WebSocket 메시지 전송과 독립적으로 실행
    // 저장 지연이 실시간 전송에 영향 없음
}
```

**AsyncConfig 설정**:
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring의 기본 ThreadPoolTaskExecutor 사용
    // 필요 시 커스터마이징 가능
}
```

### 캐시 최적화

#### Caffeine vs 다른 캐시
```
Caffeine:
- 읽기: 0.01ms (매우 빠름)
- 쓰기: 0.01ms
- LRU + LFU 하이브리드

Redis:
- 읽기: 1-5ms (네트워크 왕복)
- 쓰기: 1-5ms
- 분산 환경에 유리

단일 서버 → Caffeine 충분
다중 서버 → Redis 필요
```

#### TTL (Time To Live)
```
현재 설정: 5분
- 5분간 업데이트 없으면 자동 삭제
- 연결 해제 후에도 5분간 유지 (재연결 시 빠른 복구)
- 메모리 누수 방지
```

### 확장성

#### 수평 확장 (Scale Out)
```
단일 서버 → 다중 서버:
1. Redis Pub/Sub 추가
2. WebSocketConfig에서 SimpleBroker → Redis Broker 변경

@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableStompBrokerRelay("/topic")
          .setRelayHost("redis-host")
          .setRelayPort(6379);
}
```

#### 부하 분산 (Load Balancing)
```
Nginx 설정:

upstream websocket_backend {
    ip_hash;  # 같은 클라이언트는 같은 서버로
    server backend1:8080;
    server backend2:8080;
}

server {
    location /ws {
        proxy_pass http://websocket_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 트러블슈팅

### 문제 1: WebSocket 연결 실패

#### 증상
```
ERROR --- [WebSocket] o.s.w.s.h.ExceptionWebSocketHandlerDecorator :
Uncaught failure for session [abc123]
org.springframework.web.socket.CloseStatus: CloseStatus[code=1011, reason=userNumber는 필수입니다.]
```

#### 원인
- `userNumber` 헤더를 전송하지 않음

#### 해결
```javascript
// ❌ 잘못된 코드
const client = new Client({
  webSocketFactory: () => socket
});

// ✅ 올바른 코드
const client = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    userNumber: 'YOUR_USER_NUMBER'  // 필수!
  }
});
```

---

### 문제 2: 구독 시 403 에러

#### 증상
```
ERROR --- [WebSocket-123] o.s.m.s.s.StompSubProtocolHandler :
AccessDeniedException: 사용자 B의 위치를 볼 권한이 없습니다.
```

#### 원인
- 구독자가 대상 사용자를 Link로 등록하지 않음

#### 확인
```sql
-- Link 관계 확인
SELECT * FROM link
WHERE user = 'A' AND user_number = 'B';

-- 결과가 없으면 권한 없음
```

#### 해결
```
1. A가 B의 linkCode로 Link 추가
2. POST /links 요청
3. 다시 구독 시도
```

---

### 문제 3: 위치 업데이트가 느림

#### 증상
- 위치가 5~10초 지연되어 표시됨

#### 원인 1: 전송 주기가 너무 김
```javascript
// ❌ 5초마다 전송
setInterval(() => { sendLocation(); }, 5000);

// ✅ 2초마다 전송
setInterval(() => { sendLocation(); }, 2000);
```

#### 원인 2: DB 저장이 동기 처리됨
```java
// ❌ 동기 저장 (느림)
public void saveLocationIfNeeded() { ... }

// ✅ 비동기 저장 (빠름)
@Async
public void saveLocationIfNeeded() { ... }
```

#### 원인 3: 네트워크 지연
```
- Wifi: 10-50ms
- 4G: 50-100ms
- 3G: 100-500ms
```

**해결**: 네트워크 환경 개선 또는 전송 주기 단축

---

### 문제 4: 캐시에서 위치가 조회 안 됨

#### 증상
```
DEBUG --- [WebSocket-456] c.p.s.s.LocationCacheService :
위치 캐시 조회 실패 (캐시 없음): userNumber=B
```

#### 원인
1. B가 한 번도 위치를 전송하지 않음
2. B의 마지막 위치 전송 후 5분 경과 (TTL 만료)
3. B가 연결 해제함 (캐시 삭제됨)

#### 해결
```javascript
// B가 주기적으로 위치 전송하는지 확인
setInterval(() => {
  navigator.geolocation.getCurrentPosition((pos) => {
    client.publish({
      destination: '/app/location',
      body: JSON.stringify({
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude
      })
    });
  });
}, 2000);
```

---

### 문제 5: DB에 위치가 저장 안 됨

#### 증상
```
DEBUG --- [async-1] c.p.s.s.LocationService :
저장 조건 미충족: userNumber=A, distance=5.2m, timeDiff=10초
```

#### 원인
- 저장 조건 미충족 (100m 미만 이동 & 1분 미만 경과)

#### 정상 동작입니다!
- 조건부 저장은 **의도된 동작**
- 불필요한 DB 저장을 방지하여 성능 최적화

#### 강제 저장이 필요한 경우
```java
// LocationService에 메서드 추가
@Transactional
public void forceSaveLocation(LocationUpdateDto locationDto) {
    User user = userRepository.findByNumber(locationDto.getUserNumber());
    saveLocation(user, locationDto);
}
```

---

### 문제 6: 메모리 사용량 증가

#### 증상
```
캐시 크기: 15,000개
메모리 사용량: 800KB (예상: 540KB)
```

#### 원인
- `maximumSize` 제한 초과
- 연결 해제 시 캐시 삭제가 안 됨

#### 확인
```java
// 캐시 통계 조회
@GetMapping("/admin/cache/stats")
public String getCacheStats() {
    return cacheService.getCacheStats();
}
```

**응답 예시**:
```
CacheStats{
  hitCount=12345,
  missCount=678,
  evictionCount=90,
  size=10000
}
```

#### 해결
```java
// WebSocketEventListener에서 연결 해제 시 삭제 확인
@EventListener
public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    String userNumber = extractUserNumber(event);

    // 캐시 삭제
    cacheService.removeLocation(userNumber);

    // 로그 확인
    log.info("캐시 삭제: userNumber={}, 현재 크기={}",
            userNumber, cacheService.getCacheSize());
}
```

---

### 문제 7: CORS 에러

#### 증상
```
Access to XMLHttpRequest at 'http://localhost:8080/ws' from origin 'http://localhost:3000'
has been blocked by CORS policy
```

#### 원인
- WebSocket CORS 설정 누락

#### 해결
```java
// WebSocketConfig.java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")  // 또는 "http://localhost:3000"
            .withSockJS();
}
```

**프로덕션 환경**:
```java
.setAllowedOriginPatterns("https://yourdomain.com")
```

---

## 추가 개선 아이디어

### 1. 배터리 최적화

#### 조건부 전송 (클라이언트)
```javascript
let lastSentLocation = null;

function shouldSendLocation(current) {
  if (!lastSentLocation) return true;

  const distance = calculateDistance(
    lastSentLocation.lat,
    lastSentLocation.lng,
    current.lat,
    current.lng
  );

  const timeDiff = Date.now() - lastSentLocation.timestamp;

  // 10m 이동 또는 10초 경과 시에만 전송
  return distance > 10 || timeDiff > 10000;
}
```

### 2. 오프라인 지원

#### Service Worker로 위치 큐잉
```javascript
// 오프라인 시 위치를 로컬 저장
navigator.serviceWorker.addEventListener('message', (event) => {
  if (event.data.type === 'LOCATION_UPDATE') {
    if (!navigator.onLine) {
      // IndexedDB에 저장
      saveToIndexedDB(event.data.location);
    }
  }
});

// 온라인 복구 시 일괄 전송
window.addEventListener('online', () => {
  getFromIndexedDB().then(locations => {
    locations.forEach(loc => {
      client.publish({ destination: '/app/location', body: JSON.stringify(loc) });
    });
    clearIndexedDB();
  });
});
```

### 3. 위치 정확도 필터링

#### GPS 정확도 검증
```javascript
navigator.geolocation.watchPosition(
  (position) => {
    // 정확도 50m 이하만 전송
    if (position.coords.accuracy <= 50) {
      sendLocation(position.coords.latitude, position.coords.longitude);
    } else {
      console.warn('GPS 정확도 낮음:', position.coords.accuracy);
    }
  },
  (error) => {
    console.error('위치 조회 실패:', error);
  },
  {
    enableHighAccuracy: true,  // 고정밀도 모드
    maximumAge: 0,              // 캐시 사용 안 함
    timeout: 5000               // 5초 타임아웃
  }
);
```

### 4. 알림 기능

#### 특정 위치 진입 시 알림
```java
// GeofenceService.java
public void checkGeofenceEntry(LocationUpdateDto location) {
    List<Geofence> geofences = geofenceRepository.findByUser_Number(
        location.getUserNumber()
    );

    for (Geofence geofence : geofences) {
        if (isInside(location, geofence)) {
            // 알림 전송
            messagingTemplate.convertAndSendToUser(
                geofence.getUser().getNumber(),
                "/queue/notifications",
                new NotificationDto("사용자가 " + geofence.getName() + "에 도착했습니다.")
            );
        }
    }
}
```

---

## 요약

### 구현 완료 항목
✅ **Phase 1**: WebSocket 의존성 및 설정 (WebSocketConfig, Interceptor)
✅ **Phase 2**: DTO 생성 (LocationUpdateDto)
✅ **Phase 3**: 캐시 서비스 (LocationCacheService with Caffeine)
✅ **Phase 4**: WebSocket Controller (위치 전송/구독 처리)
✅ **Phase 5**: 권한 검증 (LinkService.hasLink)
✅ **Phase 6**: 비동기 DB 저장 (조건부 저장 로직)
✅ **Phase 7**: 연결 해제 처리 (WebSocketEventListener)

### 핵심 특징
- 🚀 **실시간**: 2초 주기 위치 업데이트
- 🔒 **보안**: Link 기반 단방향 권한 검증
- ⚡ **성능**: Caffeine 캐시로 즉시 응답
- 💾 **효율**: 조건부 DB 저장 (96% 절감)
- 🔄 **확장성**: 수평 확장 가능 (Redis Pub/Sub)

### 다음 단계
1. **Frontend 구현**: React + @stomp/stompjs
2. **지도 통합**: Kakao Map API 또는 Google Maps
3. **테스트**: 단위/통합 테스트 작성
4. **모니터링**: 캐시 히트율, WebSocket 연결 수 모니터링
5. **프로덕션 배포**: HTTPS, 도메인 CORS 설정

---

**문서 작성일**: 2025-10-24
**작성자**: Claude Code
**버전**: 1.0.0
