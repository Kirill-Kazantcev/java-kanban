package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.ManagerSaveException;
import manager.TaskManager;
import server.exceptions.HasInteractionsException;
import server.exceptions.NotFoundException;
import tasks.Subtask;

import java.io.IOException;
import java.util.List;

/**
 * Обработчик запросов для эндпоинта /subtasks.
 * Поддерживает GET (все подзадачи и по ID), POST (создание), DELETE (по ID и всех).
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    /** Менеджер задач для выполнения операций с подзадачами */
    private final TaskManager taskManager;

    /** Gson для сериализации/десериализации JSON */
    private final Gson gson;

    /**
     * Конструктор обработчика подзадач.
     *
     * @param taskManager менеджер задач
     * @param gson        экземпляр Gson для работы с JSON
     */
    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
                case "HEAD":
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
     * Обрабатывает GET и HEAD запросы.
     * Поддерживает:
     * - GET /subtasks - получение всех подзадач
     * - GET /subtasks/{id} - получение подзадачи по ID
     *
     * @param exchange объект HttpExchange
     * @param path     путь запроса
     * @throws IOException при ошибке ввода-вывода
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            // GET /subtasks - получить все подзадачи
            List<Subtask> subtasks = taskManager.getSubtasks();
            String response = gson.toJson(subtasks);
            sendText(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            // GET /subtasks/{id} - получить подзадачу по ID
            int id = extractId(path);
            Subtask subtask = taskManager.getSubtask(id);
            if (subtask == null) {
                throw new NotFoundException("Subtask with id " + id + " not found");
            }
            String response = gson.toJson(subtask);
            sendText(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обрабатывает POST запросы.
     * Поддерживает:
     * - POST /subtasks - создание новой подзадачи (id == 0)
     * - POST /subtasks - обновление существующей подзадачи (id != 0)
     *
     * @param exchange объект HttpExchange
     * @throws IOException при ошибке ввода-вывода
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (subtask == null) {
            sendNotFound(exchange);
            return;
        }

        try {
            if (subtask.getId() == 0) {
                Subtask created = taskManager.createSubtask(subtask);
                String response = gson.toJson(created);
                sendCreated(exchange, response);
            } else {
                taskManager.updateSubtask(subtask);
                String response = gson.toJson(subtask);
                sendText(exchange, response);
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Эпик с ID") && e.getMessage().contains("не существует")) {
                throw new NotFoundException(e.getMessage());
            } else {
                throw new HasInteractionsException(e.getMessage());
            }
        } catch (ManagerSaveException e) {
            throw new HasInteractionsException(e.getMessage());
        }
    }

    /**
     * Обрабатывает DELETE запросы.
     * Поддерживает:
     * - DELETE /subtasks/{id} - удаление подзадачи по ID
     * - DELETE /subtasks - удаление всех подзадач
     *
     * @param exchange объект HttpExchange
     * @param path     путь запроса
     * @throws IOException при ошибке ввода-вывода
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            int id = extractId(path);
            taskManager.deleteSubtask(id);
            sendText(exchange, "{\"message\":\"Subtask deleted successfully\"}");
        } else if (path.equals("/subtasks")) {
            taskManager.deleteAllSubtasks();
            sendText(exchange, "{\"message\":\"All subtasks deleted successfully\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}