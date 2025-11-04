package com.tuca.manager;

import com.tuca.connection.DatabaseManager;
import com.tuca.model.Task;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TaskManager {

    private final DatabaseManager db;
    private List<Task> tasks = new ArrayList<>();

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
            System.err.println("[Tasks] Failed to load tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public void createTask(String description, String ownerName, int daysToExpire) {
        if (description == null || description.isEmpty() ||
                ownerName == null || ownerName.isEmpty() ||
                daysToExpire <= 0) {
            System.out.println("[Tasks] Please provide valid parameters.");
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
            System.out.println("[Tasks] Task created successfully.");
        } catch (SQLException e) {
            System.err.println("[Tasks] Task creation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateTasks() {
        System.out.println("[Tasks] Updating tasks...");
        loadAllTasks().forEach(task -> {
            long now = System.currentTimeMillis();
            if (task.getStatus().equalsIgnoreCase("Completa")) return;
            if (task.getExpiryDate() < now) {
                updateTaskStatus(task.getId(), "Atrasada");
                System.out.println("[Updater] Tarefa ID " + task.getId() + " marcada como ATRASADA");
            }
        });

    }

    public void updateTaskDescription(int taskID, String description) {
        if (description == null || description.isEmpty()) {
            System.out.println("[Tasks] Invalid description. Please provide a valid description.");
            return;
        }

        try {
            db.execute(
                    "UPDATE tarefas SET description = ? WHERE id = ?",
                    description,
                    taskID
            );
            System.out.println("[Tasks] Task description updated successfully.");
        } catch (SQLException e) {
            System.err.println("[Tasks] Failed to update task description: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateTaskStatus(int taskID, String status) {
        if (status == null ||
                !(status.equalsIgnoreCase("PENDENTE") ||
                        status.equalsIgnoreCase("COMPLETA") ||
                        status.equalsIgnoreCase("INCOMPLETA") ||
                        status.equalsIgnoreCase("ATRASADA"))) {
            System.out.println("[Tasks] Invalid status. Only allowed: PENDENTE, COMPLETA, INCOMPLETA, ATRASADA.");
            return;
        }

        try {
            db.execute(
                    "UPDATE tarefas SET status = ? WHERE id = ?",
                    status,
                    taskID
            );
            System.out.println("[Tasks] Task status updated successfully.");
        } catch (SQLException e) {
            System.err.println("[Tasks] Failed to update task status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteTask(int taskID) {
        try {
            db.execute(
                    "DELETE FROM tarefas WHERE id = ?",
                    taskID
            );
            System.out.println("[Tasks] Task deleted successfully.");
        } catch (SQLException e) {
            System.err.println("[Tasks] Failed to delete task: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
