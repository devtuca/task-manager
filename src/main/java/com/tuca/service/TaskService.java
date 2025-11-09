package com.tuca.service;

import com.tuca.cache.TaskCache;
import com.tuca.model.Task;
import com.tuca.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCache taskCache;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskCache taskCache) {
        this.taskRepository = taskRepository;
        this.taskCache = taskCache;
    }

    private final Logger log = LoggerFactory.getLogger(TaskService.class);


    public List<Task> getAll() {
        return taskCache.getTasks();
    }

    public Task getByID(long id) {
        return taskCache.getByID(id);
    }

    public void update(String type, Task task, String newValue) {
        boolean updated = true;

        switch (type) {
            case "DESCRIPTION" -> task.setDescription(newValue);
            case "STATUS" -> task.setStatus(newValue);
            case "OWNER_NAME" -> task.setOwnerName(newValue);
            default -> updated = false;
        }
        if (updated) saveAll();
    }


    public Task create(Task task) {
        if (!taskCache.contains(task.getId())) {

            log.info("[Tasks] Creating task in database with id {}", task.getId());
            taskCache.save(task);
            return taskRepository.save(task);
        }
        log.info("[Tasks] Created task with id {}", task.getId());
        return taskCache.save(task);
    }

    public void delete(long taskID) {
        log.info("[Tasks] Deleting task with id {}", taskID);
        taskCache.remove(taskID);
        taskRepository.deleteById(taskID);
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void saveAll() {
        log.info("[Tasks] Saving all tasks");
        taskRepository.saveAll(taskCache.getTasks());
    }

    @PostConstruct
    public void loadAll() {
        taskCache.loadAll();
        log.info("[Tasks] Loading all tasks");
    }
}
