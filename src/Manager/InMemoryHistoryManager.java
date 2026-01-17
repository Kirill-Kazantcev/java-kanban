package Manager;

import Tasks.Task;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    // Метод помечает задачи как просмотренные
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        removeFromHistory(task.getId());
        history.add(task);

        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    // Метод удаляет задачу из истории просмотров
    @Override
    public void remove(int id) {

    }

    // Метод удаляет дубли задач из истории
    private void removeFromHistory(int id) {
        history.removeIf(task -> task.getId() == id);
    }

    // Метод возвращает список просмотренных задач
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}