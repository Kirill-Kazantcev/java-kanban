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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для обработчика эпиков (эндпоинт /epics).
 * Проверяет CRUD операции для эпиков через HTTP API,
 * а также получение подзадач эпика.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
class EpicsHandlerTest extends HttpTaskServerTest {

    /**
     * Проверяет создание эпика через POST /epics.
     * Ожидается статус 201 и эпик в менеджере.
     */
    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getEpics().size());
        assertEquals("Test Epic", manager.getEpics().getFirst().getTitle());
    }

    /**
     * Проверяет обновление эпика через POST /epics с указанным ID.
     */
    @Test
    void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Old Epic", "Desc"));
        epic.setTitle("Updated Epic");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic updated = manager.getEpic(epic.getId());
        assertEquals("Updated Epic", updated.getTitle());
    }

    /**
     * Проверяет получение всех эпиков через GET /epics.
     */
    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("Epic 1", "Desc"));
        manager.createEpic(new Epic("Epic 2", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length);
    }

    /**
     * Проверяет получение эпика по ID через GET /epics/{id}.
     */
    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic retrieved = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), retrieved.getId());
    }

    /**
     * Проверяет получение подзадач эпика через GET /epics/{id}/subtasks.
     */
    @Test
    void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub 1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("Sub 2", "Desc", TaskStatus.DONE, epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    /**
     * Проверяет ошибку 404 при запросе несуществующего эпика.
     */
    @Test
    void shouldReturn404ForNonExistentEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    /**
     * Проверяет удаление эпика по ID через DELETE /epics/{id}.
     */
    @Test
    void shouldDeleteEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic to Delete", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
    }

    /**
     * Проверяет удаление всех эпиков через DELETE /epics.
     */
    @Test
    void shouldDeleteAllEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("Epic 1", "Desc"));
        manager.createEpic(new Epic("Epic 2", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
    }
}