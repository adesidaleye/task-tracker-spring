package com.adesidaleye.tasks.mapper;

import com.adesidaleye.tasks.domain.dto.TaskDto;
import com.adesidaleye.tasks.domain.entities.Task;
import com.adesidaleye.tasks.domain.entities.TaskPriority;
import com.adesidaleye.tasks.domain.entities.TaskStatus;
import com.adesidaleye.tasks.mapper.impl.TaskMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapperImpl();
    }

    @Test
    void toDto_convertsEntityToDto() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task(
                id,
                "Test Task",
                "Test Description",
                now.plusDays(1),
                TaskStatus.OPEN,
                TaskPriority.HIGH,
                null,
                now,
                now
        );

        TaskDto dto = taskMapper.toDto(task);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("Test Task");
        assertThat(dto.description()).isEqualTo("Test Description");
        assertThat(dto.dueDate()).isEqualTo(now.plusDays(1));
        assertThat(dto.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(dto.status()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void fromDto_convertsDtoToEntity() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TaskDto dto = new TaskDto(
                id,
                "Test Task",
                "Test Description",
                now.plusDays(1),
                TaskPriority.LOW,
                TaskStatus.CLOSED
        );

        Task task = taskMapper.fromDto(dto);

        assertThat(task.getId()).isEqualTo(id);
        assertThat(task.getTitle()).isEqualTo("Test Task");
        assertThat(task.getDescription()).isEqualTo("Test Description");
        assertThat(task.getDueDate()).isEqualTo(now.plusDays(1));
        assertThat(task.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.CLOSED);
        assertThat(task.getTaskList()).isNull();
        assertThat(task.getCreated()).isNull();
        assertThat(task.getUpdated()).isNull();
    }

    @Test
    void fromDto_handlesNullFields() {
        TaskDto dto = new TaskDto(
                null,
                null,
                null,
                null,
                null,
                null
        );

        Task task = taskMapper.fromDto(dto);

        assertThat(task.getId()).isNull();
        assertThat(task.getTitle()).isNull();
        assertThat(task.getDescription()).isNull();
        assertThat(task.getDueDate()).isNull();
        assertThat(task.getPriority()).isNull();
        assertThat(task.getStatus()).isNull();
    }

    @Test
    void roundTrip_preservesValues() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Task original = new Task(
                id,
                "Round Trip",
                "Description",
                now.plusDays(5),
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                null,
                now,
                now
        );

        TaskDto dto = taskMapper.toDto(original);
        Task converted = taskMapper.fromDto(dto);

        assertThat(converted.getId()).isEqualTo(original.getId());
        assertThat(converted.getTitle()).isEqualTo(original.getTitle());
        assertThat(converted.getDescription()).isEqualTo(original.getDescription());
        assertThat(converted.getDueDate()).isEqualTo(original.getDueDate());
        assertThat(converted.getPriority()).isEqualTo(original.getPriority());
        assertThat(converted.getStatus()).isEqualTo(original.getStatus());
    }
}
