package Manager;

import Tasks.Task;
import Tasks.Epic;
import Tasks.Subtask;
import Tools.TaskStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int counter = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    // Метод возвращает список всех задач (раньше назывался getAllTasks)
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Метод удаляет все задачи
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    // Метод получает задачу по идентификатору (раньше назывался getTaskById)
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Метод создает новую задачу
    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        task.setId(counter++);
        tasks.put(task.getId(), task);
        return task;
    }

    // Метод обновляет существующую задачу
    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            throw new IllegalArgumentException("Задача с ID " + task.getId() + " не существует");
        }
    }

    // Метод удаляет задачу по идентификатору (раньше назывался deleteTaskById)
    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    // Метод возвращает список всех эпиков (раньше назывался getAllEpics)
    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // Метод удаляет все эпики и связанные с ними подзадачи
    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    // Метод получает эпик по идентификатору (раньше назывался getEpicById)
    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    // Метод создает новый эпик
    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не может быть null");
        }
        epic.setId(counter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    // Метод обновляет существующий эпик
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

    // Метод удаляет эпик по идентификатору (раньше назывался deleteEpicById)
    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    // Метод возвращает список всех подзадач (раньше назывался getAllSubtasks)
    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Метод удаляет все подзадачи
    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
        }
    }

    // Метод получает подзадачу по идентификатору (раньше назывался getSubtaskById)
    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Метод создает новую подзадачу
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

    // Метод обновляет существующую подзадачу
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

    // Метод удаляет подзадачу по идентификатору (раньше назывался deleteSubtaskById)
    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    // Метод возвращает список подзадач конкретного эпика (раньше назывался getSubtasksByEpicId)
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

    // Метод возвращает последние просмотренные задачи
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Метод обновляет статус эпика на основе статусов его подзадач
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