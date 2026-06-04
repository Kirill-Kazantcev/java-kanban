package server.handlers;

import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;
import tasks.Task;
import tools.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для обработчика истории (эндпоинт /history).
 * Проверяет получение истории просмотренных задач.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
class HistoryHandlerTest extends HttpTaskServerTest {

    /**
     * Проверяет получение истории после просмотра задач.
     * Ожидается статус 200 и список из 2 задач.
     */
    @Test
    void shouldGetHistory() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task("Task 1", "Desc", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Task 2", "Desc", TaskStatus.NEW));

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, history.length);
    }

    /**
     * Проверяет получение истории при отсутствии просмотров.
     * Ожидается статус 200 и пустой список.
     */
    @Test
    void shouldReturnEmptyHistoryWhenNoViews() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, history.length);
    }
}