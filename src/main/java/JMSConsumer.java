import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JMSConsumer {
    public void consumeMessages(int expectedCount, AtomicInteger receivedCount) throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("TEST.QUEUE");
        MessageConsumer consumer = session.createConsumer(destination);

        long start = System.currentTimeMillis();
        long timeout = 5000 + expectedCount * 2; // max time to wait for messages

        while (receivedCount.get() < expectedCount && (System.currentTimeMillis() - start) < timeout) {
            Message message = consumer.receive(1000);
            if (message != null) {
                receivedCount.incrementAndGet();
            }
        }

        consumer.close();
        session.close();
        connection.close();
    }

}
