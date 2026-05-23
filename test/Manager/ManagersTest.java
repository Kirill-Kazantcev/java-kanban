package Manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки работы утилитарного класса Managers.
 * Проверяет корректность фабричных методов по созданию менеджеров.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 5
 */
class ManagersTest {

    /**
     * Проверяет, что метод getDefault() возвращает корректно инициализированный TaskManager.
     * Созданный менеджер должен иметь непустые коллекции для задач, эпиков, подзадач и истории.
     */
    @Test
    void getDefaultShouldReturnInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Метод getDefault() должен возвращать проинициализированный TaskManager");

        assertNotNull(taskManager.getTasks(), "Список задач не должен быть null");
        assertNotNull(taskManager.getEpics(), "Список эпиков не должен быть null");
        assertNotNull(taskManager.getSubtasks(), "Список подзадач не должен быть null");
        assertNotNull(taskManager.getHistory(), "История просмотров не должна быть null");
    }

    /**
     * Проверяет, что метод getDefaultHistory() возвращает корректно инициализированный HistoryManager.
     * Созданный менеджер истории должен иметь возможность получить историю.
     */
    @Test
    void getDefaultHistoryShouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Метод getDefaultHistory() должен возвращать проинициализированный HistoryManager");

        assertNotNull(historyManager.getHistory(), "История просмотров не должна быть null");
    }
}