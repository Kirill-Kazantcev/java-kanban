package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import server.exceptions.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Базовый класс для всех HTTP обработчиков.
 * Содержит общие методы для отправки ответов клиенту.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public abstract class BaseHttpHandler {

    protected static final String CONTENT_TYPE = "application/json;charset=utf-8";

    /**
     * Читает тело запроса и возвращает его в виде строки.
     *
     * @param exchange объект HttpExchange
     * @return тело запроса в виде строки
     * @throws IOException при ошибке чтения
     */
    protected String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Отправляет успешный ответ с кодом 200.
     *
     * @param exchange объект HttpExchange
     * @param text     текст ответа
     * @throws IOException при ошибке записи
     */
    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    /**
     * Отправляет ответ с указанным кодом статуса.
     *
     * @param exchange   объект HttpExchange
     * @param text       текст ответа
     * @param statusCode код статуса HTTP
     * @throws IOException при ошибке записи
     */
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    /**
     * Отправляет ответ с кодом 201 (Created).
     *
     * @param exchange объект HttpExchange
     * @param text     текст ответа
     * @throws IOException при ошибке записи
     */
    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 201);
    }

    /**
     * Отправляет ответ с кодом 404 (Not Found).
     *
     * @param exchange объект HttpExchange
     * @throws IOException при ошибке записи
     */
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendError(exchange, "{\"error\":\"Resource not found\"}", 404);
    }

    /**
     * Отправляет ответ с кодом 406 (Not Acceptable) при пересечении задач.
     *
     * @param exchange объект HttpExchange
     * @throws IOException при ошибке записи
     */
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendError(exchange, "{\"error\":\"Task overlaps with existing tasks\"}", 406);
    }

    /**
     * Отправляет ответ с кодом 500 (Internal Server Error).
     *
     * @param exchange объект HttpExchange
     * @throws IOException при ошибке записи
     */
    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendError(exchange, "{\"error\":\"Internal server error\"}", 500);
    }

    /**
     * Отправляет ответ с ошибкой.
     *
     * @param exchange   объект HttpExchange
     * @param message    сообщение об ошибке
     * @param statusCode код статуса HTTP
     * @throws IOException при ошибке записи
     */
    private void sendError(HttpExchange exchange, String message, int statusCode) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    /**
     * Извлекает числовой ID из пути запроса.
     *
     * @param path путь запроса (например, "/tasks/123")
     * @return числовой ID
     * @throws NotFoundException если ID не является числом
     */
    protected int extractId(String path) {
        try {
            String[] parts = path.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            throw new NotFoundException("Invalid ID format in path: " + path);
        }
    }
}