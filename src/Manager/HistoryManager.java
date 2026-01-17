package Manager;

import Tasks.Task;
import java.util.List;

public interface HistoryManager {
    // Метод помечает задачи как просмотренные
    void add(Task task);

    // Метод удаляет задачу из истории просмотров
    void remove(int id);

    // Метод возвращает список просмотренных задач
    List<Task> getHistory();
}