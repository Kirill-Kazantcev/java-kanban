package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import server.exceptions.NotFoundException;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик запросов для эндпоинта /epics.
 * Поддерживает GET (все эпики, по ID, подзадачи эпика), POST (создание), DELETE (по ID и всех).
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    /** Менеджер задач для выполнения операций с эпиками */
    private final TaskManager taskManager;

    /** Gson для сериализации/десериализации JSON */
    private final Gson gson;

    /**
     * Конструктор обработчика эпиков.
     *
     * @param taskManager менеджер задач
     * @param gson        экземпляр Gson для работы с JSON
     */
    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    /**
     * Обрабатывает GET и HEAD запросы.
     * Поддерживает:
     * - GET /epics - получение всех эпиков
     * - GET /epics/{id} - получение эпика по ID
     * - GET /epics/{id}/subtasks - получение всех подзадач эпика
     *
     * @param exchange объект HttpExchange
     * @param path     путь запроса
     * @throws IOException при ошибке ввода-вывода
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getEpics();
            String response = gson.toJson(epics);
            sendText(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            int id = extractId(path);
            Epic epic = taskManager.getEpic(id);
            if (epic == null) {
                throw new NotFoundException("Epic with id " + id + " not found");
            }
            String response = gson.toJson(epic);
            sendText(exchange, response);
        } else if (path.matches("/epics/\\d+/subtasks")) {
            String[] parts = path.split("/");
            int epicId = Integer.parseInt(parts[2]);
            List<Subtask> allSubtasks = taskManager.getSubtasks();
            List<Subtask> epicSubtasks = new ArrayList<>();
            for (Subtask s : allSubtasks) {
                if (s.getEpicId() == epicId) {
                    epicSubtasks.add(s);
                }
            }
            String response = gson.toJson(epicSubtasks);
            sendText(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обрабатывает POST запросы.
     * Поддерживает:
     * - POST /epics - создание нового эпика (id == 0)
     * - POST /epics - обновление существующего эпика (id != 0)
     *
     * @param exchange объект HttpExchange
     * @throws IOException при ошибке ввода-вывода
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic == null) {
            sendNotFound(exchange);
            return;
        }

        if (epic.getId() == 0) {
            Epic created = taskManager.createEpic(epic);
            String response = gson.toJson(created);
            sendCreated(exchange, response);
        } else {
            taskManager.updateEpic(epic);
            String response = gson.toJson(epic);
            sendText(exchange, response);
        }
    }

    /**
     * Обрабатывает DELETE запросы.
     * Поддерживает:
     * - DELETE /epics/{id} - удаление эпика по ID
     * - DELETE /epics - удаление всех эпиков
     *
     * @param exchange объект HttpExchange
     * @param path     путь запроса
     * @throws IOException при ошибке ввода-вывода
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            int id = extractId(path);
            taskManager.deleteEpic(id);
            sendText(exchange, "{\"message\":\"Epic deleted successfully\"}");
        } else if (path.equals("/epics")) {
            taskManager.deleteAllEpics();
            sendText(exchange, "{\"message\":\"All epics deleted successfully\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}