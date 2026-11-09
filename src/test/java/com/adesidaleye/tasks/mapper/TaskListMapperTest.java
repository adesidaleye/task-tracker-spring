package com.adesidaleye.tasks.mapper;

import com.adesidaleye.tasks.domain.dto.TaskDto;
import com.adesidaleye.tasks.domain.dto.TaskListDto;
import com.adesidaleye.tasks.domain.entities.Task;
import com.adesidaleye.tasks.domain.entities.TaskList;
import com.adesidaleye.tasks.domain.entities.TaskPriority;
import com.adesidaleye.tasks.domain.entities.TaskStatus;
import com.adesidaleye.tasks.mapper.impl.TaskListMapperImpl;
import com.adesidaleye.tasks.mapper.impl.TaskMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskListMapperTest {

    private TaskListMapper taskListMapper;

    @BeforeEach
    void setUp() {
        TaskMapper taskMapper = new TaskMapperImpl();
        taskListMapper = new TaskListMapperImpl(taskMapper);
    }

    @Test
    void toDto_convertsEntityToDtoWithTasks() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task(
                UUID.randomUUID(),
                "Open Task",
                null,
                null,
                TaskStatus.OPEN,
                TaskPriority.HIGH,
                null,
                now,
                now
        );
        Task task2 = new Task(
                UUID.randomUUID(),
                "Closed Task",
                null,
                null,
                TaskStatus.CLOSED,
                TaskPriority.LOW,
                null,
                now,
                now
        );

        TaskList taskList = new TaskList(
                id,
                "My List",
                "My Description",
                List.of(task1, task2),
                now,
                now
        );

        TaskListDto dto = taskListMapper.toDto(taskList);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("My List");
        assertThat(dto.description()).isEqualTo("My Description");
        assertThat(dto.count()).isEqualTo(2);
        assertThat(dto.progress()).isEqualTo(0.5);
        assertThat(dto.tasks()).hasSize(2);
    }

    @Test
    void toDto_calculatesProgressCorrectly() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Task closed1 = createTask("Closed 1", TaskStatus.CLOSED);
        Task closed2 = createTask("Closed 2", TaskStatus.CLOSED);
        Task open1 = createTask("Open 1", TaskStatus.OPEN);

        TaskList taskList = new TaskList(
                id,
                "Progress List",
                null,
                List.of(closed1, closed2, open1),
                now,
                now
        );

        TaskListDto dto = taskListMapper.toDto(taskList);

        assertThat(dto.progress()).isEqualTo(2.0 / 3.0);
    }

    @Test
    void toDto_returnsNullProgressWhenNoTasks() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        TaskList taskList = new TaskList(
                id,
                "Empty List",
                null,
                null,
                now,
                now
        );

        TaskListDto dto = taskListMapper.toDto(taskList);

        assertThat(dto.progress()).isNull();
        assertThat(dto.count()).isEqualTo(0);
        assertThat(dto.tasks()).isNull();
    }

    @Test
    void toDto_returnsZeroProgressWhenAllOpen() {
        LocalDateTime now = LocalDateTime.now();

        Task open1 = createTask("Open 1", TaskStatus.OPEN);
        Task open2 = createTask("Open 2", TaskStatus.OPEN);

        TaskList taskList = new TaskList(
                UUID.randomUUID(),
                "All Open",
                null,
                List.of(open1, open2),
                now,
                now
        );

        TaskListDto dto = taskListMapper.toDto(taskList);

        assertThat(dto.progress()).isEqualTo(0.0);
    }

    @Test
    void toDto_returnsFullProgressWhenAllClosed() {
        LocalDateTime now = LocalDateTime.now();

        Task closed1 = createTask("Closed 1", TaskStatus.CLOSED);
        Task closed2 = createTask("Closed 2", TaskStatus.CLOSED);

        TaskList taskList = new TaskList(
                UUID.randomUUID(),
                "All Closed",
                null,
                List.of(closed1, closed2),
                now,
                now
        );

        TaskListDto dto = taskListMapper.toDto(taskList);

        assertThat(dto.progress()).isEqualTo(1.0);
    }

    @Test
    void fromDto_convertsDtoToEntity() {
        UUID id = UUID.randomUUID();
        TaskDto taskDto = new TaskDto(
                UUID.randomUUID(),
                "Task from DTO",
                "Description",
                LocalDateTime.now().plusDays(1),
                TaskPriority.HIGH,
                TaskStatus.OPEN
        );

        TaskListDto dto = new TaskListDto(
                id,
                "DTO List",
                "DTO Description",
                1,
                0.0,
                List.of(taskDto)
        );

        TaskList taskList = taskListMapper.fromDto(dto);

        assertThat(taskList.getId()).isEqualTo(id);
        assertThat(taskList.getTitle()).isEqualTo("DTO List");
        assertThat(taskList.getDescription()).isEqualTo("DTO Description");
        assertThat(taskList.getTasks()).hasSize(1);
        assertThat(taskList.getTasks().get(0).getTitle()).isEqualTo("Task from DTO");
        assertThat(taskList.getCreated()).isNull();
        assertThat(taskList.getUpdated()).isNull();
    }

    @Test
    void fromDto_handlesNullTasks() {
        UUID id = UUID.randomUUID();
        TaskListDto dto = new TaskListDto(
                id,
                "No Tasks",
                null,
                null,
                null,
                null
        );

        TaskList taskList = taskListMapper.fromDto(dto);

        assertThat(taskList.getId()).isEqualTo(id);
        assertThat(taskList.getTitle()).isEqualTo("No Tasks");
        assertThat(taskList.getTasks()).isNull();
    }

    @Test
    void fromDto_handlesEmptyTasksList() {
        UUID id = UUID.randomUUID();
        TaskListDto dto = new TaskListDto(
                id,
                "Empty Tasks",
                null,
                0,
                null,
                List.of()
        );

        TaskList taskList = taskListMapper.fromDto(dto);

        assertThat(taskList.getTasks()).isEmpty();
    }

    private Task createTask(String title, TaskStatus status) {
        return new Task(
                UUID.randomUUID(),
                title,
                null,
                null,
                status,
                TaskPriority.MEDIUM,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
