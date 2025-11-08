package com.tuca.service;

import com.tuca.model.Task;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;

public class ProducerService {

    private KafkaProducer<String, String> producer;
    private final Logger log = LoggerFactory.getLogger(ProducerService.class);
    private final JSONObject jsonObject = new JSONObject();

    public ProducerService() {
        initProducer();
    }

    private void initProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");


        producer = new KafkaProducer<>(props);
        log.info("[Queue] Kafka Producer Started");
    }

    public void sendMessage(String message) {

        jsonObject.put("appId", UUID.randomUUID().toString());
        jsonObject.put("message", message);
        send(jsonObject);

    }

    public void sendEvent(Task task, String eventName, String newValue) {
        JSONObject event = createBaseEvent(eventName);
        event.put("taskID", task.getId());
        event.put("newValue", newValue);
        send(event);
    }

    public void sendEvent(String eventName) {
        send(createBaseEvent(eventName));
    }

    public void sendEvent(String eventName, String taskID) {
        JSONObject event = createBaseEvent(eventName);
        event.put("taskID", taskID);
        send(event);
    }

    private JSONObject createBaseEvent(String eventName) {
        JSONObject event = new JSONObject();
        event.put("event", eventName);
        return event;
    }

    private void send(Object payload) {

        ProducerRecord<String, String> stringProducerRecord = new ProducerRecord<>("swing-topic", jsonObject.toString());
        producer.send(stringProducerRecord, (metadata, exception) -> {
            if (exception != null) {
                log.error("[Queue] Kafka Producer Send Error", exception);
            } else {
                log.info("[Queue] Kafka Producer Send Success, offset: {}, msg: {}", metadata.offset(), payload);
            }
        });
    }
}