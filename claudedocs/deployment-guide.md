# SafetyFence 배포 가이드

완전 초보자도 따라할 수 있는 단계별 배포 가이드입니다.

---

## 📋 목차
1. [로컬 테스트 및 빌드](#1-로컬-테스트-및-빌드)
2. [Docker 로컬 검증](#2-docker-로컬-검증)
3. [AWS EC2 준비](#3-aws-ec2-준비)
4. [프로젝트 배포](#4-프로젝트-배포)
5. [배포 후 검증](#5-배포-후-검증)

---

## 1. 로컬 테스트 및 빌드

### 1-1. PostgreSQL 테스트 DB 준비 (이미 완료)
```bash
# PostgreSQL에 접속 (이미 했음)
psql -U postgres

# 테스트 DB 생성 (이미 했음)
CREATE DATABASE safetyfence_test_db OWNER safetyfence;
\c safetyfence_test_db
CREATE EXTENSION postgis;
\q
```

### 1-2. 테스트 실행
```bash
# 프로젝트 루트에서
cd /Users/chungjongin/Desktop/forProject/safetyFence

# 모든 테스트 실행
./gradlew test

# 결과 확인
# ✅ BUILD SUCCESSFUL → 성공
# ❌ BUILD FAILED → 에러 확인 필요
```

**테스트 실패 시:**
- 에러 메시지 확인
- PostgreSQL 실행 중인지 확인: `psql -U safetyfence -d safetyfence_test_db -c "SELECT version();"`
- PostGIS 설치 확인: `psql -U safetyfence -d safetyfence_test_db -c "SELECT PostGIS_version();"`

### 1-3. 애플리케이션 빌드
```bash
# 깨끗하게 빌드
./gradlew clean build

# 성공하면 JAR 파일 생성됨
ls -lh build/libs/

# 예상 출력: safetyfence-0.0.1-SNAPSHOT.jar (약 50-100MB)
```

**빌드 성공 확인:**
- `build/libs/` 폴더에 `.jar` 파일이 있어야 함
- 파일 크기가 0이 아니어야 함

---

## 2. Docker 로컬 검증

로컬에서 Docker로 실행해서 배포 전에 문제 없는지 확인합니다.

### 2-1. Docker 실행 확인
```bash
# Docker 실행 중인지 확인
docker --version
docker-compose --version

# Docker Desktop이 실행 중이어야 함
```

### 2-2. .env 파일 확인
```bash
# .env 파일이 있는지 확인
cat .env

# 내용 확인:
# DB_USERNAME=safetyfence
# DB_PASSWORD=chung0513
# SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/safetyfence_db
# SPRING_DATASOURCE_USERNAME=safetyfence
# SPRING_DATASOURCE_PASSWORD=chung0513
# KAKAO_API_KEY=cdecb8c886f753e88eba07da2dc8f7eb
```

### 2-3. Docker Compose로 전체 스택 실행
```bash
# 이미지 빌드 및 컨테이너 시작
docker-compose up --build -d

# 실행 상태 확인
docker-compose ps

# 예상 출력:
# NAME                SERVICE   STATUS
# safetyfence-nginx   nginx     running
# safetyfence-app     app       running
# safetyfence-db      db        running
```

### 2-4. 로그 확인
```bash
# 전체 로그 확인
docker-compose logs

# Spring Boot 앱 로그만 확인
docker-compose logs app

# 실시간 로그 보기
docker-compose logs -f app

# 성공적으로 시작되면:
# "Started SafetyfenceApplication in X seconds" 메시지 확인
```

### 2-5. 로컬 테스트
```bash
# API 엔드포인트 테스트
curl http://localhost/api/health

# 또는 브라우저에서
# http://localhost 접속
```

### 2-6. 정리 (테스트 끝나면)
```bash
# 컨테이너 중지 및 삭제
docker-compose down

# 볼륨까지 삭제 (데이터도 삭제됨)
docker-compose down -v
```

---

## 3. AWS EC2 준비

### 3-1. AWS 콘솔 접속
1. https://aws.amazon.com/ko/ 접속
2. "콘솔에 로그인" 클릭
3. 계정 로그인

### 3-2. EC2 인스턴스 생성
```
1. EC2 서비스 선택
2. "인스턴스 시작" 클릭

[이름 및 태그]
- 이름: safetyfence-server

[애플리케이션 및 OS 이미지]
- Ubuntu Server 22.04 LTS 선택
- 아키텍처: 64비트(x86)

[인스턴스 유형]
- t2.micro (프리티어) 또는
- t3.small (권장, 월 약 $15)

[키 페어]
- "새 키 페어 생성" 클릭
- 이름: safetyfence-key
- 유형: RSA
- 형식: .pem
- "키 페어 생성" → 파일 다운로드 (잘 보관!)

[네트워크 설정]
- "보안 그룹 생성" 선택
- SSH, HTTP, HTTPS 체크

[스토리지 구성]
- 30 GB gp3 (기본값)

3. "인스턴스 시작" 클릭
```

### 3-3. 보안 그룹 설정
```
1. EC2 → 보안 그룹 → 방금 생성된 그룹 선택
2. "인바운드 규칙 편집" 클릭
3. 다음 규칙 추가:

규칙 1 (SSH):
- 유형: SSH
- 포트: 22
- 소스: 내 IP

규칙 2 (HTTP):
- 유형: HTTP
- 포트: 80
- 소스: 0.0.0.0/0

규칙 3 (HTTPS):
- 유형: HTTPS
- 포트: 443
- 소스: 0.0.0.0/0

규칙 4 (Spring Boot - 개발용):
- 유형: 사용자 지정 TCP
- 포트: 8080
- 소스: 내 IP

4. "규칙 저장" 클릭
```

### 3-4. Elastic IP 할당 (선택사항, 권장)
```
1. EC2 → 탄력적 IP → "탄력적 IP 주소 할당"
2. "할당" 클릭
3. 할당된 IP 선택 → "작업" → "탄력적 IP 주소 연결"
4. 인스턴스: safetyfence-server 선택
5. "연결" 클릭

※ 고정 IP 얻음. 인스턴스 재시작해도 IP 안 바뀜
※ 사용하지 않으면 과금되니 주의!
```

### 3-5. 키 페어 권한 설정 (Mac/Linux)
```bash
# 다운로드한 키 파일 이동
mv ~/Downloads/safetyfence-key.pem ~/.ssh/

# 권한 변경 (필수!)
chmod 400 ~/.ssh/safetyfence-key.pem

# Windows에서는:
# 파일 우클릭 → 속성 → 보안 → 고급
# 소유자를 본인 계정으로 변경
# 모든 권한 제거 후 본인 계정만 읽기 권한 추가
```

### 3-6. EC2 접속 테스트
```bash
# EC2 퍼블릭 IP 확인 (AWS 콘솔에서)
# 예: 3.34.123.456

# SSH 접속
ssh -i ~/.ssh/safetyfence-key.pem ubuntu@3.34.123.456

# 접속 성공하면:
# ubuntu@ip-xxx-xxx-xxx-xxx:~$ 프롬프트 보임

# 시스템 업데이트
sudo apt update
sudo apt upgrade -y
```

### 3-7. Docker 설치
```bash
# EC2에 접속한 상태에서

# 필수 패키지 설치
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Docker GPG 키 추가
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Docker 저장소 추가
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Docker 설치
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 설치 확인
docker --version
docker-compose --version

# 현재 사용자를 docker 그룹에 추가 (sudo 없이 docker 명령 실행)
sudo usermod -aG docker ubuntu

# 로그아웃 후 재접속 (그룹 적용)
exit
ssh -i ~/.ssh/safetyfence-key.pem ubuntu@3.34.123.456

# Docker 실행 확인 (sudo 없이)
docker ps
```

---

## 4. 프로젝트 배포

### 4-1. 프로젝트 파일 준비 (로컬)
```bash
# 로컬 컴퓨터에서
cd /Users/chungjongin/Desktop/forProject/safetyFence

# 필요한 파일만 묶기
tar -czf safetyfence-deploy.tar.gz \
  docker-compose.yml \
  Dockerfile \
  nginx/ \
  build/libs/*.jar \
  .env.example

# 압축 파일 생성 확인
ls -lh safetyfence-deploy.tar.gz
```

### 4-2. EC2로 파일 전송
```bash
# EC2 IP 확인 (예: 3.34.123.456)
# 로컬에서 실행:

scp -i ~/.ssh/safetyfence-key.pem safetyfence-deploy.tar.gz ubuntu@3.34.123.456:~/

# 전송 완료까지 대기 (파일 크기에 따라 1-5분)
```

### 4-3. EC2에서 파일 압축 해제
```bash
# EC2 접속
ssh -i ~/.ssh/safetyfence-key.pem ubuntu@3.34.123.456

# 작업 디렉토리 생성
mkdir -p ~/safetyfence
cd ~/safetyfence

# 압축 해제
tar -xzf ~/safetyfence-deploy.tar.gz

# 파일 확인
ls -la
# 출력: docker-compose.yml, Dockerfile, nginx/, build/, .env.example
```

### 4-4. .env 파일 생성 (EC2)
```bash
# .env.example을 복사해서 .env 생성
cp .env.example .env

# .env 편집
nano .env

# 실제 값으로 변경:
# DB_USERNAME=safetyfence
# DB_PASSWORD=실제_비밀번호_입력  ← 변경!
# SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/safetyfence_db
# SPRING_DATASOURCE_USERNAME=safetyfence
# SPRING_DATASOURCE_PASSWORD=실제_비밀번호_입력  ← 변경!
# KAKAO_API_KEY=실제_카카오_API_키_입력  ← 변경!

# 저장: Ctrl + O → Enter
# 종료: Ctrl + X

# 권한 설정 (보안)
chmod 600 .env
```

### 4-5. Docker Compose 실행
```bash
# EC2의 ~/safetyfence 디렉토리에서

# 컨테이너 시작
docker-compose up -d

# 진행 상황 확인
docker-compose logs -f

# Ctrl + C로 로그 종료 (컨테이너는 계속 실행됨)

# 컨테이너 상태 확인
docker-compose ps

# 모두 "running" 상태여야 함
```

### 4-6. 실행 확인
```bash
# EC2에서 로컬 테스트
curl http://localhost

# 로컬 컴퓨터에서 외부 접속 테스트
# 브라우저에서: http://3.34.123.456 (EC2 퍼블릭 IP)
```

---

## 5. 배포 후 검증

### 5-1. 컨테이너 상태 확인
```bash
# EC2에 접속한 상태에서

# 모든 컨테이너 확인
docker-compose ps

# 실행 중인 컨테이너 상세 정보
docker stats

# 특정 컨테이너 로그
docker-compose logs app      # Spring Boot
docker-compose logs nginx    # Nginx
docker-compose logs db       # PostgreSQL
```

### 5-2. API 엔드포인트 테스트
```bash
# Health check (있다면)
curl http://3.34.123.456/api/health

# 실제 API 엔드포인트 테스트
curl http://3.34.123.456/api/your-endpoint

# JSON 응답 확인
```

### 5-3. WebSocket 테스트
```javascript
// 브라우저 콘솔에서 (F12)
const socket = new WebSocket('ws://3.34.123.456/ws/your-endpoint');
socket.onopen = () => console.log('Connected!');
socket.onmessage = (msg) => console.log('Message:', msg.data);
socket.onerror = (err) => console.error('Error:', err);

// "Connected!" 출력되면 성공
```

### 5-4. 데이터베이스 확인
```bash
# EC2에서 PostgreSQL 컨테이너 접속
docker-compose exec db psql -U safetyfence -d safetyfence_db

# PostGIS 확인
SELECT PostGIS_version();

# 테이블 확인
\dt

# 종료
\q
```

### 5-5. 모니터링 설정
```bash
# 실시간 로그 모니터링
docker-compose logs -f app

# 에러만 필터링
docker-compose logs app | grep ERROR

# 디스크 사용량 확인
df -h

# 메모리 사용량 확인
free -h

# Docker 볼륨 사용량
docker system df
```

---

## 🔧 문제 해결

### 컨테이너가 시작 안 됨
```bash
# 로그 확인
docker-compose logs

# 특정 컨테이너 재시작
docker-compose restart app

# 전체 재시작
docker-compose down
docker-compose up -d
```

### 데이터베이스 연결 실패
```bash
# DB 컨테이너 상태 확인
docker-compose ps db

# DB 로그 확인
docker-compose logs db

# .env 파일 확인
cat .env

# 네트워크 확인
docker network ls
docker network inspect safetyfence_default
```

### 메모리 부족
```bash
# 메모리 사용량 확인
docker stats

# 불필요한 컨테이너/이미지 정리
docker system prune -a

# swap 설정 (t2.micro용)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### 포트 접근 안 됨
```bash
# 보안 그룹 확인 (AWS 콘솔)
# 방화벽 확인
sudo ufw status

# Nginx 상태 확인
docker-compose logs nginx

# 포트 리스닝 확인
sudo netstat -tulpn | grep :80
```

---

## 📊 유용한 명령어 모음

### Docker 관리
```bash
# 컨테이너 시작
docker-compose up -d

# 컨테이너 중지
docker-compose down

# 재시작
docker-compose restart

# 로그 보기
docker-compose logs -f

# 특정 컨테이너 Shell 접속
docker-compose exec app sh
docker-compose exec db bash

# 리소스 사용량
docker stats
```

### 업데이트 배포
```bash
# 1. 로컬에서 새로 빌드
./gradlew clean build

# 2. 파일 전송
scp -i ~/.ssh/safetyfence-key.pem build/libs/*.jar ubuntu@IP:~/safetyfence/build/libs/

# 3. EC2에서 재배포
cd ~/safetyfence
docker-compose down
docker-compose up -d --build

# 4. 로그 확인
docker-compose logs -f app
```

### 백업
```bash
# 데이터베이스 백업
docker-compose exec db pg_dump -U safetyfence safetyfence_db > backup.sql

# 복원
cat backup.sql | docker-compose exec -T db psql -U safetyfence -d safetyfence_db
```

---

## ✅ 체크리스트

배포 전:
- [ ] 로컬 테스트 통과
- [ ] 빌드 성공 (JAR 파일 생성)
- [ ] Docker 로컬 테스트 성공
- [ ] .env 파일 준비 완료

AWS 준비:
- [ ] EC2 인스턴스 생성
- [ ] 보안 그룹 설정 (22, 80, 443, 8080)
- [ ] Elastic IP 할당 (선택)
- [ ] SSH 접속 확인
- [ ] Docker 설치 완료

배포:
- [ ] 프로젝트 파일 전송 완료
- [ ] .env 파일 설정 (실제 값 입력)
- [ ] docker-compose up 성공
- [ ] 모든 컨테이너 running 상태

검증:
- [ ] HTTP 접속 확인
- [ ] API 응답 확인
- [ ] WebSocket 연결 확인
- [ ] 데이터베이스 연결 확인
- [ ] 로그 정상

---

## 🚀 다음 단계

배포가 완료되면:
1. **도메인 연결**: Route 53으로 도메인 설정
2. **SSL 인증서**: Let's Encrypt로 HTTPS 설정
3. **모니터링**: CloudWatch 또는 Grafana 설정
4. **백업 자동화**: 정기 DB 백업 스크립트
5. **CI/CD**: GitHub Actions로 자동 배포
6. **로드 밸런서**: ELB 설정 (트래픽 증가 시)
7. **Redis 캐싱**: 성능 최적화

---

**작성일**: 2025-11-03
**작성자**: Claude
**버전**: 1.0
