package Manager;

import Tasks.Task;
import Tasks.Epic;
import Tasks.Subtask;
import java.util.List;

public interface TaskManager {
    // Методы для Task
    List<Task> getTasks();
    void deleteAllTasks();
    Task getTask(int id);
    Task createTask(Task task);
    void updateTask(Task task);
    void deleteTask(int id);

    // Методы для Epic
    List<Epic> getEpics();
    void deleteAllEpics();
    Epic getEpic(int id);
    Epic createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpic(int id);

    // Методы для Subtask
    List<Subtask> getSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtask(int id);
    Subtask createSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtask(int id);

    // Дополнительные методы
    List<Subtask> getEpicSubtasks(int epicId);

    // Метод возвращает последние просмотренные задачи
    List<Task> getHistory();
}