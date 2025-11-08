package com.tuca.manager;

import com.tuca.model.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CacheManager {

    @Getter
    public List<Task> tasks = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);
    private final DatabaseManager db;

    @SneakyThrows
    public void load() {

        tasks = db.queryList(
                "SELECT * FROM tarefas",
                rs ->
                        new Task(
                                rs.getInt("id"),
                                rs.getString("description"),
                                rs.getString("ownerName"),
                                rs.getLong("startDate"),
                                rs.getLong("expiryDate"),
                                rs.getString("status")));

        log.info("[Tasks] Trying to caching from the database...");
        log.info("[Tasks] All tasks are catched..");
    }

    public Task getByID(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }

        log.error("[Task] Task with id {} not found in cache", id);
        return null;
    }

    public void save(Task task) {


        if (tasks.contains(task)) {
            tasks.set(tasks.indexOf(task), task);
            log.info("[Tasks] Task has been updated with name: {} and indexOf: {}", task.getDescription(), tasks.indexOf(task));
            return;

        }
        tasks.add(task);
        log.info("[Tasks] Task a description: {} has been added to cache.", task.getDescription());
        log.info("Now cache: {}", tasks);
    }

    public void remove(Task task) {
        if (!tasks.contains(task)) {
            log.error("[Tasks] Don't contain a task with description: {}.", task.getDescription());
            return;
        }

        tasks.remove(task);
        log.info("[Tasks] Task a description: {} has been removed from cache.", task.getDescription());
    }

    public void update(String type, Task task, String update) {
        boolean updated = true;

        switch (type.toUpperCase()) {
            case "DESCRIPTION" -> task.setDescription(update);
            case "STATUS" -> task.setStatus(update);
            case "OWNERNAME" -> task.setOwnerName(update);
            default -> updated = false;
        }

        if (updated) {
            save(task);
        }
    }
}