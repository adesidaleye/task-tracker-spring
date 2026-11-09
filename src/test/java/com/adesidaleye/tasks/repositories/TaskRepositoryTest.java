package com.adesidaleye.tasks.repositories;

import com.adesidaleye.tasks.domain.entities.Task;
import com.adesidaleye.tasks.domain.entities.TaskList;
import com.adesidaleye.tasks.domain.entities.TaskPriority;
import com.adesidaleye.tasks.domain.entities.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    private TaskList taskList;
    private UUID taskListId;

    @BeforeEach
    void setUp() {
        taskList = new TaskList(
                null,
                "Test List",
                "Test Description",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        taskList = taskListRepository.save(taskList);
        taskListId = taskList.getId();
    }

    @Test
    void findByTaskListId_returnsTasksForGivenList() {
        Task task1 = createTask("Task 1", taskList);
        Task task2 = createTask("Task 2", taskList);
        taskRepository.save(task1);
        taskRepository.save(task2);

        TaskList otherList = new TaskList(
                null,
                "Other List",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        otherList = taskListRepository.save(otherList);
        Task otherTask = createTask("Other Task", otherList);
        taskRepository.save(otherTask);

        List<Task> found = taskRepository.findByTaskListId(taskListId);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Task::getTitle).containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    void findByTaskListId_returnsEmptyListWhenNoTasks() {
        List<Task> found = taskRepository.findByTaskListId(taskListId);

        assertThat(found).isEmpty();
    }

    @Test
    void findByTaskListIdAndId_findsTaskWhenExists() {
        Task task = createTask("Find Me", taskList);
        Task saved = taskRepository.save(task);

        Optional<Task> found = taskRepository.findByTaskListIdAndId(taskListId, saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Find Me");
    }

    @Test
    void findByTaskListIdAndId_returnsEmptyWhenTaskNotInList() {
        Task task = createTask("Wrong List", taskList);
        Task saved = taskRepository.save(task);

        UUID wrongListId = UUID.randomUUID();
        Optional<Task> found = taskRepository.findByTaskListIdAndId(wrongListId, saved.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void findByTaskListIdAndId_returnsEmptyWhenTaskNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<Task> found = taskRepository.findByTaskListIdAndId(taskListId, nonExistentId);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteByTaskListIdAndId_removesTask() {
        Task task = createTask("Delete Me", taskList);
        Task saved = taskRepository.save(task);

        taskRepository.deleteByTaskListIdAndId(taskListId, saved.getId());

        Optional<Task> found = taskRepository.findByTaskListIdAndId(taskListId, saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteByTaskListIdAndId_doesNotRemoveTaskFromOtherList() {
        Task task = createTask("Keep Me", taskList);
        Task saved = taskRepository.save(task);

        UUID wrongListId = UUID.randomUUID();
        taskRepository.deleteByTaskListIdAndId(wrongListId, saved.getId());

        Optional<Task> found = taskRepository.findByTaskListIdAndId(taskListId, saved.getId());
        assertThat(found).isPresent();
    }

    private Task createTask(String title, TaskList taskList) {
        return new Task(
                null,
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
