package Manager;

import Tasks.Epic;
import Tasks.Subtask;
import Tasks.Task;
import Tools.TaskStatus;
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
    void taskManagerShouldAddAndFindTasks() {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        Task createdTask = taskManager.createTask(task);

        Task foundTask = taskManager.getTask(createdTask.getId());

        assertNotNull(foundTask, "Задача должна быть найдена по id");
        assertEquals(task.getTitle(), foundTask.getTitle());
        assertEquals(task.getDescription(), foundTask.getDescription());
    }

    @Test
    void shouldAddTasksOfDifferentTypes() {
        Task task = taskManager.createTask(new Task("Task", "Description", TaskStatus.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Description", TaskStatus.NEW, epic.getId()));

        assertNotNull(taskManager.getTask(task.getId()));
        assertNotNull(taskManager.getEpic(epic.getId()));
        assertNotNull(taskManager.getSubtask(subtask.getId()));
    }

    @Test
    void tasksWithGeneratedAndManualIdsShouldNotConflict() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description 1", TaskStatus.NEW));

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        task2.setId(task1.getId());
        Task createdTask2 = taskManager.createTask(task2);

        assertNotEquals(task1.getId(), createdTask2.getId(),
                "Должен быть сгенерирован новый id");
    }

    @Test
    void taskShouldNotChangeAfterAddingToManager() {
        Task original = new Task("Original", "Original Description", TaskStatus.NEW);
        original.setId(999);

        Task created = taskManager.createTask(original);

        assertEquals("Original", created.getTitle());
        assertEquals("Original Description", created.getDescription());
        assertEquals(TaskStatus.NEW, created.getStatus());
    }

    @Test
    void epicCannotAddItselfAsSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Test Description"));

        Subtask subtask = new Subtask("Test", "Test", TaskStatus.NEW, 99999);

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubtask(subtask);
        }, "Нельзя создать подзадачу с несуществующим эпиком");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Description", TaskStatus.NEW, epic.getId()));

        subtask.setEpicId(subtask.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.updateSubtask(subtask);
        }, "Подзадача не может быть своим же эпиком");
    }

    @Test
    void historyShouldPreserveTaskData() {
        Task task = new Task("Original", "Original Description", TaskStatus.DONE);
        task.setId(1);
        taskManager.createTask(task);
        taskManager.getTask(task.getId());

        Task historyTask = taskManager.getHistory().get(0);

        assertEquals(task.getId(), historyTask.getId());
        assertEquals(task.getTitle(), historyTask.getTitle());
        assertEquals(task.getDescription(), historyTask.getDescription());
        assertEquals(task.getStatus(), historyTask.getStatus());
    }

    @Test
    void shouldAddNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final Task createdTask = taskManager.createTask(task);

        final Task savedTask = taskManager.getTask(createdTask.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(createdTask, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(createdTask, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void epicCannotBeSubtaskToItself() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Description", TaskStatus.NEW, epic.getId()));

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());
        for (Subtask st : epicSubtasks) {
            assertNotEquals(epic.getId(), st.getId(), "Эпик не может быть подзадачей самому себе");
        }
    }

    @Test
    void historyShouldAllowDuplicates() {
        Task task = taskManager.createTask(new Task("Task", "Description", TaskStatus.NEW));

        taskManager.getTask(task.getId());
        taskManager.getTask(task.getId());
        taskManager.getTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size(), "TaskManager должен разрешать дубли в истории");
        assertEquals(task, history.get(0));
        assertEquals(task, history.get(1));
        assertEquals(task, history.get(2));
    }
}