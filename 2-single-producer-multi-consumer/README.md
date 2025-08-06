# 🧪 Kafka 1 Producer - 2 Consumer 실습

Spring Boot, Kafka, Docker 기반의 실습 프로젝트입니다.  
하나의 **Producer**가 Kafka 토픽에 메시지를 전송하면, **두 개의 서로 다른 Consumer**가 각각의 목적에 따라 해당 메시지를 받아 처리하는 구조입니다.

---

## 📁 프로젝트 구조
```
2-single-producer-multi-consumer/
├── order-producer/              # Kafka Producer - 주문 메시지 발행
├── order-log-consumer/         # Kafka Consumer 1 - 로그 저장
├── order-processing-consumer/  # Kafka Consumer 2 - 주문 처리
├── docker-compose.yml          # Zookeeper, Kafka, MySQL 컨테이너 정의
└── README.md
```
---

## 🛠 사용 기술

- Java 17
- Spring Boot 3.x
- Apache Kafka 3.6 (bitnami/kafka)
- MySQL 8.0 (도커 컨테이너)
- Docker Compose

---

## ⚙️ 시스템 구성도
```
Order Producer → Kafka Topic (order-topic)
↘
┌──────────┴──────────┐
Order Log Consumer     Order Processing Consumer
(Log 저장용 DB)           (주문 처리용 DB)
```
---

## ✅ Kafka 구성 방식 요약

### producer 토픽 전달 application.yml
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

kafka:
  topic: order-topic



```

### 2개의 consumer 토픽 수신 application.yml
- 같은 토픽(order-topic)을 다른 그룹(log-group, process-group)으로 받아서 한 토픽을 두 서버가 독립적으로 같이 받음
```
spring.kafka:
  bootstrap-servers: localhost:9092
  consumer:
    group-id: log-group / process-group
    auto-offset-reset: earliest
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    properties:
      spring.json.trusted.packages: '*'

	- Consumer 1: log-group (로그 DB 저장)
	- Consumer 2: process-group (주문 처리 DB 저장)

⸻
```
### 📦 Docker 구성 (docker-compose.yml)

```
version: '3.8'

services:
  zookeeper:
    image: bitnami/zookeeper:3.9
    ports:
      - "2181:2181"
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes

  kafka:
    image: bitnami/kafka:3.6
    ports:
      - "9092:9092"
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_LISTENERS=PLAINTEXT://:9092
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
      - ALLOW_PLAINTEXT_LISTENER=yes
    depends_on:
      - zookeeper

  mysql-producer:
    image: mysql:8.0
    container_name: mysql-producer
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: producer1234
      MYSQL_DATABASE: producerdb
    volumes:
      - mysql_producer_data:/var/lib/mysql

  mysql-event:
    image: mysql:8.0
    container_name: mysql-event
    ports:
      - "3307:3306"
    environment:
      MYSQL_ROOT_PASSWORD: event1234
      MYSQL_DATABASE: eventdb
    volumes:
      - mysql_event_data:/var/lib/mysql

  mysql-processing:
    image: mysql:8.0
    container_name: mysql-processing
    ports:
      - "3308:3306"
    environment:
      MYSQL_ROOT_PASSWORD: process1234
      MYSQL_DATABASE: processingdb
    volumes:
      - mysql_processing_data:/var/lib/mysql

volumes:
  mysql_producer_data:
  mysql_event_data:
  mysql_processing_data:

```


##📌 Kafka 핵심 개념 정리: Group ID에 따른 메시지 처리 방식

### ✅ 서로 다른 Group ID (Pub/Sub 구조) - 현재 프로젝트 구조
- 설정 예시
  - 	order-log-consumer → log-group
  - order-processing-consumer → process-group

- 동작 방식
  - 동일한 메시지를 모든 Consumer가 각각 수신
  - 서로 다른 역할의 Consumer가 독립적으로 처리
- 활용 예시
  - log-group: 메시지를 로그 DB에 저장
  - process-group: 메시지를 주문 처리 DB에 저장
- 장점
  - 역할 분리
  - 서비스 간 독립성
  - 유지보수 용이

⸻

### ✅ 같은 Group ID (Load Balancing 구조)
- 설정 예시
  - Consumer A, B → 동일하게 order-group
- 동작 방식
  - 하나의 메시지를 단 하나의 Consumer만 처리
  - Kafka가 파티션 기준으로 자동 분산 처리
- 활용 예시
  - 처리량이 많은 경우 Consumer 여러 개로 분산
  - 대용량 처리 시스템 구성
- 장점
  - 부하 분산
  - 처리 속도 향상

⸻

### 🎯 “1 Producer - 2 Consumer” 구조를 사용하는 이유

목적	설명
- **역할 분리** : 서로 다른 비즈니스 로직을 독립적으로 수행 (ex. 로그 저장 + 주문 처리)
- **확장성**	: 필요에 따라 새로운 Consumer를 쉽게 추가 가능
- **유지보수** : 기능별로 코드와 DB를 분리하여 관리 가능
- **안정성** : 한 Consumer가 실패해도 다른 Consumer는 계속 동작


⸻

✅ 정리
- 서로 다른 Group ID → Pub/Sub 구조 (모든 Consumer가 메시지를 받음)
- 같은 Group ID → Load Balancing 구조 (한 Consumer만 메시지를 받음)
- 1 Producer → N Consumer 구조는 확장성과 유연성을 모두 고려한 설계

⸻

