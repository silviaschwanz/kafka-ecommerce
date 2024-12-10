package br.com.alura;

import java.math.BigDecimal;

public class Order {

    private final Email email;
    private final String orderId;
    private final BigDecimal amount;

    public Order(Email email, String orderId, BigDecimal amount) {
        this.email = email;
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getEmailAddress() {
        return email.getAddress();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean invalidAmount() {
        if(amount.compareTo(new BigDecimal("4500")) >= 0) {
           return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Order{" +
                "email=" + email.getAddress() +
                ", orderId='" + orderId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
