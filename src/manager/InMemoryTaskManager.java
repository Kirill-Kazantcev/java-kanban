package manager;

import tasks.Task;
import tasks.Epic;
import tasks.Subtask;
import tools.TaskStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация менеджера задач с хранением данных в оперативной памяти.
 * Централизованное управление всеми типами задач: обычными задачами, эпиками и подзадачами.
 * <p>
 * Особенности реализации:
 * - Три HashMap для раздельного хранения задач разных типов
 * - Уникальная генерация ID через счетчик
 * - Быстрый доступ O(1) по идентификатору
 * - Автоматический расчет статуса эпика на основе статусов подзадач
 * - Интеграция с историей просмотров
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 5
 */
public class InMemoryTaskManager implements TaskManager {

    /** Счетчик для генерации уникальных ID */
    private int counter = 1;

    /** Хранилище обычных задач */
    private final Map<Integer, Task> tasks = new HashMap<>();

    /** Хранилище эпиков */
    private final Map<Integer, Epic> epics = new HashMap<>();

    /** Хранилище подзадач */
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    /** Менеджер истории просмотров */
    private final HistoryManager historyManager;

    /**
     * Конструктор. Инициализирует менеджер истории через фабричный метод.
     */
    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    // ========== Реализация методов для Task ==========

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        task.setId(counter++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            if (historyManager.getHistory().stream().anyMatch(t -> t.getId() == task.getId())) {
                historyManager.add(task);
            }
        } else {
            throw new IllegalArgumentException("Задача с ID " + task.getId() + " не существует");
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    // ========== Реализация методов для Epic ==========

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            Epic epic = epics.get(id);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не может быть null");
        }
        epic.setId(counter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не может быть null");
        }
        Epic savedEpic = epics.get(epic.getId());
        if (savedEpic != null) {
            savedEpic.setTitle(epic.getTitle());
            savedEpic.setDescription(epic.getDescription());
        } else {
            throw new IllegalArgumentException("Эпик с ID " + epic.getId() + " не существует");
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    // ========== Реализация методов для Subtask ==========

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null");
        }
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с ID " + epicId + " не существует");
        }

        subtask.setId(counter++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epicId);
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null");
        }

        Subtask savedSubtask = subtasks.get(subtask.getId());
        if (savedSubtask == null) {
            throw new IllegalArgumentException("Подзадача с ID " + subtask.getId() + " не существует");
        }

        int oldEpicId = savedSubtask.getEpicId();
        int newEpicId = subtask.getEpicId();

        if (!epics.containsKey(newEpicId)) {
            throw new IllegalArgumentException("Новый эпик с ID " + newEpicId + " не существует");
        }

        subtasks.put(subtask.getId(), subtask);

        if (historyManager.getHistory().stream().anyMatch(t -> t.getId() == subtask.getId())) {
            historyManager.add(subtask);
        }

        if (oldEpicId != newEpicId) {
            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.removeSubtaskId(subtask.getId());
                updateEpicStatus(oldEpicId);
            }

            Epic newEpic = epics.get(newEpicId);
            if (newEpic != null) {
                newEpic.addSubtaskId(subtask.getId());
                updateEpicStatus(newEpicId);
            }
        } else {
            updateEpicStatus(newEpicId);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    // ========== Дополнительные методы ==========

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с ID " + epicId + " не существует");
        }

        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // ========== Приватные методы ==========

    /**
     * Обновляет статус эпика на основе статусов его подзадач.
     * Статус рассчитывается по следующим правилам:
     * <ul>
     *   <li>Нет подзадач → NEW</li>
     *   <li>Все подзадачи NEW → NEW</li>
     *   <li>Все подзадачи DONE → DONE</li>
     *   <li>Остальные случаи → IN_PROGRESS</li>
     * </ul>
     *
     * @param epicId идентификатор эпика
     */
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}