package com.nashtech.order_service;

import com.nashtech.order_service.entities.Order;
import com.nashtech.order_service.kafka.OrderConsumer;
import com.nashtech.order_service.repository.OrderRepository;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));

    @BeforeAll
    static void createKafkaTopic() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        try (AdminClient admin = AdminClient.create(config)) {
            admin.createTopics(Collections.singleton(
                    new NewTopic("orders", 1, (short) 1)
            )).all().get();
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderConsumer orderConsumer;

    @Test
    void testOrderExist() throws InterruptedException {
        // Send the order via REST
        Order order = new Order("order123", "user123", 99.99);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/orders",
                order,
                String.class
        );

        assertThat(orderRepository.existsById("order123")).isTrue();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Wait for consumer to receive
        boolean messageConsumed = orderConsumer.getLatch().await(10, TimeUnit.SECONDS);
        assertThat(messageConsumed).isTrue();
        System.out.println("Consumer got payload: " + orderConsumer.getPayload());
    }
}
