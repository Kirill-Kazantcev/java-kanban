package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.ManagerSaveException;
import manager.TaskManager;
import server.exceptions.HasInteractionsException;
import server.exceptions.NotFoundException;
import tasks.Task;

import java.io.IOException;
import java.util.List;

/**
 * Обработчик запросов для эндпоинта /tasks.
 * Поддерживает GET (все задачи и по ID), POST (создание), DELETE (по ID и всех).
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    /** Менеджер задач для выполнения операций с задачами */
    private final TaskManager taskManager;

    /** Gson для сериализации/десериализации JSON */
    private final Gson gson;

    /**
     * Конструктор обработчика задач.
     *
     * @param taskManager менеджер задач
     * @param gson        экземпляр Gson для работы с JSON
     */
    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    /**
     * Обрабатывает входящий HTTP запрос.
     * Перенаправляет запрос в соответствующий метод в зависимости от HTTP метода.
     *
     * @param exchange объект HttpExchange, содержащий запрос и ответ
     * @throws IOException при ошибке ввода-вывода
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (HasInteractionsException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    /**
     * Обрабатывает GET запросы.
     * Поддерживает:
     * - GET /tasks - получение всех задач
     * - GET /tasks/{id} - получение задачи по ID
     *
     * @param exchange объект HttpExchange
     * @param path     путь запроса
     * @throws IOException при ошибке ввода-вывода
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getTasks();
            String response = gson.toJson(tasks);
            sendText(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            int id = extractId(path);
            Task task = taskManager.getTask(id);
            if (task == null) {
                throw new NotFoundException("Task with id " + id + " not found");
            }
            String response = gson.toJson(task);
            sendText(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обрабатывает POST запросы.
     * Поддерживает:
     * - POST /tasks - создание новой задачи (id == 0)
     * - POST /tasks - обновление существующей задачи (id != 0)
     *
     * @param exchange объект HttpExchange
     * @throws IOException при ошибке ввода-вывода
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Task task = gson.fromJson(body, Task.class);

        if (task == null) {
            sendNotFound(exchange);
            return;
        }

        try {
            if (task.getId() == 0) {
                Task created = taskManager.createTask(task);
                sendCreated(exchange, gson.toJson(created));
            } else {
                taskManager.updateTask(task);
                sendText(exchange, gson.toJson(task));
            }
        } catch (ManagerSaveException e) {
            throw new HasInteractionsException(e.getMessage());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("пересекается") || e.getMessage().contains("intersects")) {
                throw new HasInteractionsException(e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Обрабатывает DELETE запросы.
     * Поддерживает:
     * - DELETE /tasks/{id} - удаление задачи по ID
     * - DELETE /tasks - удаление всех задач
     *
     * @param exchange объект HttpExchange
     * @param path     путь запроса
     * @throws IOException при ошибке ввода-вывода
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            int id = extractId(path);
            taskManager.deleteTask(id);
            sendText(exchange, "{\"message\":\"Task deleted successfully\"}");
        } else if (path.equals("/tasks")) {
            taskManager.deleteAllTasks();
            sendText(exchange, "{\"message\":\"All tasks deleted successfully\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}