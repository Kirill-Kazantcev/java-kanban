package Tasks;

import Tools.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicShouldHaveNewStatusWhenCreated() {
        Epic epic = new Epic("Test Epic", "Test Description");
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Новый эпик должен иметь статус NEW");
    }

    @Test
    void epicShouldStoreSubtaskIds() {
        Epic epic = new Epic("Test Epic", "Test Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        assertEquals(2, epic.getSubtaskIds().size());
        assertTrue(epic.getSubtaskIds().contains(1));
        assertTrue(epic.getSubtaskIds().contains(2));
    }

    @Test
    void epicShouldNotAddDuplicateSubtaskIds() {
        Epic epic = new Epic("Test Epic", "Test Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(1);

        assertEquals(1, epic.getSubtaskIds().size());
    }
    // проверяем epic по id
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

