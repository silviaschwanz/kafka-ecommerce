package br.com.alura;

import java.math.BigDecimal;

public class Order {

    private final String orderId;
    private final BigDecimal amount;
    private final Email email;

    public Order(String orderId, BigDecimal amount, Email email) {
        this.orderId = orderId;
        this.amount = amount;
        this.email = email;
    }

    public String getEmailAddress() {
        return email.getAddress();
    }

}
