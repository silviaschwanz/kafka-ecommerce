package br.com.alura;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderServlet extends HttpServlet implements Servlet {

    private final KafkaDispatcher<Order> orderKafkaDispatcher = new KafkaDispatcher<>();
    private final KafkaDispatcher<Email> emailKafkaDispatcher = new KafkaDispatcher<>();

    @Override
    public void destroy() {
        super.destroy();
        orderKafkaDispatcher.close();
        emailKafkaDispatcher.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var emailValue = req.getParameter("email");
            var amount = new BigDecimal(req.getParameter("ammount"));

            var emailSubject = "Processing you order!";
            var emailBody = "Welcome! We are processing you order!";
            var email = new Email(emailValue, emailSubject, emailBody);
            var orderId = UUID.randomUUID().toString();
            var order = new Order(orderId, amount, email);
            orderKafkaDispatcher.send("ECOMMERCE_NEW_ORDER", emailValue, order);
            emailKafkaDispatcher.send("ECOMMERCE_SEND_EMAIL", emailValue, email);
            System.out.println("New Order sent successfully");
            resp.getWriter().println("New Order sent successfully");
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (ExecutionException e) {
            throw new ServletException(e);
        } catch (InterruptedException e) {
            throw new ServletException(e);
        }
    }

}
