# kafka-consumer

## BasicKafkaConsumer

The basicKafkaConsumer is low level kafka-consumer with some basic configuration:

- BOOTSTRAP_SERVERS_CONFIG: point to at least one listener of you kafka-cluster
- KEY_DESERIALIZER_CLASS_CONFIG: the deserializer for the key
- VALUE_DESERIALIZER_CLASS_CONFIG: the deserializer for the value
- GROUP_ID_CONFIG: the group id -> used to scale the application. Eg a topic has three partitions it is possible to scale the consumer up to three. So every consumer ready from a single partition,
- AUTO_OFFSET_RESET_CONFIG: can be earliest, latest or none. You can choose where to start reading from the broker is no offset it tracked

## KafkaConsumerWithThreads

Threads enable to interrupt the while true loop. With threads, it is a better way to shut down the application.
Therefore, we create a class which implements `Runnable`, override the `run` method and create a `shutdown` method.
In the `shutdown` method we can interrupt the `poll`. This will throw the `WakeUpException`.

## Assign and Seek

Assign and Seek is another API that is used to reply data is the most cases.
This kind of consumer has no group id. First we need to assign a single or multiple
topics to read from. Then we seek to a specific offset. If we have read our messages 
we shut down the consumer. 

## AdvancedKafkaConsumer

The advanced kafka consumer has some additional features like delivery semantics,
idempotence, poll behavior, commit of offsets, batching and offset reset behaviour.

### Delivery Semantics

- **At most once:** The offsets get commit as soon as the message batch gets received by the kafka consumer.
If something goes wrong during the processing of the batch, the messages will get lost.

- **At least once:** At least once is the default, and the offsets get committed after the 
messages have been processed. This can lead to a duplicated processing of the messages.
To counter the duplication the consumer should be idempotent. To make the processing idempotent
you need to use something unique in example an id to store messages in a database. This id 
can be in the message itself or a kafka generic id. The kafka generic id is a concatenation by
`topic + partition + offset`. 

### Poll Behavior

Kafka Consumer poll messages. This allows, to control where the consumer should start reading,
how fast the consumer reads, and the ability to replay messages. To control the behavior you can set up:

- `fetch.min.bytes:` The default is `1` and controls how many messages the consumer pulls on one request.
Increasing the minimum of bytes increases the throughput and decreases the number of requests at the cost of latency.

- `max.poll.records:` The default is `500` and controls how many messages get fetched by one poll request. 
Can be increased if messages are small and lots of ram is available. If you always get the max request per poll 
(monitor it) then increase the max poll records. 

- `max.partitions.fetch.bytes:` The default is `1MB` and is the maximum data returned by the kafka broker per partition.
So if you read from a lot of partition you need a lot of ram. 

- `fetch.max.bytes:` The default is `50MB` and is the maximum data returned by the broker and covers
multiple partitions.

These settings should only be change if you reached the maximum throughput. 

### Commits

There are several commit strategies, the two most common are:

- `enable.auto.commit=true` with a synchronous processing of the batches. So the consumer polls
again if the processing, is done for all received messages. The offsets will be committed with an interval of `5000ms` 
per default. If you don't process your messages synchronous you'll end up in a `at most once `
behaviour. 

- `enable.auto.commit=false` with a manual commit of the offsets. So have to commit the offsets after
you have done something synchronous with the batch. 

### Batching

Batching is used to make the consumer more efficient. In example for inserting data in da Database
a so called bulk request inserts multiple data at once.    

### Offset Reset Behaviour

If an error occures and the consumer gets down. If the consumer is down for longer than the retention time 
of the topic the offsets are invalid. That is where the `auto.offset.reset.setting` setting comes in:


- `auto.offset.reset.setting:` The default is latest, so the consumer will read again from the end.  
Or it can be `earliest` then the consumer will read from the beginning. It also can be `none` then an exception gets 
thrown.  

Additionally the offsets get lost if a consumer does not read data within 7 days. This can be controlled by broker 
settings `offset.retention.ms`.
   