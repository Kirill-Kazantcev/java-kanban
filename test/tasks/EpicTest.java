package tasks;

import tools.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки работы класса Epic.
 * Проверяет создание эпиков, управление подзадачами и корректность статусов.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 4
 */
class EpicTest {

    /**
     * Проверяет, что новый эпик имеет статус NEW.
     * При создании эпика без подзадач статус должен быть NEW.
     */
    @Test
    void epicShouldHaveNewStatusWhenCreated() {
        Epic epic = new Epic("Test Epic", "Test Description");
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Новый эпик должен иметь статус NEW");
    }

    /**
     * Проверяет, что эпик корректно хранит идентификаторы своих подзадач.
     * При добавлении ID подзадач они должны сохраняться в списке.
     */
    @Test
    void epicShouldStoreSubtaskIds() {
        Epic epic = new Epic("Test Epic", "Test Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        assertEquals(2, epic.getSubtaskIds().size(), "Эпик должен хранить 2 ID подзадач");
        assertTrue(epic.getSubtaskIds().contains(1), "Список должен содержать ID 1");
        assertTrue(epic.getSubtaskIds().contains(2), "Список должен содержать ID 2");
    }

    /**
     * Проверяет, что эпик не добавляет дубликаты ID подзадач.
     * При попытке добавить одинаковый ID несколько раз, в списке должна быть только одна запись.
     */
    @Test
    void epicShouldNotAddDuplicateSubtaskIds() {
        Epic epic = new Epic("Test Epic", "Test Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(1);

        assertEquals(1, epic.getSubtaskIds().size(), "Эпик не должен добавлять дубликаты ID подзадач");
    }

    /**
     * Проверяет, что эпики с одинаковым ID считаются равными.
     * Идентификатор является уникальным идентификатором задачи.
     */
    @Test
    void subtasksOfEpicShouldBeEqualWhenSameId() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        Epic epic2 = new Epic("Epic 2", "Description 2");

        epic1.setId(10);
        epic2.setId(10);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны совпадать");
        assertEquals(epic1.hashCode(), epic2.hashCode(), "HashCode эпиков с одинаковым id должны совпадать");
    }
}