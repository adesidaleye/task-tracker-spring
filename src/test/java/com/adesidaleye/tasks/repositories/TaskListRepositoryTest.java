package com.adesidaleye.tasks.repositories;

import com.adesidaleye.tasks.domain.entities.TaskList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskListRepositoryTest {

    @Autowired
    private TaskListRepository taskListRepository;

    @Test
    void save_andFindById_returnsSavedTaskList() {
        TaskList taskList = new TaskList(
                null,
                "My List",
                "My Description",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        TaskList saved = taskListRepository.save(taskList);

        Optional<TaskList> found = taskListRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("My List");
        assertThat(found.get().getDescription()).isEqualTo("My Description");
    }

    @Test
    void findAll_returnsAllTaskLists() {
        TaskList list1 = createTaskList("List 1");
        TaskList list2 = createTaskList("List 2");
        taskListRepository.save(list1);
        taskListRepository.save(list2);

        List<TaskList> all = taskListRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(TaskList::getTitle).containsExactlyInAnyOrder("List 1", "List 2");
    }

    @Test
    void findAll_returnsEmptyListWhenNone() {
        List<TaskList> all = taskListRepository.findAll();

        assertThat(all).isEmpty();
    }

    @Test
    void existsById_returnsTrueWhenExists() {
        TaskList taskList = createTaskList("Existing List");
        TaskList saved = taskListRepository.save(taskList);

        boolean exists = taskListRepository.existsById(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_returnsFalseWhenNotExists() {
        UUID nonExistentId = UUID.randomUUID();

        boolean exists = taskListRepository.existsById(nonExistentId);

        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_removesTaskList() {
        TaskList taskList = createTaskList("To Delete");
        TaskList saved = taskListRepository.save(taskList);

        taskListRepository.deleteById(saved.getId());

        Optional<TaskList> found = taskListRepository.findById(saved.getId());
        assertThat(found).isEmpty();
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
