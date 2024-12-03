package br.com.alura;

import br.com.alura.consumer.KafkaService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LogService {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var logService = new LogService();
        try (var kafkaService = new KafkaService<>(
                LogService.class.getSimpleName(),
                //Pattern.compile("ECOMMERCE.*"),
                Arrays.asList(
                        "ECOMMERCE_SEND_EMAIL",
                        "ECOMMERCE_NEW_ORDER",
                        "USER_GENERATE_READING_REPORT",
                        "SEND_MESSAGE_TO_ALL_USERS"
                ),
                logService::parse,
                Map.of(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName())
        )) {
            kafkaService.run();
        }

    }

    private void parse(ConsumerRecord<String, Message<String>> record) {
        System.out.println("------------------------------------------");
        System.out.println("LOG: " + record.topic());
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
    }

}

