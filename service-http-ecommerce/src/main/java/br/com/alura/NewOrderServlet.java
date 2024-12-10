package br.com.alura;

import br.com.alura.dispatcher.KafkaDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderServlet extends HttpServlet implements Servlet {

    private final KafkaDispatcher<Order> orderKafkaDispatcher = new KafkaDispatcher<>();

    @Override
    public void destroy() {
        super.destroy();
        orderKafkaDispatcher.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var emailValue = req.getParameter("email");
            var amount = new BigDecimal(req.getParameter("ammount"));
            var orderId = req.getParameter("uuid");
            var emailSubject = "Processing you order!";
            var emailBody = "Welcome! We are processing you order!";
            var email = new Email(emailValue, emailSubject, emailBody);
            var order = new Order(orderId, amount, email);
            try(var database = new OrdersDatabase()) {
                if(database.saveNew(order)) {
                    orderKafkaDispatcher.send(
                            "ECOMMERCE_NEW_ORDER",
                            emailValue,
                            new CorrelationId(NewOrderServlet.class.getSimpleName()),
                            order
                    );
                    System.out.println("New Order sent successfully");
                    resp.getWriter().println("New Order sent successfully");
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    System.out.println("Old Order received");
                    resp.getWriter().println("Old Order received");
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
