package Manager;

/**
 * Утилитарный фабричный класс для получения экземпляров менеджеров.
 * Предоставляет статические методы для создания стандартных реализаций
 * менеджера задач и менеджера истории.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 5
 */
public class Managers {

    /**
     * Возвращает стандартную реализацию менеджера задач.
     * Используется InMemoryTaskManager.
     *
     * @return экземпляр TaskManager
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    /**
     * Возвращает стандартную реализацию менеджера истории.
     * Используется InMemoryHistoryManager.
     *
     * @return экземпляр HistoryManager
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}