package com.adesidaleye.tasks.services;

import com.adesidaleye.tasks.domain.entities.TaskList;
import com.adesidaleye.tasks.repositories.TaskListRepository;
import com.adesidaleye.tasks.services.impl.TaskListServiceImpl;
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
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

    @InjectMocks
    private TaskListServiceImpl taskListService;

    private UUID taskListId;

    @BeforeEach
    void setUp() {
        taskListId = UUID.randomUUID();
    }

    @Test
    void listTaskList_returnsAllTaskLists() {
        TaskList list1 = createTaskList("List 1");
        TaskList list2 = createTaskList("List 2");
        when(taskListRepository.findAll()).thenReturn(List.of(list1, list2));

        List<TaskList> result = taskListService.listTaskList();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TaskList::getTitle).containsExactly("List 1", "List 2");
        verify(taskListRepository).findAll();
    }

    @Test
    void listTaskList_returnsEmptyListWhenNone() {
        when(taskListRepository.findAll()).thenReturn(List.of());

        List<TaskList> result = taskListService.listTaskList();

        assertThat(result).isEmpty();
    }

    @Test
    void createTaskList_successfullyCreatesTaskList() {
        TaskList toCreate = new TaskList(
                null,
                "New List",
                "New Description",
                null,
                null,
                null
        );

        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> {
            TaskList saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        TaskList result = taskListService.createTaskList(toCreate);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New List");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getUpdated()).isNotNull();
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    void createTaskList_throwsWhenAlreadyHasId() {
        UUID existingId = UUID.randomUUID();
        TaskList toCreate = new TaskList(
                existingId,
                "New List",
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskListService.createTaskList(toCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has an ID");

        verify(taskListRepository, never()).save(any());
    }

    @Test
    void createTaskList_throwsWhenTitleIsNull() {
        TaskList toCreate = new TaskList(
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskListService.createTaskList(toCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title must be present");
    }

    @Test
    void createTaskList_throwsWhenTitleIsBlank() {
        TaskList toCreate = new TaskList(
                null,
                "   ",
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskListService.createTaskList(toCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title must be present");
    }

    @Test
    void getTaskList_returnsTaskListWhenExists() {
        TaskList taskList = createTaskList("Existing List");
        taskList.setId(taskListId);

        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(taskList));

        Optional<TaskList> result = taskListService.getTaskList(taskListId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Existing List");
    }

    @Test
    void getTaskList_returnsEmptyWhenNotFound() {
        when(taskListRepository.findById(taskListId)).thenReturn(Optional.empty());

        Optional<TaskList> result = taskListService.getTaskList(taskListId);

        assertThat(result).isEmpty();
    }

    @Test
    void updateTaskList_successfullyUpdatesTaskList() {
        TaskList existing = createTaskList("Old Title");
        existing.setId(taskListId);

        TaskList update = new TaskList(
                taskListId,
                "New Title",
                "New Description",
                null,
                null,
                null
        );

        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(existing));
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskList result = taskListService.updateTaskList(taskListId, update);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getDescription()).isEqualTo("New Description");
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    void updateTaskList_throwsWhenNoId() {
        TaskList update = new TaskList(
                null,
                "New Title",
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskListService.updateTaskList(taskListId, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have ID");
    }

    @Test
    void updateTaskList_throwsWhenIdsDoNotMatch() {
        UUID pathId = UUID.randomUUID();
        UUID bodyId = UUID.randomUUID();
        TaskList update = new TaskList(
                bodyId,
                "New Title",
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> taskListService.updateTaskList(pathId, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not Permitted");
    }

    @Test
    void updateTaskList_throwsWhenNotFound() {
        TaskList update = new TaskList(
                taskListId,
                "New Title",
                null,
                null,
                null,
                null
        );

        when(taskListRepository.findById(taskListId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskListService.updateTaskList(taskListId, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteTaskList_successfullyDeletesTaskList() {
        when(taskListRepository.existsById(taskListId)).thenReturn(true);

        taskListService.deleteTaskList(taskListId);

        verify(taskListRepository).deleteById(taskListId);
    }

    @Test
    void deleteTaskList_throwsWhenNotFound() {
        when(taskListRepository.existsById(taskListId)).thenReturn(false);

        assertThatThrownBy(() -> taskListService.deleteTaskList(taskListId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exists");

        verify(taskListRepository, never()).deleteById(any());
    }

    private TaskList createTaskList(String title) {
        return new TaskList(
                null,
                title,
                "Description",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
