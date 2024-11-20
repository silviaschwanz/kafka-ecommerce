package br.com.alura;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrder {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (var orderKafkaDispatcher = new KafkaDispatcher<Order>()) {
            var userId = UUID.randomUUID().toString();
            var orderId = UUID.randomUUID().toString();
            var amount = new BigDecimal(Math.random() * 5000 + 1);
            var order = new Order(userId, orderId, amount);
            orderKafkaDispatcher.send("ECOMMERCE_NEW_ORDER", userId, order);
        }
        try (var emailKafkaDispatcher = new KafkaDispatcher<Email>()) {
            var userId = UUID.randomUUID().toString();
            var subject = "Processing you order!";
            var body = "Welcome! We are processing you order!";
            var email = new Email(userId, subject, body);
            emailKafkaDispatcher.send("ECOMMERCE_SEND_EMAIL", userId, email);
        }
    }

}
