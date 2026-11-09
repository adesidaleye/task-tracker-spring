package com.adesidaleye.tasks.controllers;

import com.adesidaleye.tasks.domain.dto.TaskDto;
import com.adesidaleye.tasks.domain.entities.Task;
import com.adesidaleye.tasks.domain.entities.TaskPriority;
import com.adesidaleye.tasks.domain.entities.TaskStatus;
import com.adesidaleye.tasks.mapper.TaskMapper;
import com.adesidaleye.tasks.mapper.impl.TaskMapperImpl;
import com.adesidaleye.tasks.services.TaskService;
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

@WebMvcTest(TaskController.class)
@Import({TaskMapperImpl.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;


    @Autowired
    private ObjectMapper objectMapper;

    private final UUID taskListId = UUID.randomUUID();

    @Test
    void listTasks_returnsTaskDtoList() throws Exception {
        Task task = createTask("Task 1");
        when(taskService.listTasks(taskListId)).thenReturn(List.of(task));

        mockMvc.perform(get("/task-lists/" + taskListId + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"));

        verify(taskService).listTasks(taskListId);
    }

    @Test
    void listTasks_returnsEmptyList() throws Exception {
        when(taskService.listTasks(taskListId)).thenReturn(List.of());

        mockMvc.perform(get("/task-lists/" + taskListId + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createTask_returnsCreatedTaskDto() throws Exception {
        TaskDto requestDto = new TaskDto(
                null,
                "New Task",
                "Description",
                LocalDateTime.now().plusDays(1),
                TaskPriority.HIGH,
                null
        );

        Task createdTask = createTask("New Task");
        createdTask.setPriority(TaskPriority.HIGH);
        when(taskService.createTask(eq(taskListId), any(Task.class))).thenReturn(createdTask);

        mockMvc.perform(post("/task-lists/" + taskListId + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(taskService).createTask(eq(taskListId), any(Task.class));
    }

    @Test
    void getTask_returnsTaskDtoWhenExists() throws Exception {
        UUID taskId = UUID.randomUUID();
        Task task = createTask("Existing Task");
        task.setId(taskId);
        when(taskService.getTask(taskListId, taskId)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/task-lists/" + taskListId + "/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Existing Task"));

        verify(taskService).getTask(taskListId, taskId);
    }

    @Test
    void getTask_returnsEmptyWhenNotFound() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.getTask(taskListId, taskId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/task-lists/" + taskListId + "/tasks/" + taskId))
                .andExpect(status().isOk());
    }

    @Test
    void updateTask_returnsUpdatedTaskDto() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskDto requestDto = new TaskDto(
                taskId,
                "Updated Task",
                "Updated Description",
                LocalDateTime.now().plusDays(2),
                TaskPriority.LOW,
                TaskStatus.CLOSED
        );

        Task updatedTask = createTask("Updated Task");
        updatedTask.setId(taskId);
        updatedTask.setDescription("Updated Description");
        updatedTask.setPriority(TaskPriority.LOW);
        updatedTask.setStatus(TaskStatus.CLOSED);
        when(taskService.updateTask(eq(taskListId), eq(taskId), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/task-lists/" + taskListId + "/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(taskService).updateTask(eq(taskListId), eq(taskId), any(Task.class));
    }

    @Test
    void deleteTask_returnsNoContent() throws Exception {
        UUID taskId = UUID.randomUUID();
        doNothing().when(taskService).deleteTask(taskListId, taskId);

        mockMvc.perform(delete("/task-lists/" + taskListId + "/tasks/" + taskId))
                .andExpect(status().isOk());

        verify(taskService).deleteTask(taskListId, taskId);
    }

    @Test
    void createTask_throwsBadRequestWhenValidationFails() throws Exception {
        TaskDto requestDto = new TaskDto(
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(taskService.createTask(eq(taskListId), any(Task.class)))
                .thenThrow(new IllegalArgumentException("Task must have a title"));

        mockMvc.perform(post("/task-lists/" + taskListId + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task must have a title"));
    }

    private Task createTask(String title) {
        return new Task(
                UUID.randomUUID(),
                title,
                "Description",
                LocalDateTime.now().plusDays(1),
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
