package com.adesidaleye.tasks.services;

import com.adesidaleye.tasks.domain.entities.TaskList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskListService {

    List<TaskList> listTaskList();

    TaskList createTaskList(TaskList taskList);

    Optional<TaskList> getTaskList(UUID taskListId);

    TaskList updateTaskList(UUID taskListId, TaskList taskList);

    void deleteTaskList(UUID taskListId);



}
