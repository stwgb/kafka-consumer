package com.github.stwgb;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class KafkaReplay {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(BasicKafkaConsumer.class.getName());

        final String bootstrapServer = "localhost:9094";
        final String topic = "basic-kafka-consumer-topic";
        final Integer partition = 0;
        final Long offset = 10L;

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        // we have string deserializer for key and value
        KafkaConsumer<String , String> kafkaConsumer = new KafkaConsumer<>(properties);

        // assign
        TopicPartition partitionToReadFrom = new TopicPartition(topic, partition);
        kafkaConsumer.assign(Arrays.asList(partitionToReadFrom));

        // seek
        kafkaConsumer.seek(partitionToReadFrom, offset);

        // poll data

        int numberOfMessagesToRead = 1;
        int numberOfMessagesRead = 0;
        boolean keepOnReading = true;

        while (keepOnReading){
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord consumerRecord : consumerRecords){
                numberOfMessagesRead++;
                logger.info("Received record: \n"
                        + ">>> Topic: " + consumerRecord.topic() + "\n"
                        + ">>> Partition: " + consumerRecord.partition() + "\n"
                        + ">>> Offset: " + consumerRecord.offset() + "\n"
                        + ">>> Timestamp: " + consumerRecord.timestamp() + "\n"
                        + ">>> Key: " + consumerRecord.key() + "\n"
                        + ">>> Value: " + consumerRecord.value()
                );

                if(numberOfMessagesRead >= numberOfMessagesToRead){
                    keepOnReading = false;
                    break;
                }
            }
        }

        logger.info("closing consumer");
    }
}
