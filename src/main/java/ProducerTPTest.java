import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Random;

public class ProducerTPTest {

    private static final long N = 2000;
    private static final long T_NS = (long) (1000000000.0 / N);
    private static final int SLEEP_TIME_NS = (int) (0.8*T_NS);

    public static void main(String[] args) throws Exception {
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

        // Prepare message
        byte[] bytes = new byte[1024];
        new Random().nextBytes(bytes);
        BytesMessage message = session.createBytesMessage();
        message.writeBytes(bytes);

        for (int i=0 ; i<N ; i++) {
            producer.send(message);
            Thread.sleep(0, SLEEP_TIME_NS);
        }

        session.close();
        connection.close();
    }
}
