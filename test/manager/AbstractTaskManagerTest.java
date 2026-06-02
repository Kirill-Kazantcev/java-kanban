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
 * Абстрактный базовый класс для тестирования реализаций TaskManager.
 * Содержит общие тесты для всех менеджеров:
 * - работа с приоритетами (сортировка, null startTime)
 * - проверка пересечения временных интервалов
 * - расчёт статуса и времени эпика (граничные случаи)
 *
 * @param <T> тип конкретного менеджера задач
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 8
 */
@SuppressWarnings("unused")
public abstract class AbstractTaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    // ========== Тесты приоритетов (getPrioritizedTasks) ==========

    @Test
    void shouldReturnPrioritizedTasksSortedByStartTime() {
        Task task1 = new Task("A", "", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 12, 0));
        Task task2 = new Task("B", "", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 0));
        taskManager.createTask(task2);
        taskManager.createTask(task1);
        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertTrue(prioritized.getFirst().getStartTime().isBefore(prioritized.get(1).getStartTime()));
    }

    @Test
    void shouldNotIncludeTasksWithNullStartTimeInPrioritizedList() {
        Task taskNoTime = new Task("NoTime", "", TaskStatus.NEW);
        taskManager.createTask(taskNoTime);
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void prioritizedListShouldIncludeSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask sub = new Subtask("Sub", "", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2024, 1, 1, 9, 0));
        taskManager.createSubtask(sub);
        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals(sub, prioritized.getFirst());
    }

    // ========== Тесты пересечений ==========

    @Test
    void shouldNotAllowOverlappingTasks() {
        Task task1 = new Task("First", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 10, 0));
        taskManager.createTask(task1);
        Task task2 = new Task("Second", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 10, 30));
        assertThrows(ManagerSaveException.class, () -> taskManager.createTask(task2));
    }

    @Test
    void shouldNotAllowOverlappingSubtaskWithExistingTask() {
        Task existing = new Task("Existing", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 10, 0));
        taskManager.createTask(existing);
        Epic epic = taskManager.createEpic(new Epic("Epic", ""));
        Subtask subtask = new Subtask("Sub", "", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 15));
        assertThrows(ManagerSaveException.class, () -> taskManager.createSubtask(subtask));
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        Task task1 = new Task("First", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 10, 0));
        taskManager.createTask(task1);
        Task task2 = new Task("Second", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 11, 30));
        assertDoesNotThrow(() -> taskManager.createTask(task2));
    }

    @Test
    void shouldAllowTasksWithNoStartTimeEvenIfOverlapIsNotChecked() {
        Task task1 = new Task("First", "", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 10, 0));
        taskManager.createTask(task1);
        Task task2 = new Task("Second", "", TaskStatus.NEW); // нет времени
        assertDoesNotThrow(() -> taskManager.createTask(task2));
        assertTrue(taskManager.getPrioritizedTasks().stream().noneMatch(t -> t.getId() == task2.getId()));
    }

    // ========== Граничные условия для эпиков (статусы) ==========

    @Test
    void epicStatusAllNew() {
        Epic epic = taskManager.createEpic(new Epic("E", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Epic epic = taskManager.createEpic(new Epic("E", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.DONE, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusMixedNewAndDone() {
        Epic epic = taskManager.createEpic(new Epic("E", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgress() {
        Epic epic = taskManager.createEpic(new Epic("E", ""));
        Subtask sub1 = new Subtask("S1", "", TaskStatus.IN_PROGRESS, epic.getId());
        Subtask sub2 = new Subtask("S2", "", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    // ========== Тесты расчёта времени эпика ==========

    @Test
    void epicTimesCalculatedCorrectly() {
        Epic epic = taskManager.createEpic(new Epic("E", ""));
        LocalDateTime start1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime start2 = LocalDateTime.of(2024, 1, 1, 12, 0);
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

    @Test
    void epicTimesShouldBeNullIfNoSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("E", ""));
        Epic fetched = taskManager.getEpic(epic.getId());
        assertEquals(Duration.ZERO, fetched.getDuration());
        assertNull(fetched.getStartTime());
        assertNull(fetched.getEndTime());
    }
}