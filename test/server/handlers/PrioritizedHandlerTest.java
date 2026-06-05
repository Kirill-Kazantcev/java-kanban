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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для обработчика приоритетных задач (эндпоинт /prioritized).
 * Проверяет получение задач, отсортированных по времени начала.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
class PrioritizedHandlerTest extends HttpTaskServerTest {

    /**
     * Проверяет получение приоритетных задач.
     * Ожидается статус 200 и правильная сортировка по startTime.
     */
    @Test
    void shouldGetPrioritizedTasks() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), now.plusHours(1));
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), now);

        manager.createTask(task1);
        manager.createTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length);
        assertEquals("Task 2", prioritized[0].getTitle());
    }

    /**
     * Проверяет получение приоритетных задач,
     * когда нет задач с заданным временем.
     * Ожидается статус 200 и пустой список.
     */
    @Test
    void shouldReturnEmptyPrioritizedWhenNoTasksWithTime() throws IOException, InterruptedException {
        manager.createTask(new Task("Task without time", "Desc", TaskStatus.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, prioritized.length);
    }
}