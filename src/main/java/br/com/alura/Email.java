package br.com.alura;

public class Email {

    private final String userId;
    private final String subject;
    private final String body;

    public Email(String userId, String subject, String body) {
        this.userId = userId;
        this.subject = subject;
        this.body = body;
    }
}
