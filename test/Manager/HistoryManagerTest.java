package Manager;

import Tasks.Task;
import Tools.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "В истории должна быть одна задача");
        assertEquals(task, history.get(0), "Задача в истории должна соответствовать добавленной");
    }

    @Test
    void shouldNotExceedMaxSize() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description " + i, TaskStatus.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать максимум 10 задач");

        for (int i = 6; i <= 15; i++) {
            final int id = i;
            assertTrue(history.stream().anyMatch(t -> t.getId() == id),
                    "В истории должна быть задача с id " + id);
        }
    }

    @Test
    void shouldAllowDuplicates() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        //проверка на дубликаты
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(4, history.size(), "Дубли должны быть разрешены в истории");

        // Проверяем порядок
        assertEquals(task1, history.get(0), "Первое вхождение задачи 1");
        assertEquals(task2, history.get(1), "Задача 2");
        assertEquals(task3, history.get(2), "Задача 3");
        assertEquals(task1, history.get(3), "Второе вхождение задачи 1 (дубль разрешен)");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertFalse(history.contains(task1));
        assertTrue(history.contains(task2));
    }

    @Test
    void add() {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }
}