package com.tuca.manager;

import com.tuca.model.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TaskManager {

    private final DatabaseManager db;
    @Getter
    private final Logger log = LoggerFactory.getLogger(TaskManager.class);
    private final CacheManager cacheManager;


    public void save(Task task) {
        if (!(contains(String.valueOf(task.getId())))) {
            create(task.getDescription(), task.getOwnerName(), (int) task.getExpiryDate());
            log.info("[Tasks] Task with ID: {} has been created in database.", task.getId());
        }
        db.update(
                "UPDATE tarefas SET description=?, ownerName=?, startDate=?, expiryDate=?, status=? WHERE id=?",
                task.getDescription(),
                task.getOwnerName(),
                task.getStartDate(),
                task.getExpiryDate(),
                task.getStatus(),
                task.getId()
        );
    }

    public void saveAll() {
        cacheManager.getTasks().forEach(this::save);
        log.info("[Tasks] Tasks have been saved in database.");
    }

    public void startAutoUpdateScheduler() {
        log.info("[Tasks] Starting auto update scheduler");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::saveAll, 0, 30, TimeUnit.SECONDS);
    }

    public int create(String description, String ownerName, int daysToExpire) {
        long nowMillis = System.currentTimeMillis();
        long dayMillis = 86400000L;
        long expiryDateMillis = nowMillis + (dayMillis * daysToExpire);


        int generatedTaskID = db.insert("insert into tarefas(description, ownerName, startDate, expiryDate, status) values (?,?,?,?,?)",
                description, ownerName, nowMillis, expiryDateMillis, "Pendente");
        Task task = new Task(generatedTaskID, description, ownerName, nowMillis, expiryDateMillis, "Pendente");
        cacheManager.save(task);

        log.info("[Tasks] Created task with id: {}.", generatedTaskID);
        return generatedTaskID;

    }

    public void delete(Task task) {
        if (!contains(String.valueOf(task.getId()))) {
            log.error("[Tasks] Task with id {} don't contains a database.", task.getId());
            return;
        }
        db.update("delete from tarefas where id=?", task.getId());
    }


    public boolean contains(String id) {
        return db.query("select * from tarefas where id=?",
                set -> true,
                id).orElse(false);
    }
}
