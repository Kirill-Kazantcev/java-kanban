package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tools.TaskStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки работы InMemoryTaskManager.
 * Проверяет CRUD операции с задачами, эпиками и подзадачами,
 * а также работу истории просмотров.
 *
 * @author Kirill-Kazantcev
 * @version 4.0
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
        assertEquals(task, history.getFirst());
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
        assertEquals(task, history.getFirst());
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
        assertEquals(task2, history.getFirst());
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
        assertEquals(task1, history.getFirst());
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        taskManager.getTask(task1.getId());

        history = taskManager.getHistory();
        assertEquals(task2, history.getFirst());
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));

        taskManager.deleteTask(task2.getId());

        history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task3, history.getFirst());
        assertEquals(task1, history.get(1));
    }

    // ========== Тесты истории приоритетов пересечений и времени эпика ==========
    /**
     * Проверяет, что метод getPrioritizedTasks возвращает задачи, отсортированные по startTime.
     */
    @Test
    void shouldReturnPrioritizedTasksSortedByStartTime() {
        Task task1 = new Task("A", "", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 12, 0));
        Task task2 = new Task("B", "", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 0));
        taskManager.createTask(task2);
        taskManager.createTask(task1);
        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertTrue(prioritized.getFirst().getStartTime().isBefore(prioritized.get(1).getStartTime()));
    }

    /**
     * Проверяет, что задачи с null startTime не попадают в список приоритетных.
     */
    @Test
    void shouldNotIncludeTasksWithNullStartTimeInPrioritizedList() {
        Task taskNoTime = new Task("NoTime", "", TaskStatus.NEW);
        taskManager.createTask(taskNoTime);
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    /**
     * Проверяет, что подзадачи также учитываются в приоритетном списке.
     */
    @Test
    void prioritizedListShouldIncludeSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask sub = new Subtask("Sub", "", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2025, 1, 1, 9, 0));
        taskManager.createSubtask(sub);
        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals(sub, prioritized.getFirst());
    }

    /**
     * Проверяет, что при попытке создать пересекающуюся задачу выбрасывается исключение ManagerSaveException.
     */
    @Test
    void shouldNotAllowOverlappingTasks() {
        Task task1 = new Task("First", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        taskManager.createTask(task1);
        Task task2 = new Task("Second", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 30));
        assertThrows(ManagerSaveException.class, () -> taskManager.createTask(task2));
    }

    /**
     * Проверяет, что непересекающиеся задачи добавляются без ошибок.
     */
    @Test
    void shouldAllowNonOverlappingTasks() {
        Task task1 = new Task("First", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        taskManager.createTask(task1);
        Task task2 = new Task("Second", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 11, 30));
        assertDoesNotThrow(() -> taskManager.createTask(task2));
    }

    /**
     * Проверяет, что подзадача не может пересекаться с уже существующей задачей.
     */
    @Test
    void shouldNotAllowOverlappingSubtaskWithExistingTask() {
        Task existing = new Task("Existing", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        taskManager.createTask(existing);
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask subtask = new Subtask("Sub", "", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 15));
        assertThrows(ManagerSaveException.class, () -> taskManager.createSubtask(subtask));
    }

    /**
     * Проверяет граничный случай: все подзадачи эпика имеют статус NEW.
     */
    @Test
    void epicStatusAllNew() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }

    /**
     * Проверяет граничный случай: все подзадачи эпика имеют статус DONE.
     */
    @Test
    void epicStatusAllDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.DONE, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    /**
     * Проверяет граничный случай: подзадачи со статусами NEW и DONE (эпик должен быть IN_PROGRESS).
     */
    @Test
    void epicStatusMixedNewAndDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    /**
     * Проверяет граничный случай: хотя бы одна подзадача в статусе IN_PROGRESS.
     */
    @Test
    void epicStatusInProgress() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.IN_PROGRESS, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    /**
     * Проверяет корректный расчёт продолжительности, startTime и endTime эпика на основе подзадач.
     */
    @Test
    void epicTimesCalculatedCorrectly() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        LocalDateTime start1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime start2 = LocalDateTime.of(2025, 1, 1, 12, 0);
        Subtask sub1 = new Subtask("S1", "", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(30), start1);
        Subtask sub2 = new Subtask("S2", "", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(45), start2);
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        Epic updated = taskManager.getEpic(epic.getId());
        assertEquals(Duration.ofMinutes(75), updated.getDuration());
        assertEquals(start1, updated.getStartTime());
        assertEquals(start2.plus(Duration.ofMinutes(45)), updated.getEndTime());
    }

    /**
     * Проверяет, что при отсутствии подзадач у эпика продолжительность = 0,
     * startTime и endTime равны null.
     */
    @Test
    void epicTimesShouldBeNullIfNoSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Epic fetched = taskManager.getEpic(epic.getId());
        assertEquals(Duration.ZERO, fetched.getDuration());
        assertNull(fetched.getStartTime());
        assertNull(fetched.getEndTime());
    }
}