package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tools.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

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

    @Test
    void shouldUpdateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Old Epic", "Old Description"));
        epic.setTitle("Updated Epic");
        epic.setDescription("Updated Description");

        taskManager.updateEpic(epic);
        Epic updatedEpic = taskManager.getEpic(epic.getId());

        assertEquals("Updated Epic", updatedEpic.getTitle());
        assertEquals("Updated Description", updatedEpic.getDescription());
        // Статус не должен измениться
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
    }

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

    @Test
    void shouldDeleteTask() {
        Task task = taskManager.createTask(new Task("Task to Delete", "Description", TaskStatus.NEW));
        int taskId = task.getId();

        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTask(taskId));
        assertFalse(taskManager.getTasks().contains(task));
    }

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

    @Test
    void shouldDeleteSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask to Delete", "Description", TaskStatus.NEW, epic.getId()));
        int subtaskId = subtask.getId();

        taskManager.deleteSubtask(subtaskId);

        assertNull(taskManager.getSubtask(subtaskId));
        assertFalse(taskManager.getEpicSubtasks(epic.getId()).contains(subtask));
        assertFalse(taskManager.getSubtasks().contains(subtask));
    }

    @Test
    void shouldUpdateEpicStatusBasedOnSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        // Эпик без подзадач должен иметь статус NEW
        assertEquals(TaskStatus.NEW, epic.getStatus());

        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Subtask 1", "Description", TaskStatus.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Subtask 2", "Description", TaskStatus.NEW, epic.getId()));

        // Все подзадачи NEW -> статус эпика NEW
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        // Одна DONE, одна NEW -> статус IN_PROGRESS
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        // Все подзадачи DONE -> статус DONE
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    // ========== ТЕСТЫ ДЛЯ ИСТОРИИ ==========

    @Test
    void shouldAddTaskToHistory() {
        Task task = taskManager.createTask(new Task("Task", "Desc", TaskStatus.NEW));

        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test
    void historyShouldNotAllowDuplicates() {  // ИЗМЕНЕНО: было historyShouldAllowDuplicates
        Task task = taskManager.createTask(new Task("Task 1", "Desc 1", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Desc 2", TaskStatus.NEW));
        Task task3 = taskManager.createTask(new Task("Task 3", "Desc 3", TaskStatus.NEW));

        taskManager.getTask(task.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());
        taskManager.getTask(task.getId()); // повторный просмотр

        List<Task> history = taskManager.getHistory();

        // Ожидаем 3 уникальных задачи (дубликат не добавляется)
        assertEquals(3, history.size());
    }

    @Test
    void historyShouldNotHaveSizeLimit() {  // ИЗМЕНЕНО: было historyShouldHaveSizeLimit
        //  Создаем: 15 задач и добавляем в историю
        for (int i = 0; i < 15; i++) {
            Task task = taskManager.createTask(new Task("Task " + i, "Desc " + i, TaskStatus.NEW));
            taskManager.getTask(task.getId());
        }

        List<Task> history = taskManager.getHistory();

        // Ожидаем, что история хранит все 15 задач (нет ограничения)
        assertEquals(15, history.size());
    }

    @Test
    void shouldNotKeepTaskInHistoryAfterDeletion() {
        Task task = taskManager.createTask(new Task("Task to delete", "Desc", TaskStatus.NEW));

        taskManager.getTask(task.getId());
        assertEquals(1, taskManager.getHistory().size());

        taskManager.deleteTask(task.getId());

        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void shouldRemoveEpicAndSubtasksFromHistory() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Subtask 1", "Desc", TaskStatus.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Subtask 2", "Desc", TaskStatus.NEW, epic.getId()));

        // Добавляем эпик и подзадачи в историю
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        assertEquals(3, taskManager.getHistory().size());

        // Удаляем эпик
        taskManager.deleteEpic(epic.getId());

        // Проверяем, что история очистилась
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void shouldMaintainHistoryOrderWhenTaskReaccessed() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Desc", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Desc", TaskStatus.NEW));
        Task task3 = taskManager.createTask(new Task("Task 3", "Desc", TaskStatus.NEW));

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());

        // Повторно просматриваем первую задачу
        taskManager.getTask(task1.getId());

        List<Task> history = taskManager.getHistory();

        // Задача должна переместиться в конец
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));
        assertEquals(3, history.size());
    }
}