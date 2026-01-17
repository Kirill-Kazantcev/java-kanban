package Tasks;

import Tools.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    // проверяем task по id
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны совпадать");
        assertEquals(task1.hashCode(), task2.hashCode(), "HashCode задач с одинаковым id должны совпадать");
    }

    @Test
    void taskShouldPreserveAllFields() {
        Task task = new Task("Test Task", "Test Description", TaskStatus.DONE);
        task.setId(5);

        assertEquals(5, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(TaskStatus.DONE, task.getStatus());
    }
}