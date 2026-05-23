package Tasks;

import Tools.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки работы класса Subtask.
 * Проверяет создание подзадач и их связь с эпиками.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 4
 */
class SubtaskTest {

    /**
     * Проверяет, что подзадача корректно хранит ID родительского эпика.
     * При создании подзадачи должен сохраняться переданный ID эпика.
     */
    @Test
    void subtaskShouldStoreEpicId() {
        Subtask subtask = new Subtask("Test Subtask", "Test Description", TaskStatus.NEW, 1);
        assertEquals(1, subtask.getEpicId(), "Подзадача должна хранить id эпика");
    }

    /**
     * Проверяет, что подзадачи с одинаковым ID считаются равными.
     * Идентификатор является уникальным идентификатором задачи,
     * независимо от эпика-родителя.
     */
    @Test
    void subtaskShouldBeEqualWhenSameId() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.DONE, 2);

        subtask1.setId(10);
        subtask2.setId(10);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны совпадать");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "HashCode подзадач с одинаковым id должны совпадать");
    }
}