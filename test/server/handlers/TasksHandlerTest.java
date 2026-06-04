package server.handlers;

import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;
import tasks.Task;
import tools.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для обработчика задач (эндпоинт /tasks).
 * Проверяет CRUD операции для задач через HTTP API.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
class TasksHandlerTest extends HttpTaskServerTest {

    /**
     * Проверяет создание задачи через POST /tasks.
     * Ожидается статус 201 и задача в менеджере.
     */
    @Test
    void shouldCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    /**
     * Проверяет ошибку 406 при создании задачи,
     * пересекающейся по времени с существующей.
     */
    @Test
    void shouldReturn406WhenTaskOverlaps() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(60), now);
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.NEW,
                Duration.ofMinutes(60), now.plusMinutes(30));

        manager.createTask(task1);
        String task2Json = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getTasks().size());
    }

    /**
     * Проверяет получение всех задач через GET /tasks.
     */
    @Test
    void shouldGetAllTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Task 1", "Desc 1", TaskStatus.NEW));
        manager.createTask(new Task("Task 2", "Desc 2", TaskStatus.IN_PROGRESS));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);
    }

    /**
     * Проверяет получение задачи по ID через GET /tasks/{id}.
     */
    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Task", "Desc", TaskStatus.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task retrieved = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), retrieved.getId());
        assertEquals(task.getTitle(), retrieved.getTitle());
    }

    /**
     * Проверяет ошибку 404 при запросе несуществующей задачи.
     */
    @Test
    void shouldReturn404ForNonExistentTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    /**
     * Проверяет обновление задачи через POST /tasks с указанным ID.
     */
    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Old Task", "Old Desc", TaskStatus.NEW));
        task.setTitle("Updated Task");
        task.setStatus(TaskStatus.IN_PROGRESS);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task updated = manager.getTask(task.getId());
        assertEquals("Updated Task", updated.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    /**
     * Проверяет удаление задачи по ID через DELETE /tasks/{id}.
     */
    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Task to Delete", "Desc", TaskStatus.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }

    /**
     * Проверяет удаление всех задач через DELETE /tasks.
     */
    @Test
    void shouldDeleteAllTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Task 1", "Desc", TaskStatus.NEW));
        manager.createTask(new Task("Task 2", "Desc", TaskStatus.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }
}