package tasks;

import tools.TaskStatus;
import tools.TaskType;

/**
 * Класс подзадачи - задачи, относящейся к определённому эпику.
 * Наследуется от Task. Каждая подзадача принадлежит одному эпику.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 4
 */
public class Subtask extends Task {

    /** Идентификатор родительского эпика */
    private int epicId;

    /**
     * Конструктор для создания новой подзадачи.
     *
     * @param title название подзадачи
     * @param description описание подзадачи
     * @param status статус подзадачи
     * @param epicId идентификатор эпика-родителя
     */
    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    /**
     * Возвращает идентификатор родительского эпика.
     *
     * @return ID эпика
     */
    public int getEpicId() {
        return epicId;
    }

    /**
     * Устанавливает идентификатор родительского эпика.
     *
     * @param epicId новый ID эпика
     */
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    /**
     * Возвращает тип задачи.
     * Для подзадачи возвращает SUBTASK.
     *
     * @return тип задачи (SUBTASK)
     */
    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    /**
     * Возвращает строковое представление подзадачи.
     * Включает идентификатор родительского эпика.
     *
     * @return строковое представление подзадачи
     */
    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}