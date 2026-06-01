package tasks;

import tools.TaskStatus;
import tools.TaskType;
import java.util.Objects;

/**
 * Базовый класс задачи.
 * Хранит основные свойства задачи: идентификатор, название, описание и статус.
 * Является родительским для классов Epic и Subtask.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 4
 */
public class Task {

    /** Уникальный идентификатор задачи */
    private int id;

    /** Название задачи */
    private String title;

    /** Описание задачи */
    private String description;

    /** Текущий статус задачи */
    private TaskStatus status;

    /**
     * Конструктор для создания новой задачи.
     *
     * @param title название задачи
     * @param description описание задачи
     * @param status начальный статус задачи
     */
    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // ========== Геттеры и сеттеры ==========

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // ========== Метод для определения типа ==========

    /**
     * Возвращает тип задачи.
     * Для обычной задачи возвращает TASK.
     *
     * @return тип задачи (TASK, EPIC или SUBTASK)
     */
    public TaskType getType() {
        return TaskType.TASK;
    }

    // ========== Переопределенные методы Object ==========

    /**
     * Сравнивает задачи по идентификатору.
     *
     * @param o объект для сравнения
     * @return true если идентификаторы равны, иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    /**
     * Возвращает хэш-код задачи на основе идентификатора.
     *
     * @return хэш-код задачи
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Возвращает строковое представление задачи.
     *
     * @return строковое представление с id, названием, описанием и статусом
     */
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}