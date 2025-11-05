package com.tuca.manager;

import com.tuca.connection.DatabaseManager;
import com.tuca.model.Task;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TaskManager {

    private final DatabaseManager db;
    private List<Task> tasks = new ArrayList<>();
    private final Logger log = LoggerFactory.getLogger(TaskManager.class);

    public List<Task> loadAllTasks() {
        try {
            tasks = db.queryList(
                    "SELECT * FROM tarefas",
                    rs -> new Task(
                            rs.getInt("id"),
                            rs.getString("description"),
                            rs.getString("ownerName"),
                            rs.getLong("startDate"),
                            rs.getLong("expiryDate"),
                            rs.getString("status")
                    )
            );
        } catch (SQLException e) {
            log.error("[Tasks] Failed to load tasks: {}", e.getMessage());
        }
        return tasks;
    }

    public void createTask(String description, String ownerName, int daysToExpire) {
        if (description == null || description.isEmpty() ||
                ownerName == null || ownerName.isEmpty() ||
                daysToExpire <= 0) {
            log.error("[Tasks] Invalid parameters passed to createTask");
            return;
        }

        long dayMillis = 86400000L;
        long expiryDateMillis = System.currentTimeMillis() + (dayMillis * daysToExpire);

        try {
            db.execute(
                    "INSERT INTO tarefas (description, ownerName, startDate, expiryDate, status) VALUES(?,?,?,?,?)",
                    description,
                    ownerName,
                    System.currentTimeMillis(),
                    expiryDateMillis,
                    "Pendente"
            );
            log.info("[Tasks] Created task: {}", description);

        } catch (SQLException e) {
            log.error("[Tasks] Failed to create task: {}", e.getMessage());
        }
    }

    public void updateTasks() {
        log.info("[Tasks] Updating tasks");
        loadAllTasks().forEach(task -> {
            long now = System.currentTimeMillis();
            if (task.getStatus().equalsIgnoreCase("Completa")) return;
            if (task.getExpiryDate() < now) {
                updateTaskStatus(task.getId(), "Atrasada");
                log.info("[Tasks] Updated task: {}", task.getId());

            }
        });

    }

    public void updateTaskDescription(int taskID, String description) {
        if (description == null || description.isEmpty()) {
            log.error("[Tasks] Invalid parameters passed to updateTaskDescription");
            return;
        }

        try {
            db.execute(
                    "UPDATE tarefas SET description = ? WHERE id = ?",
                    description,
                    taskID
            );
            log.info("[Tasks] Updated task description: {}", description);
        } catch (SQLException e) {
            log.error("[Tasks] Failed to update task description: {}", e.getMessage());
        }
    }

    public void updateTaskStatus(int taskID, String status) {
        if (status == null ||
                !(status.equalsIgnoreCase("PENDENTE") ||
                        status.equalsIgnoreCase("COMPLETA") ||
                        status.equalsIgnoreCase("INCOMPLETA") ||
                        status.equalsIgnoreCase("ATRASADA"))) {
            log.error("[Tasks] Invalid parameters passed to updateTaskStatus, only alloweds: PENDENTE, COMPLETA, INCOMPLETA, ATRASADA");
            return;
        }

        try {
            db.execute(
                    "UPDATE tarefas SET status = ? WHERE id = ?",
                    status,
                    taskID
            );
            log.info("[Tasks] Updated task status: {}", status);

        } catch (SQLException e) {
            log.error("[Tasks] Failed to update task status: {}", e.getMessage());
        }
    }

    public void deleteTask(int taskID) {
        try {
            db.execute(
                    "DELETE FROM tarefas WHERE id = ?",
                    taskID
            );
            log.info("[Tasks] Deleted task: {}", taskID);
        } catch (SQLException e) {
            log.error("[Tasks] Failed to delete task: {}", e.getMessage());
        }
    }
}
