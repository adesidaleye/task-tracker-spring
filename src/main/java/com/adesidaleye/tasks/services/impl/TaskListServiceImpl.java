package com.adesidaleye.tasks.services.impl;

import com.adesidaleye.tasks.domain.entities.TaskList;
import com.adesidaleye.tasks.repositories.TaskListRepository;
import com.adesidaleye.tasks.services.TaskListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskListServiceImpl implements TaskListService {

    private final TaskListRepository taskListRepository;

    public TaskListServiceImpl(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }

    @Override
    public List<TaskList> listTaskList() {
        return taskListRepository.findAll();
    }

    @Override
    public TaskList createTaskList(TaskList taskList) {
        if (taskList.getId() != null) {
            throw new IllegalArgumentException("TaskList already has an ID");
        }

        if (taskList.getTitle() == null || taskList.getTitle().isBlank()) {
            throw new IllegalArgumentException("TaskList title must be present");
        }

        LocalDateTime now = LocalDateTime.now();

        return taskListRepository.save(new TaskList(
                null,
                taskList.getTitle(),
                taskList.getDescription(),
                null,
                now,
                now
        ));
    }

    @Override
    public Optional<TaskList> getTaskList(UUID taskListId) {
        return taskListRepository.findById(taskListId);
    }

    @Transactional
    @Override
    public TaskList updateTaskList(UUID taskListId, TaskList taskList) {
        if (taskList.getId() == null) {
            throw new IllegalArgumentException("TaskList must have ID");
        }

        if (!Objects.equals(taskList.getId(), taskListId)) {
            throw new IllegalArgumentException("Not Permitted to change TaskList ID");
        }

        TaskList existingTaskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("TaskList not found"));

        if (taskList.getTitle() != null) {
            existingTaskList.setTitle(taskList.getTitle());
        }

        if (taskList.getDescription() != null) {
            existingTaskList.setDescription(taskList.getDescription());
        }

        existingTaskList.setUpdated(LocalDateTime.now());

        return taskListRepository.save(existingTaskList);
    }

    @Override
    public void deleteTaskList(UUID taskListId) {
        if (!taskListRepository.existsById(taskListId)) {
            throw new IllegalArgumentException("TaskList does not exists");
        }

        taskListRepository.deleteById(taskListId);
    }
}
