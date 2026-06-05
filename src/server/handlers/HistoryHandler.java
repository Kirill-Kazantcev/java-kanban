package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

/**
 * Обработчик запросов для эндпоинта /history.
 * Поддерживает GET (получение истории просмотров).
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    /** Менеджер задач для получения истории просмотров */
    private final TaskManager taskManager;

    /** Gson для сериализации/десериализации JSON */
    private final Gson gson;

    /**
     * Конструктор обработчика истории.
     *
     * @param taskManager менеджер задач
     * @param gson        экземпляр Gson для работы с JSON
     */
    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    /**
     * Обрабатывает входящий HTTP запрос.
     * Поддерживает GET и HEAD запросы к эндпоинту /history.
     *
     * @param exchange объект HttpExchange, содержащий запрос и ответ
     * @throws IOException при ошибке ввода-вывода
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ((method.equals("GET") || method.equals("HEAD")) && path.equals("/history")) {
                List<Task> history = taskManager.getHistory();
                String response = gson.toJson(history);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}