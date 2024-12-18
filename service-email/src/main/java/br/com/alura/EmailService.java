package br.com.alura;

import br.com.alura.consumer.ConsumerService;
import br.com.alura.consumer.ServiceRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.ExecutionException;

public class EmailService implements ConsumerService<Email> {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new ServiceRunner(EmailService::new).start(5);
    }

    public String getTopic() {
        return "ECOMMERCE_SEND_EMAIL";
    }

    public String getConsumerGroup() {
        return EmailService.class.getSimpleName();
    }

    public void parse(ConsumerRecord<String, Message<Email>> record) {
        System.out.println("Sending email");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Email sent");
    }

}

