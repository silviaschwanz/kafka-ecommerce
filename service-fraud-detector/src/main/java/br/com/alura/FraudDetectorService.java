package br.com.alura;

import br.com.alura.consumer.ConsumerService;
import br.com.alura.consumer.ServiceRunner;
import br.com.alura.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class FraudDetectorService implements ConsumerService<Order> {

    private final KafkaDispatcher<Order> orderKafkaDispatcher = new KafkaDispatcher<>();
    private final LocalDatabase database;

    public FraudDetectorService() throws SQLException {
        this.database = new LocalDatabase("frauds_database");
        this.database.createIfNotExists(
                "create table orders (" +
                        "uuid varchar(200) primary key, " +
                        "is_fraud boolean" +
                        ")"
        );
    }

    public static void main(String[] args) {
        new ServiceRunner<>(FraudDetectorService::new).start(1);
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException, SQLException {
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
        var message = record.value();
        var order = message.getPayload();
        if(wasProcessed(order)){
            System.out.println("Order " + order.getOrderId() + " was already processed");
            return;
        }
        if(order.invalidAmount()){
            database.update("insert into orders (uuid, is_fraud) values (?, true)", order.getOrderId());
            System.out.println("Order is a fraud because amount is " + order);
            orderKafkaDispatcher.send(
                    "ECOMMERCE_ORDER_REJECTED",
                    order.getEmailAddress(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
                    order
            );
        } else {
            database.update("insert into orders (uuid, is_fraud) values (?, false)", order.getOrderId());
            System.out.println("Order approved: " + order);
            orderKafkaDispatcher.send(
                    "ECOMMERCE_ORDER_APPROVED",
                    order.getEmailAddress(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
                    order
            );
        }
    }

    private boolean wasProcessed(Order order) throws SQLException {
        var results = database.query("select uuid from orders where uuid = ? limit 1", order.getOrderId());
        return results.next();
    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return FraudDetectorService.class.getSimpleName();
    }

}

