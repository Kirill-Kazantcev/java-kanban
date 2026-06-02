package manager;

import tasks.Task;
import tasks.Epic;
import tasks.Subtask;
import tools.TaskStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
 * - Автоматическая сортировка задач по приоритету (времени начала)
 * - Проверка пересечения временных интервалов при добавлении/обновлении
 *
 * @author Kirill-Kazantcev
 * @version 4.0
 * @since Sprint 5
 */
public class InMemoryTaskManager implements TaskManager {

    /** Счетчик для генерации уникальных ID */
    private static int counter = 1;

    /** Хранилище обычных задач */
    protected final Map<Integer, Task> tasks = new HashMap<>();

    /** Хранилище эпиков */
    protected final Map<Integer, Epic> epics = new HashMap<>();

    /** Хранилище подзадач */
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    /** Менеджер истории просмотров */
    private final HistoryManager historyManager;

    /**
     * Отсортированный набор задач и подзадач по времени начала.
     * Компаратор помещает задачи с null startTime в конец,
     * а при равном времени начала сортирует по id.
     */
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    /**
     * Конструктор. Инициализирует менеджер истории через фабричный метод.
     */
    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    /**
     * Устанавливает значение счетчика ID.
     * Используется при загрузке менеджера из файла для восстановления корректного значения счетчика.
     *
     * @param id новое значение счетчика
     */
    public static void setNextId(int id) {
        counter = id;
    }

    /**
     * Генерирует новый уникальный идентификатор.
     *
     * @return новый ID
     */
    protected static int generateId() {
        return counter++;
    }

    // ========== Вспомогательные методы для приоритетов ==========

    /**
     * Добавляет задачу в отсортированное множество, если у неё задано время начала.
     */
    protected void addToPrioritized(Task task) {   // было private, изменено на protected
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    /**
     * Удаляет задачу из отсортированного множества.
     */
    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    /**
     * Проверяет, пересекается ли новая задача с существующими.
     *
     * @param newTask проверяемая задача
     * @return true, если есть пересечение с любой другой задачей (кроме самой себя)
     */
    private boolean isOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            return false;
        }
        return prioritizedTasks.stream()
                .filter(t -> t.getId() != newTask.getId())
                .anyMatch(existing -> intervalsOverlap(existing, newTask));
    }

    /**
     * Проверяет пересечение двух временных интервалов.
     *
     * @param t1 первая задача
     * @param t2 вторая задача
     * @return true, если интервалы пересекаются
     */
    private boolean intervalsOverlap(Task t1, Task t2) {
        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = t2.getEndTime();
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && start2.isBefore(end1);
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
        tasks.values().forEach(this::removeFromPrioritized);
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
        if (isOverlap(task)) {
            throw new ManagerSaveException("Задача пересекается с существующей по времени выполнения");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача с ID " + task.getId() + " не существует");
        }
        if (isOverlap(task)) {
            throw new ManagerSaveException("Обновлённая задача пересекается с другой задачей");
        }
        tasks.put(task.getId(), task);
        removeFromPrioritized(task);
        addToPrioritized(task);
        if (historyManager.getHistory().stream().anyMatch(t -> t.getId() == task.getId())) {
            historyManager.add(task);
        }
    }

    @Override
    public void deleteTask(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            removeFromPrioritized(removed);
            historyManager.remove(id);
        }
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
                Subtask sub = subtasks.get(subtaskId);
                if (sub != null) {
                    removeFromPrioritized(sub);
                }
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
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        // Эпики не добавляются в prioritizedTasks (у них startTime может быть null)
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
                Subtask sub = subtasks.remove(subtaskId);
                if (sub != null) {
                    removeFromPrioritized(sub);
                    historyManager.remove(subtaskId);
                }
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
        subtasks.values().forEach(this::removeFromPrioritized);
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
        if (isOverlap(subtask)) {
            throw new ManagerSaveException("Подзадача пересекается с существующей задачей/подзадачей");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        addToPrioritized(subtask);
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
        if (isOverlap(subtask)) {
            throw new ManagerSaveException("Обновлённая подзадача пересекается с другой задачей");
        }

        subtasks.put(subtask.getId(), subtask);
        removeFromPrioritized(savedSubtask);
        addToPrioritized(subtask);

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
            removeFromPrioritized(subtask);
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
        // Используем Stream API
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // ========== Защищенные методы ==========

    /**
     * Обновляет статус эпика на основе статусов его подзадач.
     * Статус рассчитывается по следующим правилам:
     * <ul>
     *   <li>Нет подзадач → NEW</li>
     *   <li>Все подзадачи NEW → NEW</li>
     *   <li>Все подзадачи DONE → DONE</li>
     *   <li>Остальные случаи → IN_PROGRESS</li>
     * </ul>
     * Также пересчитывает продолжительность, время начала и завершения эпика.
     *
     * @param epicId идентификатор эпика
     */
    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.recalculateTimes(Collections.emptyList());
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
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

        epic.recalculateTimes(epicSubtasks);
    }
}