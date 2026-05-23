package Manager;

import Tasks.Task;
import Tools.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки работы InMemoryHistoryManager.
 * Проверяет функциональность истории просмотров: добавление, удаление, порядок, отсутствие дубликатов.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 6
 */
class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        task2.setId(2);
        task3 = new Task("Task 3", "Description 3", TaskStatus.DONE);
        task3.setId(3);
    }

    /**
     * Проверяет, что задача успешно добавляется в историю.
     */
    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    /**
     * Проверяет, что null-задача не добавляется в историю.
     */
    @Test
    void shouldNotAddNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();

        assertEquals(0, history.size());
    }

    /**
     * Проверяет, что дубликаты не создаются, а задача перемещается в конец.
     */
    @Test
    void shouldRemoveDuplicateAndAddToEnd() {
        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    /**
     * Проверяет корректный порядок после множественных добавлений.
     */
    @Test
    void shouldMaintainOrderAfterMultipleAdds() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    /**
     * Проверяет, что существующая задача перемещается в конец при повторном добавлении.
     */
    @Test
    void shouldMoveExistingTaskToEndWhenReadded() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));
    }

    /**
     * Проверяет удаление задачи из начала списка.
     */
    @Test
    void shouldRemoveTaskFromBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    /**
     * Проверяет удаление задачи из середины списка.
     */
    @Test
    void shouldRemoveTaskFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    /**
     * Проверяет удаление задачи из конца списка.
     */
    @Test
    void shouldRemoveTaskFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    /**
     * Проверяет, что удаление несуществующей задачи не вызывает ошибок.
     */
    @Test
    void shouldHandleRemoveOfNonExistentTask() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
    }

    /**
     * Проверяет удаление последней оставшейся задачи.
     */
    @Test
    void shouldRemoveLastRemainingTask() {
        historyManager.add(task1);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size());
    }

    /**
     * Проверяет полную очистку истории.
     */
    @Test
    void shouldClearAllHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.clear();

        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size());
    }

    /**
     * Комплексная проверка целостности после множественных операций.
     */
    @Test
    void shouldMaintainIntegrityAfterMultipleOperations() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        Task task4 = new Task("Task 4", "Description 4", TaskStatus.NEW);
        task4.setId(4);
        historyManager.add(task4);

        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task3, history.get(0));
        assertEquals(task4, history.get(1));
        assertEquals(task1, history.get(2));
    }
}