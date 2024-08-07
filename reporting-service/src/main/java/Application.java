import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Application {
    private static final String VALID_TRANSACTIONS_TOPIC = "valid-transactions";
    private static final String SUSPICIOUS_TRANSACTIONS_TOPIC = "suspicious-transactions";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092,localhost:9093,localhost:9094";

    public static void main(String[] args) {
        String consumerGroup = "reporting-service";

        System.out.println("Consumer is part of consumer group " + consumerGroup);

        Consumer<String, Transaction> kafkaConsumer = createKafkaConsumer(BOOTSTRAP_SERVERS, consumerGroup);

        consumeMessages(Collections.unmodifiableList(Arrays.asList(SUSPICIOUS_TRANSACTIONS_TOPIC, VALID_TRANSACTIONS_TOPIC)), kafkaConsumer);
    }

    public static void consumeMessages(List<String> topics, Consumer<String, Transaction> kafkaConsumer) {
        /**
         * Fill in the code here to subscribe to the provided topics
         * Run in a loop and consume all the transactions
         * Record the transactions for reporting based on the topic
         */
        kafkaConsumer.subscribe(topics);

        while (true) {
            ConsumerRecords<String, Transaction> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(1));

            if (consumerRecords.isEmpty()) {
                // ...
            }

            for (ConsumerRecord<String, Transaction> consumerRecord : consumerRecords) {
                recordTransactionForReporting(consumerRecord.topic(), consumerRecord.value());
            }

            kafkaConsumer.commitAsync();
        }
    }

    public static Consumer<String, Transaction> createKafkaConsumer(String bootstrapServers, String consumerGroup) {
        /**
         * Configure all the Kafka client parameters here
         * Create and return new Kafka consumer
         */
        Properties properties = new Properties();

        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Transaction.TransactionDeserializer.class.getName());

        return new KafkaConsumer<>(properties);
    }

    private static void recordTransactionForReporting(String topic, Transaction transaction) {
        if (topic.equals(SUSPICIOUS_TRANSACTIONS_TOPIC)) {
            System.out.println(String.format("Recording suspicious transaction for user %s, amount of " +
                            "$%.2f originating in %s for further investigation",
                    transaction.getUser(), transaction.getAmount(), transaction.getTransactionLocation()));

        } else if (topic.equals(VALID_TRANSACTIONS_TOPIC)) {
            System.out.println(String.format("Recording transaction for user %s, amount $%.2f to show it on user's " +
                    "monthly statement",
                    transaction.getUser(), transaction.getAmount()));
        }
    }

}
