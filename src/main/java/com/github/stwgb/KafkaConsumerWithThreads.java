package com.github.stwgb;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class KafkaConsumerWithThreads {
    Logger logger = LoggerFactory.getLogger(KafkaConsumerWithThreads.class.getName());

    public static void main(String[] args) {
        new KafkaConsumerWithThreads().run();
    }

    public KafkaConsumerWithThreads() {

    }

    public void run() {

        final String bootstrapServer = "localhost:9094";
        final String groupId = "kafka-consumer-with-threads";
        final String topic = "kafka-consumer-threads-topic";

        CountDownLatch latch = new CountDownLatch(1);

        Runnable kafkaConsumerThread = new ConsumerThread(latch, bootstrapServer, groupId, topic);

        Thread thread = new Thread(kafkaConsumerThread);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Caught shutdown hook");
            ((ConsumerThread)kafkaConsumerThread).shutdown();
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Kafka Consumer got interrupted", e);
        } finally {
            logger.info("Closing Kafka Consumer");
        }
    }

    public class ConsumerThread implements Runnable {

        // latch to shutdown the application
        private CountDownLatch latch;
        private KafkaConsumer<String, String> kafkaConsumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerThread.class.getName());

        public ConsumerThread(CountDownLatch latch, String bootstrapServer, String groupId, String topic) {


            this.latch = latch;
            // we have string deserializer for key and value
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            this.kafkaConsumer = new KafkaConsumer<>(properties);

            // use Arrays.asList to subscribe to multiple topics
            kafkaConsumer.subscribe(Collections.singletonList(topic));
        }

        @Override
        public void run() {

            try {
                while (true) {
                    ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord consumerRecord : consumerRecords) {
                        this.logger.info("Received record: \n"
                                + ">>> Topic: " + consumerRecord.topic() + "\n"
                                + ">>> Partition: " + consumerRecord.partition() + "\n"
                                + ">>> Offset: " + consumerRecord.offset() + "\n"
                                + ">>> Timestamp: " + consumerRecord.timestamp() + "\n"
                                + ">>> Key: " + consumerRecord.key() + "\n"
                                + ">>> Value: " + consumerRecord.value()
                        );
                    }
                }
            } catch (WakeupException e) {
                this.logger.info("shutting down");
            } finally {
                this.kafkaConsumer.close();
                // finished
                this.latch.countDown();
            }
            // poll data


        }

        public void shutdown() {
            // interrupt the poll method
            this.kafkaConsumer.wakeup();
        }
    }


}


