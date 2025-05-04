import java.util.concurrent.atomic.AtomicInteger;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ThroughputTestRunner {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "TEST.QUEUE";

    public static void main(String[] args) {
        int throughput = 500;
        boolean success = true;

        while (success) {
            System.out.println("\n===============================");
            System.out.println("Running test with throughput: " + throughput + " msg/sec");

            // 1. Send messages
            success = runProducer(throughput);
            if (!success) break;

            // 2. Consume messages
            success = runConsumer(throughput);
            if (!success) break;

            // 3. Drain any remaining messages (if consumer didn't fully clean up)
            drainQueue();

            // 4. Double throughput for next test
            throughput *= 2;
        }

        System.out.println("Test finished.");
    }

    public static boolean runProducer(int throughput) {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(destination);

            long interval = 1_000_000_000L / throughput;
            long sleepTime = (long) (interval - 0.2 * interval) / 1_000_000;
            long start = System.nanoTime();

            for (int i = 0; i < throughput; i++) {
                TextMessage message = session.createTextMessage("Test Message " + i);
                producer.send(message);

                Thread.sleep(sleepTime);
                while (System.nanoTime() - start < (i + 1) * interval);
            }

            session.close();
            connection.close();
            return true;
        } catch (Exception e) {
            System.err.println("Producer failed at " + throughput + " msg/sec");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean runConsumer(int throughput) {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(destination);

            long interval = 1_000_000_000L / throughput;
            long sleepTime = (long) (interval - 0.2 * interval) / 1_000_000;
            long start = System.nanoTime();
            int received = 0;

            while (received < throughput) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    received++;
                    // System.out.println("Received: " + ((TextMessage) message).getText());
                } else {
                    System.out.println("Timeout waiting for message.");
                    break;
                }

                Thread.sleep(sleepTime);
                while (System.nanoTime() - start < (received) * interval);
            }

            session.close();
            connection.close();

            if (received == throughput) {
                System.out.println("Consumer received all messages.");
                return true;
            } else {
                System.err.println("Consumer failed to receive all messages.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Consumer failed at " + throughput + " msg/sec");
            e.printStackTrace();
            return false;
        }
    }

    public static void drainQueue() {
        try {
            System.out.println("Draining queue...");
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(destination);

            int drained = 0;
            while (true) {
                Message message = consumer.receive(500);
                if (message == null) break;
                drained++;
            }

            session.close();
            connection.close();

            System.out.println("Drained " + drained + " leftover messages.");
        } catch (Exception e) {
            System.err.println("Error draining queue:");
            e.printStackTrace();
        }
    }
}
