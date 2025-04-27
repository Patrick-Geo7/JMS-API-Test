import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JMSConsumer {
    public static void main(String[] args) {
        try {
            // Connect to ActiveMQ server
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create queue
            Destination destination = session.createQueue("TEST.QUEUE");

            // Create consumer
            MessageConsumer consumer = session.createConsumer(destination);
            // Consume Times
            List<Long> consumeTimes = new ArrayList<>();

            // Receive 1000 messages
            for (int i = 0; i < 1000; i++) {
                long start = System.nanoTime();
                Message message = consumer.receive();
                long responseTime = System.nanoTime() - start;
                consumeTimes.add(responseTime);
                if (message instanceof TextMessage textMessage) {
                    System.out.println("Received: " + textMessage.getText() + " | Response time: " + responseTime + " ns");
                }
            }
            Collections.sort(consumeTimes);
            long medianConsume = consumeTimes.get(consumeTimes.size() / 2);
            System.out.println("Median Consume Time: "+medianConsume+" ns");
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
