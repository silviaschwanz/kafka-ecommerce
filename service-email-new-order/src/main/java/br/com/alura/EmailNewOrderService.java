package br.com.alura;

import br.com.alura.consumer.ConsumerService;
import br.com.alura.consumer.ServiceRunner;
import br.com.alura.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.ExecutionException;

public class EmailNewOrderService implements ConsumerService<Order> {

    private final KafkaDispatcher<Email> kafkaDispatcher = new KafkaDispatcher<>();

    public static void main(String[] args) {
        new ServiceRunner<>(EmailNewOrderService::new).start(1);
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
        System.out.println("Processing new order, preparing email");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        var message = record.value();
        var order = message.getPayload();
        var email = order.getEmail();
        var id = message.getId().continueWith(EmailNewOrderService.class.getSimpleName());
        kafkaDispatcher.send(
                "ECOMMERCE_SEND_EMAIL",
                email.getAddress(),
                id,
                email
        );
    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return "1" + EmailNewOrderService.class.getSimpleName();
    }

}
