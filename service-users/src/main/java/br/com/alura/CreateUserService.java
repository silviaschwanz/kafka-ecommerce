package br.com.alura;

import br.com.alura.consumer.ConsumerService;
import br.com.alura.consumer.ServiceRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.SQLException;
import java.util.UUID;

public class CreateUserService implements ConsumerService<Order> {

    private final LocalDatabase database;

    public CreateUserService() throws SQLException {
        this.database = new LocalDatabase("users_database");
        this.database.createIfNotExists(
                "create table users (" +
                        "uuid varchar(200) primary key, " +
                        "email varchar(200)))"
        );
    }

    public static void main(String[] args) {
        new ServiceRunner<>(CreateUserService::new).start(1);
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws SQLException {
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, checking for new user");
        System.out.println(record.value());
        var message = record.value();
        var order = message.getPayload();
        if(isNewUser(order.getEmailAddress())) {
            insertNewUser(order.getEmailAddress());
        }
    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return CreateUserService.class.getSimpleName();
    }

    private void insertNewUser(String email) throws SQLException {
        var uuid = UUID.randomUUID().toString();
        database.update("insert into users (uuid, email) values (?,?)", uuid, email);
        System.out.println("User " + uuid + " added - email " + email);
    }

    private boolean isNewUser(String email) throws SQLException {
        var results = database.query("select uuid from users where email = ? limit 1", email);
        return !results.next();
    }

}
