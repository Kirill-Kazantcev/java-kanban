import Tasks.Task;
import Tasks.Epic;
import Tasks.Subtask;
import Manager.TaskManager;
import Tools.TaskStatus;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

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
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
        System.out.println("Эпики: " + manager.getAllEpics());

        System.out.println("\n=== Проверка статусов задач ===");
        System.out.println("ID:" + task1.getId() + " - " +  task1.getTitle() + ": " + task1.getStatus());
        System.out.println("ID:" + task2.getId() + " - " +  task2.getTitle() + ": " + task2.getStatus());

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
        System.out.println("Всего задач: " + manager.getAllTasks().size());
        System.out.println("Всего подзадач: " + manager.getAllSubtasks().size());
        System.out.println("Всего эпиков: " + manager.getAllEpics().size());

        System.out.println("\n=== Проверка статусов задач ===");
        System.out.println("ID:" + task1.getId() + " - " +  task1.getTitle() + ": " + task1.getStatus());
        System.out.println("ID:" + task2.getId() + " - " +  task2.getTitle() + ": " + task2.getStatus());

        System.out.println("\n=== Проверка статусов подзадач ===");
        System.out.println("ID:" + subtask1.getId() + " - " + subtask1.getTitle() + ": " + subtask1.getStatus());
        System.out.println("ID:" + subtask2.getId() + " - " + subtask2.getTitle() + ": " + subtask2.getStatus());
        System.out.println("ID:" + subtask3.getId() + " - " + subtask3.getTitle() + ": " + subtask3.getStatus());

        System.out.println("\n=== Проверка статусов эпиков ===");
        System.out.println("ID:" + epic1.getId() + " - " + epic1.getTitle() + ": " + epic1.getStatus());
        System.out.println("ID:" + epic2.getId() + " - " + epic2.getTitle() + ": " + epic2.getStatus());

        // 4. Удаляем одну задачу и один эпик
        System.out.println("\n=== Удаление одной задачи и одного эпика ===");

        manager.deleteTaskById(task1.getId()); // ID 1
        manager.deleteEpicById(epic1.getId()); // ID 3

        System.out.println("\nПосле удаления задачи c ID:" + task1.getId() + " и эпика c ID:" + epic1.getId() + ":");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
        System.out.println("Эпики: " + manager.getAllEpics());

        System.out.println("\n=== Финальное состояние ===");
        System.out.println("Всего задач: " + manager.getAllTasks().size());
        System.out.println("Всего подзадач: " + manager.getAllSubtasks().size());
        System.out.println("Всего эпиков: " + manager.getAllEpics().size());
    }
}