package com.nashtech.order_service.controller;

import com.nashtech.order_service.config.AppConstants;
import com.nashtech.order_service.entities.Order;
import com.nashtech.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

//    @PostMapping
//    public ResponseEntity<?> createOrder(@RequestBody Order order) {
//        kafkaTemplate.send(AppConstants.ORDER_TOPIC_NAME, order.orderId(), order);
//        return new ResponseEntity<>(Map.of("message", "Order posted successfully"), HttpStatus.OK);
//    }

    @Autowired
    public OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        // Save to DB
        Order entity = new Order(order.getOrderId(), order.getUserId(), order.getAmount());
        orderRepository.save(entity);

        // Publish to Kafka
        kafkaTemplate.send(AppConstants.ORDER_TOPIC_NAME, order.getOrderId(), order);

        return new ResponseEntity<>(Map.of("message", "Order saved and posted successfully"), HttpStatus.OK);
    }
}
