import Tasks.Task;
import Tasks.Epic;
import Tasks.Subtask;
import Manager.TaskManager;
import Manager.Managers;
import Tools.TaskStatus;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("===== ТРЕКЕР ЗАДАЧ =====");

        // Базовый функционал - 4-й спринт
        BaseFunctionality(manager);

        // Функционал истории просмотров - 5-й спринт
        HistoryFeature(manager);
    }

    // Метод демонстрирует базовый функционал менеджера задач
    private static void BaseFunctionality(TaskManager manager) {
        System.out.println("\n=== Создание эпиков, задач и подзадач ===");

        // Создаем 2 задачи
        Task task1 = manager.createTask(new Task("Сделать Manager и Tools",
                "TaskManager, TaskStatus", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Сделать Tasks",
                "Epic, Subtask, Task ", TaskStatus.NEW));

        // Создаем эпик с двумя подзадачами
        Epic epic1 = manager.createEpic(new Epic("Эпик 1",
                "с двумя подзадачами"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Задача 1.1",
                "сделать что-то", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Задача 1.2",
                "сделать что-то еще", TaskStatus.NEW, epic1.getId()));

        // Создаем эпик с одной подзадачей
        Epic epic2 = manager.createEpic(new Epic("Эпик 2",
                "с одной подзадачей"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Задача 2.1",
                "сделать что-то еще лучше", TaskStatus.NEW, epic2.getId()));

        System.out.println("\n=== Списки эпиков, задач и подзадач ===");
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println("Подзадачи: " + manager.getSubtasks());
        System.out.println("Эпики: " + manager.getEpics());

        System.out.println("\n=== Проверка статусов задач ===");
        System.out.println("ID:" + task1.getId() + " - " + task1.getTitle() + ": " + task1.getStatus());
        System.out.println("ID:" + task2.getId() + " - " + task2.getTitle() + ": " + task2.getStatus());

        System.out.println("\n=== Проверка статусов подзадач ===");
        System.out.println("ID:" + subtask1.getId() + " - " + subtask1.getTitle() + ": " + subtask1.getStatus());
        System.out.println("ID:" + subtask2.getId() + " - " + subtask2.getTitle() + ": " + subtask2.getStatus());
        System.out.println("ID:" + subtask3.getId() + " - " + subtask3.getTitle() + ": " + subtask3.getStatus());

        System.out.println("\n=== Проверка статусов эпиков ===");
        System.out.println("ID:" + epic1.getId() + " - " + epic1.getTitle() + ": " + epic1.getStatus());
        System.out.println("ID:" + epic2.getId() + " - " + epic2.getTitle() + ": " + epic2.getStatus());

        System.out.println("\n=== Изменение статусов созданных объектов ===");

        // Изменяем статусы задач
        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task2);

        // Изменяем статусы подзадач первого эпика
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        // Изменяем статусы подзадач второго эпика
        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask3);

        System.out.println("\n=== После изменения статусов:  ===");
        System.out.println("Всего задач: " + manager.getTasks().size());
        System.out.println("Всего подзадач: " + manager.getSubtasks().size());
        System.out.println("Всего эпиков: " + manager.getEpics().size());

        System.out.println("\n=== Проверка статусов задач ===");
        System.out.println("Задача " + task1.getId() + ": " + task1.getStatus() + " (было NEW -> IN_PROGRESS)");
        System.out.println("Задача " + task2.getId() + ": " + task2.getStatus() + " (было NEW -> DONE)");

        System.out.println("\n=== Проверка статусов подзадач ===");
        System.out.println("Подзадача " + subtask1.getId() + ": " + subtask1.getStatus() + " (было NEW -> DONE)");
        System.out.println("Подзадача " + subtask2.getId() + ": " + subtask2.getStatus() + " (было NEW -> IN_PROGRESS)");
        System.out.println("Подзадача " + subtask3.getId() + ": " + subtask3.getStatus() + " (было NEW -> DONE)");

        System.out.println("\n=== Проверка статусов эпиков (автоматический расчет) ===");
        System.out.println("Эпик " + epic1.getId() + ": " + epic1.getStatus() +
                " (DONE + IN_PROGRESS = IN_PROGRESS)");
        System.out.println("Эпик " + epic2.getId() + ": " + epic2.getStatus() +
                " (DONE = DONE)");

        System.out.println("\n=== Удаление одной задачи и одного эпика ===");

        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\nПосле удаления задачи c ID:" + task1.getId() + " и эпика c ID:" + epic1.getId() + ":");
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println("Подзадачи: " + manager.getSubtasks());
        System.out.println("Эпики: " + manager.getEpics());

        System.out.println("\n=== Финальное состояние базового функционала ===");
        System.out.println("Всего задач: " + manager.getTasks().size());
        System.out.println("Всего подзадач: " + manager.getSubtasks().size());
        System.out.println("Всего эпиков: " + manager.getEpics().size());
    }

    // Метод демонстрирует работу истории просмотров
    private static void HistoryFeature(TaskManager manager) {
        System.out.println("\n=== Создание эпиков, задач и подзадач ===");

        // Создаем 2 задачи
        Task task1 = manager.createTask(new Task("Сделать Manager и Tools",
                "TaskManager, TaskStatus", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Сделать Tasks",
                "Epic, Subtask, Task ", TaskStatus.NEW));

        // Создаем эпик с двумя подзадачами
        Epic epic1 = manager.createEpic(new Epic("Эпик 1",
                "с двумя подзадачами"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Задача 1.1",
                "сделать что-то", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Задача 1.2",
                "сделать что-то еще", TaskStatus.NEW, epic1.getId()));

        // Создаем эпик с одной подзадачей
        Epic epic2 = manager.createEpic(new Epic("Эпик 2",
                "с одной подзадачей"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Задача 2.1",
                "сделать что-то еще лучше", TaskStatus.NEW, epic2.getId()));

        printAllTasks(manager);

        System.out.println("\n=== Проверка истории просмотров ===");

        System.out.println("\nПосле getTask(" + task1.getId() + "):");
        manager.getTask(task1.getId());
        printAllTasks(manager);

        System.out.println("\nПосле getEpic(" + epic1.getId() + "):");
        manager.getEpic(epic1.getId());
        printAllTasks(manager);

        System.out.println("\nПосле getSubtask(" + subtask1.getId() + "):");
        manager.getSubtask(subtask1.getId());
        printAllTasks(manager);

        System.out.println("\nПосле повторного getTask(" + task1.getId() + "):");
        manager.getTask(task1.getId());
        printAllTasks(manager);

        System.out.println("\nПосле getTask(" + task2.getId() + "):");
        manager.getTask(task2.getId());
        printAllTasks(manager);

        System.out.println("\n=== Проверка ограничений хранения задач в истории (10 задач) ===");

        TaskManager testManager = Managers.getDefault();

        for (int i = 1; i <= 15; i++) {
            Task testTask = testManager.createTask(
                    new Task("Тест " + i, "Описание " + i, TaskStatus.NEW));
            testManager.getTask(testTask.getId());
        }

        System.out.println("\nОграничение работает: " + (testManager.getHistory().size() <= 10));

        System.out.println("\n=== Проверка сохранения истории после удаления задач ===");

        TaskManager deleteTestManager = Managers.getDefault();
        Task deleteTask = deleteTestManager.createTask(
                new Task("Задача для удаления", "Будет удалена", TaskStatus.NEW));

        deleteTestManager.getTask(deleteTask.getId());

        List<Task> historyBeforeDelete = deleteTestManager.getHistory();
        int historySizeBeforeDelete = historyBeforeDelete.size();

        deleteTestManager.deleteTask(deleteTask.getId());

        int historySizeAfterDelete = deleteTestManager.getHistory().size();
        boolean historyPreserved = historySizeAfterDelete == historySizeBeforeDelete;

        System.out.println("\nИстория сохранилась: " + historyPreserved);

        System.out.println();
    }

    // Метод выводит все задачи, эпики, подзадачи и историю просмотров
    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nЗадачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("\nЭпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("\nПодзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("\nИстория:");
        if (manager.getHistory().isEmpty()) {
            System.out.println("Пока пусто, выполните просмотр задач");
        } else {
            for (Task task : manager.getHistory()) {
                System.out.println(task);
            }
        }
    }
}