import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.Random;

public class ProducerLatencyTest {
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

            // Create producer
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // Prepare message
            byte[] bytes = new byte[1024];
            new Random().nextBytes(bytes);

            // Send 10000 messages
            for (int i = 0; i < 10000; i++) {
                BytesMessage message = session.createBytesMessage();
                message.writeBytes(bytes);
                producer.send(message);
                // Notify every 1000 messages
                if ((i+1)%1000 == 0) {
                    System.out.println("Message " + (i+1) + " is sent");
                }
            }

            session.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
