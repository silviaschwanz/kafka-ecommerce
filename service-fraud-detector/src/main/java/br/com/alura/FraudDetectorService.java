package br.com.alura;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class FraudDetectorService {

    private final KafkaDispatcher<Order> orderKafkaDispatcher = new KafkaDispatcher<>();

    public static void main(String[] args) {
        var fraudService = new FraudDetectorService();
        try (var kafkaService = new KafkaService<>(
                FraudDetectorService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                fraudService::parse,
                Order.class,
                new HashMap<>()
        )) {
            kafkaService.run();
        }
    }

    private void parse(ConsumerRecord<String, Order> record) throws ExecutionException, InterruptedException {
        System.out.println("Processing new order, checking for fraud");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var order = record.value();
        if(order.invalidAmount()){
            System.out.println("Order is a fraud because amount is " + order.getAmount());
            orderKafkaDispatcher.send("ECOMMERCE_ORDER_REJECTED", order.getEmailAddress(), order);
        } else {
            System.out.println("Order approved: " + order);
            orderKafkaDispatcher.send("ECOMMERCE_ORDER_APPROVED", order.getEmailAddress(), order);
        }
    }

}

