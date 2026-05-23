import Tasks.Epic;
import Tasks.Subtask;
import Tasks.Task;
import Manager.TaskManager;
import Manager.Managers;
import Tools.TaskStatus;
import java.util.List;

/**
 * Главный класс приложения для демонстрации работы трекера задач.
 * Содержит тестовые сценарии для проверки базового функционала и истории просмотров.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 4
 */
public class Main {

    /**
     * Точка входа в приложение.
     * Демонстрирует работу с задачами, эпиками, подзадачами и историей просмотров.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("===== ТРЕКЕР ЗАДАЧ =====");

        BaseFunctionality(manager);
        HistoryFeature(manager);
    }

    /**
     * Демонстрирует базовый функционал менеджера задач.
     * Включает создание, обновление и удаление задач, эпиков и подзадач.
     *
     * @param manager экземпляр менеджера задач
     */
    private static void BaseFunctionality(TaskManager manager) {
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

    /**
     * Демонстрирует работу улучшенной истории просмотров.
     * Показывает неограниченный размер истории, отсутствие дубликатов
     * и удаление задач из истории при их удалении из менеджера.
     *
     * @param manager экземпляр менеджера задач
     */
    private static void HistoryFeature(TaskManager manager) {
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

    /**
     * Выводит текущее состояние истории просмотров в консоль.
     *
     * @param manager экземпляр менеджера задач
     */
    private static void printHistory(TaskManager manager) {
        List<Task> history = manager.getHistory();
        System.out.println("История (" + history.size() + "):");
        for (Task task : history) {
            System.out.println("  - " + task.getTitle() + " (ID: " + task.getId() + ")");
        }
        System.out.println();
    }
}