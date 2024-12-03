package br.com.alura;

import br.com.alura.dispatcher.KafkaDispatcher;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrder {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (var orderKafkaDispatcher = new KafkaDispatcher<Order>()) {
            for (var i = 0; i < 10; i++) {
                var emailValue = Math.random() + "@gmail.com";
                var emailSubject = "Processing you order!";
                var emailBody = "Welcome! We are processing you order!";
                var email = new Email(emailValue, emailSubject, emailBody);
                var orderId = UUID.randomUUID().toString();
                var amount = new BigDecimal(Math.random() * 5000 + 1);
                var order = new Order(orderId, amount, email);
                var id = new CorrelationId(NewOrder.class.getSimpleName());
                orderKafkaDispatcher.send(
                        "ECOMMERCE_NEW_ORDER",
                        emailValue,
                        id,
                        order
                );

            }
        }
    }

}
