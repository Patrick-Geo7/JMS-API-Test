import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsumerLatencyTest {
    public static void main(String[] args) {
        try {
            // Connect to ActiveMQ server
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create queue
            Destination destination = session.createQueue("LATENCY.QUEUE");

            // Create consumer
            MessageConsumer consumer = session.createConsumer(destination);
            // Latency Times
            List<Long> latencies = new ArrayList<>();

            // Receive 1000 messages
            for (int i=0 ; i<10000 ; i++) {
                Message message = consumer.receive();
                long nowMS = System.currentTimeMillis();
                latencies.add(nowMS - message.getJMSTimestamp());
            }

            Collections.sort(latencies);
            long medianLatency = latencies.get(latencies.size() / 2);
            System.out.println("Median Latency Time: " + medianLatency + " ms");
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
