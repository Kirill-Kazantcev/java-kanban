package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import server.handlers.*;
import tools.DurationAdapter;
import tools.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HTTP сервер для управления трекером задач.
 * Предоставляет REST API для взаимодействия с TaskManager.
 * <p>
 * Эндпоинты:
 * - GET    /tasks             - получение всех задач
 * - GET    /tasks/{id}        - получение задачи по ID
 * - POST   /tasks             - создание новой задачи
 * - DELETE /tasks/{id}        - удаление задачи
 * - DELETE /tasks             - удаление всех задач
 * - GET    /subtasks          - получение всех подзадач
 * - GET    /subtasks/{id}     - получение подзадачи по ID
 * - POST   /subtasks          - создание новой подзадачи
 * - DELETE /subtasks/{id}     - удаление подзадачи
 * - DELETE /subtasks          - удаление всех подзадач
 * - GET    /epics             - получение всех эпиков
 * - GET    /epics/{id}        - получение эпика по ID
 * - GET    /epics/{id}/subtasks - получение подзадач эпика
 * - POST   /epics             - создание нового эпика
 * - DELETE /epics/{id}        - удаление эпика
 * - DELETE /epics             - удаление всех эпиков
 * - GET    /history           - получение истории просмотров
 * - GET    /prioritized       - получение задач в порядке приоритета
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class HttpTaskServer {
    private static final int DEFAULT_PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;
    private final int actualPort;

    /**
     * Конструктор сервера с использованием менеджера по умолчанию.
     *
     * @throws IOException если не удалось создать HTTP сервер
     */
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault(), DEFAULT_PORT);
    }

    /**
     * Конструктор сервера с указанным менеджером задач.
     *
     * @param taskManager экземпляр менеджера задач
     * @throws IOException если не удалось создать HTTP сервер
     */
    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this(taskManager, DEFAULT_PORT);
    }

    /**
     * Конструктор сервера с указанным менеджером задач и портом.
     *
     * @param taskManager экземпляр менеджера задач
     * @param port        порт для запуска сервера (0 - любой свободный)
     * @throws IOException если не удалось создать HTTP сервер
     */
    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.actualPort = server.getAddress().getPort();
        setupHandlers();
    }

    /**
     * Возвращает порт, на котором запущен сервер.
     *
     * @return номер порта
     */
    public int getPort() {
        return actualPort;
    }

    /**
     * Возвращает экземпляр Gson для сериализации/десериализации JSON.
     *
     * @return экземпляр Gson
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Настраивает обработчики для всех эндпоинтов.
     */
    private void setupHandlers() {
        server.createContext("/tasks", new TasksHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        server.createContext("/epics", new EpicsHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    /**
     * Запускает HTTP сервер.
     */
    public void start() {
        server.start();
        System.out.println("HTTP сервер запущен на порту " + actualPort);
    }

    /**
     * Останавливает HTTP сервер.
     */
    public void stop() {
        server.stop(0);
        System.out.println("HTTP сервер остановлен");
    }

    /**
     * Точка входа для запуска сервера.
     *
     * @param args аргументы командной строки
     * @throws IOException если не удалось создать сервер
     */
    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}