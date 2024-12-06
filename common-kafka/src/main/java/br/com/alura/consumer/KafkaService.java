package br.com.alura.consumer;

import br.com.alura.Message;
import br.com.alura.dispatcher.GsonSerializer;
import br.com.alura.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class KafkaService<T> implements Closeable {

    private final KafkaConsumer<String, Message<T>> consumer;
    private final ConsumerFunction parse;

    private KafkaService(String groupId, ConsumerFunction<T> parse, Map<String, String> properties) {
        this.consumer = new KafkaConsumer<>(getProperties(groupId, properties));
        this.parse = parse;
    }

    public KafkaService(String groupId, String topic, ConsumerFunction<T> parse, Map<String, String> properties) {
        this(groupId, parse, properties);
        consumer.subscribe(Collections.singleton(topic));
    }

    public KafkaService(String groupId, Pattern topic, ConsumerFunction<T> parse, Map<String, String> properties) {
        this(groupId, parse, properties);
        consumer.subscribe(topic);
    }

    public KafkaService(String groupId, List<String> topics, ConsumerFunction<T> parse, Map<String, String> properties) {
        this(groupId, parse, properties);
        consumer.subscribe(topics);
    }

    public void run() throws ExecutionException, InterruptedException {
        try(var dispatcherDeadLetter = new KafkaDispatcher<>()) {
            while (true) {
                var records = consumer.poll(Duration.ofMillis(100));
                if(!records.isEmpty()) {
                    System.out.println("Encontrei " + records.count() + " registros");
                    for(var record: records) {
                        try {
                            parse.consume(record);
                        } catch (Exception e) {
                            e.printStackTrace();
                            var message = record.value();
                            dispatcherDeadLetter.send(
                                    "ECOMMERCE_DEADLETTER",
                                    message.getId().toString(),
                                    message.getId().continueWith("DeadLetter"),
                                    new GsonSerializer<>().serialize("", message)
                            );
                        }
                    }
                }
            }
        }
    }

    private Properties getProperties(String groupID, Map<String, String> overrideProperties) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:19092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,"1");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        properties.putAll(overrideProperties);
        return properties;
    }

    @Override
    public void close()  {
        consumer.close();
    }
}
