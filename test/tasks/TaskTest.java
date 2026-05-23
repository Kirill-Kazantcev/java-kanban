package tasks;

import tools.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки работы базового класса Task.
 * Проверяет создание задач, равенство по ID и сохранение полей.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 4
 */
class TaskTest {

    /**
     * Проверяет, что задачи с одинаковым ID считаются равными.
     * Идентификатор является уникальным идентификатором задачи,
     * остальные поля не влияют на равенство.
     */
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны совпадать");
        assertEquals(task1.hashCode(), task2.hashCode(), "HashCode задач с одинаковым id должны совпадать");
    }

    /**
     * Проверяет, что задача сохраняет все переданные при создании и установке поля.
     * Включая ID, название, описание и статус.
     */
    @Test
    void taskShouldPreserveAllFields() {
        Task task = new Task("Test Task", "Test Description", TaskStatus.DONE);
        task.setId(5);

        assertEquals(5, task.getId(), "ID задачи должен быть 5");
        assertEquals("Test Task", task.getTitle(), "Название задачи должно сохраниться");
        assertEquals("Test Description", task.getDescription(), "Описание задачи должно сохраниться");
        assertEquals(TaskStatus.DONE, task.getStatus(), "Статус задачи должен быть DONE");
    }
}