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

        // Демонстрация базового функционала (4-й спринт)
        BaseFunctionality(manager);

        // Демонстрация истории просмотров (5-й спринт)
        HistoryFeature(manager);
    }

    // Метод для демонстрации базового функционала (4-й спринт)
    private static void BaseFunctionality(TaskManager manager) {
        // 1. Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        System.out.println("\n=== Создание эпиков, задач и подзадач ===");

        // 1.1. Создаем 2 задачи ID 1, 2
        Task task1 = manager.createTask(new Task("Сделать Manager и Tools",
                "TaskManager, TaskStatus", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Сделать Tasks",
                "Epic, Subtask, Task ", TaskStatus.NEW));

        // 1.2. Создаем эпик "Эпик 1 с двумя подзадачами" ID 3 (4, 5)
        Epic epic1 = manager.createEpic(new Epic("Эпик 1",
                "с двумя подзадачами"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Задача 1.1",
                "сделать что-то", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Задача 1.2",
                "сделать что-то еще", TaskStatus.NEW, epic1.getId()));

        // 1.3. Создаем эпик "Эпик 2 с одной подзадачей" ID 6 (7)
        Epic epic2 = manager.createEpic(new Epic("Эпик 2",
                "с одной подзадачей"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Задача 2.1",
                "сделать что-то еще лучше", TaskStatus.NEW, epic2.getId()));

        // 2. Распечатайте списки эпиков, задач и подзадач
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

        // 3. Изменяем статусы созданных объектов
        System.out.println("\n=== Изменение статусов созданных объектов===");

        // 3.1. Изменяем статусы задач
        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task2);

        // 3.2. Изменяем статусы подзадач 1-го эпика, статус теперь должен быть IN_PROGRESS
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        // 3.3. Изменяем статусы подзадач 2-го эпика, статус теперь должен быть DONE
        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask3);

        // 3.4 Проверяем что получилось
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

        // 4. Удаляем одну задачу и один эпик
        System.out.println("\n=== Удаление одной задачи и одного эпика ===");

        manager.deleteTask(task1.getId()); // ID 1
        manager.deleteEpic(epic1.getId()); // ID 3

        System.out.println("\nПосле удаления задачи c ID:" + task1.getId() + " и эпика c ID:" + epic1.getId() + ":");
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println("Подзадачи: " + manager.getSubtasks());
        System.out.println("Эпики: " + manager.getEpics());

        System.out.println("\n=== Финальное состояние базового функционала ===");
        System.out.println("Всего задач: " + manager.getTasks().size());
        System.out.println("Всего подзадач: " + manager.getSubtasks().size());
        System.out.println("Всего эпиков: " + manager.getEpics().size());
    }

    // Метод для демонстрации истории просмотров (5-й спринт)
    private static void HistoryFeature(TaskManager manager) {
        // 1. Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        System.out.println("\n=== Создание эпиков, задач и подзадач ===");

        // 1.1. Создаем 2 задачи ID 1, 2
        Task task1 = manager.createTask(new Task("Сделать Manager и Tools",
                "TaskManager, TaskStatus", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Сделать Tasks",
                "Epic, Subtask, Task ", TaskStatus.NEW));

        // 1.2. Создаем эпик "Эпик 1 с двумя подзадачами" ID 3 (4, 5)
        Epic epic1 = manager.createEpic(new Epic("Эпик 1",
                "с двумя подзадачами"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Задача 1.1",
                "сделать что-то", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Задача 1.2",
                "сделать что-то еще", TaskStatus.NEW, epic1.getId()));

        // 1.3. Создаем эпик "Эпик 2 с одной подзадачей" ID 6 (7)
        Epic epic2 = manager.createEpic(new Epic("Эпик 2",
                "с одной подзадачей"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Задача 2.1",
                "сделать что-то еще лучше", TaskStatus.NEW, epic2.getId()));

        // Вызов метода вывода всех задач
        printAllTasks(manager);

        //Проверка истории
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

        // Дубли разрешены по ТЗ
        System.out.println("\nПосле повторного getTask(" + task1.getId() + ") (дубль разрешен):");
        manager.getTask(task1.getId());
        printAllTasks(manager);

        System.out.println("\nПосле getTask(" + task2.getId() + "):");
        manager.getTask(task2.getId());
        printAllTasks(manager);

        // Проверка ограничения истории (10 элементов)
        System.out.println("\n=== Проверка ограничений хранения задач в истории (10 задач) ===");

        // Создаем нового менеджера для чистого теста (используем Managers.getDefault())
        TaskManager testManager = Managers.getDefault();

        // Создаем и смотрим 15 задач
        for (int i = 1; i <= 15; i++) {
            Task testTask = testManager.createTask(
                    new Task("Тест " + i, "Описание " + i, TaskStatus.NEW));
            testManager.getTask(testTask.getId());
        }
        // Проверяем что ограничение работает - true
        System.out.println("\nОграничение работает: " + (testManager.getHistory().size() <= 10));

        // Проверяем что история сохраняется после удаления задачи
        System.out.println("\n=== Проверка сохранения истории после удаления задач ===");

        // Создаем и просматриваем задачу
        TaskManager deleteTestManager = Managers.getDefault();
        Task deleteTask = deleteTestManager.createTask(
                new Task("Задача для удаления", "Будет удалена", TaskStatus.NEW));

        // Добавляем в историю
        deleteTestManager.getTask(deleteTask.getId());

        //  Сохраняем историю до удаления для правильной проверки
        List<Task> historyBeforeDelete = deleteTestManager.getHistory();
        int historySizeBeforeDelete = historyBeforeDelete.size();

        // Удаляем задачу
        deleteTestManager.deleteTask(deleteTask.getId());

        // Сравниваем размер истории
        int historySizeAfterDelete = deleteTestManager.getHistory().size();
        boolean historyPreserved = historySizeAfterDelete == historySizeBeforeDelete;

        // Проверяем что работат - true
        System.out.println("\nИстория сохранилась: " + historyPreserved);

        System.out.println();
    }

    // Метод для вывода всех задач
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

        //Для истории добавил вывод если история пуста.
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