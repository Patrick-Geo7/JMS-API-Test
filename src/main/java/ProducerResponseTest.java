import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ProducerResponseTest {
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

            // Create producer
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //Produce Times
            List<Long> produceTimes = new ArrayList<>();

            // Prepare message
            byte[] bytes = new byte[1024];
            new Random().nextBytes(bytes);

            // Send 1000 messages
            for (int i = 0; i < 1000; i++) {
                String text = "Message #" + i;
                BytesMessage message = session.createBytesMessage();
                message.writeBytes(bytes);

                long start = System.nanoTime();
                producer.send(message);
                long responseTime = System.nanoTime() - start;
                produceTimes.add(responseTime);
                System.out.println("Sent message: " + text + " | Response time: " + responseTime + " ns");
            }

            Collections.sort(produceTimes);
            long medianProduce = produceTimes.get(produceTimes.size()/2);
            System.out.println("Median Produce Response Time: "+medianProduce+" ns");
            session.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
