package com.adesidaleye.tasks.mapper;

import com.adesidaleye.tasks.domain.dto.TaskListDto;
import com.adesidaleye.tasks.domain.entities.TaskList;

public interface TaskListMapper {

    TaskList fromDto(TaskListDto taskListDto);

    TaskListDto toDto(TaskList taskList);

}
