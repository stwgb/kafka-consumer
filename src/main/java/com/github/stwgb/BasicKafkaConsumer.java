package com.github.stwgb;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class BasicKafkaConsumer {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(BasicKafkaConsumer.class.getName());

        final String bootstrapServer = "localhost:9094";
        final String groupId = "basic-kafka-consumer";
        final String topic = "basic-kafka-consumer-topic";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        // we have string deserializer for key and value
        KafkaConsumer<String , String> kafkaConsumer = new KafkaConsumer<>(properties);

        // use Arrays.asList to subscribe to multiple topics
        kafkaConsumer.subscribe(Collections.singletonList(topic));

        // poll data

        while (true){
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord consumerRecord : consumerRecords){
                logger.info("Received record: \n"
                        + ">>> Topic: " + consumerRecord.topic() + "\n"
                        + ">>> Partition: " + consumerRecord.partition() + "\n"
                        + ">>> Offset: " + consumerRecord.offset() + "\n"
                        + ">>> Timestamp: " + consumerRecord.timestamp() + "\n"
                        + ">>> Key: " + consumerRecord.key() + "\n"
                        + ">>> Value: " + consumerRecord.value()
                );
            }
        }
    }
}
