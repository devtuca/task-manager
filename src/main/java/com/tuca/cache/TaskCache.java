package com.tuca.cache;

import com.tuca.model.Task;
import com.tuca.repository.TaskRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskCache {

    @Getter
    private final List<Task> tasks = new ArrayList<>();

    private final TaskRepository taskRepository;

    @Autowired
    public TaskCache(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    public boolean contains(long taskID) {
        return getByID(taskID) != null;
    }

    public Task getByID(long taskID) {
        for (Task task : tasks) {
            if (task.getId() == taskID) {
                return task;
            }
        }
        return null;
    }

    public Task save(Task task) {
        if (contains(task.getId())) {
            update(task);
            return taskRepository.save(task);
        }
        tasks.add(task);
        return getByID(task.getId());
    }

    private void update(Task task) {
        tasks.set(tasks.indexOf(task), task);
    }

    public void remove(long taskID) {
        tasks.remove(getByID(taskID));
    }

    public void loadAll() {
        taskRepository.findAll().forEach(this::save);
    }
}
