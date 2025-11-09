package com.tuca.service;

import com.tuca.manager.QueueManager;
import com.tuca.model.Task;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

@Service
public class ConsumerService {

    private final Logger log = LoggerFactory.getLogger(ConsumerService.class);
    private KafkaConsumer<String, String> consumer;
    private final QueueManager queueManager;
    private final TaskService taskService;
    private volatile boolean listening = false;

    @Autowired
    public ConsumerService(QueueManager queueManager, TaskService taskService) {
        this.queueManager = queueManager;
        this.taskService = taskService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        init();
        log.info("[Tasks] Application ready, tasks loaded");
    }

    public void init() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "swing-consumer-group");
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "swing-consumer-group-unique-id");

        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000"); // 3 segundos
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");   // 10 segundos
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("swing-topic"));
        startListening();
        log.info("[Queue] Kafka Consumer Started");
    }

    private void processRecord(ConsumerRecord<String, String> recordMessage) {

        try {
            JSONObject json = new JSONObject(recordMessage.value());
            String event = json.optString("event");

            if (!isValidEvent(event)) return;

            long taskID = json.optLong("taskID");
            Task task = taskService.getByID(taskID);
            handleEvent(event, json, task);

        } catch (Exception e) {
            log.error("[Queue] Error processing record: {}", recordMessage.value(), e);
        }
    }

    private boolean isValidEvent(String event) {
        if (event == null || event.isEmpty()) return false;

        String[] accepted = {"UPDATE_DESCRIPTION", "UPDATE_STATUS", "CREATE_TASK", "DELETE_TASK", "CLOSING_PROGRAM"};
        return Arrays.asList(accepted).contains(event.toUpperCase());
    }

    private void handleEvent(String event, JSONObject json, Task task) {
        String newValue = json.optString("newValue", null);

        switch (event.toUpperCase()) {
            case "UPDATE_DESCRIPTION" -> taskService.update("DESCRIPTION", task, newValue);
            case "UPDATE_STATUS" -> taskService.update("STATUS", task, newValue);
            case "CREATE_TASK" -> taskService.create(task);
            case "DELETE_TASK" -> {
                log.info("testando..");
                taskService.delete(task.getId());
            }
            case "CLOSING_PROGRAM" -> taskService.saveAll();
            default -> consumer.commitAsync();

        }
        consumer.commitAsync();
    }

    @PostConstruct
    public void startListening() {
        if (consumer == null || listening) return;

        Thread listenerThread = new Thread(() -> {
            log.info("[Queue] Kafka Consumer Listening, queue size: {}", queueManager.getQueue().size());
            listening = true;

            try {
                while (listening) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                    records.forEach(this::processRecord);
                }
            } catch (org.apache.kafka.common.errors.WakeupException e) {
                if (listening) throw e;
            } finally {
                consumer.commitAsync();
                log.info("[Queue] Kafka Consumer committed.");
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}