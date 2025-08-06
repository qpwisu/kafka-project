## **✅ 1. 프로젝트 개요**

**주제:**

Kafka를 활용한 주문 이벤트 처리 시스템 구축

**구성 요소:**

- **order-producer**: 주문 요청을 받아 Kafka에 이벤트 전송 (Spring Boot + MySQL + Kafka)
- **order-consumer**: Kafka에서 주문 이벤트를 수신하여 로그 DB에 저장 (Spring Boot + MySQL)
- **Kafka**: 이벤트 중심 메시지 브로커
- **MySQL**: 각 모듈별 데이터 저장소 (eventdb, consumerdb)
- **Docker Compose**: Kafka, Zookeeper, 두 개의 MySQL 컨테이너 관리

---

## **✅ 2. 시스템 흐름도**

```java
[사용자 요청]
        ↓
        [order-producer 서버]
        ↓ Kafka 메시지 전송
[Kafka Topic: order-topic]
        ↓ Kafka 메시지 수신
[order-consumer 서버]
        ↓
        [consumer DB에 저장]
```

---

## **✅ 3. 핵심 기술 및 설정 요약**

### **🔹 Kafka + Zookeeper + MySQL 구성 (Docker Compose)**

- docker-compose up
- spring 서버는 로컬에서 실행 (spring 서버 이미지로 빌드는 생략)

```yaml
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

  mysql-event:
    image: mysql:8.0
    ports:
      - "3307:3306"
    environment:
      MYSQL_ROOT_PASSWORD: event1234
      MYSQL_DATABASE: eventdb

  mysql-processor:
    image: mysql:8.0
    ports:
      - "3308:3306"
    environment:
      MYSQL_ROOT_PASSWORD: processor1234
      MYSQL_DATABASE: consumerdb
```

---

### **🔹 Kafka producer 설정 (application.yml)**

- **key-serializer**: 메시지의 key를 문자열로 직렬화
- **value-serializer**: 메시지의 value를 JSON 형식으로 직렬화

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

### **🔹 Kafka consumer 설정 (application.yml)**

- **group-id**: 컨슈머 그룹 ID — 같은 그룹에 속한 컨슈머들은 메시지를 나눠서 받음 (로드밸런싱)
- **earliest**: 처음 실행될 때 소비자 그룹의 offset이 없으면 가장 오래된 메시지부터 수신
- **trusted.packages**: 역직렬화할 클래스 패키지를 허용. *은 모든 패키지 허용 (주의: 보안상 프로덕션에선 지양)

```yaml
spring.kafka:
  bootstrap-servers: localhost:9092
  consumer:
    group-id: order-group
    auto-offset-reset: earliest
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    properties:
      spring.json.trusted.packages: '*'
```

---

## **✅ 4. 핵심 코드 설명**

### **🟦 order-producer**

### **- 주문 이벤트 전송**

```java
@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, OrderRequestDto> kafkaTemplate;

    @Value("${kafka.topic}")
    private String topic;

    public void send(OrderRequestDto dto) {
        kafkaTemplate.send(topic, dto);
    }
}
```

- **역할:** 주문 요청 → DB 저장 → Kafka 이벤트 전송
- **Kafka Producer:** 직접 생성하여 order-topic에 DTO 전송

---

### **🟩 order-consumer**

- **KafkaConsumerConfig** : Kafka 수신기 설정 (토픽, 그룹, 역직렬화 등)
- @**KafkaListener** :  Kafka 메시지를 리스닝하여 자동 실행되는 메서드

### **Kafka 메시지 수신 및 로그 DB 저장**

```java
// KafkaConsumerConfig
@EnableKafka // Kafka 리스너 활성화
@Configuration // 스프링 설정 클래스임을 명시
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderRequestDto> consumerFactory() {
        // Kafka 메시지를 OrderRequestDto로 역직렬화하기 위한 설정
        JsonDeserializer<OrderRequestDto> deserializer = new JsonDeserializer<>(OrderRequestDto.class);
        deserializer.setRemoveTypeHeaders(false); // 헤더 제거 비활성화
        deserializer.addTrustedPackages("*");     // 모든 패키지 신뢰 (보안상 운영환경에선 주의)
        deserializer.setUseTypeMapperForKey(true); // Key에도 타입 매핑 사용

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Kafka 서버 주소
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");             // Kafka 컨슈머 그룹 ID
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);  // Key 역직렬화
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);            // Value 역직렬화

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderRequestDto> kafkaListenerContainerFactory() {
        // Kafka 리스너에 사용할 팩토리 생성
        ConcurrentKafkaListenerContainerFactory<String, OrderRequestDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory()); // 위에서 정의한 컨슈머 팩토리 등록
        return factory;
    }
}

//OrderConsumer
@Component // 스프링 빈으로 등록
@RequiredArgsConstructor // 생성자 주입 자동 생성
public class OrderConsumer {

    private final OrderConsumerService orderConsumerService; // 비즈니스 로직 처리 서비스 주입

    @KafkaListener(
        topics = "order-topic",                      // 수신할 토픽 이름
        groupId = "order-group",                     // 컨슈머 그룹 ID
        containerFactory = "kafkaListenerContainerFactory" // 사용할 컨테이너 팩토리
    )
    public void consume(OrderRequestDto dto) {
        // Kafka에서 받은 메시지를 서비스에 전달하여 DB 저장
        orderConsumerService.save(dto);
    }
}
```

- **Kafka 설정 → 수신 → DTO 변환 → 서비스 호출(DB 저장)**
- **역할:** Kafka 메시지를 소비하고 DB에 저장
- **Kafka Consumer:** @KafkaListener로 order-topic을 지속 수신

---

> ✔️ producer는 group-id 필요 없음
>

> ✔️ consumer는 group-id 반드시 필요함 -
•  **같은 그룹의 컨슈머끼리 메시지를 나눠서 받는 것**을 보장
>