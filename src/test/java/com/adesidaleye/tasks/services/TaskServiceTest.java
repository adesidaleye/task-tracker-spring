package com.adesidaleye.tasks.services;

import com.adesidaleye.tasks.domain.entities.Task;
import com.adesidaleye.tasks.domain.entities.TaskList;
import com.adesidaleye.tasks.domain.entities.TaskPriority;
import com.adesidaleye.tasks.domain.entities.TaskStatus;
import com.adesidaleye.tasks.repositories.TaskListRepository;
import com.adesidaleye.tasks.repositories.TaskRepository;
import com.adesidaleye.tasks.services.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskListRepository taskListRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID taskListId;
    private TaskList taskList;

    @BeforeEach
    void setUp() {
        taskListId = UUID.randomUUID();
        taskList = new TaskList(
                taskListId,
                "Test List",
                "Description",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void listTasks_returnsTasksForGivenList() {
        Task task1 = createTask("Task 1", taskList);
        Task task2 = createTask("Task 2", taskList);
        when(taskRepository.findByTaskListId(taskListId)).thenReturn(List.of(task1, task2));

        List<Task> result = taskService.listTasks(taskListId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Task::getTitle).containsExactly("Task 1", "Task 2");
        verify(taskRepository).findByTaskListId(taskListId);
    }

    @Test
    void listTasks_returnsEmptyListWhenNoTasks() {
        when(taskRepository.findByTaskListId(taskListId)).thenReturn(List.of());

        List<Task> result = taskService.listTasks(taskListId);

        assertThat(result).isEmpty();
    }

    @Test
    void createTask_successfullyCreatesTask() {
        Task taskToCreate = new Task(
                null,
                "New Task",
                "Description",
                LocalDateTime.now().plusDays(1),
                null,
                TaskPriority.HIGH,
                null,
                null,
                null
        );

        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(taskList));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        Task result = taskService.createTask(taskListId, taskToCreate);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.OPEN);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getTaskList()).isEqualTo(taskList);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_defaultsPriorityToMediumWhenNull() {
        Task taskToCreate = new Task(
                null,
                "New Task",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(taskList));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        Task result = taskService.createTask(taskListId, taskToCreate);

        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void createTask_throwsWhenTaskHasId() {
        UUID existingId = UUID.randomUUID();
        Task taskToCreate = new Task(
                existingId,
                "New Task",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskService.createTask(taskListId, taskToCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has ID");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_throwsWhenTitleIsNull() {
        Task taskToCreate = new Task(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskService.createTask(taskListId, taskToCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have a title");
    }

    @Test
    void createTask_throwsWhenTitleIsBlank() {
        Task taskToCreate = new Task(
                null,
                "   ",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskService.createTask(taskListId, taskToCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have a title");
    }

    @Test
    void createTask_throwsWhenTaskListNotFound() {
        Task taskToCreate = new Task(
                null,
                "New Task",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(taskListRepository.findById(taskListId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(taskListId, taskToCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Task List ID");
    }

    @Test
    void getTask_returnsTaskWhenExists() {
        UUID taskId = UUID.randomUUID();
        Task task = createTask("Existing Task", taskList);
        task.setId(taskId);

        when(taskRepository.findByTaskListIdAndId(taskListId, taskId)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getTask(taskListId, taskId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Existing Task");
    }

    @Test
    void getTask_returnsEmptyWhenNotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByTaskListIdAndId(taskListId, taskId)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.getTask(taskListId, taskId);

        assertThat(result).isEmpty();
    }

    @Test
    void updateTask_successfullyUpdatesTask() {
        UUID taskId = UUID.randomUUID();
        Task existing = createTask("Old Title", taskList);
        existing.setId(taskId);

        Task update = new Task(
                taskId,
                "New Title",
                "New Description",
                LocalDateTime.now().plusDays(2),
                TaskStatus.CLOSED,
                TaskPriority.HIGH,
                null,
                null,
                null
        );

        when(taskRepository.findByTaskListIdAndId(taskListId, taskId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.updateTask(taskListId, taskId, update);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.CLOSED);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_throwsWhenTaskHasNoId() {
        Task update = new Task(
                null,
                "New Title",
                null,
                null,
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskService.updateTask(taskListId, UUID.randomUUID(), update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have an ID");
    }

    @Test
    void updateTask_throwsWhenIdsDoNotMatch() {
        UUID pathId = UUID.randomUUID();
        UUID bodyId = UUID.randomUUID();
        Task update = new Task(
                bodyId,
                "New Title",
                null,
                null,
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskService.updateTask(taskListId, pathId, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must match");
    }

    @Test
    void updateTask_throwsWhenPriorityIsNull() {
        UUID taskId = UUID.randomUUID();
        Task update = new Task(
                taskId,
                "New Title",
                null,
                null,
                TaskStatus.OPEN,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskService.updateTask(taskListId, taskId, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid priority");
    }

    @Test
    void updateTask_throwsWhenStatusIsNull() {
        UUID taskId = UUID.randomUUID();
        Task update = new Task(
                taskId,
                "New Title",
                null,
                null,
                null,
                TaskPriority.MEDIUM,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskService.updateTask(taskListId, taskId, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid status");
    }

    @Test
    void updateTask_throwsWhenTaskNotFound() {
        UUID taskId = UUID.randomUUID();
        Task update = new Task(
                taskId,
                "New Title",
                null,
                null,
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                null,
                null,
                null
        );

        when(taskRepository.findByTaskListIdAndId(taskListId, taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(taskListId, taskId, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not Found");
    }

    @Test
    void deleteTask_delegatesToRepository() {
        UUID taskId = UUID.randomUUID();

        taskService.deleteTask(taskListId, taskId);

        verify(taskRepository).deleteByTaskListIdAndId(taskListId, taskId);
    }

    private Task createTask(String title, TaskList taskList) {
        return new Task(
                UUID.randomUUID(),
                title,
                "Description",
                LocalDateTime.now().plusDays(1),
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                taskList,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
