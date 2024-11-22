package br.com.alura;

public class Email {

    private final String address;
    private final String subject;
    private final String body;

    public Email(String address, String subject, String body) {
        this.address = address;
        this.subject = subject;
        this.body = body;
    }

    public String getAddress() {
        return address;
    }

}
