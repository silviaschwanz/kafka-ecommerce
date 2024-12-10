package br.com.alura;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

public class OrdersDatabase implements Closeable {

    private final LocalDatabase database;

    public OrdersDatabase() throws SQLException {
        this.database = new LocalDatabase("orders_database");
        this.database.createIfNotExists(
                "create table orders (" +
                        "uuid varchar(200) primary key)"
        );
    }

    public boolean saveNew(Order order) throws SQLException {
        if(wasProcessed(order)) {
            return false;
        }
        database.update("insert into orders (uuid) values (?)", order.getOrderId());
        return true;
    }

    private boolean wasProcessed(Order order) throws SQLException {
        var results = database.query("select uuid from orders where uuid = ? limit 1", order.getOrderId());
        return results.next();
    }

    @Override
    public void close() {
        database.close();
    }
}
