package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

/**
 * Обработчик запросов для эндпоинта /prioritized.
 * Поддерживает GET (получение задач в порядке приоритета по времени начала).
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    /** Менеджер задач для получения приоритетных задач */
    private final TaskManager taskManager;

    /** Gson для сериализации/десериализации JSON */
    private final Gson gson;

    /**
     * Конструктор обработчика приоритетных задач.
     *
     * @param taskManager менеджер задач
     * @param gson        экземпляр Gson для работы с JSON
     */
    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    /**
     * Обрабатывает входящий HTTP запрос.
     * Поддерживает GET и HEAD запросы к эндпоинту /prioritized.
     *
     * @param exchange объект HttpExchange, содержащий запрос и ответ
     * @throws IOException при ошибке ввода-вывода
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ((method.equals("GET") || method.equals("HEAD")) && path.equals("/prioritized")) {
                List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                String response = gson.toJson(prioritizedTasks);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}