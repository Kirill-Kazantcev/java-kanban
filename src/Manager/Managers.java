package Manager;

public class Managers {
    // Метод возвращает объект-менеджер
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    // Метод возвращает менеджер истории просмотров
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}