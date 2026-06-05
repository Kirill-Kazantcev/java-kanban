package server.handlers;

import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;
import tasks.Epic;
import tasks.Subtask;
import tools.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для обработчика подзадач (эндпоинт /subtasks).
 * Проверяет CRUD операции для подзадач через HTTP API.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
class SubtasksHandlerTest extends HttpTaskServerTest {

    /**
     * Проверяет создание подзадачи через POST /subtasks.
     * Ожидается статус 201 и подзадача в менеджере.
     */
    @Test
    void shouldCreateSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = new Subtask("Subtask", "Desc", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getSubtasks().size());
    }

    /**
     * Проверяет ошибку 404 при создании подзадачи
     * с несуществующим ID эпика.
     */
    @Test
    void shouldReturn404WhenEpicNotFound() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask", "Desc", TaskStatus.NEW, 999);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    /**
     * Проверяет получение всех подзадач через GET /subtasks.
     */
    @Test
    void shouldGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub 1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("Sub 2", "Desc", TaskStatus.DONE, epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    /**
     * Проверяет получение подзадачи по ID через GET /subtasks/{id}.
     */
    @Test
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Subtask", "Desc", TaskStatus.NEW, epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask retrieved = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), retrieved.getId());
    }

    /**
     * Проверяет удаление подзадачи по ID через DELETE /subtasks/{id}.
     */
    @Test
    void shouldDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Subtask", "Desc", TaskStatus.NEW, epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    /**
     * Проверяет обновление подзадачи через POST /subtasks с указанным ID.
     */
    @Test
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Old Subtask", "Desc", TaskStatus.NEW, epic.getId()));
        subtask.setTitle("Updated Subtask");
        subtask.setStatus(TaskStatus.DONE);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask updated = manager.getSubtask(subtask.getId());
        assertEquals("Updated Subtask", updated.getTitle());
        assertEquals(TaskStatus.DONE, updated.getStatus());
    }

    /**
     * Проверяет удаление всех подзадач через DELETE /subtasks.
     */
    @Test
    void shouldDeleteAllSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub 1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("Sub 2", "Desc", TaskStatus.DONE, epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
    }
}