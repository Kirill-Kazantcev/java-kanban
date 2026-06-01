package manager;

import tasks.Task;
import tasks.Epic;
import tasks.Subtask;
import tools.TaskStatus;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки функциональности FileBackedTaskManager.
 * Проверяет сохранение и загрузку состояния менеджера в/из файла.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 6
 */
class FileBackedTaskManagerTest {

    /**
     * Проверяет сохранение и загрузку пустого менеджера.
     * Создает временный файл, сохраняет пустое состояние и проверяет,
     * что при загрузке все списки задач пусты.
     *
     * @throws IOException если возникает ошибка при создании временного файла
     */
    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    /**
     * Проверяет сохранение и загрузку нескольких задач разных типов.
     * Создает задачу, эпик и подзадачу, сохраняет их в файл,
     * затем загружает и проверяет корректность восстановления всех данных.
     *
     * @throws IOException если возникает ошибка при создании временного файла
     */
    @Test
    void shouldSaveAndLoadMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task = new Task("Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);

        Epic epic = new Epic("Epic1", "Epic description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask1", "Subtask description", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getTasks().size());
        assertEquals(1, loaded.getEpics().size());
        assertEquals(1, loaded.getSubtasks().size());

        Task loadedTask = loaded.getTasks().get(0);
        assertEquals("Task1", loadedTask.getTitle());
        assertEquals(TaskStatus.NEW, loadedTask.getStatus());

        Epic loadedEpic = loaded.getEpics().get(0);
        assertEquals("Epic1", loadedEpic.getTitle());

        Subtask loadedSubtask = loaded.getSubtasks().get(0);
        assertEquals("Subtask1", loadedSubtask.getTitle());
        assertEquals(loadedEpic.getId(), loadedSubtask.getEpicId());
    }

    /**
     * Проверяет сохранение и загрузку эпика с несколькими подзадачами.
     * Проверяет, что при загрузке корректно восстанавливаются связи между эпиком и подзадачами,
     * а также правильно рассчитывается статус эпика на основе статусов подзадач.
     *
     * @throws IOException если возникает ошибка при создании временного файла
     */
    @Test
    void shouldSaveAndLoadWithMultipleSubtasks() throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Epic epic = new Epic("Epic1", "Test epic");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "First", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Second", TaskStatus.DONE, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Epic loadedEpic = loaded.getEpics().get(0);
        assertEquals(2, loaded.getEpicSubtasks(loadedEpic.getId()).size());
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus());
    }

    /**
     * Проверяет обработку исключения при попытке загрузить данные из несуществующего файла.
     * Ожидается, что метод loadFromFile выбросит исключение ManagerSaveException.
     */
    @Test
    void shouldThrowExceptionWhenReadingInvalidFile() {
        File invalidFile = new File("non_existent_file.csv");

        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(invalidFile);
        });
    }

    /**
     * Проверяет сохранение и загрузку менеджера с последующей модификацией задач.
     * Проверяет, что после загрузки можно успешно создавать новые задачи и они корректно сохраняются.
     *
     * @throws IOException если возникает ошибка при создании временного файла
     */
    @Test
    void shouldSaveAndLoadThenModify() throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        Task task = new Task("Task1", "Description", TaskStatus.NEW);
        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = new Task("Task2", "New description", TaskStatus.IN_PROGRESS);
        loaded.createTask(newTask);

        assertEquals(2, loaded.getTasks().size());

        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(2, reloaded.getTasks().size());
    }
}