# 🛒 Order Service – Spring Boot + Kafka + PostgreSQL + Testcontainers

This project is a **Spring Boot microservice** that:
- Accepts orders via a REST API.
- Saves them in **PostgreSQL**.
- Publishes them to **Apache Kafka** for downstream processing.
- Uses **Testcontainers** for clean, repeatable integration tests with real Dockerized Kafka + PostgreSQL.

---
##  Features

- **Spring Boot REST API** — place new orders with `POST /orders`  
- **PostgreSQL persistence** — store orders reliably  
- **Apache Kafka Producer** — send order messages to Kafka topics  
- **Kafka Consumer** — receive and log messages for verification  
- **Testcontainers** — spin up real Kafka & Postgres in Docker for end-to-end tests  
- **Production-like tests** — no mocks, real services

## Project Structure
```
order-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nashtech/order_service/
│   │   │       ├── OrderServiceApplication.java
│   │   │       ├── controller/
│   │   │       │   └── OrderController.java
│   │   │       ├── entities/
│   │   │       │   └── Order.java
│   │   │       ├── repository/
│   │   │       │   └── OrderRepository.java
│   │   │       ├── kafka/
│   │   │       │   └── OrderConsumer.java
│   │   │       └── config/
│   │   │           ├── KafkaProducerConfig.java
│   │   │           └── AppConstants.java
│   ├── resources/
│   │   ├── application.properties
│   │   └── application-cloud.properties  # Optional if you use cloud config
├── src/
│   ├── test/
│   │   ├── java/
│   │   │   └── com/nashtech/order_service/
│   │   │       ├── OrderControllerIntegrationTest.java
│   │   │       └── OrderServiceApplicationTests.java  # Optional default test
├── docker-compose.yml
├── pom.xml
├── README.md
└── .gitignore

```

## Tech Stack

- **Spring Boot 3.x**
- **Spring Data JPA**
- **Apache Kafka** (Confluent)
- **PostgreSQL**
- **Docker Compose** (for local dev)
- **Testcontainers** (for tests)

---

## What is Testcontainers?
TestContainers is an open-source Java library that provides a lightweight and flexible framework for running integration tests inside Docker containers. It allows developers to easily spin up containers for databases, message queues, web servers, and other external services required by their applications during testing. This ensures that the testing environment closely mimics the production environment, providing more accurate and reliable results.

## Why Testcontainers?

**Before Testcontainers:**
- You manually install Kafka/Postgres on your machine.
- Tests depend on your local environment being perfect.
- If ports are used, tests break.
- Tests run slower and are fragile in CI/CD.

**After Testcontainers:**
- Testcontainers **spins up disposable Kafka & Postgres** inside Docker **only for tests**.
- Tests run in isolation, same everywhere (local or CI).
- No manual installation, no conflicts.
- Makes your integration tests **reliable, repeatable, and realistic**.

---

## How to Run the Application

Clone the Repo:

```declarative
git clone https://github.com/rahulsharma9001/Tech-hub-project.git
cd order-service
```
## Local Development Setup
To run Kafka and PostgreSQL for manual dev, use docker-compose.yml.
```declarative
version: '3.8'

services:
  postgres:
    image: postgres:16
    container_name: order-postgres
    environment:
      POSTGRES_DB: testdb
      POSTGRES_USER: testuser
      POSTGRES_PASSWORD: testpassword
    ports:
      - "5432:5432"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.2.1
    container_name: order-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.2.1
    container_name: order-kafka
    ports:
      - "9093:9093"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper
```
Run:
```declarative
docker-compose up -d
```
This will:
- Start **PostgreSQL** on localhost:5432

- Start **Zookeeper** & **Kafka broker** on localhost:9093

## Run the App
```declarative
./mvnw spring-boot:run
```

## Test with Postman or cURL
```declarative
curl -X POST http://localhost:8081/orders \
     -H "Content-Type: application/json" \
     -d '{"orderId":"order123", "userId":"user123", "amount":99.99}'
```
Expected response:

```declarative
json

{
  "message": "Order saved and posted successfully"
}
```

✅ Order is stored in DB → published to Kafka → consumed by the listener → logged in console.

## Running Test With Testcontainers

To run tests with Testcontainers, simply:

```declarative
./mvnw test
```
The test class spins up Kafka & Postgres in containers, runs tests, tears them down — no manual work.

## How to Verify
- Check PostgreSQL manually with psql or any DB tool.

- Check Kafka topics with tools like Kafka Tool, Kafdrop, or Conduktor.

    - Or use the logs — your OrderConsumer will log consumed payloads!

## Summary: Compose vs. Testcontainers
| Use Case                        | Use Compose? | Use Testcontainers? | Explanation                                                                                                                                                                                                                  |
| ------------------------------- | ------------ | ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Local Manual Development**    | ✅ Yes        | ❌ No                | When you are **developing or debugging locally**, you need services like **Kafka** and **Postgres** running **continuously**. Use `docker-compose up` to spin them up once and keep them available for your Spring Boot app. |
| **Automated Integration Tests** | ❌ No         | ✅ Yes               | For **automated tests** (`mvn test`), Testcontainers **starts Kafka and Postgres containers programmatically**, just for the test scope. You don’t need to manage Docker Compose or manual containers here.                  |
| **CI/CD Pipeline Tests**        | ❌ No         | ✅ Yes               | In **CI/CD pipelines** (GitHub Actions, Jenkins, GitLab, etc.), you don’t want manual setup. Testcontainers **automatically runs Kafka/Postgres containers on the build agent**, making tests **portable and reproducible**. |


## Rule of thumb:

- docker-compose up → manual local dev

- Testcontainers → mvn test → runs containers automatically

## Author
Designed & Developed By - Rahul Sharma

Maintained by NashTech — for internal Tech Hub templates & learning.

