package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tools.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки работы InMemoryTaskManager.
 * Проверяет CRUD операции с задачами, эпиками и подзадачами,
 * а также работу истории просмотров.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 5
 */
class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    // ========== Базовые тесты CRUD операций ==========

    /**
     * Проверяет создание и поиск обычной задачи.
     */
    @Test
    void shouldCreateAndFindTask() {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        Task createdTask = taskManager.createTask(task);

        Task foundTask = taskManager.getTask(createdTask.getId());

        assertNotNull(foundTask);
        assertEquals(createdTask.getId(), foundTask.getId());
        assertEquals("Test Task", foundTask.getTitle());
        assertEquals("Test Description", foundTask.getDescription());
        assertEquals(TaskStatus.NEW, foundTask.getStatus());
    }

    /**
     * Проверяет создание и поиск эпика.
     */
    @Test
    void shouldCreateAndFindEpic() {
        Epic epic = new Epic("Test Epic", "Test Description");
        Epic createdEpic = taskManager.createEpic(epic);

        Epic foundEpic = taskManager.getEpic(createdEpic.getId());

        assertNotNull(foundEpic);
        assertEquals(createdEpic.getId(), foundEpic.getId());
        assertEquals("Test Epic", foundEpic.getTitle());
        assertEquals("Test Description", foundEpic.getDescription());
        assertTrue(foundEpic.getSubtaskIds().isEmpty());
    }

    /**
     * Проверяет создание и поиск подзадачи.
     */
    @Test
    void shouldCreateAndFindSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = new Subtask("Test Subtask", "Test Description", TaskStatus.NEW, epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        Subtask foundSubtask = taskManager.getSubtask(createdSubtask.getId());

        assertNotNull(foundSubtask);
        assertEquals(createdSubtask.getId(), foundSubtask.getId());
        assertEquals("Test Subtask", foundSubtask.getTitle());
        assertEquals("Test Description", foundSubtask.getDescription());
        assertEquals(TaskStatus.NEW, foundSubtask.getStatus());
        assertEquals(epic.getId(), foundSubtask.getEpicId());
    }

    /**
     * Проверяет обновление обычной задачи.
     */
    @Test
    void shouldUpdateTask() {
        Task task = taskManager.createTask(new Task("Old Task", "Old Description", TaskStatus.NEW));
        task.setTitle("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateTask(task);
        Task updatedTask = taskManager.getTask(task.getId());

        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    /**
     * Проверяет обновление эпика (только название и описание).
     */
    @Test
    void shouldUpdateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Old Epic", "Old Description"));
        epic.setTitle("Updated Epic");
        epic.setDescription("Updated Description");

        taskManager.updateEpic(epic);
        Epic updatedEpic = taskManager.getEpic(epic.getId());

        assertEquals("Updated Epic", updatedEpic.getTitle());
        assertEquals("Updated Description", updatedEpic.getDescription());
    }

    /**
     * Проверяет обновление подзадачи.
     */
    @Test
    void shouldUpdateSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Old Subtask", "Old Description", TaskStatus.NEW, epic.getId()));

        subtask.setTitle("Updated Subtask");
        subtask.setDescription("Updated Description");
        subtask.setStatus(TaskStatus.DONE);

        taskManager.updateSubtask(subtask);
        Subtask updatedSubtask = taskManager.getSubtask(subtask.getId());

        assertEquals("Updated Subtask", updatedSubtask.getTitle());
        assertEquals("Updated Description", updatedSubtask.getDescription());
        assertEquals(TaskStatus.DONE, updatedSubtask.getStatus());
    }

    /**
     * Проверяет удаление обычной задачи.
     */
    @Test
    void shouldDeleteTask() {
        Task task = taskManager.createTask(new Task("Task to Delete", "Description", TaskStatus.NEW));
        int taskId = task.getId();

        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTask(taskId));
        assertFalse(taskManager.getTasks().contains(task));
    }

    /**
     * Проверяет удаление эпика со всеми его подзадачами.
     */
    @Test
    void shouldDeleteEpicAndItsSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic to Delete", "Description"));
        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Subtask 1", "Description", TaskStatus.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Subtask 2", "Description", TaskStatus.NEW, epic.getId()));

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()));
        assertNull(taskManager.getSubtask(subtask1.getId()));
        assertNull(taskManager.getSubtask(subtask2.getId()));
        assertTrue(taskManager.getSubtasks().isEmpty());
    }

    /**
     * Проверяет удаление подзадачи.
     */
    @Test
    void shouldDeleteSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask to Delete", "Description", TaskStatus.NEW, epic.getId()));
        int subtaskId = subtask.getId();

        taskManager.deleteSubtask(subtaskId);

        assertNull(taskManager.getSubtask(subtaskId));
        assertFalse(taskManager.getSubtasks().contains(subtask));
    }

    /**
     * Проверяет автоматический расчет статуса эпика на основе подзадач.
     */
    @Test
    void shouldUpdateEpicStatusBasedOnSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        assertEquals(TaskStatus.NEW, epic.getStatus());

        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Subtask 1", "Description", TaskStatus.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Subtask 2", "Description", TaskStatus.NEW, epic.getId()));

        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    // ========== Тесты истории просмотров (Sprint 6) ==========

    /**
     * Проверяет, что задача добавляется в историю при получении.
     */
    @Test
    void shouldAddToHistoryWhenGettingTask() {
        Task task = taskManager.createTask(new Task("Task", "Desc", TaskStatus.NEW));

        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    /**
     * Проверяет, что в истории нет дубликатов при многократном просмотре одной задачи.
     */
    @Test
    void shouldNotAllowDuplicatesInHistory() {
        Task task = taskManager.createTask(new Task("Task", "Desc", TaskStatus.NEW));

        taskManager.getTask(task.getId());
        taskManager.getTask(task.getId());
        taskManager.getTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "В истории не должно быть дубликатов");
        assertEquals(task, history.get(0));
    }

    /**
     * Проверяет, что задача перемещается в конец истории при повторном просмотре.
     */
    @Test
    void shouldMoveExistingTaskToEndWhenReaccessed() {
        Task task1 = taskManager.createTask(new Task("Task1", "Desc", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Task2", "Desc", TaskStatus.NEW));
        Task task3 = taskManager.createTask(new Task("Task3", "Desc", TaskStatus.NEW));

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());
        taskManager.getTask(task1.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));
    }

    /**
     * Проверяет, что история не имеет ограничения по размеру.
     */
    @Test
    void shouldHaveUnlimitedHistory() {
        for (int i = 0; i < 20; i++) {
            Task task = taskManager.createTask(new Task("Task" + i, "Desc", TaskStatus.NEW));
            taskManager.getTask(task.getId());
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(20, history.size(), "История должна хранить все 20 просмотров");
    }

    /**
     * Проверяет, что задача удаляется из истории при удалении из менеджера.
     */
    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = taskManager.createTask(new Task("Task", "Desc", TaskStatus.NEW));

        taskManager.getTask(task.getId());
        assertEquals(1, taskManager.getHistory().size());

        taskManager.deleteTask(task.getId());

        assertTrue(taskManager.getHistory().isEmpty(), "Задача должна быть удалена из истории");
    }

    /**
     * Проверяет, что эпик и все его подзадачи удаляются из истории.
     */
    @Test
    void shouldRemoveEpicAndSubtasksFromHistoryWhenDeleted() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub1 = taskManager.createSubtask(
                new Subtask("Sub1", "Desc", TaskStatus.NEW, epic.getId()));
        Subtask sub2 = taskManager.createSubtask(
                new Subtask("Sub2", "Desc", TaskStatus.NEW, epic.getId()));

        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(sub1.getId());
        taskManager.getSubtask(sub2.getId());

        assertEquals(3, taskManager.getHistory().size());

        taskManager.deleteEpic(epic.getId());

        assertTrue(taskManager.getHistory().isEmpty(),
                "Эпик и все подзадачи должны быть удалены из истории");
    }

    /**
     * Проверяет корректный порядок истории после различных операций.
     */
    @Test
    void shouldMaintainHistoryOrderAfterOperations() {
        Task task1 = taskManager.createTask(new Task("Task1", "Desc", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Task2", "Desc", TaskStatus.NEW));
        Task task3 = taskManager.createTask(new Task("Task3", "Desc", TaskStatus.NEW));

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        taskManager.getTask(task1.getId());

        history = taskManager.getHistory();
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));

        taskManager.deleteTask(task2.getId());

        history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task3, history.get(0));
        assertEquals(task1, history.get(1));
    }
}