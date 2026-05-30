package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tools.TaskType;
import tools.TaskStatus;

import java.io.*;
import java.nio.file.Files;

/**
 * Реализация менеджера задач с автоматическим сохранением состояния в файл.
 * Наследует логику InMemoryTaskManager и добавляет функциональность автосохранения.
 * <p>
 * Особенности реализации:
 * - Автоматическое сохранение после каждой модифицирующей операции
 * - Сохранение данных в текстовом формате CSV
 * - Возможность восстановления состояния менеджера из файла
 * - Использование собственного непроверяемого исключения ManagerSaveException при ошибках ввода-вывода
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 6
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    /**
     * Конструктор менеджера с автосохранением.
     *
     * @param file файл для сохранения состояния менеджера
     */
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    /**
     * Сохраняет текущее состояние менеджера в файл.
     * Формат сохранения - CSV с заголовком.
     * При возникновении ошибки ввода-вывода выбрасывает ManagerSaveException.
     */
    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : tasks.values()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : epics.values()) {
                writer.write(toString(epic) + "\n");
            }

            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Преобразует задачу в строку формата CSV.
     *
     * @param task задача для преобразования
     * @return строка в формате CSV, представляющая задачу
     */
    private String toString(Task task) {
        String type;
        if (task instanceof Epic) {
            type = TaskType.EPIC.name();
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK.name();
        } else {
            type = TaskType.TASK.name();
        }

        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId
        );
    }

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask created = super.createSubtask(subtask);
        save();
        return created;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    /**
     * Загружает состояние менеджера из файла.
     * Восстанавливает все задачи, эпики и подзадачи, а также их связи.
     *
     * @param file файл для загрузки данных
     * @return восстановленный экземпляр FileBackedTaskManager
     * @throws ManagerSaveException если произошла ошибка при чтении файла или парсинге данных
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                if (lines[i].trim().isEmpty()) {
                    continue;
                }
                Task task = fromString(lines[i]);
                if (task != null) {
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        // Восстанавливаем связь с эпиком
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                            // Обновляем статус эпика через метод родителя
                            manager.updateEpicStatus(epic.getId());
                        }
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }

            // Восстанавливаем счетчик ID
            int maxId = 0;
            for (int id : manager.tasks.keySet()) maxId = Math.max(maxId, id);
            for (int id : manager.epics.keySet()) maxId = Math.max(maxId, id);
            for (int id : manager.subtasks.keySet()) maxId = Math.max(maxId, id);
            setNextId(maxId + 1);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла: " + file.getAbsolutePath(), e);
        }
        return manager;
    }

    /**
     * Преобразует строку из файла CSV обратно в объект задачи.
     *
     * @param value строка в формате CSV
     * @return восстановленный объект Task, Epic или Subtask
     */
    private static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            default:
                return null;
        }
    }
}