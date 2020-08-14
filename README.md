# kafka-consumer

## BasicKafkaConsumer

The basicKafkaConsumer is low level kafka-consumer with some basic configuration:

- BOOTSTRAP_SERVERS_CONFIG: point to at least one listener of you kafka-cluster
- KEY_DESERIALIZER_CLASS_CONFIG: the deserializer for the key
- VALUE_DESERIALIZER_CLASS_CONFIG: the deserializer for the value
- GROUP_ID_CONFIG: the group id -> used to scale the application. Eg a topic has three partitions it is possible to scale the consumer up to three. So every consumer ready from a single partition,
- AUTO_OFFSET_RESET_CONFIG: can be earliest, latest or none. You can choose where to start reading from the broker is no offset it tracked