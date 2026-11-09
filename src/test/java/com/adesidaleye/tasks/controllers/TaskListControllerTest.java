package com.adesidaleye.tasks.controllers;

import com.adesidaleye.tasks.domain.dto.TaskListDto;
import com.adesidaleye.tasks.domain.entities.TaskList;
import com.adesidaleye.tasks.mapper.TaskListMapper;
import com.adesidaleye.tasks.mapper.impl.TaskListMapperImpl;
import com.adesidaleye.tasks.mapper.impl.TaskMapperImpl;
import com.adesidaleye.tasks.services.TaskListService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskListController.class)
@Import({TaskListMapperImpl.class, TaskMapperImpl.class})
class TaskListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskListService taskListService;


    @Autowired
    private ObjectMapper objectMapper;

    private final UUID taskListId = UUID.randomUUID();

    @Test
    void listTaskLists_returnsTaskListDtoList() throws Exception {
        TaskList taskList = createTaskList("List 1");
        when(taskListService.listTaskList()).thenReturn(List.of(taskList));

        mockMvc.perform(get("/task-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("List 1"));

        verify(taskListService).listTaskList();
    }

    @Test
    void listTaskLists_returnsEmptyList() throws Exception {
        when(taskListService.listTaskList()).thenReturn(List.of());

        mockMvc.perform(get("/task-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createTaskList_returnsCreatedTaskListDto() throws Exception {
        TaskListDto requestDto = new TaskListDto(
                null,
                "New List",
                "New Description",
                null,
                null,
                null
        );

        TaskList created = createTaskList("New List");
        created.setDescription("New Description");
        when(taskListService.createTaskList(any(TaskList.class))).thenReturn(created);

        mockMvc.perform(post("/task-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New List"))
                .andExpect(jsonPath("$.description").value("New Description"));

        verify(taskListService).createTaskList(any(TaskList.class));
    }

    @Test
    void getTaskList_returnsTaskListDtoWhenExists() throws Exception {
        TaskList taskList = createTaskList("Existing List");
        taskList.setId(taskListId);
        when(taskListService.getTaskList(taskListId)).thenReturn(Optional.of(taskList));

        mockMvc.perform(get("/task-lists/" + taskListId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Existing List"));

        verify(taskListService).getTaskList(taskListId);
    }

    @Test
    void getTaskList_returnsEmptyWhenNotFound() throws Exception {
        when(taskListService.getTaskList(taskListId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/task-lists/" + taskListId))
                .andExpect(status().isOk());
    }

    @Test
    void updateTaskList_returnsUpdatedTaskListDto() throws Exception {
        TaskListDto requestDto = new TaskListDto(
                taskListId,
                "Updated List",
                "Updated Description",
                null,
                null,
                null
        );

        TaskList updated = createTaskList("Updated List");
        updated.setId(taskListId);
        updated.setDescription("Updated Description");
        when(taskListService.updateTaskList(eq(taskListId), any(TaskList.class))).thenReturn(updated);

        mockMvc.perform(put("/task-lists/" + taskListId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated List"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(taskListService).updateTaskList(eq(taskListId), any(TaskList.class));
    }

    @Test
    void deleteTaskList_returnsNoContent() throws Exception {
        doNothing().when(taskListService).deleteTaskList(taskListId);

        mockMvc.perform(delete("/task-lists/" + taskListId))
                .andExpect(status().isOk());

        verify(taskListService).deleteTaskList(taskListId);
    }

    @Test
    void createTaskList_throwsBadRequestWhenValidationFails() throws Exception {
        TaskListDto requestDto = new TaskListDto(
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(taskListService.createTaskList(any(TaskList.class)))
                .thenThrow(new IllegalArgumentException("TaskList title must be present"));

        mockMvc.perform(post("/task-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("TaskList title must be present"));
    }

    @Test
    void deleteTaskList_throwsBadRequestWhenNotFound() throws Exception {
        doThrow(new IllegalArgumentException("TaskList does not exists"))
                .when(taskListService).deleteTaskList(taskListId);

        mockMvc.perform(delete("/task-lists/" + taskListId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("TaskList does not exists"));
    }

    private TaskList createTaskList(String title) {
        return new TaskList(
                UUID.randomUUID(),
                title,
                "Description",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
