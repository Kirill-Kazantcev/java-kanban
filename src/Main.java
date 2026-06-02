import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import manager.TaskManager;
import manager.Managers;
import manager.FileBackedTaskManager;
import manager.ManagerSaveException;
import tools.TaskStatus;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Главный класс приложения для демонстрации работы трекера задач.
 *
 * @author Kirill-Kazantcev
 * @version 4.0
 * @since Sprint 4
 */
public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("===== ТРЕКЕР ЗАДАЧ =====");

        baseFunctionality(manager);
        historyFeature(manager);
        prioritizedAndOverlapDemo(manager);

        System.out.println("\n===== Демонстрация fileBackedTaskManager =====");
        try {
            fileBackedTaskManagerDemo();
        } catch (IOException e) {
            System.out.println("Ошибка при демонстрации: " + e.getMessage());
        }
    }

    private static void baseFunctionality(TaskManager manager) {
        System.out.println("\n=== Создание эпиков, задач и подзадач ===");

        Task task1 = manager.createTask(new Task("Сделать Manager и Tools",
                "TaskManager, TaskStatus", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Сделать Tasks",
                "Epic, Subtask, Task ", TaskStatus.NEW));

        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "с двумя подзадачами"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Задача 1.1",
                "сделать что-то", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Задача 1.2",
                "сделать что-то еще", TaskStatus.NEW, epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "с одной подзадачей"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Задача 2.1",
                "сделать что-то еще лучше", TaskStatus.NEW, epic2.getId()));

        System.out.println("\n=== Списки эпиков, задач и подзадач ===");
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println("Подзадачи: " + manager.getSubtasks());
        System.out.println("Эпики: " + manager.getEpics());

        System.out.println("\n=== Изменение статусов созданных объектов ===");

        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task2);

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask3);

        System.out.println("\n=== Проверка статусов эпиков ===");
        System.out.println("Эпик " + epic1.getId() + ": " + epic1.getStatus());
        System.out.println("Эпик " + epic2.getId() + ": " + epic2.getStatus());

        System.out.println("\n=== Удаление одной задачи и одного эпика ===");

        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\n=== Финальное состояние базового функционала ===");
        System.out.println("Всего задач: " + manager.getTasks().size());
        System.out.println("Всего подзадач: " + manager.getSubtasks().size());
        System.out.println("Всего эпиков: " + manager.getEpics().size());
    }

    private static void historyFeature(TaskManager manager) {
        System.out.println("\n=== Создание объектов для демонстрации истории ===");

        Task task1 = manager.createTask(new Task("Задача 1", "Описание 1", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2", TaskStatus.NEW));
        Task task3 = manager.createTask(new Task("Задача 3", "Описание 3", TaskStatus.NEW));

        System.out.println("\n=== Просмотр задач (добавление в историю) ===");

        System.out.println("Просмотр task1:");
        manager.getTask(task1.getId());
        printHistory(manager);

        System.out.println("Просмотр task2:");
        manager.getTask(task2.getId());
        printHistory(manager);

        System.out.println("Просмотр task3:");
        manager.getTask(task3.getId());
        printHistory(manager);

        System.out.println("Повторный просмотр task1 (должен переместиться в конец):");
        manager.getTask(task1.getId());
        printHistory(manager);

        System.out.println("\n=== Демонстрация удаления задачи из истории ===");
        System.out.println("Удаление task2 из менеджера:");
        manager.deleteTask(task2.getId());
        printHistory(manager);

        System.out.println("\n=== Демонстрация неограниченной истории ===");
        for (int i = 4; i <= 15; i++) {
            Task task = manager.createTask(new Task("Задача " + i, "Описание " + i, TaskStatus.NEW));
            manager.getTask(task.getId());
        }
        System.out.println("Размер истории после 15 просмотров: " + manager.getHistory().size());
        System.out.println("(Ожидается 14, так как task2 был удалён)");
    }

    private static void prioritizedAndOverlapDemo(TaskManager manager) {
        System.out.println("\n=== Демонстрация приоритетов и проверки пересечений ===");

        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

        Task taskA = new Task("Встреча A", "Описание A", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        manager.createTask(taskA);

        Task taskB = new Task("Встреча B", "Описание B", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 12, 0));
        manager.createTask(taskB);

        try {
            Task taskC = new Task("Встреча C (пересекается)", "Описание C", TaskStatus.NEW,
                    Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 30));
            manager.createTask(taskC);
            System.out.println("ОШИБКА: Пересекающаяся задача была добавлена, хотя не должна была.");
        } catch (ManagerSaveException e) {
            System.out.println("Корректно перехвачено исключение: " + e.getMessage());
        }

        manager.deleteTask(taskA.getId());
        manager.deleteTask(taskB.getId());

        Epic epicTime = new Epic("Эпик с временем", "Проверка расчёта времени эпика");
        manager.createEpic(epicTime);

        Subtask sub1 = new Subtask("Подзадача 1", "", TaskStatus.NEW, epicTime.getId(),
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 9, 0));
        Subtask sub2 = new Subtask("Подзадача 2", "", TaskStatus.NEW, epicTime.getId(),
                Duration.ofMinutes(45), LocalDateTime.of(2025, 1, 1, 10, 0));
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        System.out.println("\nЗадачи в порядке приоритета (по startTime):");
        List<Task> prioritized = manager.getPrioritizedTasks();
        for (Task t : prioritized) {
            System.out.printf("  - %s (id=%d): start=%s, end=%s\n",
                    t.getTitle(), t.getId(),
                    t.getStartTime() != null ? t.getStartTime() : "не задано",
                    t.getEndTime() != null ? t.getEndTime() : "не задано");
        }

        Epic updatedEpic = manager.getEpic(epicTime.getId());
        System.out.println("\nВремя эпика '" + updatedEpic.getTitle() + "':");
        System.out.println("  - Продолжительность: " + updatedEpic.getDuration().toMinutes() + " мин");
        System.out.println("  - Старт: " + updatedEpic.getStartTime());
        System.out.println("  - Завершение: " + updatedEpic.getEndTime());
    }

    private static void printHistory(TaskManager manager) {
        List<Task> history = manager.getHistory();
        System.out.println("История (" + history.size() + "):");
        for (Task task : history) {
            System.out.println("  - " + task.getTitle() + " (ID: " + task.getId() + ")");
        }
        System.out.println();
    }

    private static void fileBackedTaskManagerDemo() throws IOException {
        File file = File.createTempFile("kanban", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

        Task task1 = new Task("Задача для файла", "Описание", TaskStatus.NEW,
                Duration.ofMinutes(90), LocalDateTime.now());
        Epic epic1 = new Epic("Эпик для файла", "Описание эпика");
        manager1.createTask(task1);
        manager1.createEpic(epic1);

        System.out.println("Создано задач: " + manager1.getTasks().size());
        System.out.println("Создано эпиков: " + manager1.getEpics().size());

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Загружено задач: " + manager2.getTasks().size());
        System.out.println("Загружено эпиков: " + manager2.getEpics().size());
        System.out.println("Данные успешно сохранены и восстановлены!");
    }
}