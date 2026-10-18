package com.adesidaleye.tasks.services;

import com.adesidaleye.tasks.domain.entities.TaskList;

import java.util.List;

public interface TaskListService {

    List<TaskList> listTaskList();

    TaskList createTaskList(TaskList taskList);

}
