package server;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.http.HttpClient;

/**
 * Базовый класс для тестирования HTTP сервера.
 * Содержит общую настройку и утилиты для всех тестовых классов.
 * <p>
 * Особенности:
 * - Запуск и остановка сервера перед каждым тестом
 * - Использование динамического порта (0 - любой свободный)
 * - Предоставляет общие утилиты: менеджер задач, HTTP клиент, Gson
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public abstract class HttpTaskServerTest {

    /** Менеджер задач для хранения данных */
    protected TaskManager manager;

    /** HTTP сервер для тестирования */
    protected HttpTaskServer server;

    /** HTTP клиент для отправки запросов */
    protected HttpClient client;

    /** Gson для сериализации/десериализации JSON */
    protected Gson gson;

    /**
     * Выполняется перед каждым тестом.
     * Инициализирует менеджер, сервер, клиент и Gson.
     */
    @BeforeEach
    void setUpBase() throws IOException, InterruptedException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager, 0);
        client = HttpClient.newHttpClient();
        gson = server.getGson();
        server.start();
        Thread.sleep(100);
    }

    /**
     * Выполняется после каждого теста.
     * Останавливает сервер.
     */
    @AfterEach
    void tearDownBase() throws InterruptedException {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(100);
    }

    /**
     * Возвращает базовый URL сервера с динамическим портом.
     */
    protected String getBaseUrl() {
        return "http://localhost:" + server.getPort();
    }
}