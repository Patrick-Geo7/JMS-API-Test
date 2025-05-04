import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JMSProducer {
    public void sendMessages(int throughput) throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("TEST.QUEUE");
        MessageProducer producer = session.createProducer(destination);

        long interval = 1_000_000_000L / throughput;
        long sleepTime = (long) (interval - 0.2 * interval) / 1_000_000;
        long start = System.nanoTime();

        for (int i = 0; i < throughput; i++) {
            TextMessage message = session.createTextMessage("Throughput Test Message " + i);
            producer.send(message);
            Thread.sleep(sleepTime);

            while (System.nanoTime() - start < (i + 1) * interval) {
                // wait
            }
        }

        session.close();
        connection.close();
    }
}

