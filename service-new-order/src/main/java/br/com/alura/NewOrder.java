package br.com.alura;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrder {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (var orderKafkaDispatcher = new KafkaDispatcher<Order>()) {
            try (var emailKafkaDispatcher = new KafkaDispatcher<Email>()) {
                for (var i = 0; i < 10; i++) {
                    var emailValue = Math.random() + "@gmail.com";
                    var emailSubject = "Processing you order!";
                    var emailBody = "Welcome! We are processing you order!";
                    var email = new Email(emailValue, emailSubject, emailBody);
                    var orderId = UUID.randomUUID().toString();
                    var amount = new BigDecimal(Math.random() * 5000 + 1);
                    var order = new Order(orderId, amount, email);

                    orderKafkaDispatcher.send(
                            "ECOMMERCE_NEW_ORDER",
                            emailValue,
                            new CorrelationId(NewOrder.class.getSimpleName()),
                            order
                    );

                    emailKafkaDispatcher.send(
                            "ECOMMERCE_SEND_EMAIL",
                            emailValue,
                            new CorrelationId(NewOrder.class.getSimpleName()),
                            email
                    );
                }
            }
        }
    }

}
